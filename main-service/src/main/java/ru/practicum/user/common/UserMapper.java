package ru.practicum.user.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.user.dto.NewUserRequest;
import ru.practicum.user.dto.UserDto;
import ru.practicum.user.dto.UserShort;
import ru.practicum.user.dto.UserShortDto;
import ru.practicum.user.model.User;
import ru.practicum.user.model.UserRole;
import ru.practicum.user.request.model.RegistrationRequest;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class UserMapper {
    public static User mapToUser(NewUserRequest dto, String encodePassword) {
        return User.builder()
                .email(dto.getEmail())
                .name(dto.getName())
                .password(encodePassword)
                .build();
    }

    public static UserDto mapToDto(User user) {
        return UserDto.builder()
                .email(user.getEmail())
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static UserShortDto mapToShortDto(UserShort user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static UserShortDto mapToShortDto(User user) {
        return UserShortDto.builder()
                .id(user.getId())
                .name(user.getName())
                .build();
    }

    public static User mapToUser(RegistrationRequest request, UserRole role) {
        return User.builder()
        		.email(request.getEmail())
        		.name(request.getName())
                .password(request.getPassword())
                .role(role)
        		.build();
    }
}