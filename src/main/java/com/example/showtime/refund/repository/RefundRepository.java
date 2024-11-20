package com.example.showtime.refund.repository;

import com.example.showtime.refund.model.entity.RefundItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface RefundRepository extends JpaRepository<RefundItem, Long> {

}
