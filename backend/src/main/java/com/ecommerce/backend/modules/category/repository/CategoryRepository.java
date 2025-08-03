package com.ecommerce.backend.modules.category.repository;

import com.ecommerce.backend.modules.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    List<Category> findByParentIsNull();

    List<Category> findByName(String name);

    boolean existsByName(String name);
}
