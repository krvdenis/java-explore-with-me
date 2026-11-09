package ru.practicum.security;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import ru.practicum.user.admin.AdminUserService;
import ru.practicum.user.model.User;

@Component
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityChecker {
    AdminUserService userService;

    public void checkResourceOwner(Long userId) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AccessDeniedException("Полная аутентификация требуется для доступа к этому ресурсу");
        }

        String currentEmail = authentication.getName();
        User requestedUser = userService.getUserById(userId);

        if (!requestedUser.getEmail().equalsIgnoreCase(currentEmail)) {
            log.warn("ОТКАЗ В ДОСТУПЕ: Пользователь [{}] пытается получить доступ к данным id = {}",
                    currentEmail, userId);
            throw new AccessDeniedException("Вы не имеете доступа к ресурсам другого пользователя!");
        }
    }
}
