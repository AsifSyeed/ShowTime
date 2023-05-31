package com.example.showtime.event.repository;

import com.example.showtime.event.model.entity.Event;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EventRepository extends JpaRepository<Event, Long> {
    boolean existsByEventName(String eventName);

    boolean existsByEventId(String eventId);

    Event findByEventId(String eventId);
}
