package ru.practicum.event.admin;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.common.model.embeddable.Location;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {
    @Size(max = 2000, message = "The annotation must not exceed 2000 characters.")
    @Size(min = 20, message = "The annotation must be at least 20 characters long.")
    String annotation;

    Long category;

    @Size(max = 7000, message = "The description must not exceed 7000 characters.")
    @Size(min = 20, message = "The description must be at least 20 characters long.")
    String description;

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    @Future
    LocalDateTime eventDate;
    Location location;
    Boolean paid;

    @PositiveOrZero
    Long participantLimit;

    Boolean requestModeration;
    AdminStateAction stateAction;

    @Size(max = 120, message = "The title must not exceed 120 characters.")
    @Size(min = 3, message = "The title must be at least 3 characters long.")
    String title;

    public boolean hasAnnotation() {
        return !(annotation == null || annotation.isBlank());
    }

    public boolean hasCategory() {
        return category != null;
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasEventDate() {
        return eventDate != null;
    }

    public boolean hasLocation() {
        return location != null;
    }

    public boolean hasPaid() {
        return paid != null;
    }

    public boolean hasParticipantLimit() {
        return participantLimit != null;
    }

    public boolean hasStateAction() {
        return stateAction != null;
    }

    public boolean hasRequestModeration() {
        return requestModeration != null;
    }

    public boolean hasTitle() {
        return !(title == null || title.isBlank());
    }
}
