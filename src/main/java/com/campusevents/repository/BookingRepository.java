package com.edap.campusevents.repository;

import com.edap.campusevents.model.AppUser;
import com.edap.campusevents.model.Booking;
import com.edap.campusevents.model.Event;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    boolean existsByUserAndEvent(AppUser user, Event event);

    List<Booking> findByEventIdOrderByBookedAtAsc(Long eventId);

    long countByEventId(Long eventId);
}
