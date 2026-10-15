package ru.practicum.event.pub;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum EventSort {
    EVENT_DATE,
    VIEWS;

    @JsonCreator
    public static EventSort fromString(String value) {
        return EventSort.valueOf(value.toUpperCase());
    }
}
