package ru.practicum.comment.priv;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.common.CommentDto;

import static ru.practicum.util.Constants.PATH_COMMENTS;
import static ru.practicum.util.Constants.PATH_COMMENT_ID;
import static ru.practicum.util.Constants.PATH_EVENTS;
import static ru.practicum.util.Constants.PATH_EVENT_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PrivateCommentController {
    PrivateCommentService commentService;

    @PostMapping(PATH_EVENTS + PATH_EVENT_ID + PATH_COMMENTS)
    @ResponseStatus(HttpStatus.CREATED)
    public CommentDto createCommentForEvent(
            @RequestParam long userId,
            @PathVariable long eventId,
            @Valid @RequestBody CommentRequestDto newCommentDto) {
        log.info("Запрос на создание комментария {} к событию с ID {} от пользователя с ID {}", newCommentDto, eventId,
                userId);
        return commentService.createComment(userId, eventId, newCommentDto);
    }

    @PatchMapping(PATH_COMMENTS + PATH_COMMENT_ID)
    public CommentDto updateComment(
            @RequestParam long userId,
            @PathVariable long commentId,
            @RequestBody CommentRequestDto updateCommentDto) {
        log.info("Запрос на обновление текста комментария с ID {} к событию от пользователя с ID {} : {}",
                commentId, userId, updateCommentDto);
        return commentService.updateComment(userId, commentId, updateCommentDto);
    }

    @DeleteMapping(PATH_COMMENTS + PATH_COMMENT_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCommentById(@RequestParam long userId, @PathVariable long commentId) {
        log.info("Запрос на удаления комментария с ID {} к событию от пользователя с ID {}", commentId, userId);
        commentService.deleteCommentById(userId, commentId);
    }
}
