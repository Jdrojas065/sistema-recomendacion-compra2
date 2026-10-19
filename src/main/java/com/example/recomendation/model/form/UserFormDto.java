package com.example.recomendation.model.form;

import com.example.recomendation.model.enums.Role;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class UserFormDto {

    private Long id;

    @NotBlank(message = "El nombre es obligatorio")
    private String name;

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "El email no tiene un formato válido")
    private String email;

    // Solo obligatorio al crear; en edición se deja vacío si no se desea cambiar.
    private String password;

    @NotNull(message = "Debe seleccionar un rol")
    private Role role;
}