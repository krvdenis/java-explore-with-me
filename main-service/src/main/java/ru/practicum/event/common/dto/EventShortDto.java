package ru.practicum.event.common.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.category.common.CategoryDto;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Builder
@Getter
@Setter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EventShortDto {
    Long id;
    String annotation;
    CategoryDto category;
    Long confirmedRequests;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;

    UserShortDto initiator;
    Boolean paid;
    String title;
    Long views;
}
