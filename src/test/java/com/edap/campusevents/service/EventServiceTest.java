package com.edap.campusevents.service;

import com.edap.campusevents.exception.AlreadyBookedException;
import com.edap.campusevents.exception.EventFullException;
import com.edap.campusevents.exception.EventNotFoundException;
import com.edap.campusevents.model.AppUser;
import com.edap.campusevents.model.Event;
import com.edap.campusevents.repository.BookingRepository;
import com.edap.campusevents.repository.EventRepository;
import com.edap.campusevents.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

// The booking flow is the app's one real business rule (capacity limits, one
// booking per user), so it's the part most worth covering with fast,
// dependency-free unit tests rather than only exercising it by hand.
@ExtendWith(MockitoExtension.class)
class EventServiceTest {

    @Mock
    private EventRepository eventRepository;
    @Mock
    private UserRepository userRepository;
    @Mock
    private BookingRepository bookingRepository;

    private EventService eventService;

    private Event event;
    private AppUser user;

    @BeforeEach
    void setUp() {
        eventService = new EventService(eventRepository, userRepository, bookingRepository);

        event = new Event();
        event.setId(1L);
        event.setCapacity(2);

        user = new AppUser();
        user.setId(1L);
        user.setUsername("alice");

        lenient().when(eventRepository.findById(1L)).thenReturn(Optional.of(event));
        lenient().when(userRepository.findByUsernameIgnoreCase("alice")).thenReturn(Optional.of(user));
        lenient().when(eventRepository.save(any(Event.class))).thenAnswer(inv -> inv.getArgument(0));
        // bookingCount is derived live from real Booking rows, not stored on Event -
        // this stub is what "how many people have actually booked" means for these tests.
        lenient().when(bookingRepository.countByEventId(1L)).thenReturn(0L);
    }

    @Test
    void bookingRecordsTheBookingAndIncrementsBookingCount() {
        // bookingCount is re-queried from BookingRepository after the save, so the mock
        // reflects the one real booking that now exists for this event.
        when(bookingRepository.countByEventId(1L)).thenReturn(1L);

        Event booked = eventService.book(1L, "alice");

        assertThat(booked.getBookingCount()).isEqualTo(1);
    }

    @Test
    void bookingTheSameEventTwiceForTheSameUserIsRejected() {
        when(bookingRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        assertThatThrownBy(() -> eventService.book(1L, "alice"))
                .isInstanceOf(AlreadyBookedException.class);
    }

    @Test
    void bookingAFullEventIsRejected() {
        when(bookingRepository.countByEventId(1L)).thenReturn(2L); // capacity is 2 - already full

        assertThatThrownBy(() -> eventService.book(1L, "alice"))
                .isInstanceOf(EventFullException.class);
    }

    @Test
    void bookingAnUnknownEventRaisesNotFound() {
        when(eventRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> eventService.book(99L, "alice"))
                .isInstanceOf(EventNotFoundException.class);
    }

    @Test
    void hasUserBookedReflectsTheBookingRepository() {
        when(bookingRepository.existsByUserAndEvent(user, event)).thenReturn(true);

        assertThat(eventService.hasUserBooked(1L, "alice")).isTrue();
    }
}
