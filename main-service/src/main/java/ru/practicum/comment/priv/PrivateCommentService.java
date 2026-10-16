package ru.practicum.comment.priv;

import jakarta.validation.Valid;
import ru.practicum.comment.common.CommentDto;

public interface PrivateCommentService {
    CommentDto createComment(long userId, long eventId, @Valid CommentRequestDto newCommentDto);

    CommentDto updateComment(long userId, long commentId, CommentRequestDto updateCommentDto);

    void deleteCommentById(long userId, long commentId);
}
