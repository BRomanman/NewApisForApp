package com.clinica.api.personal_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.personal_service.dto.UsuarioResponse;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.model.Usuario;
import com.clinica.api.personal_service.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class UsuarioServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UsuarioService usuarioService;

    @Test
    @DisplayName("findAllUsuarios filtra administradores")
    void findAllUsuarios_filtersAdmins() {
        Usuario paciente = usuario(1L, "paciente");
        Usuario admin = usuario(2L, "administrador");
        when(usuarioRepository.findAll()).thenReturn(List.of(paciente, admin));

        List<UsuarioResponse> responses = usuarioService.findAllUsuarios();

        assertThat(responses).hasSize(1);
        assertThat(responses.get(0).getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("findUsuarioById lanza 404 para administradores")
    void findUsuarioById_blocksAdmins() {
        Usuario admin = usuario(3L, "administrador");
        when(usuarioRepository.findById(3L)).thenReturn(Optional.of(admin));

        assertThatThrownBy(() -> usuarioService.findUsuarioById(3L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("saveUsuario codifica la contraseña")
    void saveUsuario_encodesPassword() {
        Usuario paciente = usuario(null, "paciente");
        paciente.setContrasena("clave");
        when(passwordEncoder.encode("clave")).thenReturn("encoded");
        when(usuarioRepository.save(paciente)).thenReturn(paciente);

        Usuario saved = usuarioService.saveUsuario(paciente);

        assertThat(saved.getContrasena()).isEqualTo("encoded");
        verify(passwordEncoder).encode("clave");
    }

    @Test
    @DisplayName("saveUsuario bloquea administradores")
    void saveUsuario_blocksAdmins() {
        Usuario admin = usuario(null, "administrador");

        assertThatThrownBy(() -> usuarioService.saveUsuario(admin))
            .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("deleteUsuarioById elimina cuando existe y no es admin")
    void deleteUsuarioById_removesUsuario() {
        Usuario usuario = usuario(10L, "paciente");
        when(usuarioRepository.findById(10L)).thenReturn(Optional.of(usuario));

        usuarioService.deleteUsuarioById(10L);

        verify(usuarioRepository).delete(usuario);
    }

    private Usuario usuario(Long id, String rolNombre) {
        Usuario usuario = new Usuario();
        usuario.setId(id);
        usuario.setNombre("Nombre");
        usuario.setApellido("Apellido");
        usuario.setFechaNacimiento(LocalDate.now());
        usuario.setCorreo("correo@example.com");
        usuario.setContrasena("clave");
        Rol rol = new Rol();
        rol.setNombre(rolNombre);
        usuario.setRol(rol);
        usuario.setTelefono("+56900000000");
        return usuario;
    }
}
