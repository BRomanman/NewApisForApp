package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
import com.clinica.api.personal_service.repository.EmpleadoRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class PersonalService {

    private final EmpleadoRepository empleadoRepository;

    public PersonalService(EmpleadoRepository empleadoRepository) {
        this.empleadoRepository = empleadoRepository;
    }

    public List<Empleado> findAllDoctores() {
        return empleadoRepository.findByActivoTrueAndTipo(EmpleadoTipo.DOCTOR);
    }

    public Empleado findDoctorById(Long id) {
        Empleado empleado = empleadoRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado"));
        if (empleado.getTipo() != EmpleadoTipo.DOCTOR) {
            throw new EntityNotFoundException("Doctor no encontrado");
        }
        return empleado;
    }

    public Empleado saveDoctor(Empleado doctor) {
        Empleado safeDoctor = Objects.requireNonNull(doctor, "Doctor entity must not be null");
        if (safeDoctor.getTipo() != EmpleadoTipo.DOCTOR) {
            safeDoctor.setTipo(EmpleadoTipo.DOCTOR);
        }
        if (safeDoctor.getActivo() == null) {
            safeDoctor.setActivo(true);
        }
        return empleadoRepository.save(safeDoctor);
    }

    public void deleteDoctorById(Long id) {
        Empleado doctor = findDoctorById(id);
        doctor.setActivo(false);
        empleadoRepository.save(doctor);
    }
}
