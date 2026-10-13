package ru.practicum.event.common;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventState {
    PENDING,
    PUBLISHED,
    CANCELED;

    @JsonCreator
    public static EventState fromString(String value) {
        return EventState.valueOf(value.toUpperCase());
    }
}