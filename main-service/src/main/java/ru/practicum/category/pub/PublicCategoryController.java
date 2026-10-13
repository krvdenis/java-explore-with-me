package ru.practicum.category.pub;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.common.CategoryDto;

import java.util.List;

import static ru.practicum.util.Constants.PATH_CATEGORIES;
import static ru.practicum.util.Constants.PATH_CATEGORY_ID;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping(PATH_CATEGORIES)
public class PublicCategoryController {
    private final PublicCategoryService categoryService;

    @GetMapping(PATH_CATEGORY_ID)
    public CategoryDto getCategoryById(@PathVariable long catId) {
        log.info("Запрос на получение информации о категории с ID {}", catId);
        return categoryService.getCategoryDtoById(catId);
    }

    @GetMapping
    public List<CategoryDto> getCategories(
            @RequestParam(defaultValue = "0") int from,
            @RequestParam(defaultValue = "10") int size) {
        log.info("Запрос на получения списка категорий: from = {}, size = {}", from, size);
        return categoryService.getCategories(from, size);
    }
}
