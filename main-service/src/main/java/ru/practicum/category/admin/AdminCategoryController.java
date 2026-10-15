package ru.practicum.category.admin;

import jakarta.validation.Valid;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.common.CategoryDto;

import static ru.practicum.util.Constants.PATH_ADMIN;
import static ru.practicum.util.Constants.PATH_CATEGORIES;
import static ru.practicum.util.Constants.PATH_CATEGORY_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_ADMIN + PATH_CATEGORIES)
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AdminCategoryController {
    AdminCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Запрос на добавление новой категории {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @PatchMapping(PATH_CATEGORY_ID)
    public CategoryDto updateCategory(@PathVariable long catId, @Valid @RequestBody NewCategoryDto updateCategoryDto) {
        log.info("Запрос на изменение характеристик категории с ID {} - {}", catId, updateCategoryDto);
        return categoryService.updateCategory(catId, updateCategoryDto);
    }

    @DeleteMapping(PATH_CATEGORY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryById(@PathVariable long catId) {
        log.info("Запрос на удаление категории с ID {}", catId);
        categoryService.deleteCategoryById(catId);
    }
}