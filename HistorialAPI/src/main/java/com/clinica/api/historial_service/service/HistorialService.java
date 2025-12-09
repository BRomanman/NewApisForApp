package com.clinica.api.historial_service.service;

import com.clinica.api.historial_service.model.Historial;
import com.clinica.api.historial_service.repository.HistorialRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class HistorialService {

    private final HistorialRepository historialRepository;

    public HistorialService(HistorialRepository historialRepository) {
        this.historialRepository = historialRepository;
    }

    public List<Historial> findHistorialesByUsuarioId(Long usuarioId) {
        return historialRepository.findByIdUsuario(usuarioId);
    }

    public List<Historial> findHistorialesByDoctorId(Long doctorId) {
        return historialRepository.findByIdDoctor(doctorId);
    }

    @SuppressWarnings("null")
    public Historial findHistorialById(Long id) {
        return historialRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Historial no encontrado"));
    }
}
