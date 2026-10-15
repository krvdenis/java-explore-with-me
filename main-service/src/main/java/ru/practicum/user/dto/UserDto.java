package ru.practicum.user.dto;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.ToString;
import lombok.experimental.FieldDefaults;

@Builder
@Getter
@ToString
@FieldDefaults(level = AccessLevel.PRIVATE)
public class UserDto {
    Long id;
    String email;
    String name;
}