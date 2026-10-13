package ru.practicum.category.common;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import ru.practicum.category.admin.NewCategoryDto;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public final class CategoryMapper {
    public static Category mapToCategory(NewCategoryDto dto) {
        return Category.builder()
                .name(dto.getName())
                .build();
    }

    public static CategoryDto mapToDto(CategoryShort category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static CategoryDto mapToDto(Category category) {
        return CategoryDto.builder()
                .id(category.getId())
                .name(category.getName())
                .build();
    }

    public static void updateCategoryFields(Category category, NewCategoryDto updateCategory) {
        if (updateCategory != null) {
            category.setName(updateCategory.getName());
        }
    }
}
