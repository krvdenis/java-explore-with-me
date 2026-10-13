package ru.practicum.event.common.dto;

import ru.practicum.category.common.CategoryShort;
import ru.practicum.user.dto.UserShort;

import java.time.LocalDateTime;

public interface EventShort {
    String getAnnotation();

    CategoryShort getCategory();

    Long getConfirmedRequests();

    LocalDateTime getEventDate();

    Long getId();

    UserShort getInitiator();

    Boolean getPaid();

    String getTitle();
}
