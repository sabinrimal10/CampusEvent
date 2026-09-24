package com.edap.campusevents.dto;

import java.time.LocalDateTime;

// A deliberately narrow projection of a Booking - never serialize Booking or
// AppUser directly, since AppUser carries the (hashed) password field.
public record AttendeeView(String username, LocalDateTime bookedAt) {
}
