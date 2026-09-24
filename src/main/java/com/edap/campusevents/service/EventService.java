package com.edap.campusevents.service;

import com.edap.campusevents.exception.EventFullException;
import com.edap.campusevents.exception.EventNotFoundException;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.repository.EventRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventService {

    private final EventRepository eventRepository;

    public EventService(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    public List<Event> findAll(String category) {
        if (category == null || category.isBlank()) {
            return eventRepository.findAllByOrderByStartTimeAsc();
        }
        return eventRepository.findByCategoryIgnoreCaseOrderByStartTimeAsc(category);
    }

    public Event findById(Long id) {
        return eventRepository.findById(id)
                .orElseThrow(() -> new EventNotFoundException(id));
    }

    public Event create(Event event) {
        event.setId(null);
        event.setRsvpCount(0);
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

    public Event rsvp(Long id) {
        Event event = findById(id);
        if (event.isFull()) {
            throw new EventFullException(id);
        }
        event.setRsvpCount(event.getRsvpCount() + 1);
        return eventRepository.save(event);
    }
}
