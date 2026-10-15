package ru.practicum.category.pub;

import ru.practicum.category.common.Category;
import ru.practicum.category.common.CategoryDto;

import java.util.List;

public interface PublicCategoryService {
    CategoryDto getCategoryDtoById(long id);

    Category getCategoryById(long id);

    List<CategoryDto> getCategories(int from, int size);
}