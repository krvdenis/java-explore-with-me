package ru.practicum.category.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.ToString;
import lombok.Value;

@Value
@ToString
@NoArgsConstructor(force = true)
public class NewCategoryDto {
    @NotBlank
    @Size(max = 50, message = "The name must not exceed 50 characters.")
    String name;
}