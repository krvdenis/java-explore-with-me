package ru.practicum.user.request;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.request.dto.NewRegistrationRequest;
import ru.practicum.user.request.dto.RegistrationRequestDto;
import ru.practicum.user.request.mapper.RegistrationRequestMapper;
import ru.practicum.user.request.model.RegistrationRequest;
import ru.practicum.user.request.model.RegistrationStatus;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class RegistrationRequestServiceImpl implements RegistrationRequestService {
    RegistrationRequestRepository registrationRequestRepository;
    PasswordEncoder passwordEncoder;

    @Override
    public RegistrationRequestDto createRequest(NewRegistrationRequest newRequest) {
        log.info("Создание записи нового запроса на регистрацию пользователя с email = {} в БД", newRequest.getEmail());
        if (registrationRequestRepository.existsByEmailAndStatusIn(newRequest.getEmail(),
                List.of(RegistrationStatus.APPROVED, RegistrationStatus.PENDING))) {
            log.warn("Ошибка проверки: запрос на регистрацию пользователя с email {} уже есть в БД с активным статусом",
                    newRequest.getEmail());
            throw new ConditionsNotMetException("A registration request for this email already exists with an active" +
                    " status.");
        }

        String encodePassword = passwordEncoder.encode(newRequest.getPassword());
        RegistrationRequest request = RegistrationRequestMapper.mapToRegistrationRequest(newRequest, encodePassword);
        RegistrationRequest savedRequest = registrationRequestRepository.save(request);
        RegistrationRequestDto requestDto = RegistrationRequestMapper.mapToDto(savedRequest);
        log.info("Сгенерирован ID {} для нового запроса на регистрацию", requestDto.getId());
        return requestDto;
    }

    @Override
    public void changeRequestStatusById(long requestId, RegistrationStatus status) {
        log.info("Изменение статуса заявки на регистрацию с ID {} на {}", requestId, status);

        RegistrationRequest registrationRequest = getRegistrationRequest(requestId);
        registrationRequest.setStatus(status);
    }


    @Override
    public RegistrationRequest findById(long requestId) {
        return getRegistrationRequest(requestId);
    }

    @Override
    public RegistrationRequest findByEmail(String email, RegistrationStatus status) {
        return registrationRequestRepository.findByEmailIgnoreCaseAndStatus(email, status)
                .orElseThrow(() -> new NotFoundException(String.format("RegistrationRequest with email %s was not found",
                        email)));
    }

    private RegistrationRequest getRegistrationRequest(long requestId) {
        log.info("Получение заявки на регистрацию с ID {}", requestId);
        return registrationRequestRepository.findById(requestId)
                .orElseThrow(() -> new NotFoundException(String.format("RegistrationRequest with ID %s was not found",
                        requestId)));
    }
}
