package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.AdminPasswordChangeRequest;
import com.clinica.api.personal_service.dto.AdministradorDto;
import com.clinica.api.personal_service.dto.AdministradorUpdateRequestDto;
import com.clinica.api.personal_service.service.AdminPasswordService;
import com.clinica.api.personal_service.service.AdministradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/v1/administradores")
@Tag(name = "Administradores", description = "Servicios para acceder a datos administrativos de la clínica.")
public class AdministradorController {

    private final AdministradorService administradorService;
    private final AdminPasswordService adminPasswordService;

    public AdministradorController(
        AdministradorService administradorService,
        AdminPasswordService adminPasswordService
    ) {
        this.administradorService = administradorService;
        this.adminPasswordService = adminPasswordService;
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene un administrador por su identificador.",
        description = "Devuelve los datos básicos del administrador para el panel móvil."
    )
    public ResponseEntity<AdministradorDto> getAdminById(@PathVariable("id") Long id) {
        return administradorService.findByIdDto(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Administrador no encontrado con id " + id
            ));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza los datos de un administrador.",
        description = "Permite modificar campos personales y salariales del administrador."
    )
    public ResponseEntity<AdministradorDto> updateAdmin(
        @PathVariable("id") Long id,
        @RequestBody @Valid AdministradorUpdateRequestDto request
    ) {
        try {
            AdministradorDto updated = administradorService.update(id, request);
            return ResponseEntity.ok(updated);
        } catch (EntityNotFoundException ex) {
            throw new ResponseStatusException(
                HttpStatus.NOT_FOUND,
                "Administrador no encontrado con id " + id,
                ex
            );
        }
    }

    @PostMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Sube o actualiza la foto del administrador.",
        description = "Guarda la imagen enviada como multipart en la columna foto_perfil de Administradores."
    )
    public ResponseEntity<Void> actualizarFotoPerfilAdmin(
        @PathVariable("id") Long id,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            administradorService.actualizarFotoPerfilAdmin(id, file);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Descarga la foto del administrador.",
        description = "Retorna la imagen almacenada para mostrar el perfil administrativo."
    )
    public ResponseEntity<byte[]> obtenerFotoPerfilAdmin(@PathVariable("id") Long id) {
        try {
            byte[] foto = administradorService.obtenerFotoPerfilAdmin(id);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(foto);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/contrasena")
    @Operation(
        summary = "Actualiza la contraseña del administrador autenticado.",
        description = "Valida la contraseña actual y la reemplaza por una nueva que cumpla las reglas."
    )
    public ResponseEntity<Void> cambiarContrasena(
        @PathVariable("id") Long id,
        @RequestBody @Valid AdminPasswordChangeRequest request
    ) {
        adminPasswordService.changePassword(id, request.getCurrentPassword(), request.getNewPassword());
        return ResponseEntity.noContent().build();
    }
}
