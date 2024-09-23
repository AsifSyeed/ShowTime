package com.example.showtime.ticket.service.impl;

import com.example.showtime.referral.model.entity.Referral;
import com.example.showtime.referral.service.IReferralService;
import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.request.CategoryRequest;
import com.example.showtime.ticket.model.response.EventCategoryResponse;
import com.example.showtime.ticket.repository.CategoryRepository;
import com.example.showtime.ticket.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;
    private final IReferralService referralService;

    @Override
    public Category getCategoryById(Long categoryId) {
        return categoryRepository.findByCategoryId(categoryId);
    }

    @Override
    public Category getCategoryByIdAndEventId(Long categoryId, String eventId) {
        return categoryRepository.findByCategoryIdAndEventId(categoryId, eventId);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void createCategory(List<CategoryRequest> categoryList, String eventId) {
        prepareCategoryModel(categoryList, eventId);
    }

    @Override
    public List<EventCategoryResponse> getAllCategoriesByEventId(String eventId) {
        return categoryRepository.findAllByEventId(eventId).stream()
                .map(category -> EventCategoryResponse.builder()
                        .categoryId(category.getCategoryId())
                        .categoryName(category.getCategoryName())
                        .categoryPrice(category.getCategoryPrice())
                        .categoryCapacity(category.getCategoryCapacity())
                        .categoryAvailableCount(category.getCategoryAvailableCount())
                        .discountedPrice(category.getCategoryPrice() - getDefaultDiscount(eventId))
                        .maximumQuantity(category.getMaximumQuantity())
                        .build())
                .collect(Collectors.toList());
    }

    private Double getDefaultDiscount(String eventId) {
        Referral defaultReferral = referralService.getDefaultReferral(eventId);
        return defaultReferral != null ? defaultReferral.getReferralDiscount() : 0.0;
    }

    @Override
    public void updateAvailableTickets(Long categoryId, String eventId, long size) {
        Category category = getCategoryByIdAndEventId(categoryId, eventId);
        category.setCategoryAvailableCount(category.getCategoryAvailableCount() - size);
        categoryRepository.save(category);
    }

    @Override
    public Double getTicketPrice(Long ticketCategory, String eventId) {
        return getCategoryByIdAndEventId(ticketCategory, eventId).getCategoryPrice();
    }

    private void prepareCategoryModel(List<CategoryRequest> categoryList, String eventId) {

        for (CategoryRequest categoryRequest : categoryList) {
            Category category = new Category();
            category.setCategoryName(categoryRequest.getCategoryName());
            category.setCategoryPrice(categoryRequest.getCategoryPrice());
            category.setCategoryCapacity(categoryRequest.getCategoryCapacity());
            category.setCategoryAvailableCount(categoryRequest.getCategoryCapacity());
            category.setCategoryDescription(categoryRequest.getCategoryDescription());
            category.setEventId(eventId);

            categoryRepository.save(category);
        }
    }
}
