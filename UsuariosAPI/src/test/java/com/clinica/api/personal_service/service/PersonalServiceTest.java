package com.clinica.api.personal_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.repository.DoctorRepository;
import com.clinica.api.personal_service.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

@ExtendWith(MockitoExtension.class)
class PersonalServiceTest {

    @Mock
    private DoctorRepository empleadoRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private PersonalService personalService;

    @Test
    @DisplayName("findDoctorById retorna el doctor cuando está activo")
    void findDoctorById_returnsEntity() {
        Doctor doctor = doctor(true);
        when(empleadoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.of(doctor));

        Doctor result = personalService.findDoctorById(1L);

        assertThat(result).isSameAs(doctor);
    }

    @Test
    @DisplayName("findDoctorById lanza EntityNotFoundException cuando no existe")
    void findDoctorById_throwsWhenMissing() {
        when(empleadoRepository.findByIdAndActivoTrue(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> personalService.findDoctorById(1L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("saveDoctor asigna activo=true cuando no viene especificado")
    void saveDoctor_setsActiveDefault() {
        Doctor doctor = doctor(null);
        doctor.setActivo(null);
        when(empleadoRepository.save(any(Doctor.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Doctor saved = personalService.saveDoctor(doctor);

        assertThat(saved.getActivo()).isTrue();
        verify(empleadoRepository).save(doctor);
    }

    @Test
    @DisplayName("deleteDoctorById marca el doctor como inactivo")
    void deleteDoctorById_marksInactive() {
        Doctor doctor = doctor(true);
        when(empleadoRepository.findByIdAndActivoTrue(2L)).thenReturn(Optional.of(doctor));

        personalService.deleteDoctorById(2L);

        assertThat(doctor.getActivo()).isFalse();
        verify(empleadoRepository).save(doctor);
    }

    private Doctor doctor(Boolean activo) {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setActivo(activo);
        doctor.setRol(rol());
        return doctor;
    }

    private Rol rol() {
        Rol rol = new Rol();
        rol.setId(2L);
        rol.setNombre("Doctor");
        return rol;
    }
}
