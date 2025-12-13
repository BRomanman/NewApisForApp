package com.clinica.api.personal_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.personal_service.dto.DoctorCreateRequest;
import com.clinica.api.personal_service.dto.DoctorUpdateRequest;
import com.clinica.api.personal_service.model.Doctor;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.service.PersonalService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
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

@WebMvcTest(DoctorController.class)
@AutoConfigureMockMvc(addFilters = false)
class DoctorControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PersonalService personalService;

    @Test
    @DisplayName("GET /api/v1/doctores responde 200 con la lista de doctores")
    void getAllDoctores_returnsOk() throws Exception {
        Doctor doctor = doctor();
        when(personalService.findAllDoctores()).thenReturn(List.of(doctor));

        mockMvc.perform(get("/api/v1/doctores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idDoctor").value(1L))
            .andExpect(jsonPath("$[0].idEspecialidad").value(5L))
            .andExpect(jsonPath("$[0].nombre").value("Ana"));
    }

    @Test
    @DisplayName("GET /api/v1/doctores responde 204 cuando no hay resultados")
    void getAllDoctores_returnsNoContent() throws Exception {
        when(personalService.findAllDoctores()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/doctores"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/doctores/{id} responde 200 cuando el doctor existe")
    void getDoctorById_returnsOk() throws Exception {
        Doctor doctor = doctor();
        when(personalService.findDoctorById(1L)).thenReturn(doctor);

        mockMvc.perform(get("/api/v1/doctores/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo").value("ana@demo.com"));
    }

    @Test
    @DisplayName("GET /api/v1/doctores/{id} responde 404 cuando el doctor no existe")
    void getDoctorById_returnsNotFound() throws Exception {
        when(personalService.findDoctorById(10L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/doctores/{id}", 10L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/doctores responde 201 con el doctor creado")
    void createDoctor_returnsCreated() throws Exception {
        Doctor created = doctor();
        created.setTarifaConsulta(40000);
        when(personalService.createDoctor(any(DoctorCreateRequest.class))).thenReturn(created);

        mockMvc.perform(post("/api/v1/doctores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorCreateRequest())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.idDoctor").value(1L));
    }

    @Test
    @DisplayName("POST /api/v1/doctores responde 400 cuando el correo ya existe")
    void createDoctor_returnsBadRequestWhenEmailExists() throws Exception {
        when(personalService.createDoctor(any(DoctorCreateRequest.class)))
            .thenThrow(new IllegalArgumentException("Ya existe un doctor con ese correo"));

        mockMvc.perform(post("/api/v1/doctores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorCreateRequest())))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PUT /api/v1/doctores/{id} responde 200 cuando se actualiza correctamente")
    void updateDoctor_returnsOk() throws Exception {
        Doctor existente = doctor();
        Doctor actualizado = doctor();
        actualizado.setTarifaConsulta(60000);
        actualizado.setSueldo(1500000L);
        when(personalService.updateDoctor(1L, any(DoctorUpdateRequest.class))).thenReturn(actualizado);

        mockMvc.perform(put("/api/v1/doctores/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorUpdateRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tarifaConsulta").value(60000))
            .andExpect(jsonPath("$.idEspecialidad").value(5L));
    }

    @Test
    @DisplayName("PUT /api/v1/doctores/{id} responde 404 cuando el doctor no existe")
    void updateDoctor_returnsNotFound() throws Exception {
        when(personalService.updateDoctor(88L, any(DoctorUpdateRequest.class)))
            .thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/doctores/{id}", 88L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorUpdateRequest())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/doctores/{id} responde 204 cuando se elimina")
    void deleteDoctor_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/doctores/{id}", 3L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/doctores/{id} responde 404 cuando no existe")
    void deleteDoctor_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("no existe")).when(personalService).deleteDoctorById(9L);

        mockMvc.perform(delete("/api/v1/doctores/{id}", 9L))
            .andExpect(status().isNotFound());
    }

    private Doctor doctor() {
        Doctor doctor = new Doctor();
        doctor.setId(1L);
        doctor.setTarifaConsulta(50000);
        doctor.setSueldo(1200000L);
        doctor.setBono(200000L);
        doctor.setNombre("Ana");
        doctor.setApellido("Gomez");
        doctor.setCorreo("ana@demo.com");
        doctor.setTelefono("+56999999999");
        doctor.setFechaNacimiento(LocalDate.of(1990, 5, 4));
        doctor.setContrasena("secreta");
        doctor.setRol(rol("doctor"));
        doctor.setEspecialidad(especialidad("Cardiologia"));
        return doctor;
    }

    private DoctorUpdateRequest doctorUpdateRequest() {
        DoctorUpdateRequest request = new DoctorUpdateRequest();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        request.setFechaNacimiento("1990-05-04");
        request.setCorreo("ana@demo.com");
        request.setTelefono("+56999999999");
        request.setIdEspecialidad(5L);
        request.setTarifaConsulta(50000);
        request.setSueldo(1200000L);
        request.setBono(150000L);
        request.setActivo(true);
        return request;
    }
    private DoctorCreateRequest doctorCreateRequest() {
        DoctorCreateRequest request = new DoctorCreateRequest();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        request.setFechaNacimiento("1990-05-04");
        request.setCorreo("ana@demo.com");
        request.setTelefono("+56999999999");
        request.setContrasena("secreta");
        request.setIdRol(2L);
        request.setIdEspecialidad(5L);
        request.setTarifaConsulta(50000);
        request.setSueldo(1200000L);
        request.setBono(150000L);
        request.setActivo(true);
        return request;
    }

    private Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setId(3L);
        rol.setNombre(nombre);
        return rol;
    }

    private Especialidad especialidad(String nombre) {
        Especialidad especialidad = new Especialidad();
        especialidad.setId(5L);
        especialidad.setNombre(nombre);
        return especialidad;
    }
}
