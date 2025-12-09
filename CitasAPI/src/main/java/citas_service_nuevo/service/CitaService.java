package citas_service_nuevo.service;

import citas_service_nuevo.model.Cita;
import citas_service_nuevo.repository.CitaRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
public class CitaService {

    private static final String ESTADO_DISPONIBLE = "Disponible";
    private static final String ESTADO_CONFIRMADO = "Confirmado";

    private final CitaRepository citaRepository;

    public CitaService(CitaRepository citaRepository) {
        this.citaRepository = citaRepository;
    }

    public List<Cita> findAll() {
        return citaRepository.findAll();
    }

    public Cita findById(Long id) {
        return citaRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Cita no encontrada"));
    }

    public List<Cita> findByUsuario(Long idUsuario) {
        return citaRepository.findByIdUsuario(idUsuario);
    }

    public List<Cita> findProximasByUsuario(Long idUsuario) {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findByIdUsuarioAndFechaCitaGreaterThanEqual(idUsuario, hoy);
    }

    public List<Cita> findProximasByDoctor(Long idDoctor) {
        LocalDate hoy = LocalDate.now();
        return citaRepository.findByIdDoctorAndFechaCitaGreaterThanEqual(idDoctor, hoy);
    }

    public List<Cita> findByDoctorAndFecha(Long idDoctor, LocalDate fecha) {
        return citaRepository.findByIdDoctorAndFechaCita(idDoctor, fecha);
    }

    public List<Cita> findDisponiblesByDoctorAndFecha(Long idDoctor, LocalDate fecha) {
        return citaRepository.findByIdDoctorAndFechaCitaAndDisponibleTrue(idDoctor, fecha);
    }

    public boolean isDisponible(Long id) {
        Cita cita = findById(id);
        return Boolean.TRUE.equals(cita.getDisponible()) && ESTADO_DISPONIBLE.equals(cita.getEstado());
    }

    public Cita save(Cita cita) {
        return citaRepository.save(cita);
    }

    public void deleteById(Long id) {
        findById(id);
        citaRepository.deleteById(id);
    }

    public Cita cancelarCita(Long id) {
        Cita cita = findById(id);
        cita.setEstado(ESTADO_DISPONIBLE);
        cita.setIdUsuario(null);
        cita.setDisponible(true);
        return citaRepository.save(cita);
    }

    public Cita reservar(Long idCita, Long idUsuario) {
        Cita cita = findById(idCita);
        if (!Boolean.TRUE.equals(cita.getDisponible()) || !ESTADO_DISPONIBLE.equals(cita.getEstado())) {
            throw new IllegalStateException("El bloque no está disponible para reservar");
        }
        cita.setIdUsuario(idUsuario);
        cita.setEstado(ESTADO_CONFIRMADO);
        cita.setDisponible(false);
        return citaRepository.save(cita);
    }
}
