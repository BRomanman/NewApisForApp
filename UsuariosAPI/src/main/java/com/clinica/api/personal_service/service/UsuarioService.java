package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.dto.UsuarioResponse;
import com.clinica.api.personal_service.model.Usuario;
import com.clinica.api.personal_service.repository.RolRepository;
import com.clinica.api.personal_service.repository.UsuarioRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class UsuarioService {

    private static final String ADMIN_ROLE_NAME = "administrador";

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final RolRepository rolRepository;

    public UsuarioService(
        UsuarioRepository usuarioRepository,
        PasswordEncoder passwordEncoder,
        RolRepository rolRepository
    ) {
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
        this.rolRepository = rolRepository;
    }

    public List<UsuarioResponse> findAllUsuarios() {
        return usuarioRepository.findAll().stream()
            .filter(this::isAllowedUsuario)
            .map(this::mapToResponse)
            .collect(Collectors.toList());
    }

    public UsuarioResponse findUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        ensureNotAdmin(usuario);
        return mapToResponse(usuario);
    }

    public Usuario saveUsuario(Usuario usuario) {
        Usuario safeUsuario = Objects.requireNonNull(usuario, "Usuario entity must not be null");
        ensurePayloadNotAdmin(safeUsuario);
        if (safeUsuario.getContrasena() == null || safeUsuario.getContrasena().isBlank()) {
            throw new IllegalArgumentException("La contraseña es obligatoria");
        }
        if (safeUsuario.getRol() == null) {
            rolRepository.findByNombreIgnoreCase("Paciente").ifPresent(safeUsuario::setRol);
            if (safeUsuario.getRol() == null) {
                throw new IllegalArgumentException("Rol Paciente no configurado");
            }
        }
        safeUsuario.setContrasena(passwordEncoder.encode(safeUsuario.getContrasena()));
        return usuarioRepository.save(safeUsuario);
    }

    public Usuario updateUsuario(Long id, Usuario changes) {
        Usuario existente = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        ensureNotAdmin(existente);
        Usuario safeChanges = Objects.requireNonNull(changes, "Usuario updates must not be null");
        ensurePayloadNotAdmin(safeChanges);

        if (safeChanges.getNombre() != null) existente.setNombre(safeChanges.getNombre());
        if (safeChanges.getApellido() != null) existente.setApellido(safeChanges.getApellido());
        if (safeChanges.getCorreo() != null) existente.setCorreo(safeChanges.getCorreo());
        if (safeChanges.getTelefono() != null) existente.setTelefono(safeChanges.getTelefono());
        if (safeChanges.getFechaNacimiento() != null) existente.setFechaNacimiento(safeChanges.getFechaNacimiento());
        if (safeChanges.getContrasena() != null && !safeChanges.getContrasena().isBlank()) {
            existente.setContrasena(passwordEncoder.encode(safeChanges.getContrasena()));
        }
        if (safeChanges.getRol() != null) existente.setRol(safeChanges.getRol());

        return usuarioRepository.save(existente);
    }

    public void deleteUsuarioById(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado para eliminar"));
        ensureNotAdmin(usuario);
        usuarioRepository.delete(usuario);
    }

    public void actualizarFotoPerfilUsuario(Long id, MultipartFile file) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        ensureNotAdmin(usuario);
        validateImageFile(file);
        try {
            usuario.setFotoPerfil(new SerialBlob(file.getBytes()));
        } catch (IOException ex) {
            throw new IllegalStateException("Error al leer el archivo de imagen", ex);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al almacenar la foto de perfil", ex);
        }
        usuarioRepository.save(usuario);
    }

    public byte[] obtenerFotoPerfilUsuario(Long id) {
        Usuario usuario = usuarioRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
        ensureNotAdmin(usuario);
        Blob foto = usuario.getFotoPerfil();
        if (foto == null) {
            throw new EntityNotFoundException("Foto de perfil no encontrada");
        }
        try {
            long length = foto.length();
            if (length == 0) {
                throw new EntityNotFoundException("Foto de perfil no encontrada");
            }
            return foto.getBytes(1, Math.toIntExact(length));
        } catch (SQLException | ArithmeticException ex) {
            throw new IllegalStateException("Error al leer la foto de perfil almacenada", ex);
        }
    }

    private UsuarioResponse mapToResponse(Usuario usuarioInput) {
        Usuario usuario = Objects.requireNonNull(usuarioInput, "Usuario entity must not be null");
        UsuarioResponse r = new UsuarioResponse();
        r.setId(usuario.getId());
        r.setNombre(usuario.getNombre());
        r.setApellido(usuario.getApellido());
        r.setFechaNacimiento(usuario.getFechaNacimiento());
        r.setCorreo(usuario.getCorreo());
        r.setTelefono(usuario.getTelefono());
        r.setRol(usuario.getRol() != null ? usuario.getRol().getNombre() : null);
        return r;
    }

    private void ensureNotAdmin(Usuario usuario) {
        if (isAdmin(usuario)) {
            throw new EntityNotFoundException("Usuario no encontrado");
        }
    }

    private void ensurePayloadNotAdmin(Usuario usuario) {
        if (isAdmin(usuario)) {
            throw new IllegalArgumentException("No se permiten operaciones sobre administradores");
        }
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es requerido");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe tener un tipo de imagen válido");
        }
    }

    private boolean isAllowedUsuario(Usuario usuario) {
        return usuario != null && !isAdmin(usuario);
    }

    private boolean isAdmin(Usuario usuario) {
        return usuario != null
            && usuario.getRol() != null
            && ADMIN_ROLE_NAME.equalsIgnoreCase(usuario.getRol().getNombre());
    }
}
