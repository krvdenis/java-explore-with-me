package ru.practicum.user.request;

import ru.practicum.user.request.dto.NewRegistrationRequest;
import ru.practicum.user.request.dto.RegistrationRequestDto;
import ru.practicum.user.request.model.RegistrationRequest;
import ru.practicum.user.request.model.RegistrationStatus;

public interface RegistrationRequestService {
    RegistrationRequestDto createRequest(NewRegistrationRequest newRequest);

    void changeRequestStatusById(long requestId, RegistrationStatus status);

    RegistrationRequest findById(long requestId);

    RegistrationRequest findByEmail(String email, RegistrationStatus status);
}
