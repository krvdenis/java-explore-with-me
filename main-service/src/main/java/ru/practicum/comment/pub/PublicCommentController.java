package ru.practicum.comment.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.comment.common.CommentDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_COMMENTS;
import static ru.practicum.util.Constants.PATH_EVENTS;
import static ru.practicum.util.Constants.PATH_EVENT_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_EVENTS + PATH_EVENT_ID + PATH_COMMENTS)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCommentController {
    PublicCommentService commentService;

    @GetMapping
    public List<CommentDto> getComments(
            @PathVariable long eventId,
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получение всех комментариев к событию с ID {}", eventId);
        return commentService.getComments(eventId, from, size);
    }
}
