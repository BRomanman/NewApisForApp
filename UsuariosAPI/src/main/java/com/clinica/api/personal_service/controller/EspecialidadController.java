package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.EspecialidadResponse;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.service.EspecialidadService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
@RequestMapping("/api/v1")
@Tag(name = "Especialidades", description = "API para relacionar especialidades médicas con doctores y administrar el catálogo general.")
public class EspecialidadController {

    private final EspecialidadService especialidadService;

    public EspecialidadController(EspecialidadService especialidadService) {
        this.especialidadService = especialidadService;
    }

    @GetMapping("/doctores/{doctorId}/especialidades")
    @Operation(
        summary = "Lista las especialidades asociadas a un doctor.",
        description = "Permite conocer todas las áreas en las que un doctor atiende. Responde 204 si el profesional no tiene especialidades asignadas."
    )
    public ResponseEntity<List<EspecialidadResponse>> getEspecialidadesByDoctor(
        @PathVariable("doctorId") Long doctorId
    ) {
        try {
            List<Especialidad> especialidades = especialidadService.findByDoctorId(doctorId);
            if (especialidades.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            List<EspecialidadResponse> response = especialidades.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
            return ResponseEntity.ok(response);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/doctores/{doctorId}/especialidades")
    @Operation(
        summary = "Agrega una especialidad a un doctor.",
        description = "Crea una nueva relación entre doctor y especialidad y devuelve 201 con el recurso generado. "
            + "Si el doctor o la información no son válidos, retorna 400 o 404 según corresponda."
    )
    public ResponseEntity<EspecialidadResponse> createEspecialidadForDoctor(
        @PathVariable("doctorId") Long doctorId,
        @RequestBody EspecialidadRequest payload
    ) {
        if (!isValidRequest(payload)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Especialidad creada = especialidadService.createForDoctor(doctorId, payload.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(creada));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // CRUD global de especialidades
    @GetMapping("/especialidades")
    @Operation(
        summary = "Lista todas las especialidades.",
        description = "Recupera el catálogo global de especialidades disponible para asignar a nuevos doctores."
    )
    public ResponseEntity<List<EspecialidadResponse>> getAllEspecialidades() {
        List<Especialidad> especialidades = especialidadService.findAll();
        if (especialidades.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<EspecialidadResponse> response = especialidades.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/especialidades/{id}")
    @Operation(
        summary = "Obtiene una especialidad por su ID.",
        description = "Entrega la información básica de una especialidad específica o 404 cuando el registro no existe."
    )
    public ResponseEntity<EspecialidadResponse> getEspecialidadById(@PathVariable("id") Long id) {
        try {
            Especialidad especialidad = especialidadService.findById(id);
            return ResponseEntity.ok(mapToResponse(especialidad));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/especialidades")
    @Operation(
        summary = "Crea una especialidad asociada a un doctor.",
        description = "Permite registrar y asociar simultáneamente una especialidad con un doctor existente, devolviendo 201 con el resultado."
    )
    public ResponseEntity<EspecialidadResponse> createEspecialidad(@RequestBody EspecialidadRequest payload) {
        if (!isValidRequest(payload) || payload.getDoctorId() == null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Especialidad especialidad = especialidadService.createForDoctor(payload.getDoctorId(), payload.getNombre());
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(especialidad));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/especialidades/{id}")
    @Operation(
        summary = "Actualiza nombre y/o doctor de una especialidad.",
        description = "Habilita la modificación selectiva del nombre o del doctor asociado. Retorna 200 con el registro actualizado o 404 si no existe."
    )
    public ResponseEntity<EspecialidadResponse> updateEspecialidad(
        @PathVariable("id") Long id,
        @RequestBody EspecialidadRequest payload
    ) {
        if (payload == null || (isBlank(payload.getNombre()) && payload.getDoctorId() == null)) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Especialidad updated = especialidadService.update(id, payload.getNombre(), payload.getDoctorId());
            return ResponseEntity.ok(mapToResponse(updated));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/especialidades/{id}")
    @Operation(
        summary = "Elimina una especialidad.",
        description = "Borra el registro de especialidad indicado y responde 204 cuando se completa. Si no existe, devuelve 404."
    )
    public ResponseEntity<Void> deleteEspecialidad(@PathVariable("id") Long id) {
        try {
            especialidadService.delete(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private EspecialidadResponse mapToResponse(Especialidad especialidadInput) {
        Especialidad especialidad = Objects.requireNonNull(especialidadInput, "Especialidad entity must not be null");
        EspecialidadResponse response = new EspecialidadResponse();
        response.setId(especialidad.getId());
        response.setNombre(especialidad.getNombre());
        List<Empleado> doctores = especialidadService.findDoctoresPorEspecialidad(especialidad.getId());
        response.setDoctorId(doctores.isEmpty() ? null : doctores.get(0).getId());
        return response;
    }

    private boolean isValidRequest(EspecialidadRequest payload) {
        return payload != null && !isBlank(payload.getNombre());
    }

    private boolean isBlank(String value) {
        return value == null || value.isBlank();
    }

    // Payload compacto para creaciones/actualizaciones, evitando DTO extra.
    public static class EspecialidadRequest {
        private String nombre;
        private Long doctorId;

        public String getNombre() {
            return nombre;
        }

        public void setNombre(String nombre) {
            this.nombre = nombre;
        }

        public Long getDoctorId() {
            return doctorId;
        }

        public void setDoctorId(Long doctorId) {
            this.doctorId = doctorId;
        }
    }
}
