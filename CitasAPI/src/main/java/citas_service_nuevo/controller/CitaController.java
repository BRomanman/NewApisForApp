package citas_service_nuevo.controller;

import citas_service_nuevo.model.Cita;
import citas_service_nuevo.service.CitaService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/citas")
@Tag(name = "Citas", description = "Operaciones para reservar, actualizar y cancelar citas médicas dentro del ecosistema clínico.")
public class CitaController {

    private final CitaService citaService;

    public CitaController(CitaService citaService) {
        this.citaService = citaService;
    }

    @GetMapping
    @Operation(summary = "Obtiene todas las citas registradas.")
    public ResponseEntity<List<Cita>> getAllCitas() {
        List<Cita> citas = citaService.findAll();
        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/{id}")
    @Operation(summary = "Obtiene una cita por su identificador.")
    public ResponseEntity<Cita> getCitaById(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(citaService.findById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/usuario/{idUsuario}")
    public ResponseEntity<List<Cita>> getCitasByUsuario(@PathVariable("idUsuario") Long idUsuario) {
        List<Cita> citas = citaService.findByUsuario(idUsuario);
        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/usuario/{idUsuario}/proximas")
    public ResponseEntity<List<Cita>> getProximasCitasByUsuario(@PathVariable("idUsuario") Long idUsuario) {
        List<Cita> citas = citaService.findProximasByUsuario(idUsuario);
        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/doctor/{idDoctor}/proximas")
    public ResponseEntity<List<Cita>> getProximasCitasByDoctor(@PathVariable("idDoctor") Long idDoctor) {
        List<Cita> citas = citaService.findProximasByDoctor(idDoctor);
        if (citas.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(citas);
    }

    @GetMapping("/doctor/{idDoctor}/fecha/{fecha}")
    public ResponseEntity<List<Cita>> getCitasPorDoctorYFecha(
        @PathVariable("idDoctor") Long idDoctor,
        @PathVariable("fecha") String fecha
    ) {
        try {
            LocalDate fechaBusqueda = LocalDate.parse(fecha);
            List<Cita> citas = citaService.findByDoctorAndFecha(idDoctor, fechaBusqueda);
            if (citas.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(citas);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/disponibles")
    public ResponseEntity<List<Cita>> getDisponiblesPorDoctorYFecha(
        @RequestParam("doctorId") Long doctorId,
        @RequestParam("fecha") String fecha
    ) {
        try {
            LocalDate fechaBusqueda = LocalDate.parse(fecha);
            List<Cita> disponibles = citaService.findDisponiblesByDoctorAndFecha(doctorId, fechaBusqueda);
            if (disponibles.isEmpty()) {
                return ResponseEntity.noContent().build();
            }
            return ResponseEntity.ok(disponibles);
        } catch (DateTimeParseException ex) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/{id}/disponible")
    public ResponseEntity<Boolean> isCitaDisponible(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(citaService.isDisponible(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    public ResponseEntity<Cita> createCita(@RequestBody Cita cita) {
        return ResponseEntity.status(HttpStatus.CREATED).body(citaService.save(cita));
    }

    @PutMapping("/{id}")
    public ResponseEntity<Cita> updateCita(@PathVariable("id") Long id, @RequestBody Cita citaDetails) {
        try {
            Cita existente = citaService.findById(id);
            existente.setFechaCita(citaDetails.getFechaCita());
            existente.setHoraInicio(citaDetails.getHoraInicio());
            existente.setHoraFin(citaDetails.getHoraFin());
            existente.setEstado(citaDetails.getEstado());
            existente.setIdUsuario(citaDetails.getIdUsuario());
            existente.setIdDoctor(citaDetails.getIdDoctor());
            existente.setDisponible(citaDetails.getDisponible());
            return ResponseEntity.ok(citaService.save(existente));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}/reservar")
    public ResponseEntity<Cita> reservarCita(
        @PathVariable("id") Long id,
        @RequestBody ReservarCitaRequest request
    ) {
        try {
            Cita cita = citaService.reservar(id, request.idUsuario());
            return ResponseEntity.ok(cita);
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        } catch (IllegalStateException ex) {
            return ResponseEntity.status(HttpStatus.CONFLICT).build();
        }
    }

    @PatchMapping("/{id}/cancelar")
    public ResponseEntity<Cita> cancelarCita(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(citaService.cancelarCita(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCita(@PathVariable("id") Long id) {
        try {
            citaService.deleteById(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    public record ReservarCitaRequest(Long idUsuario) {
    }
}
