package com.clinica.api.historial_service.controller;

import com.clinica.api.historial_service.model.Historial;
import com.clinica.api.historial_service.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/historial")
@Tag(name = "Historial clínico", description = "Endpoints para consultar los antecedentes clínicos y evolutivos de pacientes y doctores.")
public class HistorialController {

    private final HistorialService historialService;

    public HistorialController(HistorialService historialService) {
        this.historialService = historialService;
    }

    @GetMapping("/usuario/{usuarioId}")
    @Operation(
        summary = "Obtiene todos los historiales asociados a un usuario.",
        description = "Provee el detalle clínico completo de un paciente específico. "
            + "Puede responder 200 con la lista, 204 si aún no registra atenciones y 500 ante fallos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de historiales encontrada para el usuario.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Historial.class))
            )
        ),
        @ApiResponse(responseCode = "204", description = "El usuario no tiene historiales registrados.")
    })
    public ResponseEntity<List<Historial>> getHistorialesByUsuarioId(@PathVariable("usuarioId") Long usuarioId) {
        List<Historial> historiales = historialService.findHistorialesByUsuarioId(usuarioId);
        if (historiales.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/doctor/{doctorId}")
    @Operation(
        summary = "Obtiene todos los historiales asociados a un doctor.",
        description = "Permite revisar todas las atenciones realizadas por un médico para análisis de desempeño u ocupación. "
            + "Puede responder 200 con resultados, 204 cuando no tiene historiales o 500 si ocurre un error."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Lista de historiales encontrada para el doctor.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = Historial.class))
            )
        ),
        @ApiResponse(responseCode = "204", description = "El doctor no registra historiales.")
    })
    public ResponseEntity<List<Historial>> getHistorialesByDoctorId(@PathVariable("doctorId") Long doctorId) {
        List<Historial> historiales = historialService.findHistorialesByDoctorId(doctorId);
        if (historiales.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(historiales);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Busca un historial por ID Historial.",
        description = "Entrega los datos clínicos detallados de un historial específico. "
            + "Puede devolver 200 con el detalle, 404 si el ID no existe y 500 ante fallos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Historial encontrado y devuelto.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = Historial.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe un historial con el ID indicado.")
    })
    public ResponseEntity<Historial> getHistorialById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(historialService.findHistorialById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
