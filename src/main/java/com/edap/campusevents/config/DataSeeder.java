package com.edap.campusevents.config;

import com.edap.campusevents.model.Event;
import com.edap.campusevents.repository.EventRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private final EventRepository eventRepository;

    public DataSeeder(EventRepository eventRepository) {
        this.eventRepository = eventRepository;
    }

    @Override
    public void run(String... args) {
        if (eventRepository.count() > 0) {
            return;
        }

        eventRepository.save(seed("Algorithms Study Group", "Study Group",
                "Working through past exam papers together ahead of the mid-semester test. Bring your notes.",
                "Library, Room 2.14", "Priya N.", LocalDateTime.now().plusDays(3).withHour(17).withMinute(0), 8));

        eventRepository.save(seed("Welcome Back Mixer", "Social",
                "Casual meetup for new and returning students. Free pizza while it lasts.",
                "Student Union Hall", "CampusEvents Team", LocalDateTime.now().plusDays(5).withHour(18).withMinute(30), 60));

        eventRepository.save(seed("Intro to Git Workshop", "Workshop",
                "Hands-on session covering branches, merges, and resolving conflicts. Laptop required.",
                "Computing Lab 1", "Dev Society", LocalDateTime.now().plusDays(7).withHour(14).withMinute(0), 25));

        eventRepository.save(seed("5-a-side Football", "Sports",
                "Casual kickabout, all skill levels welcome. Boots not required.",
                "Sports Field 3", "Campus Sports Club", LocalDateTime.now().plusDays(2).withHour(16).withMinute(0), 10));
    }

    private Event seed(String title, String category, String description, String location,
                        String organizer, LocalDateTime startTime, int capacity) {
        Event event = new Event();
        event.setTitle(title);
        event.setCategory(category);
        event.setDescription(description);
        event.setLocation(location);
        event.setOrganizerName(organizer);
        event.setStartTime(startTime);
        event.setCapacity(capacity);
        return event;
    }
}
