package ru.practicum.comment.priv;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.common.Comment;
import ru.practicum.comment.common.CommentDto;
import ru.practicum.comment.common.CommentMapper;
import ru.practicum.comment.common.CommentRepository;
import ru.practicum.event.common.EventState;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.pub.PublicEventService;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;
import ru.practicum.user.User;
import ru.practicum.user.UserService;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateCommentServiceImpl implements PrivateCommentService {
    CommentRepository commentRepository;
    PublicEventService publicEventService;
    UserService userService;

    @Override
    public CommentDto createComment(long userId, long eventId, CommentRequestDto newCommentDto) {
        log.info("Получен запрос на создание комментария от пользователя с ID {} к событию с ID {}", userId, eventId);
        log.debug("Данные нового комментария: {}. Пользователь с ID {}, событие с ID {}.", newCommentDto, userId,
                eventId);

        Event event = publicEventService.getEventById(eventId);

        if (event.getState() != EventState.PUBLISHED) {
            log.warn("Ошибка создания комментария: событие с ID {} не опубликовано (текущий статус: {})",
                    eventId, event.getState());
            throw new ConditionsNotMetException(String.format("Cannot add a comment because the event is " +
                    "not published (current state: %s.)", event.getState()));
        }

        User author = userService.getUserById(userId);
        Comment comment = CommentMapper.toComment(newCommentDto);
        comment.setEvent(event);
        comment.setAuthor(author);

        Comment savedComment = commentRepository.save(comment);
        log.info("Комментарий успешно создан с ID {} для события с ID {}", savedComment.getId(), eventId);

        return CommentMapper.mapToDto(savedComment);
    }

    @Override
    public CommentDto updateComment(long userId, long commentId, CommentRequestDto updateCommentDto) {
        log.info("Получен запрос на обновление комментария с ID {} от пользователя с ID={}", commentId, userId);
        log.debug("Данные обновления комментария: {}. Пользователь с ID {}, комментарий с ID {}.", updateCommentDto,
                userId, commentId);

        Comment comment = getComment(commentId);
        validateCommentAuthor(userId, commentId, comment);

        CommentMapper.updateCommentFields(comment, updateCommentDto);
        log.info("Комментарий с ID {} успешно обновлен пользователем с ID {}", commentId, userId);

        return CommentMapper.mapToDto(comment);
    }

    @Override
    public void deleteCommentById(long userId, long commentId) {
        log.info("Получен запрос на удаление комментария с ID={} от пользователя с ID={}", commentId, userId);

        Comment comment = getComment(commentId);
        validateCommentAuthor(userId, commentId, comment);

        commentRepository.deleteById(commentId);
        log.info("Комментарий с ID={} успешно удален пользователем с ID={}", commentId, userId);
    }

    private Comment getComment(long commentId) {
        log.debug("Запрос комментария с ID {} из репозитория", commentId);
        return commentRepository.findById(commentId)
                .orElseThrow(() -> {
                    log.warn("Комментарий с ID {} не найден в базе данных", commentId);
                    return new NotFoundException(String.format("Comment with ID %d was not found", commentId));
                });
    }

    private void validateCommentAuthor(long userId, long commentId, Comment comment) {
        log.debug("Проверка авторства для комментария с ID {} и пользователя с ID {}", commentId, userId);
        if (!comment.getAuthor().getId().equals(userId)) {
            log.warn("Нет доступа: пользователь с ID {} не является автором комментария с ID {}", userId, commentId);
            throw new ConditionsNotMetException(String.format(
                    "User with ID %d is not the author of comment with ID %d", userId, commentId));
        }
    }
}
