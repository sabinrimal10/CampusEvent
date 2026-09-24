package com.edap.campusevents.config;

import com.edap.campusevents.model.AppUser;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.model.Role;
import com.edap.campusevents.repository.EventRepository;
import com.edap.campusevents.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

@Component
public class DataSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

    // Seeded admin credentials for local demo/dev use only - there is no public
    // signup path to ADMIN, so this is the only way to reach the admin panel.
    private static final String ADMIN_USERNAME = "admin";
    private static final String ADMIN_PASSWORD = "admin123";

    private final EventRepository eventRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(EventRepository eventRepository, UserRepository userRepository,
                       PasswordEncoder passwordEncoder) {
        this.eventRepository = eventRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        seedAdmin();

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

        // Small capacity so it's easy to demonstrate the 409 Conflict "event
        // full" response by booking it out with two different accounts.
        eventRepository.save(seed("Resume Review Drop-in", "Workshop",
                "One-on-one resume feedback with careers advisors. Only a couple of slots each session.",
                "Careers Hub, Room 1.03", "Careers Service", LocalDateTime.now().plusDays(1).withHour(11).withMinute(0), 2));
    }

    private void seedAdmin() {
        if (userRepository.existsByUsernameIgnoreCase(ADMIN_USERNAME)) {
            return;
        }
        AppUser admin = new AppUser();
        admin.setUsername(ADMIN_USERNAME);
        admin.setPassword(passwordEncoder.encode(ADMIN_PASSWORD));
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
        log.info("Seeded admin account - username: '{}', password: '{}' (local dev only)",
                ADMIN_USERNAME, ADMIN_PASSWORD);
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
