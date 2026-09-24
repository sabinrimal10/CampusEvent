package com.edap.campusevents.controller;

import com.edap.campusevents.exception.AlreadyBookedException;
import com.edap.campusevents.exception.EventFullException;
import com.edap.campusevents.exception.EventNotFoundException;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.service.EventService;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.view.RedirectView;

@Controller
@RequestMapping("/events")
public class EventController {

    private final EventService eventService;

    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @GetMapping
    public String list(@RequestParam(required = false) String category, Model model, Authentication authentication) {
        model.addAttribute("events", eventService.findAll(category));
        model.addAttribute("categories", eventService.findDistinctCategories());
        model.addAttribute("categoryCounts", eventService.countsByCategory());
        model.addAttribute("totalEventCount", eventService.countAll());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("currentUsername", authentication.getName());
        return "events/list";
    }

    @GetMapping("/new")
    public String newForm(Model model) {
        model.addAttribute("event", new Event());
        model.addAttribute("formAction", "/events");
        model.addAttribute("isEdit", false);
        return "events/form";
    }

    @PostMapping
    public ModelAndView create(@Valid @ModelAttribute("event") Event event,
                                BindingResult result,
                                Model model,
                                HttpServletResponse response) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/events");
            model.addAttribute("isEdit", false);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ModelAndView("events/form");
        }
        Event saved = eventService.create(event);
        // 303 See Other: the browser should re-GET the new resource, not resubmit the POST
        return seeOther("/events/" + saved.getId());
    }

    @GetMapping("/{id}")
    public String detail(@PathVariable Long id, Model model, Authentication authentication) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("alreadyBooked", eventService.hasUserBooked(id, authentication.getName()));
        return "events/detail";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        model.addAttribute("event", eventService.findById(id));
        model.addAttribute("formAction", "/events/" + id);
        model.addAttribute("isEdit", true);
        return "events/form";
    }

    @PutMapping("/{id}")
    public ModelAndView update(@PathVariable Long id,
                                @Valid @ModelAttribute("event") Event event,
                                BindingResult result,
                                Model model,
                                HttpServletResponse response) {
        if (result.hasErrors()) {
            model.addAttribute("formAction", "/events/" + id);
            model.addAttribute("isEdit", true);
            response.setStatus(HttpStatus.BAD_REQUEST.value());
            return new ModelAndView("events/form");
        }
        eventService.update(id, event);
        return seeOther("/events/" + id);
    }

    @DeleteMapping("/{id}")
    public ModelAndView cancel(@PathVariable Long id) {
        eventService.cancel(id);
        return seeOther("/events");
    }

    @PostMapping("/{id}/book")
    public ModelAndView book(@PathVariable Long id, Model model, HttpServletResponse response,
                              Authentication authentication) {
        try {
            eventService.book(id, authentication.getName());
            return seeOther("/events/" + id);
        } catch (EventFullException e) {
            model.addAttribute("event", eventService.findById(id));
            model.addAttribute("alreadyBooked", false);
            model.addAttribute("bookingError", "Sorry, this event is full.");
            response.setStatus(HttpStatus.CONFLICT.value());
            return new ModelAndView("events/detail");
        } catch (AlreadyBookedException e) {
            model.addAttribute("event", eventService.findById(id));
            model.addAttribute("alreadyBooked", true);
            model.addAttribute("bookingError", "You've already booked this event.");
            response.setStatus(HttpStatus.CONFLICT.value());
            return new ModelAndView("events/detail");
        }
    }

    @ExceptionHandler(EventNotFoundException.class)
    public String handleNotFound(EventNotFoundException ex, Model model, HttpServletResponse response) {
        model.addAttribute("message", ex.getMessage());
        response.setStatus(HttpStatus.NOT_FOUND.value());
        return "error/404";
    }

    private ModelAndView seeOther(String path) {
        RedirectView redirectView = new RedirectView(path);
        redirectView.setStatusCode(HttpStatus.SEE_OTHER);
        return new ModelAndView(redirectView);
    }
}
