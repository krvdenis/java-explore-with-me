package ru.practicum.event.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.common.dto.CategoryDto;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.model.embeddable.Location;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventFullDto {
    String annotation;
    CategoryDto category;
    Long confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime createdOn;

    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    Location location;
    Long id;
    UserShortDto initiator;
    Boolean paid;
    Long participantLimit;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime publishedOn;

    Boolean requestModeration;
    State state;
    String title;
    Long views;
}
