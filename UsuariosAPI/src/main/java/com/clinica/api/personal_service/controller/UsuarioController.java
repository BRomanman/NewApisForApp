package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.UsuarioResponse;
import com.clinica.api.personal_service.model.Usuario;
import com.clinica.api.personal_service.service.UsuarioService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/usuarios")
@Tag(name = "Usuarios", description = "Servicios para administrar cuentas de pacientes y personal no administrador.")
public class UsuarioController {

    private final UsuarioService usuarioService;

    public UsuarioController(UsuarioService usuarioService) {
        this.usuarioService = usuarioService;
    }

    @GetMapping
    @Operation(
        summary = "Lista los usuarios no administradores.",
        description = "Recupera únicamente cuentas no administrativas para evitar exponer perfiles críticos."
    )
    public ResponseEntity<List<UsuarioResponse>> getAllUsuarios() {
        List<UsuarioResponse> usuarios = usuarioService.findAllUsuarios();
        if (usuarios.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(usuarios);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene un usuario por su identificador.",
        description = "Retorna la información del usuario si no es administrador; de lo contrario responde 404 para mantener seguridad."
    )
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(usuarioService.findUsuarioById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo usuario.",
        description = "Registra una nueva cuenta respetando las reglas de exclusión de administradores y devuelve 201 con el DTO resultante."
    )
    public ResponseEntity<UsuarioResponse> createUsuario(@RequestBody Usuario usuario) {
        Usuario safeUsuario = requireUsuarioPayload(usuario);
        Usuario saved = usuarioService.saveUsuario(safeUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.findUsuarioById(saved.getId()));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza un usuario existente.",
        description = "Permite modificar datos personales y credenciales cuando el usuario no es administrador, retornando el estado actualizado."
    )
    public ResponseEntity<UsuarioResponse> updateUsuario(
        @PathVariable("id") Long id,
        @RequestBody Usuario usuarioDetails
    ) {
        try {
            Usuario safeUsuario = requireUsuarioPayload(usuarioDetails);
            Usuario updated = usuarioService.updateUsuario(id, safeUsuario);
            return ResponseEntity.ok(usuarioService.findUsuarioById(updated.getId()));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Elimina un usuario no administrador.",
        description = "Elimina permanentemente la cuenta siempre que no sea administrativa; de lo contrario responde 404."
    )
    public ResponseEntity<Void> deleteUsuario(@PathVariable("id") Long id) {
        try {
            usuarioService.deleteUsuarioById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private Usuario requireUsuarioPayload(Usuario usuario) {
        return Objects.requireNonNull(usuario, "Usuario payload must not be null");
    }
}
