package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.DoctorCreateRequest;
import com.clinica.api.personal_service.dto.DoctorResponse;
import com.clinica.api.personal_service.dto.DoctorUpdateRequest;
import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.service.PersonalService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;
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
@RequestMapping("/api/v1/doctores")
@Tag(name = "Doctores", description = "API para administrar la ficha laboral y remuneraciones de los médicos de la institución.")
public class DoctorController {

    private final PersonalService personalService;

    public DoctorController(PersonalService personalService) {
        this.personalService = personalService;
    }

    @GetMapping
    @Operation(
        summary = "Lista los doctores activos.",
        description = "Entrega el listado filtrado sólo con doctores vigentes e incluye los datos contractuales para el admin. "
            + "Puede responder 200 con la lista, 204 si no hay doctores o 500 ante un fallo."
    )
    public ResponseEntity<List<DoctorResponse>> getAllDoctores() {
        List<Doctor> doctores = personalService.findAllDoctores();
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
        description = "Devuelve el detalle completo de un doctor específico, incluyendo los datos personales y su especialidad. "
            + "Puede devolver 200, 404 si no existe o está inactivo, y 500 si ocurre un error."
    )
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable("id") Long id) {
        try {
            Doctor doctor = personalService.findDoctorById(id);
            return ResponseEntity.ok(mapToResponse(doctor));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Sube o reemplaza la foto del doctor.",
        description = "Recibe la imagen como multipart y la almacena en la columna foto_perfil de Doctores. "
            + "Puede responder 204 al guardar, 400 si el archivo es inválido, 404 si el doctor no existe y 500 ante fallos."
    )
    public ResponseEntity<Void> actualizarFotoPerfilDoctor(
        @PathVariable("id") Long id,
        @RequestParam("file") MultipartFile file
    ) {
        try {
            personalService.actualizarFotoPerfilDoctor(id, file);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @GetMapping("/{id}/foto-perfil")
    @Operation(
        summary = "Descarga la foto del doctor.",
        description = "Retorna los bytes almacenados para que el frontend pueda renderizar el perfil. "
            + "Puede devolver 200 con image/jpeg, 404 si no hay foto o doctor y 500 ante un error."
    )
    public ResponseEntity<byte[]> obtenerFotoPerfilDoctor(@PathVariable("id") Long id) {
        try {
            byte[] foto = personalService.obtenerFotoPerfilDoctor(id);
            return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_JPEG)
                .body(foto);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Consume el DoctorCreateRequest que envía AdminAddDoctorViewModel para mantener el contrato JSON.
    @PostMapping
    @Operation(
        summary = "Crea un nuevo doctor.",
        description = "Registra un profesional con su información contractual y de usuario. "
            + "Puede responder 201 al crear, 400 si el payload es inválido o el correo está duplicado, 404 si faltan especialidad/rol, 409 por conflictos o 500 si algo falla."
    )
    public ResponseEntity<DoctorResponse> createDoctor(@RequestBody @Valid DoctorCreateRequest request) {
        try {
            Doctor nuevoDoctor = personalService.createDoctor(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(mapToResponse(nuevoDoctor));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza los datos económicos del doctor.",
        description = "Permite modificar tarifa, sueldo, bono y datos del profesional de forma segura. "
            + "Puede devolver 200 al actualizar, 400 si el payload es inválido, 404 si el doctor no existe, 409 por conflictos o 500 ante un error."
    )
    public ResponseEntity<DoctorResponse> updateDoctor(
        @PathVariable("id") Long id,
        @RequestBody @Valid DoctorUpdateRequest request
    ) {
        try {
            Doctor actualizado = personalService.updateDoctor(id, request);
            return ResponseEntity.ok(mapToResponse(actualizado));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, ex.getMessage(), ex);
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Marca como inactivo a un doctor.",
        description = "Realiza una baja lógica para impedir nuevas asignaciones. "
            + "Puede responder 204, 404 si no existe y 500 si ocurre un error."
    )
    public ResponseEntity<Void> deleteDoctor(@PathVariable("id") Long id) {
        try {
            personalService.deleteDoctorById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    // Alineamos esta respuesta con el esquema Doctores y el contrato que usa AdminAddDoctorViewModel.
    private DoctorResponse mapToResponse(Doctor doctor) {
        Doctor safeDoctor = Objects.requireNonNull(doctor, "Doctor entity must not be null");
        DoctorResponse response = new DoctorResponse();
        response.setIdDoctor(safeDoctor.getId());
        response.setNombre(safeDoctor.getNombre());
        response.setApellido(safeDoctor.getApellido());
        response.setCorreo(safeDoctor.getCorreo());
        response.setTelefono(safeDoctor.getTelefono());
        response.setTarifaConsulta(safeDoctor.getTarifaConsulta());
        response.setSueldo(safeDoctor.getSueldo());
        response.setBono(safeDoctor.getBono());
        response.setActivo(safeDoctor.getActivo());
        response.setFechaNacimiento(safeDoctor.getFechaNacimiento());
        if (safeDoctor.getEspecialidad() != null) {
            response.setIdEspecialidad(safeDoctor.getEspecialidad().getId());
        }
        return response;
    }
}
