package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.exception.InvalidNewPasswordException;
import com.clinica.api.personal_service.exception.WrongCurrentPasswordException;
import com.clinica.api.personal_service.model.Administrador;
import com.clinica.api.personal_service.repository.AdministradorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AdminPasswordServiceImpl implements AdminPasswordService {

    private final AdministradorRepository administradorRepository;
    private final PasswordEncoder passwordEncoder;

    public AdminPasswordServiceImpl(
        AdministradorRepository administradorRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.administradorRepository = administradorRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void changePassword(Long adminId, String currentPassword, String newPassword) {
        Objects.requireNonNull(currentPassword, "currentPassword es requerido");
        Objects.requireNonNull(newPassword, "newPassword es requerido");
        Administrador admin = administradorRepository.findByIdAndActivoTrue(adminId)
            .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado"));

        if (!passwordEncoder.matches(currentPassword, admin.getContrasena())) {
            throw new WrongCurrentPasswordException();
        }

        validateNewPassword(newPassword);
        admin.setContrasena(passwordEncoder.encode(newPassword));
        administradorRepository.save(admin);
    }

    private void validateNewPassword(String password) {
        if (password.length() < 8) {
            throw new InvalidNewPasswordException("La contraseña debe tener al menos 8 caracteres");
        }
        if (!password.chars().anyMatch(Character::isUpperCase)) {
            throw new InvalidNewPasswordException("La contraseña debe contener al menos una letra mayúscula");
        }
        long digits = password.chars().filter(Character::isDigit).count();
        if (digits < 2) {
            throw new InvalidNewPasswordException("La contraseña debe contener al menos dos dígitos");
        }
    }
}
