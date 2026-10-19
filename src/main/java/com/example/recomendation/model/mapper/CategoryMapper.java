package com.example.recomendation.model.mapper;

import com.example.recomendation.model.entity.Category;
import com.example.recomendation.model.form.CategoryFormDto;

public class CategoryMapper {

    private CategoryMapper() {
    }

    public static CategoryFormDto toForm(Category category) {
        if (category == null) {
            return null;
        }
        CategoryFormDto form = new CategoryFormDto();
        form.setId(category.getId());
        form.setName(category.getName());
        form.setDescription(category.getDescription());
        return form;
    }

    public static void applyForm(Category category, CategoryFormDto form) {
        category.setName(form.getName());
        category.setDescription(form.getDescription());
    }
}