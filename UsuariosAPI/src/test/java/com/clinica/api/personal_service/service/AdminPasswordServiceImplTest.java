package com.clinica.api.personal_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.personal_service.exception.InvalidNewPasswordException;
import com.clinica.api.personal_service.exception.WrongCurrentPasswordException;
import com.clinica.api.personal_service.model.Administrador;
import com.clinica.api.personal_service.repository.AdministradorRepository;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class AdminPasswordServiceImplTest {

    @Mock
    private AdministradorRepository administradorRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AdminPasswordServiceImpl adminPasswordService;

    @Test
    @DisplayName("changePassword actualiza la contraseña cuando la actual es correcta y la nueva es válida")
    void changePassword_updatesWhenValid() {
        Administrador admin = administrador();
        when(administradorRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);
        when(passwordEncoder.encode("NewPass12")).thenReturn("newHash");

        adminPasswordService.changePassword(1L, "oldPass1", "NewPass12");

        assertThat(admin.getContrasena()).isEqualTo("newHash");
        verify(administradorRepository).save(admin);
    }

    @Test
    @DisplayName("changePassword lanza WrongCurrentPasswordException cuando la contraseña actual no coincide")
    void changePassword_throwsWhenCurrentInvalid() {
        Administrador admin = administrador();
        when(administradorRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("wrong", "hashed")).thenReturn(false);

        assertThatThrownBy(() -> adminPasswordService.changePassword(1L, "wrong", "NewPass12"))
            .isInstanceOf(WrongCurrentPasswordException.class);
    }

    @Test
    @DisplayName("changePassword lanza InvalidNewPasswordException cuando la nueva contraseña no cumple las reglas")
    void changePassword_throwsWhenNewPasswordWeak() {
        Administrador admin = administrador();
        when(administradorRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(admin));
        when(passwordEncoder.matches("oldPass1", "hashed")).thenReturn(true);

        assertThatThrownBy(() -> adminPasswordService.changePassword(1L, "oldPass1", "short"))
            .isInstanceOf(InvalidNewPasswordException.class);
    }

    private Administrador administrador() {
        Administrador admin = new Administrador();
        admin.setId(1L);
        admin.setContrasena("hashed");
        admin.setActivo(true);
        return admin;
    }
}
