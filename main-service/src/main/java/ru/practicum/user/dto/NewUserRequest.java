package ru.practicum.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class NewUserRequest {
    @NotBlank
    @Email
    @Size(max = 254, message = "The email must not exceed 254 characters.")
    @Size(min = 6, message = "The email must be at least 6 characters long.")
    String email;

    @NotBlank(message = "must not be blank")
    @Size(min = 2, message = "The name must be at least 2 characters long.")
    @Size(max = 250, message = "The name must not exceed 250 characters.")
    String name;
}
