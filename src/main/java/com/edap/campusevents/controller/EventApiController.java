package com.edap.campusevents.controller;

import com.edap.campusevents.dto.AttendeeView;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.repository.BookingRepository;
import com.edap.campusevents.service.EventService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

// Read-only JSON view of the same event data the HTML pages serve. This is
// purely additional - the Thymeleaf pages under /events keep serving HTML
// with embedded hypermedia controls; this just gives a plain-data view of
// the same resources for inspecting in Postman / browser dev tools.
@RestController
@RequestMapping("/api/events")
public class EventApiController {

    private final EventService eventService;
    private final BookingRepository bookingRepository;

    public EventApiController(EventService eventService, BookingRepository bookingRepository) {
        this.eventService = eventService;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public List<Event> list(@RequestParam(required = false) String category) {
        return eventService.findAll(category);
    }

    @GetMapping("/{id}")
    public Event detail(@PathVariable Long id) {
        return eventService.findById(id);
    }

    // Admin-only per SecurityConfig - deliberately returns AttendeeView, not
    // Booking/AppUser, so the password hash never reaches a JSON response.
    @GetMapping("/{id}/attendees")
    public List<AttendeeView> attendees(@PathVariable Long id) {
        eventService.findById(id); // 404s via EventNotFoundException if the event doesn't exist
        return bookingRepository.findByEventIdOrderByBookedAtAsc(id).stream()
                .map(booking -> new AttendeeView(booking.getUser().getUsername(), booking.getBookedAt()))
                .toList();
    }
}
