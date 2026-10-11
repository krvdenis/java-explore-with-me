package ru.practicum.category.pub;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.practicum.category.common.repository.CategoryRepository;
import ru.practicum.category.common.dto.CategoryDto;
import ru.practicum.category.common.mapper.CategoryMapper;
import ru.practicum.category.common.model.Category;
import ru.practicum.exception.NotFoundException;

import java.util.List;

@Service
@Transactional
@Slf4j
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class PublicCategoryServiceImpl implements PublicCategoryService {
    CategoryRepository categoryRepository;

    @Transactional(readOnly = true)
    @Override
    public CategoryDto getCategoryDtoById(long id) {
        log.info("Поиск категории по её ID = {}", id);
        CategoryDto categoryDto = CategoryMapper.mapToDto(categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID = %d не найдена.", id))));
        log.debug("Отправляется информация о категории с ID {}: {}", id, categoryDto);
        return categoryDto;
    }

    @Transactional(readOnly = true)
    @Override
    public Category getCategoryById(long id) {
        log.info("Поиск категории по её ID = {}", id);
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Категория с ID = %d не найдена.", id)));
        log.debug("Отправляется информация о категории с ID {}: {}", id, category);
        return category;
    }

    @Override
    public List<CategoryDto> getCategories(int from, int size) {
        log.info("Получение списка категорий");

        Sort sort = Sort.by("id");
        Pageable pageable = PageRequest.of(from / size, size, sort);
        Page<Category> categoryDtoPage = categoryRepository.findAll(pageable);
        List<CategoryDto> categoryDtos = categoryDtoPage.getContent().stream()
                .map(CategoryMapper::mapToDto)
                .toList();
        log.debug("Отправляется список из {} категорий", categoryDtos.size());
        return categoryDtos;
    }

}