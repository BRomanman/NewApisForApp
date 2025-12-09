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

import com.clinica.api.personal_service.model.Empleado;
import com.clinica.api.personal_service.model.EmpleadoTipo;
import com.clinica.api.personal_service.model.Especialidad;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.service.EspecialidadService;
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

    @MockBean
    private EspecialidadService especialidadService;

    @Test
    @DisplayName("GET /api/v1/doctores responde 200 con la lista de doctores")
    void getAllDoctores_returnsOk() throws Exception {
        Empleado doctor = empleado();
        when(personalService.findAllDoctores()).thenReturn(List.of(doctor));
        when(especialidadService.findByDoctorId(doctor.getId())).thenReturn(List.of(especialidad("Cardiología")));

        mockMvc.perform(get("/api/v1/doctores"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].especialidad").value("Cardiología"))
            .andExpect(jsonPath("$[0].usuario.nombre").value("Ana"));
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
        Empleado doctor = empleado();
        when(personalService.findDoctorById(1L)).thenReturn(doctor);
        when(especialidadService.findByDoctorId(doctor.getId())).thenReturn(List.of(especialidad("Cardiología")));

        mockMvc.perform(get("/api/v1/doctores/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.usuario.correo").value("ana@demo.com"));
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
        Empleado created = empleado();
        created.setTarifaConsulta(40000);
        when(personalService.saveDoctor(any(Empleado.class))).thenReturn(created);
        when(especialidadService.findByDoctorId(created.getId())).thenReturn(List.of(especialidad("Cardiología")));

        mockMvc.perform(post("/api/v1/doctores")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorPayload())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/doctores/{id} responde 200 cuando se actualiza correctamente")
    void updateDoctor_returnsOk() throws Exception {
        Empleado existente = empleado();
        when(personalService.findDoctorById(1L)).thenReturn(existente);
        when(personalService.saveDoctor(any(Empleado.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(especialidadService.findByDoctorId(existente.getId())).thenReturn(List.of(especialidad("Neurología")));

        Empleado cambios = doctorPayload();
        cambios.setTarifaConsulta(60000);
        cambios.setSueldo(1500000L);

        mockMvc.perform(put("/api/v1/doctores/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cambios)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.tarifaConsulta").value(60000))
            .andExpect(jsonPath("$.especialidad").value("Neurología"));
    }

    @Test
    @DisplayName("PUT /api/v1/doctores/{id} responde 404 cuando el doctor no existe")
    void updateDoctor_returnsNotFound() throws Exception {
        when(personalService.findDoctorById(88L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/doctores/{id}", 88L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(doctorPayload())))
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

    private Empleado empleado() {
        Empleado empleado = new Empleado();
        empleado.setId(1L);
        empleado.setTarifaConsulta(50000);
        empleado.setSueldo(1200000L);
        empleado.setBono(200000L);
        empleado.setNombre("Ana");
        empleado.setApellido("Gómez");
        empleado.setCorreo("ana@demo.com");
        empleado.setTelefono("+56999999999");
        empleado.setFechaNacimiento(LocalDate.of(1990, 5, 4));
        empleado.setContrasena("secreta");
        empleado.setRol(rol("doctor"));
        empleado.setTipo(EmpleadoTipo.DOCTOR);
        return empleado;
    }

    private Empleado doctorPayload() {
        Empleado empleado = new Empleado();
        empleado.setTarifaConsulta(50000);
        empleado.setSueldo(1200000L);
        empleado.setBono(150000L);
        empleado.setNombre("Ana");
        empleado.setApellido("Gómez");
        empleado.setCorreo("ana@demo.com");
        empleado.setTelefono("+56999999999");
        empleado.setFechaNacimiento(LocalDate.of(1990, 5, 4));
        empleado.setContrasena("secreta");
        empleado.setRol(rol("doctor"));
        empleado.setTipo(EmpleadoTipo.DOCTOR);
        return empleado;
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
