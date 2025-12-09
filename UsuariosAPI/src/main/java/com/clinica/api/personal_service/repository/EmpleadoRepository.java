package com.clinica.api.personal_service.repository;

import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
import com.clinica.api.personal_service.model.Especialidad;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface EmpleadoRepository extends JpaRepository<Empleado, Long> {

    List<Empleado> findByActivoTrueAndTipo(EmpleadoTipo tipo);

    Optional<Empleado> findByIdAndActivoTrue(Long id);

    Optional<Empleado> findByCorreoAndActivoTrue(String correo);

    List<Empleado> findByEspecialidad_IdAndTipo(Long especialidadId, EmpleadoTipo tipo);
    
    List<Empleado> findByEspecialidad(Especialidad especialidad);
}
