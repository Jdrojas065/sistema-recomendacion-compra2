package com.example.recomendation.model.service.interfaces;

import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.form.UserFormDto;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface UserService {

    Page<User> findAll(Pageable pageable);

    User findById(Long id);

    User create(UserFormDto form);

    User update(Long id, UserFormDto form);

    void deactivate(Long id);
}