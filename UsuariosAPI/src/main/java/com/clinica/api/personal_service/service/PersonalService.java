package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.repository.DoctorRepository;
import com.clinica.api.personal_service.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PersonalService {

    private final DoctorRepository doctorRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;

    public PersonalService(
        DoctorRepository doctorRepository,
        RolRepository rolRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.doctorRepository = doctorRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public List<Doctor> findAllDoctores() {
        return doctorRepository.findByActivoTrue();
    }

    public Doctor findDoctorById(Long id) {
        return doctorRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado"));
    }

    public Doctor saveDoctor(Doctor doctor) {
        Doctor safeDoctor = Objects.requireNonNull(doctor, "Doctor entity must not be null");
        if (safeDoctor.getActivo() == null) {
            safeDoctor.setActivo(true);
        }
        if (safeDoctor.getRol() == null) {
            rolRepository.findByNombreIgnoreCase("Doctor").ifPresent(safeDoctor::setRol);
            if (safeDoctor.getRol() == null) {
                throw new IllegalArgumentException("Rol Doctor no configurado");
            }
        }
        if (shouldEncode(safeDoctor.getContrasena())) {
            safeDoctor.setContrasena(passwordEncoder.encode(safeDoctor.getContrasena()));
        }
        return doctorRepository.save(safeDoctor);
    }

    public void deleteDoctorById(Long id) {
        Doctor doctor = findDoctorById(id);
        doctor.setActivo(false);
        doctorRepository.save(doctor);
    }

    public void actualizarFotoPerfilDoctor(Long id, MultipartFile file) {
        Doctor doctor = findDoctorById(id);
        validateImageFile(file);
        try {
            doctor.setFotoPerfil(file.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Error al leer el archivo de imagen", ex);
        }
        doctorRepository.save(doctor);
    }

    public byte[] obtenerFotoPerfilDoctor(Long id) {
        Doctor doctor = findDoctorById(id);
        byte[] foto = doctor.getFotoPerfil();
        if (foto == null || foto.length == 0) {
            throw new EntityNotFoundException("Foto de perfil del doctor no encontrada");
        }
        return foto;
    }

    private boolean shouldEncode(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            return false;
        }
        // Evita re-hashear valores ya codificados en SHA-256 (64 caracteres hex)
        return !contrasena.matches("^[0-9a-fA-F]{64}$");
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("El archivo de imagen es requerido");
        }
        String contentType = file.getContentType();
        if (contentType == null || !contentType.startsWith("image/")) {
            throw new IllegalArgumentException("El archivo debe tener un tipo de imagen válido");
        }
    }
}
