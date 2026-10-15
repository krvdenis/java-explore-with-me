package ru.practicum.category.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.common.Category;
import ru.practicum.category.common.CategoryDto;
import ru.practicum.category.common.CategoryMapper;
import ru.practicum.category.common.CategoryRepository;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Transactional(readOnly = true)
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCategoryServiceImpl implements PublicCategoryService {
    CategoryRepository categoryRepository;

    @Override
    public CategoryDto getCategoryDtoById(long id) {
        return CategoryMapper.mapToDto(getCategoryById(id));
    }

    @Override
    public Category getCategoryById(long id) {
        log.info("Поиск категории по её ID = {}", id);

        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn("Ошибка получения: категория с ID {} не найдена", id);
                    return new NotFoundException(String.format("Category with ID %d was not found", id));
                });
        log.info("Отправляется информация о категории с ID {}: {}", id, category);
        return category;
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получение списка категорий");

        Page<Category> categoryDtoPage = categoryRepository.findAll(PageRequest.of(
                from / size,
                size,
                Sort.by("id")));
        List<CategoryDto> categoryDtos = categoryDtoPage.getContent().stream()
                .map(CategoryMapper::mapToDto)
                .toList();
        log.info("Отправляется список из {} категорий", categoryDtos.size());
        return categoryDtos;
    }

}