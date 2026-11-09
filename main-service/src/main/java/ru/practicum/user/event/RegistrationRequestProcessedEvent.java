package ru.practicum.user.event;

import ru.practicum.user.request.model.RegistrationStatus;

public record RegistrationRequestProcessedEvent(String email, String name, RegistrationStatus status) {}
