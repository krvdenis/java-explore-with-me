package ru.practicum.event.common.dto;

import ru.practicum.category.common.model.Category;
import ru.practicum.user.model.User;

import java.time.LocalDateTime;

public interface EventShort {
    String getAnnotation();

    Category getCategory();

    Long getConfirmedRequests();

    LocalDateTime getEventDate();

    Long getId();

    User getInitiator();

    Boolean getPaid();

    String getTitle();
}
