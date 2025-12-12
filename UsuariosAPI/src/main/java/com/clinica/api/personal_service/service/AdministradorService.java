package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.model.Administrador;
import com.clinica.api.personal_service.repository.AdministradorRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.io.IOException;
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
            administrador.setFotoPerfil(file.getBytes());
        } catch (IOException ex) {
            throw new IllegalStateException("Error al leer el archivo de imagen", ex);
        }
        administradorRepository.save(administrador);
    }

    public byte[] obtenerFotoPerfilAdmin(Long id) {
        Administrador administrador = findActiveAdministrador(id);
        byte[] foto = administrador.getFotoPerfil();
        if (foto == null || foto.length == 0) {
            throw new EntityNotFoundException("Foto de perfil del administrador no encontrada");
        }
        return foto;
    }

    private Administrador findActiveAdministrador(Long id) {
        return administradorRepository.findByIdAndActivoTrue(id)
            .orElseThrow(() -> new EntityNotFoundException("Administrador no encontrado"));
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
