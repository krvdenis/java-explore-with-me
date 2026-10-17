package ru.practicum.comment.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.comment.priv.CommentRequestDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CommentMapper {
    public static Comment toComment(CommentRequestDto newCommentDto) {
        return Comment.builder()
        		.text(newCommentDto.getText())
        		.build();
    }

    public static CommentDto mapToDto(Comment comment) {
        return CommentDto.builder()
        		.id(comment.getId())
        		.text(comment.getText())
        		.eventId(comment.getEvent().getId())
        		.authorName(comment.getAuthor().getName())
        		.created(comment.getCreated())
        		.build();
    }

    public static void updateCommentFields(Comment comment, CommentRequestDto updateCommentDto) {
        if (!updateCommentDto.getText().equals(comment.getText())) {
            comment.setText(updateCommentDto.getText());
        }
    }
}
