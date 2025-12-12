package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.repository.DoctorRepository;
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
    private final DoctorRepository doctorRepository;

    public EspecialidadService(
        EspecialidadRepository especialidadRepository,
        DoctorRepository doctorRepository
    ) {
        this.especialidadRepository = especialidadRepository;
        this.doctorRepository = doctorRepository;
    }

    public List<Especialidad> findByDoctorId(Long doctorId) {
        Doctor doctor = findDoctor(doctorId);
        Especialidad especialidad = doctor.getEspecialidad();
        return especialidad != null ? List.of(especialidad) : List.of();
    }

    public Especialidad createForDoctor(Long doctorId, String nombreEspecialidad) {
        String nombre = Objects.requireNonNull(nombreEspecialidad, "El nombre de la especialidad es requerido")
            .trim();
        Doctor doctor = findDoctor(doctorId);
        Especialidad especialidad = especialidadRepository.findByNombreIgnoreCase(nombre)
            .orElseGet(() -> {
                Especialidad nueva = new Especialidad();
                nueva.setNombre(nombre);
                return especialidadRepository.save(nueva);
            });
        doctor.setEspecialidad(especialidad);
        doctorRepository.save(doctor);
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
            Doctor doctor = findDoctor(doctorId);
            doctor.setEspecialidad(especialidad);
            doctorRepository.save(doctor);
        }
        return especialidadRepository.save(especialidad);
    }

    public void delete(Long id) {
        Especialidad especialidad = findById(id);
        List<Doctor> asignados = doctorRepository.findByEspecialidadAndActivoTrue(especialidad);
        if (!asignados.isEmpty()) {
            throw new IllegalStateException("La especialidad tiene doctores asociados");
        }
        especialidadRepository.delete(especialidad);
    }

    private Doctor findDoctor(Long doctorId) {
        return doctorRepository.findByIdAndActivoTrue(doctorId)
            .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado"));
    }

    public List<Doctor> findDoctoresPorEspecialidad(Long especialidadId) {
        Especialidad especialidad = findById(especialidadId);
        return doctorRepository.findByEspecialidadAndActivoTrue(especialidad);
    }
}
