package com.clinica.api.seguros_service.controller;

import com.clinica.api.seguros_service.model.Seguro;
import com.clinica.api.seguros_service.service.SeguroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
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
@RequestMapping("/api/v1/seguros")
@Tag(name = "Seguros", description = "Operaciones para administrar los planes de seguros médicos ofrecidos por la clínica.")
public class SeguroController {

    private final SeguroService seguroService;

    public SeguroController(SeguroService seguroService) {
        this.seguroService = seguroService;
    }

    @GetMapping
    @Operation(
        summary = "Lista todos los seguros disponibles.",
        description = "Devuelve el catálogo vigente de seguros con su información esencial. "
            + "Puede responder 200 con la lista, 204 cuando no hay seguros o 500 si ocurre un fallo inesperado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de seguros devuelta correctamente.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Seguro.class))
            )
        ),
        @ApiResponse(responseCode = "204", description = "No hay seguros configurados en el sistema.")
    })
    public ResponseEntity<List<Seguro>> listarSeguros() {
        List<Seguro> seguros = seguroService.findAllSeguros();
        if (seguros.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(seguros);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene los detalles de un seguro por su ID.",
        description = "Permite inspeccionar un plan particular. Puede devolver 200 con el seguro, 404 si no existe y 500 si ocurre un error."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Seguro encontrado y retornado.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Seguro.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Seguro no encontrado para el ID indicado.")
    })
    public ResponseEntity<Seguro> obtenerSeguro(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(seguroService.findSeguroById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo seguro.",
        description = "Registra un plan de seguro con la información proporcionada y devuelve 201 con el recurso almacenado. "
            + "Puede responder 400 si el payload es inválido, 409 por violaciones de datos y 500 ante fallos internos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Seguro creado correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Seguro.class)
            )
        )
    })
    public ResponseEntity<Seguro> crearSeguro(@RequestBody Seguro seguro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seguroService.createSeguro(seguro));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza la información del seguro.",
        description = "Reemplaza los atributos fundamentales del plan (nombre, descripción y valor) para mantenerlo al día. "
            + "Puede responder 200 si se actualiza, 404 si el ID no existe, 400/409 por inconsistencias y 500 ante errores."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Seguro actualizado correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Seguro.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No se encontró el seguro a actualizar.")
    })
    public ResponseEntity<Seguro> actualizarSeguro(
        @PathVariable("id") Long id,
        @RequestBody Seguro seguro
    ) {
        try {
            return ResponseEntity.ok(seguroService.updateSeguro(id, seguro));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Elimina un seguro.",
        description = "Borra definitivamente el plan indicado y responde 204 al completar la operación. "
            + "Puede devolver 404 si no existe, 409 cuando hay dependencias (FK) y 500 ante fallos."
    )
    @ApiResponses({
        @ApiResponse(responseCode = "204", description = "Seguro eliminado exitosamente."),
        @ApiResponse(responseCode = "404", description = "Seguro no encontrado para eliminar.")
    })
    public ResponseEntity<Void> eliminarSeguro(@PathVariable("id") Long id) {
        try {
            seguroService.deleteSeguro(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
