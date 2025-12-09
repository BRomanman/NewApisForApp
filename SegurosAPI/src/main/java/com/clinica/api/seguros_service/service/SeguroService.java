package com.clinica.api.seguros_service.service;

import com.clinica.api.seguros_service.model.ContratoSeguro;
import com.clinica.api.seguros_service.model.Seguro;
import com.clinica.api.seguros_service.repository.ContratoSeguroRepository;
import com.clinica.api.seguros_service.repository.SeguroRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import java.time.LocalDateTime;
import java.util.List;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class SeguroService {

    private final SeguroRepository seguroRepository;
    private final ContratoSeguroRepository contratoSeguroRepository;

    public SeguroService(SeguroRepository seguroRepository, ContratoSeguroRepository contratoSeguroRepository) {
        this.seguroRepository = seguroRepository;
        this.contratoSeguroRepository = contratoSeguroRepository;
    }

    public List<Seguro> findAllSeguros() {
        return seguroRepository.findAll();
    }

    public Seguro findSeguroById(Long id) {
        return seguroRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Seguro no encontrado"));
    }

    public Seguro createSeguro(Seguro seguro) {
        seguro.setId(null);
        return seguroRepository.save(seguro);
    }

    public Seguro updateSeguro(Long id, Seguro cambios) {
        Seguro existente = findSeguroById(id);
        existente.setNombreSeguro(cambios.getNombreSeguro());
        existente.setDescripcion(cambios.getDescripcion());
        existente.setValor(cambios.getValor());
        return seguroRepository.save(existente);
    }

    public void deleteSeguro(Long id) {
        Seguro seguro = findSeguroById(id);
        seguroRepository.delete(seguro);
    }

    public ContratoSeguro createContrato(ContratoSeguro contrato) {
        contrato.setId(null);
        if (contrato.getFechaContratacion() == null) {
            contrato.setFechaContratacion(LocalDateTime.now());
        }
        if (contrato.getEstado() == null) {
            contrato.setEstado("ACTIVO");
        }
        return contratoSeguroRepository.save(contrato);
    }

    public List<ContratoSeguro> findContratosByUsuario(Long idUsuario) {
        return contratoSeguroRepository.findByIdUsuario(idUsuario);
    }

    public List<ContratoSeguro> findContratosBySeguro(Long idSeguro) {
        return contratoSeguroRepository.findByIdSeguro(idSeguro);
    }

    public ContratoSeguro findContratoById(Long id) {
        return contratoSeguroRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Contrato no encontrado"));
    }

    public ContratoSeguro cancelarContrato(Long id) {
        ContratoSeguro contrato = findContratoById(id);
        if (contrato.getFechaCancelacion() != null) {
            return contrato;
        }
        contrato.setEstado("CANCELADO");
        contrato.setFechaCancelacion(LocalDateTime.now());
        return contratoSeguroRepository.save(contrato);
    }
}
