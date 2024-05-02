package com.example.showtime.ticket.service;

import com.example.showtime.ticket.model.entity.Category;
import com.example.showtime.ticket.model.request.CategoryRequest;

import java.util.List;

public interface ICategoryService {
    Category getCategoryById(Long categoryId);
    Category getCategoryByIdAndEventId(Long categoryId, String eventId);
    void createCategory(List<CategoryRequest> categoryList, String eventId);
    List<Category> getAllCategoriesByEventId(String eventId);

    void updateAvailableTickets(Long categoryId, String eventId, long size);

    Double getTicketPrice(Long ticketCategory, String eventId);
}
