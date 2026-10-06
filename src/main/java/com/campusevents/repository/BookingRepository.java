package com.campusevents.repository;

import com.campusevents.model.AppUser;
import com.campusevents.model.Booking;
import com.campusevents.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserAndEvent(AppUser user, Event event);

    List<Booking> findByEventIdOrderByBookedAtAsc(Long eventId);

    long countByEventId(Long eventId);
}
