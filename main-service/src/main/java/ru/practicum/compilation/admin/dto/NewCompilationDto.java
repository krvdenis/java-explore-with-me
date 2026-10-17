package ru.practicum.compilation.admin.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Value;

import java.util.Set;

@Value
@NoArgsConstructor(force = true)
public class NewCompilationDto {
    Set<Long> events;
    boolean pinned;

    @NotBlank
    @Size(max = 50, message = "The title must not exceed 50 characters.")
    String title;
}
