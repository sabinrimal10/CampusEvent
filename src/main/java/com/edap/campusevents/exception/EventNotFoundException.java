package com.edap.campusevents.exception;

public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("No event found with id " + id);
    }
}
