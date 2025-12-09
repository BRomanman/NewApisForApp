package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.DoctorResponse;
import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.service.EspecialidadService;
import com.clinica.api.personal_service.service.PersonalService;
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
@RequestMapping("/api/v1/doctores")
@Tag(name = "Doctores", description = "API para administrar la ficha laboral y remuneraciones de los médicos de la institución.")
public class DoctorController {

    private final PersonalService personalService;
    private final EspecialidadService especialidadService;

    public DoctorController(PersonalService personalService, EspecialidadService especialidadService) {
        this.personalService = personalService;
        this.especialidadService = especialidadService;
    }

    @GetMapping
    @Operation(
        summary = "Lista los doctores activos.",
        description = "Entrega el listado filtrado sólo con doctores vigentes e incluye los datos económico-laborales y la especialidad principal."
    )
    public ResponseEntity<List<DoctorResponse>> getAllDoctores() {
        List<Empleado> doctores = personalService.findAllDoctores();
        if (doctores.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        List<DoctorResponse> response = doctores.stream()
            .map(this::mapToResponse)
            .collect(Collectors.toList());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene la información del doctor por su ID.",
        description = "Devuelve el detalle completo de un doctor específico, incluyendo los datos personales y su especialidad."
    )
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable("id") Long id) {
        try {
            Empleado doctor = personalService.findDoctorById(id);
            return ResponseEntity.ok(mapToResponse(doctor));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo doctor.",
        description = "Registra un profesional con su información contractual y de usuario, respondiendo 201 al persistirlo."
    )
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody Empleado doctor) {
        Empleado nuevoDoctor = personalService.saveDoctor(requireDoctorPayload(doctor));
        return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(nuevoDoctor));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza los datos económicos del doctor.",
        description = "Permite modificar tarifa, sueldo, bono y datos del profesional de forma segura."
    )
    public ResponseEntity<DoctorResponse> updateDoctor(
        @PathVariable("id") Long id,
        @RequestBody Empleado doctorDetails
    ) {
        try {
            Empleado existente = personalService.findDoctorById(id);
            Empleado safeDetails = requireDoctorPayload(doctorDetails);
            existente.setTarifaConsulta(safeDetails.getTarifaConsulta());
            existente.setSueldo(safeDetails.getSueldo());
            existente.setBono(safeDetails.getBono());
            existente.setNombre(safeDetails.getNombre());
            existente.setApellido(safeDetails.getApellido());
            existente.setCorreo(safeDetails.getCorreo());
            existente.setTelefono(safeDetails.getTelefono());
            existente.setFechaNacimiento(safeDetails.getFechaNacimiento());
            existente.setRol(safeDetails.getRol());
            Empleado actualizado = personalService.saveDoctor(existente);
            return ResponseEntity.ok(mapToResponse(actualizado));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Marca como inactivo a un doctor.",
        description = "Realiza una baja lógica para impedir nuevas asignaciones, devolviendo 204 o 404 si el doctor no existe."
    )
    public ResponseEntity<Void> deleteDoctor(@PathVariable("id") Long id) {
        try {
            personalService.deleteDoctorById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    private DoctorResponse mapToResponse(Empleado doctor) {
        Empleado safeDoctor = Objects.requireNonNull(doctor, "Doctor entity must not be null");
        DoctorResponse response = new DoctorResponse();
        response.setId(safeDoctor.getId());
        response.setTarifaConsulta(safeDoctor.getTarifaConsulta());
        response.setSueldo(safeDoctor.getSueldo());
        response.setBono(safeDoctor.getBono());
        response.setEspecialidad(resolveEspecialidad(safeDoctor.getId()));

        DoctorResponse.UsuarioInfo usuario = new DoctorResponse.UsuarioInfo();
        usuario.setId(safeDoctor.getId());
        usuario.setNombre(safeDoctor.getNombre());
        usuario.setApellido(safeDoctor.getApellido());
        usuario.setFechaNacimiento(safeDoctor.getFechaNacimiento());
        usuario.setCorreo(safeDoctor.getCorreo());
        usuario.setTelefono(safeDoctor.getTelefono());
        usuario.setRol(safeDoctor.getRol() != null ? safeDoctor.getRol().getNombre() : null);
        response.setUsuario(usuario);
        return response;
    }

    private String resolveEspecialidad(Long doctorId) {
        List<Especialidad> especialidades = especialidadService.findByDoctorId(doctorId);
        if (especialidades.isEmpty()) {
            return null;
        }
        return especialidades.get(0).getNombre();
    }

    private Empleado requireDoctorPayload(Empleado doctor) {
        return Objects.requireNonNull(doctor, "Doctor payload must not be null");
    }
}
