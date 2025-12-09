package com.clinica.api.seguros_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.seguros_service.model.Seguro;
import com.clinica.api.seguros_service.service.SeguroService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(SeguroController.class)
class SeguroControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SeguroService seguroService;

    @Test
    @DisplayName("GET /api/v1/seguros responde 200 con resultados")
    void listarSeguros_returnsOk() throws Exception {
        when(seguroService.findAllSeguros()).thenReturn(List.of(seguro()));

        mockMvc.perform(get("/api/v1/seguros"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombreSeguro").value("Dental"));
    }

    @Test
    @DisplayName("GET /api/v1/seguros responde 204 cuando no hay datos")
    void listarSeguros_returnsNoContent() throws Exception {
        when(seguroService.findAllSeguros()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/seguros"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/seguros/{id} responde 404 cuando no existe")
    void obtenerSeguro_returnsNotFound() throws Exception {
        when(seguroService.findSeguroById(9L)).thenThrow(new EntityNotFoundException("No existe"));

        mockMvc.perform(get("/api/v1/seguros/{id}", 9L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/seguros responde 201 con el seguro creado")
    void crearSeguro_returnsCreated() throws Exception {
        when(seguroService.createSeguro(any(Seguro.class))).thenReturn(seguro());

        mockMvc.perform(post("/api/v1/seguros")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seguro())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.valor").value(25000));
    }

    @Test
    @DisplayName("PUT /api/v1/seguros/{id} responde 200 cuando se actualiza correctamente")
    void actualizarSeguro_returnsOk() throws Exception {
        Seguro actualizado = seguro();
        actualizado.setNombreSeguro("Actualizado");
        when(seguroService.updateSeguro(any(Long.class), any(Seguro.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/seguros/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seguro())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombreSeguro").value("Actualizado"));
    }

    @Test
    @DisplayName("PUT /api/v1/seguros/{id} responde 404 cuando no existe")
    void actualizarSeguro_returnsNotFound() throws Exception {
        when(seguroService.updateSeguro(any(Long.class), any(Seguro.class)))
            .thenThrow(new EntityNotFoundException("No existe"));

        mockMvc.perform(put("/api/v1/seguros/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(seguro())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/seguros/{id} responde 204")
    void eliminarSeguro_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/seguros/{id}", 4L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/seguros/{id} responde 404 cuando no existe")
    void eliminarSeguro_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("No existe")).when(seguroService).deleteSeguro(10L);

        mockMvc.perform(delete("/api/v1/seguros/{id}", 10L))
            .andExpect(status().isNotFound());
    }

    private Seguro seguro() {
        Seguro seguro = new Seguro();
        seguro.setId(1L);
        seguro.setNombreSeguro("Dental");
        seguro.setDescripcion("Cobertura dental");
        seguro.setValor(25000);
        return seguro;
    }
}
