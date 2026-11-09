package ru.practicum.user.common;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.model.User;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailIgnoreCase(String email);

}