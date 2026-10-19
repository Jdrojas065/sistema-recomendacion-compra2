package com.example.recomendation.model.mapper;

import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.form.UserFormDto;

public class UserMapper {

    private UserMapper() {
    }

    public static UserFormDto toForm(User user) {
        if (user == null) {
            return null;
        }
        UserFormDto form = new UserFormDto();
        form.setId(user.getId());
        form.setName(user.getName());
        form.setEmail(user.getEmail());
        form.setRole(user.getRole());
        // password nunca se expone hacia el formulario de edición
        return form;
    }

    /**
     * Aplica nombre, email y rol. El password se maneja aparte en el service
     * (solo se re-encripta si el campo viene con un valor no vacío).
     */
    public static void applyForm(User user, UserFormDto form) {
        user.setName(form.getName());
        user.setEmail(form.getEmail());
        user.setRole(form.getRole());
    }
}