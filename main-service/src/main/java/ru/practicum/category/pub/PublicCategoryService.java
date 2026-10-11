package ru.practicum.category.pub;

import ru.practicum.category.common.dto.CategoryDto;
import ru.practicum.category.common.model.Category;

import java.util.List;

public interface PublicCategoryService {
    CategoryDto getCategoryDtoById(long id);

    Category getCategoryById(long id);

    List<CategoryDto> getCategories(int from, int size);
}