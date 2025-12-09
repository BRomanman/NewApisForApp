package com.clinica.api.historial_service.controller;

import com.clinica.api.historial_service.model.Historial;
import com.clinica.api.historial_service.service.HistorialService;
import io.swagger.v3.oas.annotations.Operation;
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
        description = "Provee el detalle clínico completo de un paciente específico, devolviendo 204 si aún no registra atenciones."
    )
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
            + "Cuando no tiene historiales, se responde 204."
    )
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
            + "Si el identificador no existe, se devuelve 404 Not Found."
    )
    public ResponseEntity<Historial> getHistorialById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(historialService.findHistorialById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
