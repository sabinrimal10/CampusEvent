package com.edap.campusevents.controller;

import com.edap.campusevents.repository.BookingRepository;
import com.edap.campusevents.repository.UserRepository;
import com.edap.campusevents.service.EventService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

// Everything under /admin is restricted to ROLE_ADMIN by SecurityConfig - this
// controller only needs to worry about assembling the dashboard, not access control.
@Controller
@RequestMapping("/admin")
public class AdminController {

    private final EventService eventService;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public AdminController(EventService eventService, UserRepository userRepository,
                            BookingRepository bookingRepository) {
        this.eventService = eventService;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        model.addAttribute("events", eventService.findAll(null));
        model.addAttribute("userCount", userRepository.count());
        return "admin/dashboard";
    }

    @GetMapping("/events/{id}/attendees")
    public String attendees(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("bookings", bookingRepository.findByEventIdOrderByBookedAtAsc(id));
        return "admin/attendees";
    }
}
