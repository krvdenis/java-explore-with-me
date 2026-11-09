package ru.practicum.user.request.dto;


import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.experimental.FieldDefaults;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class NewRegistrationRequest {
    @NotBlank
    @Email
    @Size(min = 6, message = "The email must be at least 6 characters long.")
    @Size(max = 254, message = "The email must not exceed 254 characters.")
    String email;

    @NotBlank
    @Size(min = 2, message = "The name must be at least 2 characters long.")
    @Size(max = 250, message = "The name must not exceed 250 characters.")
    String name;

    @NotBlank
    @Size(min = 6, message = "The password must be at least 6 characters long.")
    @Size(max = 20, message = "The password must not exceed 100 characters.")
    String password;
}
