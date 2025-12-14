package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.dto.DoctorCreateRequest;
import com.clinica.api.personal_service.dto.DoctorUpdateRequest;
import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.repository.DoctorRepository;
import com.clinica.api.personal_service.repository.EspecialidadRepository;
import com.clinica.api.personal_service.repository.RolRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Blob;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class PersonalService {

    private final DoctorRepository doctorRepository;
    private final RolRepository rolRepository;
    private final EspecialidadRepository especialidadRepository;
    private final PasswordEncoder passwordEncoder;
    private byte[] defaultDoctorAvatar;

    public PersonalService(
        DoctorRepository doctorRepository,
        RolRepository rolRepository,
        EspecialidadRepository especialidadRepository,
        PasswordEncoder passwordEncoder
    ) {
        this.doctorRepository = doctorRepository;
        this.rolRepository = rolRepository;
        this.especialidadRepository = especialidadRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // Mapea el DTO con los campos de la tabla Doctores y aplica la lógica de negocio adicional.
    public Doctor createDoctor(DoctorCreateRequest request) {
        Objects.requireNonNull(request, "DoctorCreateRequest must not be null");
        String correo = trimValue(request.getCorreo());
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("Correo es requerido");
        }
        if (doctorRepository.findByCorreo(correo).isPresent()) {
            throw new IllegalArgumentException("Ya existe un doctor con ese correo");
        }
        Especialidad especialidad = especialidadRepository.findById(request.getIdEspecialidad())
            .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));

        Doctor doctor = new Doctor();
        doctor.setNombre(trimValue(request.getNombre()));
        doctor.setApellido(trimValue(request.getApellido()));
        doctor.setFechaNacimiento(requireFechaNacimiento(request.getFechaNacimiento()));
        doctor.setCorreo(correo);
        doctor.setTelefono(request.getTelefono());
        doctor.setContrasena(request.getContrasena());
        doctor.setRol(resolveRole(request.getIdRol()));
        doctor.setEspecialidad(especialidad);
        doctor.setTarifaConsulta(request.getTarifaConsulta());
        doctor.setSueldo(request.getSueldo());
        doctor.setBono(request.getBono() != null ? request.getBono() : 0L);
        doctor.setActivo(request.getActivo() != null ? request.getActivo() : true);
        assignDefaultAvatar(doctor);
        return saveDoctor(doctor);
    }

    public Doctor updateDoctor(Long id, DoctorUpdateRequest request) {
        Objects.requireNonNull(request, "DoctorUpdateRequest must not be null");
        Doctor doctor = doctorRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Doctor no encontrado"));

        String correo = trimValue(request.getCorreo());
        if (correo == null || correo.isBlank()) {
            throw new IllegalArgumentException("Correo es requerido");
        }
        doctorRepository.findByCorreo(correo)
            .filter(existing -> !existing.getId().equals(id))
            .ifPresent(existing -> {
                throw new IllegalArgumentException("Ya existe un doctor con ese correo");
            });

        Especialidad especialidad = especialidadRepository.findById(request.getIdEspecialidad())
            .orElseThrow(() -> new IllegalArgumentException("Especialidad no encontrada"));

        doctor.setNombre(trimValue(request.getNombre()));
        doctor.setApellido(trimValue(request.getApellido()));
        doctor.setFechaNacimiento(requireFechaNacimiento(request.getFechaNacimiento()));
        doctor.setCorreo(correo);
        doctor.setTelefono(request.getTelefono());
        doctor.setTarifaConsulta(request.getTarifaConsulta());
        doctor.setSueldo(request.getSueldo());
        doctor.setBono(request.getBono() != null ? request.getBono() : 0L);
        doctor.setActivo(request.getActivo());
        doctor.setEspecialidad(especialidad);
        // no cambiamos contraseña ni rol para evitar nulidad
        return doctorRepository.save(doctor);
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
            doctor.setFotoPerfil(new SerialBlob(file.getBytes()));
        } catch (IOException ex) {
            throw new IllegalStateException("Error al leer el archivo de imagen", ex);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al almacenar la foto de perfil", ex);
        }
        doctorRepository.save(doctor);
    }

    public byte[] obtenerFotoPerfilDoctor(Long id) {
        Doctor doctor = findDoctorById(id);
        Blob foto = doctor.getFotoPerfil();
        if (foto == null) {
            throw new EntityNotFoundException("Foto de perfil del doctor no encontrada");
        }
        try {
            long length = foto.length();
            if (length == 0) {
                throw new EntityNotFoundException("Foto de perfil del doctor no encontrada");
            }
            return foto.getBytes(1, Math.toIntExact(length));
        } catch (SQLException | ArithmeticException ex) {
            throw new IllegalStateException("Error al leer la foto de perfil almacenada", ex);
        }
    }

    private LocalDate requireFechaNacimiento(LocalDate fecha) {
        if (fecha == null) {
            throw new IllegalArgumentException("fechaNacimiento es requerido");
        }
        return fecha;
    }

    private String trimValue(String value) {
        return value != null ? value.trim() : null;
    }

    private Rol resolveRole(Long roleId) {
        Long resolvedId = roleId != null ? roleId : 2L;
        return rolRepository.findById(resolvedId)
            .orElseGet(() -> rolRepository.findByNombreIgnoreCase("Doctor")
                .orElseThrow(() -> new IllegalArgumentException("Rol Doctor no configurado")));
    }

    // Carga la imagen static/default_doctor_avatar.png cuando el doctor no tiene foto.
    private void assignDefaultAvatar(Doctor doctor) {
        if (doctor.getFotoPerfil() != null) {
            return;
        }
        byte[] avatar = getDefaultDoctorAvatar();
        if (avatar == null || avatar.length == 0) {
            return;
        }
        try {
            doctor.setFotoPerfil(new SerialBlob(avatar));
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al asignar la foto por defecto", ex);
        }
    }

    private byte[] getDefaultDoctorAvatar() {
        if (defaultDoctorAvatar == null) {
            synchronized (this) {
                if (defaultDoctorAvatar == null) {
                    defaultDoctorAvatar = loadDefaultDoctorAvatar();
                }
            }
        }
        return defaultDoctorAvatar;
    }

    private byte[] loadDefaultDoctorAvatar() {
        ClassPathResource resource = new ClassPathResource("static/default_doctor_avatar.png");
        if (!resource.exists()) {
            return null;
        }
        try (InputStream is = resource.getInputStream()) {
            return is.readAllBytes();
        } catch (IOException ex) {
            return null;
        }
    }

    private boolean shouldEncode(String contrasena) {
        if (contrasena == null || contrasena.isBlank()) {
            return false;
        }
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
