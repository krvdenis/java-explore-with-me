package ru.practicum.category.admin;

import ru.practicum.category.common.CategoryDto;

public interface AdminCategoryService {
    CategoryDto createCategory(NewCategoryDto newCategoryDto);

    CategoryDto updateCategory(long id, NewCategoryDto updateCategoryDto);

    void deleteCategoryById(long id);
}
