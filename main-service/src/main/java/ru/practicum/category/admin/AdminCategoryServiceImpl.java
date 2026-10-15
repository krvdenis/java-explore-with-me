package ru.practicum.category.admin;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.common.Category;
import ru.practicum.category.common.CategoryDto;
import ru.practicum.category.common.CategoryMapper;
import ru.practicum.category.common.CategoryRepository;
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
    public CategoryDto createCategory(NewCategoryDto newCategoryDto) {
        log.info("Создание записи с данными новой категории в БД");

        Category category = CategoryMapper.mapToCategory(newCategoryDto);
        CategoryDto categoryDto = CategoryMapper.mapToDto(categoryRepository.save(category));
        log.info("Сгенерирован ID {} для новой категории", categoryDto.getId());
        return categoryDto;
    }

    @Override
    public CategoryDto updateCategory(long id, NewCategoryDto updateCategoryDto) {
        log.info("Изменение характеристик категории с ID {}: {}", id, updateCategoryDto);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка обновления: категория с ID {} не найдена", id);
                    return new NotFoundException(String.format("Category with ID %d was not found", id));
                });

        CategoryMapper.updateCategoryFields(category, updateCategoryDto);

        CategoryDto updatedCategoryDto = CategoryMapper.mapToDto(category);
        log.info("Изменены характеристики категории с ID {}: {}", id, updatedCategoryDto);
        return updatedCategoryDto;
    }

    @Override
    public void deleteCategoryById(long id) {
        log.info("Удаляется категория с ID {}", id);

        if (adminEventService.checkEventExistsByCategoryId(id)) {
            log.warn("Ошибка удаления: категория с ID {} содержит связанные события и не может быть удалена", id);
            throw new ConditionsNotMetException("The category is not empty");
        }

        if (!categoryRepository.existsById(id)) {
            log.warn("Ошибка удаления: категория с ID {} не найдена", id);
            throw new NotFoundException(String.format("Category with ID %d was not found", id));
        }

        categoryRepository.deleteById(id);
        log.info("Запрос на удаление категории с ID {} обработан", id);
    }
}
