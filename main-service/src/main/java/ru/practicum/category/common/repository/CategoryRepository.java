package ru.practicum.category.common.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.category.common.model.Category;

public interface CategoryRepository extends JpaRepository<Category, Long> {
}