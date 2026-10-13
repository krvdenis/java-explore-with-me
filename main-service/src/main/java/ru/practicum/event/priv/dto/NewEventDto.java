package ru.practicum.event.priv.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.common.model.embeddable.Location;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Getter
@Setter
@ToString
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewEventDto {
    @NotBlank
    @Size(max = 2000, message = "The annotation must not exceed 2000 characters.")
    @Size(min = 20, message = "The annotation must be at least 20 characters long.")
    String annotation;

    @NotNull
    Long category;

    @NotBlank
    @Size(max = 7000, message = "The description must not exceed 7000 characters.")
    @Size(min = 20, message = "The description must be at least 20 characters long.")
    String description;

    @NotNull
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    @Future
    LocalDateTime eventDate;

    @NotNull
    Location location;

    boolean paid;

    @PositiveOrZero
    long participantLimit;

    Boolean requestModeration;

    @NotBlank
    @Size(max = 120, message = "The title must not exceed 120 characters.")
    @Size(min = 3, message = "The title must be at least 3 characters long.")
    String title;
}
