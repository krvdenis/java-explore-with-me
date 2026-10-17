package ru.practicum.comment.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.common.Comment;
import ru.practicum.comment.common.CommentRepository;
import ru.practicum.exception.NotFoundException;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCommentServiceImpl implements AdminCommentService {
    CommentRepository commentRepository;

    @Override
    public void deleteCommentById(long commentId) {
        log.info("Администратор инициировал удаление комментария с ID {}", commentId);

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException(String.format("Comment with ID %d was not found", commentId)));

        commentRepository.delete(comment);
        log.info("Администратор удалил комментарий с ID {}", commentId);
    }
}
