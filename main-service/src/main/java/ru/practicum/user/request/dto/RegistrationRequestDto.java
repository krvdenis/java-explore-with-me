package ru.practicum.user.request.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;
import ru.practicum.user.request.model.RegistrationStatus;

@Builder
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class RegistrationRequestDto {
    Long id;
    String email;
    String name;
    RegistrationStatus status;
}
