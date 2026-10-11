package ru.practicum.event.admin.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.event.admin.dto.UpdateEventAdminRequest;
import ru.practicum.event.common.model.Event;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class AdminEventMapper {
    public static void updateEventFields(Event event, UpdateEventAdminRequest updateRequest) {
        if (updateRequest.hasAnnotation()) {
            event.setAnnotation(updateRequest.getAnnotation());
        }

        if (updateRequest.hasDescription()) {
            event.setDescription(updateRequest.getDescription());
        }

        if (updateRequest.hasEventDate()) {
            event.setEventDate(updateRequest.getEventDate());
        }

        if (updateRequest.hasLocation()) {
            event.setLocation(updateRequest.getLocation());
        }

        if (updateRequest.hasPaid()) {
            event.setPaid(updateRequest.getPaid());
        }

        if (updateRequest.hasParticipantLimit()) {
            event.setParticipantLimit(updateRequest.getParticipantLimit());
        }

        if (updateRequest.hasRequestModeration()) {
            event.setRequestModeration(updateRequest.getRequestModeration());
        }

        if (updateRequest.hasTitle()) {
            event.setTitle(updateRequest.getTitle());
        }
    }
}

