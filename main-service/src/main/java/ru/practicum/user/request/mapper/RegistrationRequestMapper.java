package ru.practicum.user.request.mapper;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.user.request.model.RegistrationRequest;
import ru.practicum.user.request.dto.NewRegistrationRequest;
import ru.practicum.user.request.dto.RegistrationRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class RegistrationRequestMapper {


    public static RegistrationRequest mapToRegistrationRequest(
            NewRegistrationRequest newRequest,
            String encodePassword) {
        return RegistrationRequest.builder()
                .email(newRequest.getEmail())
                .name(newRequest.getName())
                .password(encodePassword)
                .build();
    }

    public static RegistrationRequestDto mapToDto(RegistrationRequest request) {
        return RegistrationRequestDto.builder()
                .id(request.getId())
                .email(request.getEmail())
                .name(request.getName())
                .status(request.getStatus())
                .build();
    }
}
