package com.example.recomendation.model.form;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class CategoryFormDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @Size(max = 255, message = "La descripción no puede superar 255 caracteres")
    private String description;
}