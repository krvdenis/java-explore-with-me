package ru.practicum.category;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.practicum.category.dto.CategoryDto;
import ru.practicum.category.dto.NewCategoryDto;

@RestController
@Slf4j
@RequiredArgsConstructor
@RequestMapping("admin/categories")
public class AdminCategoryController {
    private final CategoryService categoryService;

    @PostMapping
    public CategoryDto createCategory(NewCategoryDto dto) {
    return null;
    }
}
