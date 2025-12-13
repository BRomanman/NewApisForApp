package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.dto.AdministradorDto;
import com.clinica.api.personal_service.dto.AdministradorUpdateRequestDto;
import com.clinica.api.personal_service.model.Administrador;
import com.clinica.api.personal_service.repository.AdministradorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
import java.sql.Blob;
import java.sql.SQLException;
import java.util.Objects;
import java.util.Optional;
import javax.sql.rowset.serial.SerialBlob;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
public class AdministradorService {

    private final AdministradorRepository administradorRepository;

    public AdministradorService(AdministradorRepository administradorRepository) {
        this.administradorRepository = administradorRepository;
    }

    public void actualizarFotoPerfilAdmin(Long id, MultipartFile file) {
        Administrador administrador = findActiveAdministrador(id);
        validateImageFile(file);
        try {
            administrador.setFotoPerfil(new SerialBlob(file.getBytes()));
        } catch (IOException ex) {
            throw new IllegalStateException("Error al leer el archivo de imagen", ex);
        } catch (SQLException ex) {
            throw new IllegalStateException("Error al almacenar la foto de perfil", ex);
        }
        administradorRepository.save(administrador);
    }

    public byte[] obtenerFotoPerfilAdmin(Long id) {
        Administrador administrador = findActiveAdministrador(id);
        Blob foto = administrador.getFotoPerfil();
        if (foto == null) {
            throw new EntityNotFoundException("Foto de perfil del administrador no encontrada");
        }
        try {
            long length = foto.length();
            if (length == 0) {
                throw new EntityNotFoundException("Foto de perfil del administrador no encontrada");
            }
            return foto.getBytes(1, Math.toIntExact(length));
        } catch (SQLException | ArithmeticException ex) {
            throw new IllegalStateException("Error al leer la foto de perfil almacenada", ex);
        }
    }

    public Optional<AdministradorDto> findByIdDto(Long id) {
        return administradorRepository.findByIdAndActivoTrue(id).map(this::mapToDto);
    }

    public AdministradorDto update(Long id, AdministradorUpdateRequestDto request) {
        Administrador administrador = findActiveAdministrador(id);
        Objects.requireNonNull(request, "Request de actualización no puede ser nulo");
        applyUpdates(administrador, request);
        Administrador actualizado = administradorRepository.save(administrador);
        return mapToDto(actualizado);
    }

    private Administrador findActiveAdministrador(Long id) {
        return administradorRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado"));
    }

    private void applyUpdates(Administrador administrador, AdministradorUpdateRequestDto request) {
        if (request.getNombre() != null) administrador.setNombre(request.getNombre());
        if (request.getApellido() != null) administrador.setApellido(request.getApellido());
        if (request.getFechaNacimiento() != null) administrador.setFechaNacimiento(request.getFechaNacimiento());
        if (request.getCorreo() != null) administrador.setCorreo(request.getCorreo());
        if (request.getTelefono() != null) administrador.setTelefono(request.getTelefono());
        if (request.getSueldo() != null) administrador.setSueldo(request.getSueldo());
        if (request.getActivo() != null) administrador.setActivo(request.getActivo());
    }

    private AdministradorDto mapToDto(Administrador administrador) {
        AdministradorDto dto = new AdministradorDto();
        dto.setId(administrador.getId());
        dto.setNombre(administrador.getNombre());
        dto.setApellido(administrador.getApellido());
        dto.setFechaNacimiento(administrador.getFechaNacimiento());
        dto.setCorreo(administrador.getCorreo());
        dto.setTelefono(administrador.getTelefono());
        dto.setSueldo(administrador.getSueldo());
        dto.setActivo(administrador.getActivo());
        dto.setRol(administrador.getRol() != null ? administrador.getRol().getNombre() : null);
        return dto;
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
