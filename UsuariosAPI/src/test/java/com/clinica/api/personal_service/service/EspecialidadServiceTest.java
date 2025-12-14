package com.clinica.api.personal_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.repository.DoctorRepository;
import com.clinica.api.personal_service.repository.EspecialidadRepository;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class EspecialidadServiceTest {

    @Mock
    private EspecialidadRepository especialidadRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @InjectMocks
    private EspecialidadService especialidadService;

    @Test
    @DisplayName("createForDoctor crea la especialidad si no existe y la asigna al doctor")
    void createForDoctor_createsAndAssigns() {
        Doctor doctor = doctor(1L);
        when(doctorRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(doctor));
        Especialidad persistida = especialidad(3L, "Cardiología");
        when(especialidadRepository.findByNombreIgnoreCase("Cardiología")).thenReturn(Optional.empty());
        when(especialidadRepository.save(any(Especialidad.class))).thenReturn(persistida);

        Especialidad result = especialidadService.createForDoctor(1L, " Cardiología ");

        assertThat(result).isSameAs(persistida);
        assertThat(doctor.getEspecialidad()).isSameAs(persistida);
        verify(doctorRepository).save(doctor);
    }

    @Test
    @DisplayName("update modifica el nombre y reasigna la especialidad al doctor indicado")
    void update_changesNameAndAssignsDoctor() {
        Especialidad especialidad = especialidad(5L, "Antigua");
        when(especialidadRepository.findById(5L)).thenReturn(Optional.of(especialidad));
        Doctor doctor = doctor(9L);
        when(doctorRepository.findByIdAndActivoTrue(9L)).thenReturn(Optional.of(doctor));
        when(especialidadRepository.save(any(Especialidad.class))).thenAnswer(inv -> inv.getArgument(0));

        Especialidad result = especialidadService.update(5L, "Nueva", 9L);

        assertThat(result.getNombre()).isEqualTo("Nueva");
        assertThat(doctor.getEspecialidad()).isSameAs(especialidad);
        verify(doctorRepository).save(doctor);
    }

    @Test
    @DisplayName("delete lanza IllegalStateException cuando hay doctores activos asociados")
    void delete_throwsWhenEspecialidadHasActiveDoctors() {
        Especialidad especialidad = especialidad(2L, "Pediatría");
        when(especialidadRepository.findById(2L)).thenReturn(Optional.of(especialidad));
        when(doctorRepository.findByEspecialidadAndActivoTrue(especialidad)).thenReturn(List.of(doctor(7L)));

        assertThatThrownBy(() -> especialidadService.delete(2L))
            .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("findByDoctorId retorna la especialidad del doctor activo")
    void findByDoctorId_returnsEspecialidad() {
        Especialidad especialidad = especialidad(4L, "Dermatología");
        Doctor doctor = doctor(10L);
        doctor.setEspecialidad(especialidad);
        when(doctorRepository.findByIdAndActivoTrue(10L)).thenReturn(Optional.of(doctor));

        List<Especialidad> result = especialidadService.findByDoctorId(10L);

        assertThat(result).containsExactly(especialidad);
    }

    private Doctor doctor(Long id) {
        Doctor doctor = new Doctor();
        doctor.setId(id);
        doctor.setActivo(true);
        return doctor;
    }

    private Especialidad especialidad(Long id, String nombre) {
        Especialidad especialidad = new Especialidad();
        especialidad.setId(id);
        especialidad.setNombre(nombre);
        return especialidad;
    }
}
