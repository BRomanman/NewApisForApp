package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.service.AdministradorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
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

    public AdministradorController(AdministradorService administradorService) {
        this.administradorService = administradorService;
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
}
