package com.example.recomendation.model.repository;

import com.example.recomendation.model.entity.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    Page<Category> findByActiveTrue(Pageable pageable);

    boolean existsByNameIgnoreCase(String name);
}