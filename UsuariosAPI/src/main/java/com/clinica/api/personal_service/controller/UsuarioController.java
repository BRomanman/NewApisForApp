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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

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
        description = "Recupera únicamente cuentas no administrativas para evitar exponer perfiles críticos. "
            + "Puede responder 200 con la lista, 204 si no hay registros o 500 ante un error."
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
        description = "Retorna la información del usuario si no es administrador. "
            + "Puede devolver 200, o 404 cuando no existe o es admin, y 500 si ocurre un problema inesperado."
    )
    public ResponseEntity<UsuarioResponse> getUsuarioById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(usuarioService.findUsuarioById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Sube o actualiza la foto de perfil del usuario.",
        description = "Guarda la imagen enviada como multipart y reemplaza la foto existente en la tabla Usuarios. "
            + "Puede responder 204 al guardar, 400 si el archivo no es válido, 404 si el usuario no existe y 500 ante errores."
    )
    public ResponseEntity<Void> actualizarFotoPerfilUsuario(
        @PathVariable("id") Long id,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            usuarioService.actualizarFotoPerfilUsuario(id, file);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Descarga la foto de perfil del usuario.",
        description = "Retorna los bytes almacenados en la columna foto_perfil para que la app pueda mostrarlos. "
            + "Puede entregar 200 con image/jpeg, 404 si no hay foto o usuario y 500 si ocurre un fallo."
    )
    public ResponseEntity<byte[]> obtenerFotoPerfilUsuario(@PathVariable("id") Long id) {
        try {
            byte[] foto = usuarioService.obtenerFotoPerfilUsuario(id);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(foto);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo usuario.",
        description = "Registra una nueva cuenta respetando las reglas de exclusión de administradores. "
            + "Puede devolver 201 al crearla, 400 si el payload es inválido, 409 por conflicto de datos o 500 si falla algo."
    )
    public ResponseEntity<UsuarioResponse> createUsuario(@RequestBody Usuario usuario) {
        Usuario safeUsuario = requireUsuarioPayload(usuario);
        Usuario saved = usuarioService.saveUsuario(safeUsuario);
        return ResponseEntity.status(HttpStatus.CREATED).body(usuarioService.findUsuarioById(saved.getId()));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza un usuario existente.",
        description = "Permite modificar datos personales y credenciales cuando el usuario no es administrador. "
            + "Puede responder 200 si actualiza, 400 si el payload es inválido, 404 si no se encuentra o es admin, 409 por conflictos o 500 ante fallos."
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
        description = "Elimina permanentemente la cuenta siempre que no sea administrativa. "
            + "Puede devolver 204 si elimina, 404 si no se encuentra o es admin y 500 si hay un error."
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
