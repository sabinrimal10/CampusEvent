package com.edap.campusevents.service;

import com.edap.campusevents.exception.AlreadyBookedException;
import com.edap.campusevents.exception.EventFullException;
import com.edap.campusevents.exception.EventNotFoundException;
import com.edap.campusevents.model.AppUser;
import com.edap.campusevents.model.Booking;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.repository.BookingRepository;
import com.edap.campusevents.repository.EventRepository;
import com.edap.campusevents.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class EventService {

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;

    public EventService(EventRepository eventRepository, UserRepository userRepository,
                         BookingRepository bookingRepository) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Event> findAll(String category) {
        List<Event> events = (category == null || category.isBlank())
                ? eventRepository.findAllByOrderByStartTimeAsc()
                : eventRepository.findByCategoryIgnoreCaseOrderByStartTimeAsc(category);
        events.forEach(this::attachRealBookingCount);
        return events;
    }

    public List<String> findDistinctCategories() {
        return eventRepository.findDistinctCategories();
    }

    public Map<String, Long> countsByCategory() {
        Map<String, Long> counts = new LinkedHashMap<>();
        for (String category : eventRepository.findDistinctCategories()) {
            counts.put(category, eventRepository.countByCategoryIgnoreCase(category));
        }
        return counts;
    }

    public long countAll() {
        return eventRepository.count();
    }

    public Event findById(Long id) {
        Event event = eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
        attachRealBookingCount(event);
        return event;
    }

    public Event create(Event event) {
        event.setId(null);
        return eventRepository.save(event);
    }

    public Event update(Long id, Event updated) {
        Event existing = findById(id);
        existing.setTitle(updated.getTitle());
        existing.setDescription(updated.getDescription());
        existing.setLocation(updated.getLocation());
        existing.setCategory(updated.getCategory());
        existing.setOrganizerName(updated.getOrganizerName());
        existing.setStartTime(updated.getStartTime());
        existing.setCapacity(updated.getCapacity());
        return eventRepository.save(existing);
    }

    public void cancel(Long id) {
        Event existing = findById(id);
        eventRepository.delete(existing);
    }

    public boolean hasUserBooked(Long eventId, String username) {
        Event event = findById(eventId);
        AppUser user = requireUser(username);
        return bookingRepository.existsByUserAndEvent(user, event);
    }

    public Event book(Long id, String username) {
        Event event = findById(id);
        AppUser user = requireUser(username);

        if (bookingRepository.existsByUserAndEvent(user, event)) {
            throw new AlreadyBookedException(id);
        }
        if (event.isFull()) {
            throw new EventFullException(id);
        }

        Booking booking = new Booking();
        booking.setUser(user);
        booking.setEvent(event);
        bookingRepository.save(booking);

        attachRealBookingCount(event);
        return event;
    }

    private AppUser requireUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new IllegalStateException("No account for authenticated user " + username));
    }

    private void attachRealBookingCount(Event event) {
        event.setBookingCount((int) bookingRepository.countByEventId(event.getId()));
    }
}
