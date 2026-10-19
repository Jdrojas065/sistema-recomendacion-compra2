package com.example.recomendation.model.service.impl;

import com.example.recomendation.exception.ResourceNotFoundException;
import com.example.recomendation.model.entity.User;
import com.example.recomendation.model.form.UserFormDto;
import com.example.recomendation.model.mapper.UserMapper;
import com.example.recomendation.model.repository.UserRepository;
import com.example.recomendation.model.service.interfaces.UserService;

import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public Page<User> findAll(Pageable pageable) {
        return userRepository.findByActiveTrue(pageable);
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuario no encontrado con id: " + id));
    }

    @Override
    @Transactional
    public User create(UserFormDto form) {
        if (userRepository.existsByEmail(form.getEmail())) {
            throw new IllegalArgumentException("Ya existe un usuario con ese email");
        }
        User user = new User();
        UserMapper.applyForm(user, form);
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setActive(true);
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User update(Long id, UserFormDto form) {
        User user = findById(id);
        UserMapper.applyForm(user, form);
        // Solo se re-encripta si el usuario ingresó un nuevo password
        if (form.getPassword() != null && !form.getPassword().isBlank()) {
            user.setPassword(passwordEncoder.encode(form.getPassword()));
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public void deactivate(Long id) {
        User user = findById(id);
        user.setActive(false);
        userRepository.save(user);
    }
}