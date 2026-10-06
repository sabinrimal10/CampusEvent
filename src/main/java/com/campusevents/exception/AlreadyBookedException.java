package com.edap.campusevents.exception;

public class AlreadyBookedException extends RuntimeException {

    public AlreadyBookedException(Long eventId) {
        super("You have already booked event " + eventId);
    }
}
