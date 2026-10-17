package ru.practicum.comment.pub;

import ru.practicum.comment.common.CommentDto;

import java.util.List;

public interface PublicCommentService {
    List<CommentDto> getComments(long eventId, int from, int size);
}
