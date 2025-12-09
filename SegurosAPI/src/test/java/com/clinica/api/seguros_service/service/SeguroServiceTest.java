package com.clinica.api.seguros_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.seguros_service.model.ContratoSeguro;
import com.clinica.api.seguros_service.model.Seguro;
import com.clinica.api.seguros_service.repository.ContratoSeguroRepository;
import com.clinica.api.seguros_service.repository.SeguroRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class SeguroServiceTest {

    @Mock
    private SeguroRepository seguroRepository;

    @Mock
    private ContratoSeguroRepository contratoSeguroRepository;

    @InjectMocks
    private SeguroService seguroService;

    @Test
    @DisplayName("findAllSeguros delega en el repositorio")
    void findAllSeguros_returnsRepositoryData() {
        when(seguroRepository.findAll()).thenReturn(List.of(seguro()));

        List<Seguro> seguros = seguroService.findAllSeguros();

        assertThat(seguros).hasSize(1);
        verify(seguroRepository).findAll();
    }

    @Test
    @DisplayName("findSeguroById lanza EntityNotFoundException cuando el registro no existe")
    void findSeguroById_throwsWhenMissing() {
        when(seguroRepository.findById(1L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguroService.findSeguroById(1L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("createSeguro limpia el ID antes de persistir")
    void createSeguro_resetsIdBeforeSaving() {
        Seguro request = seguro();
        request.setId(10L);
        when(seguroRepository.save(any(Seguro.class))).thenAnswer(inv -> inv.getArgument(0));

        Seguro created = seguroService.createSeguro(request);

        assertThat(created.getId()).isNull();
        ArgumentCaptor<Seguro> captor = ArgumentCaptor.forClass(Seguro.class);
        verify(seguroRepository).save(captor.capture());
        assertThat(captor.getValue().getId()).isNull();
    }

    @Test
    @DisplayName("updateSeguro reemplaza los campos principales")
    void updateSeguro_updatesExistingFields() {
        Seguro existente = seguro();
        when(seguroRepository.findById(5L)).thenReturn(Optional.of(existente));
        when(seguroRepository.save(any(Seguro.class))).thenAnswer(inv -> inv.getArgument(0));

        Seguro cambios = new Seguro();
        cambios.setNombreSeguro("Actualizado");
        cambios.setDescripcion("Nueva desc");
        cambios.setValor(75000);

        Seguro actualizado = seguroService.updateSeguro(5L, cambios);

        assertThat(actualizado.getNombreSeguro()).isEqualTo("Actualizado");
        assertThat(actualizado.getValor()).isEqualTo(75000);
        verify(seguroRepository).save(existente);
    }

    @Test
    @DisplayName("deleteSeguro elimina el registro existente")
    void deleteSeguro_removesEntity() {
        Seguro existente = seguro();
        when(seguroRepository.findById(2L)).thenReturn(Optional.of(existente));

        seguroService.deleteSeguro(2L);

        verify(seguroRepository).delete(existente);
    }

    @Test
    @DisplayName("createContrato establece valores por defecto")
    void createContrato_appliesDefaults() {
        ContratoSeguro contrato = contrato();
        contrato.setId(5L);
        contrato.setFechaContratacion(null);
        contrato.setEstado(null);
        when(contratoSeguroRepository.save(any(ContratoSeguro.class))).thenAnswer(inv -> inv.getArgument(0));

        ContratoSeguro creado = seguroService.createContrato(contrato);

        assertThat(creado.getId()).isNull();
        assertThat(creado.getFechaContratacion()).isNotNull();
        assertThat(creado.getEstado()).isEqualTo("ACTIVO");
        verify(contratoSeguroRepository).save(contrato);
    }

    @Test
    @DisplayName("findContratosByUsuario delega en el repositorio")
    void findContratosByUsuario_returnsRepositoryData() {
        when(contratoSeguroRepository.findByIdUsuario(3L)).thenReturn(List.of(contrato()));

        List<ContratoSeguro> contratos = seguroService.findContratosByUsuario(3L);

        assertThat(contratos).hasSize(1);
        verify(contratoSeguroRepository).findByIdUsuario(3L);
    }

    @Test
    @DisplayName("findContratoById lanza EntityNotFoundException cuando no existe")
    void findContratoById_throwsWhenMissing() {
        when(contratoSeguroRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> seguroService.findContratoById(99L))
            .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("cancelarContrato actualiza estado y fecha cuando está activo")
    void cancelarContrato_updatesState() {
        ContratoSeguro contrato = contrato();
        when(contratoSeguroRepository.findById(1L)).thenReturn(Optional.of(contrato));
        when(contratoSeguroRepository.save(any(ContratoSeguro.class))).thenAnswer(inv -> inv.getArgument(0));

        ContratoSeguro cancelado = seguroService.cancelarContrato(1L);

        assertThat(cancelado.getEstado()).isEqualTo("CANCELADO");
        assertThat(cancelado.getFechaCancelacion()).isNotNull();
        verify(contratoSeguroRepository).save(contrato);
    }

    @Test
    @DisplayName("cancelarContrato retorna el contrato sin cambios si ya estaba cancelado")
    void cancelarContrato_returnsExistingWhenAlreadyCancelled() {
        ContratoSeguro contrato = contrato();
        contrato.setFechaCancelacion(LocalDateTime.now());
        when(contratoSeguroRepository.findById(4L)).thenReturn(Optional.of(contrato));

        ContratoSeguro resultado = seguroService.cancelarContrato(4L);

        assertThat(resultado).isSameAs(contrato);
        verify(contratoSeguroRepository, never()).save(any(ContratoSeguro.class));
    }

    private Seguro seguro() {
        Seguro seguro = new Seguro();
        seguro.setId(1L);
        seguro.setNombreSeguro("Dental");
        seguro.setDescripcion("Cobertura dental");
        seguro.setValor(25000);
        return seguro;
    }

    private ContratoSeguro contrato() {
        ContratoSeguro contrato = new ContratoSeguro();
        contrato.setId(1L);
        contrato.setIdSeguro(5L);
        contrato.setIdUsuario(10L);
        contrato.setRutBeneficiarios("11.111.111-1");
        contrato.setNombreBeneficiarios("Juan Pérez");
        contrato.setFechaNacimientoBeneficiarios("2000-01-01");
        contrato.setCorreoContacto("correo@demo.cl");
        contrato.setTelefonoContacto("+56911111111");
        contrato.setMetodoPago("TARJETA");
        contrato.setFechaContratacion(LocalDateTime.of(2024, 1, 1, 9, 0));
        contrato.setEstado("ACTIVO");
        return contrato;
    }
}
