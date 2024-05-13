package com.example.showtime.ticket.repository;

import com.example.showtime.ticket.model.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Category findByCategoryId(Long id);
    Category findByCategoryIdAndEventId(Long id, String eventId);
    List<Category> findAllByEventId(String eventId);

    List<Category> findByEventIdOrderByCategoryPriceDesc(String eventId);
}
