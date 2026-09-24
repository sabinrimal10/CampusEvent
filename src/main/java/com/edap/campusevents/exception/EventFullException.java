package com.edap.campusevents.exception;

public class EventFullException extends RuntimeException {

    public EventFullException(Long id) {
        super("Event " + id + " is already full");
    }
}
