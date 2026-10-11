package ru.practicum.event.admin.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.event.admin.enums.AdminStateAction;
import ru.practicum.event.common.model.embeddable.Location;
import ru.practicum.util.Constants;

import java.time.LocalDateTime;

@Getter
@ToString
@NoArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UpdateEventAdminRequest {
    String annotation;
    Long category;
    String description;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = Constants.DATE_TIME_PATTERN)
    LocalDateTime eventDate;
    Location location;
    Boolean paid;
    Long participantLimit;
    Boolean requestModeration = true;
    AdminStateAction stateAction;
    String title;

    public boolean hasAnnotation() {
        return !(annotation == null || annotation.isBlank());
    }

    public boolean hasCategory() {
        return !(category == null);
    }

    public boolean hasDescription() {
        return !(description == null || description.isBlank());
    }

    public boolean hasEventDate() {
        return !(eventDate == null);
    }

    public boolean hasLocation() {
        return !(location == null);
    }

    public boolean hasPaid() {
        return !(paid == null);
    }

    public boolean hasParticipantLimit() {
        return !(participantLimit == null);
    }

    public boolean hasStateAction() {
        return !(stateAction == null);
    }

    public boolean hasRequestModeration() {
        return !(requestModeration == null);
    }

    public boolean hasTitle() {
        return !(title == null || title.isBlank());
    }
}
