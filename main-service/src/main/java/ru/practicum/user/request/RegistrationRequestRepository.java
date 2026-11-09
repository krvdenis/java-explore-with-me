package ru.practicum.user.request;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.user.request.model.RegistrationRequest;
import ru.practicum.user.request.model.RegistrationStatus;

import java.util.List;
import java.util.Optional;

public interface RegistrationRequestRepository extends JpaRepository<RegistrationRequest, Long> {
    boolean existsByEmailAndStatusIn(String email, List<RegistrationStatus> statuses);

    Optional<RegistrationRequest> findByEmailIgnoreCaseAndStatus(String email, RegistrationStatus status);
}
