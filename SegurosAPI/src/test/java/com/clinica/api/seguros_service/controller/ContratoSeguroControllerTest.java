package com.clinica.api.seguros_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.seguros_service.model.ContratoSeguro;
import com.clinica.api.seguros_service.service.SeguroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(ContratoSeguroController.class)
class ContratoSeguroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeguroService seguroService;

    @Test
    @DisplayName("GET /api/v1/seguros/contratos/usuario/{id} responde 200 con contratos")
    void listarContratosPorUsuario_returnsOk() throws Exception {
        when(seguroService.findContratosByUsuario(4L)).thenReturn(List.of(contrato()));

        mockMvc.perform(get("/api/v1/seguros/contratos/usuario/{idUsuario}", 4L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idUsuario").value(10L));
    }

    @Test
    @DisplayName("GET /api/v1/seguros/contratos/usuario/{id} responde 204 cuando no hay datos")
    void listarContratosPorUsuario_returnsNoContent() throws Exception {
        when(seguroService.findContratosByUsuario(7L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/seguros/contratos/usuario/{idUsuario}", 7L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/seguros/contratos/seguro/{id} responde 200 con contratos")
    void listarContratosPorSeguro_returnsOk() throws Exception {
        when(seguroService.findContratosBySeguro(2L)).thenReturn(List.of(contrato()));

        mockMvc.perform(get("/api/v1/seguros/contratos/seguro/{idSeguro}", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idSeguro").value(5L));
    }

    @Test
    @DisplayName("GET /api/v1/seguros/contratos/{id} responde 404 cuando el contrato no existe")
    void obtenerContrato_returnsNotFound() throws Exception {
        when(seguroService.findContratoById(90L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/seguros/contratos/{id}", 90L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/seguros/contratos responde 201 con el contrato creado")
    void crearContrato_returnsCreated() throws Exception {
        when(seguroService.createContrato(any(ContratoSeguro.class))).thenReturn(contrato());

        mockMvc.perform(post("/api/v1/seguros/contratos")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(contrato())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.estado").value("ACTIVO"));
    }

    @Test
    @DisplayName("POST /api/v1/seguros/contratos/{id}/cancelar responde 404 cuando no existe")
    void cancelarContrato_returnsNotFound() throws Exception {
        when(seguroService.cancelarContrato(8L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(post("/api/v1/seguros/contratos/{id}/cancelar", 8L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/seguros/contratos/{id}/cancelar responde 200 cuando se cancela correctamente")
    void cancelarContrato_returnsOk() throws Exception {
        ContratoSeguro contrato = contrato();
        contrato.setEstado("CANCELADO");
        when(seguroService.cancelarContrato(1L)).thenReturn(contrato);

        mockMvc.perform(post("/api/v1/seguros/contratos/{id}/cancelar", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("CANCELADO"));
    }

    private ContratoSeguro contrato() {
        ContratoSeguro contrato = new ContratoSeguro();
        contrato.setId(1L);
        contrato.setIdSeguro(5L);
        contrato.setIdUsuario(10L);
        contrato.setRutBeneficiarios("11.111.111-1");
        contrato.setNombreBeneficiarios("Juan Perez");
        contrato.setFechaNacimientoBeneficiarios("2000-01-01");
        contrato.setCorreoContacto("correo@demo.cl");
        contrato.setTelefonoContacto("+56911111111");
        contrato.setMetodoPago("TARJETA");
        contrato.setFechaContratacion(LocalDateTime.of(2024, 1, 1, 10, 0));
        contrato.setEstado("ACTIVO");
        return contrato;
    }
}
