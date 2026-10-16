package ru.practicum.comment.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.comment.common.CommentDto;
import ru.practicum.comment.common.CommentMapper;
import ru.practicum.comment.common.CommentRepository;
import ru.practicum.event.pub.PublicEventService;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCommentServiceImpl implements PublicCommentService {
    CommentRepository commentRepository;
    PublicEventService eventService;

    @Override
    public List<CommentDto> getComments(long eventId, int from, int size) {
        log.info("Получен публичный запрос на получение комментариев к событию с ID={}, начиная с элемента={}, размер страницы={}",
                eventId, from, size);

        eventService.checkEventExists(eventId);

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("created").descending());

        List<CommentDto> commentDtos = commentRepository.findByEventId(eventId, pageable).stream()
                .map(CommentMapper::mapToDto)
                .toList();

        log.info("Возвращено {} комментариев для события с ID {}", commentDtos.size(), eventId);
        return commentDtos;
    }
}
