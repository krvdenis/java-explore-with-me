package ru.practicum.event.admin;

import com.querydsl.core.BooleanBuilder;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.ViewStatsDto;
import ru.practicum.category.pub.PublicCategoryService;
import ru.practicum.category.common.model.Category;
import ru.practicum.event.admin.dto.UpdateEventAdminRequest;
import ru.practicum.event.admin.mapper.AdminEventMapper;
import ru.practicum.event.common.dto.EventFullDto;
import ru.practicum.event.common.enums.State;
import ru.practicum.event.common.mapper.EventMapper;
import ru.practicum.event.common.model.Event;
import ru.practicum.event.common.model.QEvent;
import ru.practicum.event.common.repository.EventRepository;
import ru.practicum.event.common.util.EventStatsHelper;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminEventServiceImpl implements AdminEventService {
    EventRepository eventRepository;
    PublicCategoryService categoryService;
    EventStatsHelper eventStatsHelper;

    @Override
    @Transactional
    public EventFullDto updateEvent(long eventId, UpdateEventAdminRequest updateEventAdminRequest) {
        Event event = eventRepository.findById(eventId)
                .orElseThrow(() -> new NotFoundException(String.format("Event with ID %s was not found", eventId)));

        Long views = event.getState() == State.PUBLISHED ? eventStatsHelper.getViewsCountForOneEvent(eventId) : 0L;

        if (updateEventAdminRequest.getCategory() != null) {
            Category category = categoryService.getCategoryById(updateEventAdminRequest.getCategory());
            event.setCategory(category);
        }

        LocalDateTime timeToPublish = LocalDateTime.now();
        validateRequest(updateEventAdminRequest, event, timeToPublish);

        AdminEventMapper.updateEventFields(event, updateEventAdminRequest);

        if (updateEventAdminRequest.hasStateAction()) {
            switch (updateEventAdminRequest.getStateAction()) {
                case PUBLISH_EVENT -> {
                    event.setState(State.PUBLISHED);
                    event.setPublishedOn(timeToPublish);
                }
                case REJECT_EVENT -> {
                    event.setState(State.CANCELED);
                }
            }
        }

        EventFullDto eventFullDto = EventMapper.mapToFullDto(event);
        eventFullDto.setViews(views);
        return eventFullDto;
    }

    @Override
    public List<EventFullDto> getEvents(
            List<Long> users,
            List<State> states,
            List<Long> categories,
            LocalDateTime rangeStart,
            LocalDateTime rangeEnd,
            int from,
            int size) {

        List<Long> filteredUsers = (users == null) ? List.of() : users.stream()
                .filter(Objects::nonNull)
                .toList();
        List<State> filteredStates = (states == null) ? List.of() : states.stream()
                .filter(Objects::nonNull)
                .toList();
        List<Long> filteredCategories = (categories == null) ? List.of() : categories.stream()
                .filter(Objects::nonNull)
                .toList();

        QEvent qEvent = QEvent.event;

        BooleanBuilder builder = new BooleanBuilder();

        if (!filteredUsers.isEmpty()) {
            builder.and(qEvent.initiator.id.in(users));
        }

        if (!filteredStates.isEmpty()) {
            builder.and(qEvent.state.in(filteredStates));
        }

        if (!filteredCategories.isEmpty()) {
            builder.and(qEvent.category.id.in(filteredCategories));
        }

//        if (rangeStart != null) {
//            builder.and(qEvent.eventDate.goe(rangeStart));
//        }
//
//        if (rangeEnd != null) {
//            builder.and(qEvent.eventDate.loe(rangeEnd));
//        }

        if (rangeStart != null & rangeEnd != null) {
            builder.and(qEvent.eventDate.between(rangeStart, rangeEnd));
        }

        Pageable pageable = PageRequest.of(from / size, size, Sort.by("id"));
        Map<Long, Event> eventMap = eventRepository.findAll(builder, pageable).getContent().stream()
                .collect(Collectors.toMap(Event::getId, Function.identity()));

        if (eventMap.isEmpty()) {
            return List.of();
        }

        List<String> uris = eventMap.keySet().stream()
                .map(eventId -> String.format(EventStatsHelper.EVENT_URI_TEMPLATE, eventId))
                .toList();

        Map<Long, ViewStatsDto> eventIdToViewStats = eventStatsHelper.getViewStatsDtos(
                        LocalDateTime.now().minusYears(5),
                        LocalDateTime.now().plusYears(5),
                        uris,
                        true
                ).stream()
                .collect(Collectors.toMap(viewStatsDto -> {
                            String[] uriParts = viewStatsDto.getUri().split("/");
                            Long eventId = Long.valueOf(uriParts[uriParts.length - 1]);
                            return eventId;
                        },
                        Function.identity()));

        List<EventFullDto> eventFullDtos = eventMap.values().stream()
                .map(event -> {
                    EventFullDto dto = EventMapper.mapToFullDto(event);
                    ViewStatsDto viewStatsDto = eventIdToViewStats.get(dto.getId());
                    Long views = (viewStatsDto == null) ? 0L : viewStatsDto.getHits();
                    dto.setViews(views);
                    return dto;
                })
                .toList();

        return eventFullDtos;
    }

    @Override
    public boolean checkEventExistsByCategoryId(long categoryId) {
        return eventRepository.existsByCategoryId(categoryId);
    }

    private void validateRequest(
            UpdateEventAdminRequest request,
            Event event,
            LocalDateTime timeToPublish
    ) {

        if (event.getEventDate().isBefore(timeToPublish.plusHours(1))) {
            throw new ConditionsNotMetException("Cannot update the event because the event date must be at least" +
                    " one hour from now.");
        }

        if (request.getStateAction() == null) {
            return;
        }

        switch (request.getStateAction()) {
            case PUBLISH_EVENT -> {
                event.validateCanBePublished(timeToPublish);
            }
            case REJECT_EVENT -> event.validateCanBeRejected();
        }
    }

}
