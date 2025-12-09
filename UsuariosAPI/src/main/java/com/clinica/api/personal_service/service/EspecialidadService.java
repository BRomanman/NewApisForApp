package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.repository.EmpleadoRepository;
import com.clinica.api.personal_service.repository.EspecialidadRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class EspecialidadService {

    private final EspecialidadRepository especialidadRepository;
    private final EmpleadoRepository empleadoRepository;

    public EspecialidadService(
        EspecialidadRepository especialidadRepository,
        EmpleadoRepository empleadoRepository
    ) {
        this.especialidadRepository = especialidadRepository;
        this.empleadoRepository = empleadoRepository;
    }

    public List<Especialidad> findByDoctorId(Long doctorId) {
        Empleado doctor = findDoctor(doctorId);
        Especialidad especialidad = doctor.getEspecialidad();
        return especialidad != null ? List.of(especialidad) : List.of();
    }

    public Especialidad createForDoctor(Long doctorId, String nombreEspecialidad) {
        String nombre = Objects.requireNonNull(nombreEspecialidad, "El nombre de la especialidad es requerido")
            .trim();
        Empleado doctor = findDoctor(doctorId);
        Especialidad especialidad = especialidadRepository.findByNombreIgnoreCase(nombre)
            .orElseGet(() -> {
                Especialidad nueva = new Especialidad();
                nueva.setNombre(nombre);
                return especialidadRepository.save(nueva);
            });
        doctor.setEspecialidad(especialidad);
        empleadoRepository.save(doctor);
        return especialidad;
    }

    public List<Especialidad> findAll() {
        return especialidadRepository.findAll();
    }

    public Especialidad findById(Long id) {
        return especialidadRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Especialidad no encontrada"));
    }

    public Especialidad update(Long id, String nombre, Long doctorId) {
        Especialidad especialidad = findById(id);
        if (nombre != null && !nombre.isBlank()) {
            especialidad.setNombre(nombre.trim());
        }
        if (doctorId != null) {
            Empleado doctor = findDoctor(doctorId);
            doctor.setEspecialidad(especialidad);
            empleadoRepository.save(doctor);
        }
        return especialidadRepository.save(especialidad);
    }

    public void delete(Long id) {
        Especialidad especialidad = findById(id);
        List<Empleado> asignados = empleadoRepository.findByEspecialidad(especialidad);
        for (Empleado empleado : asignados) {
            empleado.setEspecialidad(null);
        }
        empleadoRepository.saveAll(asignados);
        especialidadRepository.delete(especialidad);
    }

    private Empleado findDoctor(Long doctorId) {
        Empleado doctor = empleadoRepository.findByIdAndActivoTrue(doctorId)
            .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado"));
        if (doctor.getTipo() != EmpleadoTipo.DOCTOR) {
            throw new EntityNotFoundException("Doctor no encontrado");
        }
        return doctor;
    }

    public List<Empleado> findDoctoresPorEspecialidad(Long especialidadId) {
        Especialidad especialidad = findById(especialidadId);
        return empleadoRepository.findByEspecialidad(especialidad);
    }
}
