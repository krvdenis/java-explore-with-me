package ru.practicum.util;

import java.time.LocalDateTime;

public class Constants {
    public static final String PATH_ADMIN = "/admin";
    public static final String PATH_USER_ID = "/{userId}";
    public static final String PATH_USERS = "/users";
    public static final String PATH_CATEGORIES = "/categories";
    public static final String PATH_CATEGORY_ID = "/{catId}";
    public static final String PATH_EVENT_ID = "/{eventId}";
    public static final String PATH_EVENTS = "/events";
    public static final String PATH_REQUESTS = "/requests";
    public static final String PATH_REQUEST_ID = "/{requestId}";
    public static final String PATH_CANCEL = "/cancel";
    public static final String PATH_COMPILATIONS = "/compilations";
    public static final String PATH_COMPILATION_ID = "/{compId}";
    public static final String DATE_TIME_PATTERN = "yyyy-MM-dd HH:mm:ss";
    public static final LocalDateTime START_OF_TIME = LocalDateTime.of(2020, 1, 1, 0,
            0);

    private Constants() {
    }
}
