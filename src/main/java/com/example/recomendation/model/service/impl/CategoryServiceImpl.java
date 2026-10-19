package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.Category;
import com.example.recomendation.model.form.CategoryFormDto;
import com.example.recomendation.model.mapper.CategoryMapper;
import com.example.recomendation.model.repository.CategoryRepository;
import com.example.recomendation.model.service.interfaces.CategoryService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CategoryServiceImpl implements CategoryService {

    private final CategoryRepository categoryRepository;

    @Override
    public Page<Category> findAll(Pageable pageable) {
        return categoryRepository.findByActiveTrue(pageable);
    }

    @Override
    public Category findById(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Categoría no encontrada con id: " + id));
    }

    @Override
    @Transactional
    public Category create(CategoryFormDto form) {
        if (categoryRepository.existsByNameIgnoreCase(form.getName())) {
            throw new IllegalArgumentException("Ya existe una categoría con ese nombre");
        }
        Category category = new Category();
        CategoryMapper.applyForm(category, form);
        category.setActive(true);
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public Category update(Long id, CategoryFormDto form) {
        Category category = findById(id);
        CategoryMapper.applyForm(category, form);
        return categoryRepository.save(category);
    }
}