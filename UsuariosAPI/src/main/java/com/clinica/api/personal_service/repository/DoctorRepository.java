package com.clinica.api.personal_service.repository;

import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    List<Doctor> findByActivoTrue();
    Optional<Doctor> findByIdAndActivoTrue(Long id);
    Optional<Doctor> findByCorreoAndActivoTrue(String correo);
    Optional<Doctor> findByCorreo(String correo);

    List<Doctor> findByEspecialidad(Especialidad especialidad);

    List<Doctor> findByEspecialidadAndActivoTrue(Especialidad especialidad);
}
