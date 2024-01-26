package com.example.showtime.ticket.service.impl;

import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.request.CategoryRequest;
import com.example.showtime.ticket.repository.CategoryRepository;
import com.example.showtime.ticket.service.ICategoryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CategoryService implements ICategoryService {

    private final CategoryRepository categoryRepository;

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
    public List<Category> getAllCategoriesByEventId(String eventId) {
        return categoryRepository.findAllByEventId(eventId);
    }

    private void prepareCategoryModel(List<CategoryRequest> categoryList, String eventId) {

        for (int i = 0; i < categoryList.size(); i++) {
            Category category = new Category();
            category.setCategoryName(categoryList.get(i).getCategoryName());
            category.setCategoryPrice(categoryList.get(i).getCategoryPrice());
            category.setCategoryCapacity(categoryList.get(i).getCategoryCapacity());
            category.setCategoryAvailableCount(categoryList.get(i).getCategoryCapacity());
            category.setCategoryDescription(categoryList.get(i).getCategoryDescription());
            category.setEventId(eventId);

            categoryRepository.save(category);
        }
    }
}
