package ru.practicum.email;

import ru.practicum.user.event.RegistrationRequestProcessedEvent;

public interface EmailService {

    void handleRegistrationEvent(RegistrationRequestProcessedEvent event);

    void sendApprovalEmail(String toEmail, String userName);

    void sendRejectionEmail(String toEmail, String userName);
}
