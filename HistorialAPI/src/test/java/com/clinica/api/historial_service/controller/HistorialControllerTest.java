package com.clinica.api.historial_service.controller;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.historial_service.model.Historial;
import com.clinica.api.historial_service.service.HistorialService;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(HistorialController.class)
class HistorialControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private HistorialService historialService;

    @Test
    @DisplayName("GET /api/v1/historial/usuario/{id} responde 200 con historiales")
    void getHistorialesByUsuario_returnsOk() throws Exception {
        when(historialService.findHistorialesByUsuarioId(3L)).thenReturn(List.of(historial()));

        mockMvc.perform(get("/api/v1/historial/usuario/{usuarioId}", 3L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idUsuario").value(3L));
    }

    @Test
    @DisplayName("GET /api/v1/historial/usuario/{id} responde 204 sin resultados")
    void getHistorialesByUsuario_returnsNoContent() throws Exception {
        when(historialService.findHistorialesByUsuarioId(4L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/historial/usuario/{usuarioId}", 4L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/historial/doctor/{id} responde 200 con historiales")
    void getHistorialesByDoctor_returnsOk() throws Exception {
        when(historialService.findHistorialesByDoctorId(9L)).thenReturn(List.of(historial()));

        mockMvc.perform(get("/api/v1/historial/doctor/{doctorId}", 9L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idDoctor").value(9L));
    }

    @Test
    @DisplayName("GET /api/v1/historial/{id} responde 200 cuando existe")
    void getHistorialById_returnsOk() throws Exception {
        Historial historial = historial();
        historial.setId(20L);
        when(historialService.findHistorialById(20L)).thenReturn(historial);

        mockMvc.perform(get("/api/v1/historial/{id}", 20L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(20L));
    }

    @Test
    @DisplayName("GET /api/v1/historial/{id} responde 404 cuando no existe")
    void getHistorialById_returnsNotFound() throws Exception {
        when(historialService.findHistorialById(40L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/historial/{id}", 40L))
            .andExpect(status().isNotFound());
    }

    private Historial historial() {
        Historial historial = new Historial();
        historial.setId(1L);
        historial.setIdUsuario(3L);
        historial.setIdDoctor(9L);
        historial.setIdConsulta(5L);
        historial.setEstado("COMPLETADA");
        historial.setFechaConsulta(LocalDate.of(2024, 1, 10));
        historial.setHoraInicio(LocalTime.of(9, 0));
        historial.setHoraFin(LocalTime.of(9, 30));
        historial.setDuracionMinutos(30);
        historial.setDisponible(Boolean.TRUE);
        historial.setObservaciones("Sin novedades");
        historial.setDiagnostico("Control");
        return historial;
    }
}
