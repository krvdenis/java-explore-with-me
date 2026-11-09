package ru.practicum.email;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import ru.practicum.user.event.RegistrationRequestProcessedEvent;
import ru.practicum.user.request.model.RegistrationStatus;

@Service
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class EmailServiceImpl implements EmailService {
    JavaMailSender mailSender;

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    @Override
    public void handleRegistrationEvent(RegistrationRequestProcessedEvent event) {
        log.info("Событие транзакции успешно перехвачено для email: {}", event.email());

        if (event.status() == RegistrationStatus.APPROVED) {
            this.sendApprovalEmail(event.email(), event.name());
        } else if (event.status() == RegistrationStatus.REJECTED) {
            this.sendRejectionEmail(event.email(), event.name());
        }
    }

    @Override
    public void sendApprovalEmail(String toEmail, String userName) {
        log.info("Отправка асинхронного письма об успешной регистрации на email: {}", toEmail);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@explorewithme.ru");
        message.setTo(toEmail);
        message.setSubject("Explore With Me: Ваша заявка одобрена!");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                        "Администратор успешно одобрил вашу заявку на регистрацию.\n" +
                        "Теперь вы можете войти в систему, используя ваш email и пароль, который вы указали при " +
                        "подаче заявки.\n\n" +
                        "Добро пожаловать на платформу!",
                userName
        ));

        mailSender.send(message);
        log.info("Письмо об одобрении успешно отправлено на сервер SMTP");
    }

    @Override
    public void sendRejectionEmail(String toEmail, String userName) {
        log.info("Отправка асинхронного письма об отклонении заявки на email: {}", toEmail);


        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("noreply@explorewithme.ru");
        message.setTo(toEmail);
        message.setSubject("Explore With Me: Статус вашей заявки");
        message.setText(String.format(
                "Здравствуйте, %s!\n\n" +
                        "К сожалению, ваша заявка на регистрацию в системе Explore With Me была отклонена " +
                        "администратором после модерации.\n\n" +
                        "Если у вас возникли вопросы, пожалуйста, свяжитесь с поддержкой платформы.",
                userName
        ));

        mailSender.send(message);
        log.info("Письмо об отклонении успешно отправлено на сервер SMTP");
    }
}
