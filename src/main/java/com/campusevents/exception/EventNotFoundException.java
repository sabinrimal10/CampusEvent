package com.edap.campusevents.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

// @ResponseStatus only kicks in when nothing else handles the exception - it
// gives the JSON API a plain 404 automatically, while EventController's own
// @ExceptionHandler still takes over for the HTML pages as before.
@ResponseStatus(HttpStatus.NOT_FOUND)
public class EventNotFoundException extends RuntimeException {

    public EventNotFoundException(Long id) {
        super("No event found with id " + id);
    }
}
