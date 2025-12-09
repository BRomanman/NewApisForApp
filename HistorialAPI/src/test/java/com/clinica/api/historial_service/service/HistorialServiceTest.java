package com.clinica.api.historial_service.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.clinica.api.historial_service.model.Historial;
import com.clinica.api.historial_service.repository.HistorialRepository;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class HistorialServiceTest {

    @Mock
    private HistorialRepository historialRepository;

    @InjectMocks
    private HistorialService historialService;

    @Test
    @DisplayName("findHistorialesByUsuarioId retorna la lista proporcionada por el repositorio")
    void findHistorialesByUsuarioId_returnsRepositoryList() {
        Historial historial = new Historial();
        historial.setId(1L);
        historial.setDiagnostico("Diagnóstico");
        historial.setObservaciones("Observación");
        historial.setFechaConsulta(LocalDate.of(2024, 1, 15));

        List<Historial> expected = List.of(historial);
        when(historialRepository.findByIdUsuario(5L)).thenReturn(expected);

        List<Historial> result = historialService.findHistorialesByUsuarioId(5L);

        assertThat(result).containsExactlyElementsOf(expected);
        verify(historialRepository).findByIdUsuario(5L);
    }

    @Test
    @DisplayName("findHistorialById retorna el historial cuando existe")
    void findHistorialById_returnsEntity() {
        Historial historial = new Historial();
        historial.setId(20L);

        when(historialRepository.findById(20L)).thenReturn(Optional.of(historial));

        Historial result = historialService.findHistorialById(20L);

        assertThat(result).isSameAs(historial);
        verify(historialRepository).findById(20L);
    }

    @Test
    @DisplayName("findHistorialById lanza EntityNotFoundException cuando no existe el registro")
    void findHistorialById_throwsWhenNotFound() {
        when(historialRepository.findById(30L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> historialService.findHistorialById(30L))
            .isInstanceOf(EntityNotFoundException.class)
            .hasMessageContaining("Historial no encontrado");
        verify(historialRepository).findById(30L);
    }
}
