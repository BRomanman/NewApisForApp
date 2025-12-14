package citas_service_nuevo.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import citas_service_nuevo.model.Cita;
import citas_service_nuevo.service.CitaService;
import com.fasterxml.jackson.databind.ObjectMapper;
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
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(CitaController.class)
class CitaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CitaService citaService;

    @Test
    @DisplayName("GET /api/v1/citas retorna 200 con contenido cuando existen registros")
    void getAllCitas_returnsOk() throws Exception {
        Cita cita = sampleCita();
        when(citaService.findAll()).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].id").value(1L))
            .andExpect(jsonPath("$[0].estado").value("Confirmado"));
    }

    @Test
    @DisplayName("GET /api/v1/citas responde 204 cuando no hay registros")
    void getAllCitas_returnsNoContent() throws Exception {
        when(citaService.findAll()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/citas"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id} responde 200 cuando la cita existe")
    void getCitaById_returnsOk() throws Exception {
        when(citaService.findById(1L)).thenReturn(sampleCita());

        mockMvc.perform(get("/api/v1/citas/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id} responde 404 cuando la cita no existe")
    void getCitaById_returnsNotFound() throws Exception {
        when(citaService.findById(99L)).thenThrow(new EntityNotFoundException("No existe"));

        mockMvc.perform(get("/api/v1/citas/{id}", 99L))
            .andExpect(status().isNotFound());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("POST /api/v1/citas responde 201 con la cita creada")
    void createCita_returnsCreated() throws Exception {
        Cita cita = sampleCita();
        cita.setId(null);
        Cita saved = sampleCita();
        when(citaService.save(any(Cita.class))).thenReturn(saved);

        mockMvc.perform(post("/api/v1/citas")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(cita)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("DELETE /api/v1/citas/{id} responde 204 cuando se elimina correctamente")
    void deleteCita_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/citas/{id}", 10L))
            .andExpect(status().isNoContent());
    }

    @SuppressWarnings("null")
    @Test
    @DisplayName("PUT /api/v1/citas/{id} responde 404 cuando el servicio lanza EntityNotFoundException")
    void updateCita_returnsNotFoundWhenMissing() throws Exception {
        Cita changes = sampleCita();
        when(citaService.findById(55L)).thenThrow(new EntityNotFoundException("No existe"));

        mockMvc.perform(put("/api/v1/citas/{id}", 55L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(changes)))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/citas/doctor/{idDoctor}/proximas responde 200 con registros futuros")
    void getProximasCitasByDoctor_returnsOk() throws Exception {
        Cita cita = sampleCita();
        cita.setFechaCita(LocalDate.of(2025, 1, 1));
        when(citaService.findProximasByDoctor(3L)).thenReturn(List.of(cita));

        mockMvc.perform(get("/api/v1/citas/doctor/{idDoctor}/proximas", 3L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idDoctor").value(3L))
            .andExpect(jsonPath("$[0].id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/citas/doctor/{idDoctor}/proximas responde 204 cuando no hay registros futuros")
    void getProximasCitasByDoctor_returnsNoContent() throws Exception {
        when(citaService.findProximasByDoctor(7L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/citas/doctor/{idDoctor}/proximas", 7L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/citas/usuario/{id} responde 200 con citas del usuario")
    void getCitasByUsuario_returnsOk() throws Exception {
        when(citaService.findByUsuario(2L)).thenReturn(List.of(sampleCita()));

        mockMvc.perform(get("/api/v1/citas/usuario/{idUsuario}", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idUsuario").value(2L));
    }

    @Test
    @DisplayName("GET /api/v1/citas/usuario/{id}/proximas responde 204 sin citas futuras")
    void getProximasCitasByUsuario_returnsNoContent() throws Exception {
        when(citaService.findProximasByUsuario(3L)).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/citas/usuario/{idUsuario}/proximas", 3L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/citas/doctor/{id}/fecha/{fecha} responde 400 para fecha inválida")
    void getCitasPorDoctorYFecha_returnsBadRequestOnInvalidDate() throws Exception {
        mockMvc.perform(get("/api/v1/citas/doctor/{idDoctor}/fecha/{fecha}", 3L, "2024-13-01"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/citas/doctor/{id}/fecha/{fecha} responde 200 con resultados")
    void getCitasPorDoctorYFecha_returnsOk() throws Exception {
        when(citaService.findByDoctorAndFecha(3L, LocalDate.parse("2024-06-01")))
            .thenReturn(List.of(sampleCita()));

        mockMvc.perform(get("/api/v1/citas/doctor/{idDoctor}/fecha/{fecha}", 3L, "2024-06-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].idDoctor").value(3L));
    }

    @Test
    @DisplayName("GET /api/v1/citas/disponibles responde 400 cuando la fecha es inválida")
    void getDisponiblesPorDoctorYFecha_returnsBadRequestOnInvalidDate() throws Exception {
        mockMvc.perform(get("/api/v1/citas/disponibles")
                .param("doctorId", "1")
                .param("fecha", "2024-02-31"))
            .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("GET /api/v1/citas/disponibles responde 200 con bloques disponibles")
    void getDisponiblesPorDoctorYFecha_returnsOk() throws Exception {
        when(citaService.findDisponiblesByDoctorAndFecha(1L, LocalDate.parse("2024-06-01")))
            .thenReturn(List.of(sampleCita()));

        mockMvc.perform(get("/api/v1/citas/disponibles")
                .param("doctorId", "1")
                .param("fecha", "2024-06-01"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].disponible").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id}/disponible responde 200 con el estado")
    void isCitaDisponible_returnsOk() throws Exception {
        when(citaService.isDisponible(8L)).thenReturn(Boolean.TRUE);

        mockMvc.perform(get("/api/v1/citas/{id}/disponible", 8L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$").value(true));
    }

    @Test
    @DisplayName("GET /api/v1/citas/{id}/disponible responde 404 cuando la cita no existe")
    void isCitaDisponible_returnsNotFound() throws Exception {
        when(citaService.isDisponible(9L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/citas/{id}/disponible", 9L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/citas/{id}/reservar responde 200 cuando se reserva correctamente")
    void reservarCita_returnsOk() throws Exception {
        when(citaService.reservar(4L, 10L)).thenReturn(sampleCita());

        mockMvc.perform(put("/api/v1/citas/{id}/reservar", 4L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idUsuario\":10}"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.idUsuario").value(2L));
    }

    @Test
    @DisplayName("PUT /api/v1/citas/{id}/reservar responde 409 cuando no está disponible")
    void reservarCita_returnsConflict() throws Exception {
        when(citaService.reservar(4L, 10L)).thenThrow(new IllegalStateException("ocupada"));

        mockMvc.perform(put("/api/v1/citas/{id}/reservar", 4L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idUsuario\":10}"))
            .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/citas/{id}/reservar responde 404 cuando no existe")
    void reservarCita_returnsNotFound() throws Exception {
        when(citaService.reservar(40L, 10L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/citas/{id}/reservar", 40L)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"idUsuario\":10}"))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /api/v1/citas/{id}/cancelar responde 200 cuando se libera la cita")
    void cancelarCita_returnsOk() throws Exception {
        Cita cita = sampleCita();
        cita.setEstado("Disponible");
        when(citaService.cancelarCita(2L)).thenReturn(cita);

        mockMvc.perform(patch("/api/v1/citas/{id}/cancelar", 2L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.estado").value("Disponible"));
    }

    @Test
    @DisplayName("PATCH /api/v1/citas/{id}/cancelar responde 404 cuando no existe")
    void cancelarCita_returnsNotFound() throws Exception {
        when(citaService.cancelarCita(2L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(patch("/api/v1/citas/{id}/cancelar", 2L))
            .andExpect(status().isNotFound());
    }

    private Cita sampleCita() {
        Cita cita = new Cita();
        cita.setId(1L);
        cita.setEstado("Confirmado");
        cita.setFechaCita(LocalDate.of(2024, 6, 1));
        cita.setHoraInicio(LocalTime.of(10, 0));
        cita.setHoraFin(LocalTime.of(10, 30));
        cita.setIdUsuario(2L);
        cita.setIdDoctor(3L);
        cita.setDisponible(true);
        return cita;
    }
}
