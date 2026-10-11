package ru.practicum.category.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.common.repository.CategoryRepository;
import ru.practicum.category.admin.dto.NewCategoryDto;
import ru.practicum.category.common.dto.CategoryDto;
import ru.practicum.category.common.mapper.CategoryMapper;
import ru.practicum.category.common.model.Category;
import ru.practicum.event.admin.AdminEventService;
import ru.practicum.exception.ConditionsNotMetException;
import ru.practicum.exception.NotFoundException;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCategoryServiceImpl implements AdminCategoryService {
    CategoryRepository categoryRepository;
    AdminEventService adminEventService;

    @Override
    public CategoryDto createCategory(NewCategoryDto dto) {
        log.info("Создание записи с данными новой категории в БД");
        Category category = CategoryMapper.mapToCategory(dto);
        CategoryDto categoryDto = CategoryMapper.mapToDto(categoryRepository.save(category));
        log.debug("Сгенерирован ID {} для новой категории", categoryDto.getId());
        return categoryDto;
    }

    @Override
    public CategoryDto updateCategory(long id, NewCategoryDto updateCategoryDto) {
        log.info("Изменение характеристик категории с ID {}: {}", id, updateCategoryDto);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID = %d не найдена.", id)));
        CategoryMapper.updateCategoryFields(category, updateCategoryDto);
        CategoryDto updatedCategoryDto = CategoryMapper.mapToDto(category);
        log.debug("Изменены характеристики категории с ID {}: {}", id, updatedCategoryDto);
        return updatedCategoryDto;
    }

    @Override
    public void deleteCategoryById(long id) {
        log.info("Удаляется категория с ID {}", id);

        if (adminEventService.checkEventExistsByCategoryId(id)) {
            throw new ConditionsNotMetException("The category is not empty");
        }

        CategoryDto categoryDto = CategoryMapper.mapToDto(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID = %d не найдена.", id))));
        categoryRepository.deleteById(id);
        log.debug("Запрос на удаление категории с ID {} обработан", id);
    }
}
