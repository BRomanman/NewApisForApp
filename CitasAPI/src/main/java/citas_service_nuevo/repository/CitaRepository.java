package citas_service_nuevo.repository;

import citas_service_nuevo.model.Cita;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CitaRepository extends JpaRepository<Cita, Long> {

    List<Cita> findByIdUsuario(Long idUsuario);

    List<Cita> findByIdDoctorAndFechaCita(Long idDoctor, LocalDate fechaCita);

    List<Cita> findByIdUsuarioAndFechaCitaGreaterThanEqual(Long idUsuario, LocalDate fechaDesde);

    List<Cita> findByIdDoctorAndFechaCitaGreaterThanEqual(Long idDoctor, LocalDate fechaDesde);

    List<Cita> findByIdDoctorAndFechaCitaAndDisponibleTrue(Long idDoctor, LocalDate fechaCita);

    Optional<Cita> findFirstByIdDoctorAndFechaCitaAndHoraInicioAndDisponibleTrue(
        Long idDoctor,
        LocalDate fechaCita,
        LocalTime horaInicio
    );

    Optional<Cita> findFirstByIdDoctorAndFechaCitaAndHoraInicio(
        Long idDoctor,
        LocalDate fechaCita,
        LocalTime horaInicio
    );
}
