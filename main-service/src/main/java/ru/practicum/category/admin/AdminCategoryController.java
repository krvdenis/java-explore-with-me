package ru.practicum.category.admin;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
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
import ru.practicum.category.admin.dto.NewCategoryDto;
import ru.practicum.category.common.dto.CategoryDto;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("/admin/categories")
public class AdminCategoryController {
    public static final String PATH_CATEGORY_ID = "/{catId}";
    private final AdminCategoryService categoryService;

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public CategoryDto createCategory(@Valid @RequestBody NewCategoryDto newCategoryDto) {
        log.info("Запрос на добавление категории {}", newCategoryDto);
        return categoryService.createCategory(newCategoryDto);
    }

    @PatchMapping(PATH_CATEGORY_ID)
    public CategoryDto updateCategory(@PathVariable long catId, @Valid @RequestBody NewCategoryDto updateCategoryDto) {
        log.info("Запрос на изменение характеристик категории  {}", updateCategoryDto);
        return categoryService.updateCategory(catId, updateCategoryDto);
    }

    @DeleteMapping(PATH_CATEGORY_ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCategoryById(@PathVariable long catId) {
        log.info("Запрос на удаление категории с ID {}", catId);
        categoryService.deleteCategoryById(catId);
    }
}