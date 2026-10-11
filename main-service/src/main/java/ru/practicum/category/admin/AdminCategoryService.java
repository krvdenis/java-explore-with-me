package ru.practicum.category.admin;

import ru.practicum.category.admin.dto.NewCategoryDto;
import ru.practicum.category.common.dto.CategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto dto);

    CategoryDto updateCategory(long id, NewCategoryDto updateCategoryDto);

    void deleteCategoryById(long id);
}
