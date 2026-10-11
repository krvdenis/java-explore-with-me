package ru.practicum.event.common.enums;

import com.fasterxml.jackson.annotation.JsonCreator;

public enum State {
    PENDING,
    PUBLISHED,
    CANCELED;

    @JsonCreator
    public static State fromString(String value) {
        return State.valueOf(value.toUpperCase());
    }
}

//Добавить для всех enum Unknown и добавить обработку этого варианта?