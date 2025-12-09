package com.clinica.api.personal_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.personal_service.controller.EspecialidadController.EspecialidadRequest;
import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.service.EspecialidadService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(EspecialidadController.class)
@AutoConfigureMockMvc(addFilters = false)
class EspecialidadControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private EspecialidadService especialidadService;

    @Test
    @DisplayName("GET /api/v1/doctores/{id}/especialidades responde 200 cuando hay registros")
    void getEspecialidadesByDoctor_returnsOk() throws Exception {
        Especialidad especialidad = especialidad();
        when(especialidadService.findByDoctorId(2L)).thenReturn(List.of(especialidad));
        when(especialidadService.findDoctoresPorEspecialidad(especialidad.getId()))
            .thenReturn(List.of(doctor()));

        mockMvc.perform(get("/api/v1/doctores/{doctorId}/especialidades", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].doctorId").value(5L));
    }

    @Test
    @DisplayName("GET /api/v1/doctores/{id}/especialidades responde 204 cuando no hay datos")
    void getEspecialidadesByDoctor_returnsNoContent() throws Exception {
        when(especialidadService.findByDoctorId(4L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/doctores/{doctorId}/especialidades", 4L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/doctores/{id}/especialidades responde 404 cuando el doctor no existe")
    void getEspecialidadesByDoctor_returnsNotFound() throws Exception {
        when(especialidadService.findByDoctorId(7L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/doctores/{doctorId}/especialidades", 7L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/doctores/{id}/especialidades responde 400 con payload inválido")
    void createEspecialidadForDoctor_returnsBadRequest() throws Exception {
        EspecialidadRequest request = new EspecialidadRequest();
        request.setNombre("");

        mockMvc.perform(post("/api/v1/doctores/{doctorId}/especialidades", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/doctores/{id}/especialidades responde 201 cuando se crea la especialidad")
    void createEspecialidadForDoctor_returnsCreated() throws Exception {
        when(especialidadService.createForDoctor(3L, "Pediatría")).thenReturn(especialidad("Pediatría"));

        mockMvc.perform(post("/api/v1/doctores/{doctorId}/especialidades", 3L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Pediatría", null))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Pediatría"));
    }

    @Test
    @DisplayName("POST /api/v1/doctores/{id}/especialidades responde 404 cuando el doctor no existe")
    void createEspecialidadForDoctor_returnsNotFound() throws Exception {
        when(especialidadService.createForDoctor(4L, "Pediatría"))
            .thenThrow(new EntityNotFoundException("doctor"));

        mockMvc.perform(post("/api/v1/doctores/{doctorId}/especialidades", 4L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Pediatría", null))))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/especialidades responde 200 con la lista completa")
    void getAllEspecialidades_returnsOk() throws Exception {
        when(especialidadService.findAll()).thenReturn(List.of(especialidad()));

        mockMvc.perform(get("/api/v1/especialidades"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].nombre").value("Cardiología"));
    }

    @Test
    @DisplayName("GET /api/v1/especialidades responde 204 cuando no hay registros")
    void getAllEspecialidades_returnsNoContent() throws Exception {
        when(especialidadService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/especialidades"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/especialidades/{id} responde 404 cuando no existe")
    void getEspecialidadById_returnsNotFound() throws Exception {
        when(especialidadService.findById(8L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/especialidades/{id}", 8L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/especialidades responde 400 cuando falta el doctorId")
    void createEspecialidad_returnsBadRequest() throws Exception {
        mockMvc.perform(post("/api/v1/especialidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Pediatría", null))))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("POST /api/v1/especialidades responde 201 cuando se crea la especialidad")
    void createEspecialidad_returnsCreated() throws Exception {
        Especialidad creada = especialidad("Dermatología");
        when(especialidadService.createForDoctor(6L, "Dermatología")).thenReturn(creada);

        mockMvc.perform(post("/api/v1/especialidades")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Dermatología", 6L))))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.nombre").value("Dermatología"));
    }

    @Test
    @DisplayName("PUT /api/v1/especialidades/{id} responde 400 cuando el payload es inválido")
    void updateEspecialidad_returnsBadRequest() throws Exception {
        mockMvc.perform(put("/api/v1/especialidades/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(new EspecialidadRequest())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/especialidades/{id} responde 200 cuando se actualiza la especialidad")
    void updateEspecialidad_returnsOk() throws Exception {
        Especialidad actualizada = especialidad("Oncología");
        when(especialidadService.update(any(Long.class), any(String.class), any(Long.class))).thenReturn(actualizada);

        mockMvc.perform(put("/api/v1/especialidades/{id}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Oncología", 5L))))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Oncología"));
    }

    @Test
    @DisplayName("PUT /api/v1/especialidades/{id} responde 404 cuando no existe")
    void updateEspecialidad_returnsNotFound() throws Exception {
        when(especialidadService.update(any(Long.class), any(String.class), any(Long.class)))
            .thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/especialidades/{id}", 9L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request("Oncología", 5L))))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/especialidades/{id} responde 404 cuando no existe")
    void deleteEspecialidad_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("no existe")).when(especialidadService).delete(4L);

        mockMvc.perform(delete("/api/v1/especialidades/{id}", 4L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/especialidades/{id} responde 204 cuando se elimina")
    void deleteEspecialidad_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/especialidades/{id}", 3L))
            .andExpect(status().isNoContent());
    }

    private Especialidad especialidad() {
        return especialidad("Cardiología");
    }

    private Especialidad especialidad(String nombre) {
        Especialidad especialidad = new Especialidad();
        especialidad.setId(1L);
        especialidad.setNombre(nombre);
        return especialidad;
    }

    private Empleado doctor() {
        Empleado doctor = new Empleado();
        doctor.setId(5L);
        doctor.setTipo(EmpleadoTipo.DOCTOR);
        doctor.setNombre("Doctor");
        return doctor;
    }

    private EspecialidadRequest request(String nombre, Long doctorId) {
        EspecialidadRequest request = new EspecialidadRequest();
        request.setNombre(nombre);
        request.setDoctorId(doctorId);
        return request;
    }
}
