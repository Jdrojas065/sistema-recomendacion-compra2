package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.Category;
import com.example.recomendation.model.form.CategoryFormDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CategoryService {

    Page<Category> findAll(Pageable pageable);

    Category findById(Long id);

    Category create(CategoryFormDto form);

    Category update(Long id, CategoryFormDto form);
}