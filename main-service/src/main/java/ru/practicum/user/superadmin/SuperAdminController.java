package ru.practicum.user.superadmin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.user.admin.AdminUserService;
import ru.practicum.user.model.UserRole;

import static ru.practicum.util.Constants.PATH_SUPER_ADMIN;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_SUPER_ADMIN)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SuperAdminController {
    AdminUserService adminUserService;

    @PatchMapping("/make-user-admin")
    void makeUserAdmin(@RequestParam long userId) {
        log.info("Запрос на предоставление роли ADMIN пользователю с ID {}", userId);
        adminUserService.updateRole(userId, UserRole.ADMIN);
    }
}
