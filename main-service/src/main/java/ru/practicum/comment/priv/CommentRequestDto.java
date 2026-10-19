package ru.practicum.comment.priv;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.NoArgsConstructor;
import lombok.Value;

@Value
@NoArgsConstructor(force = true)
public class CommentRequestDto {

    @NotBlank
    @Size(max = 3000, message = "The comment must not exceed 3000 characters.")
    String text;
}
