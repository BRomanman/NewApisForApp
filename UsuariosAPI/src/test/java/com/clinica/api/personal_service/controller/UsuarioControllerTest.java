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

import com.clinica.api.personal_service.dto.UsuarioResponse;
import com.clinica.api.personal_service.exception.PersonalServiceExceptionHandler;
import com.clinica.api.personal_service.model.Rol;
import com.clinica.api.personal_service.model.Usuario;
import com.clinica.api.personal_service.service.UsuarioService;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import jakarta.persistence.EntityNotFoundException;
import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class UsuarioControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule());

    @Mock
    private UsuarioService usuarioService;

    @InjectMocks
    private UsuarioController usuarioController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(usuarioController)
            .setControllerAdvice(new PersonalServiceExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /api/v1/usuarios responde 200 con la lista de usuarios")
    void getAllUsuarios_returnsOk() throws Exception {
        when(usuarioService.findAllUsuarios()).thenReturn(List.of(usuarioResponse()));

        mockMvc.perform(get("/api/v1/usuarios"))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$[0].correo").value("ana@demo.com"));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios responde 204 cuando no hay registros")
    void getAllUsuarios_returnsNoContent() throws Exception {
        when(usuarioService.findAllUsuarios()).thenReturn(Collections.emptyList());

        mockMvc.perform(get("/api/v1/usuarios"))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{id} responde 200 cuando el usuario existe")
    void getUsuarioById_returnsOk() throws Exception {
        when(usuarioService.findUsuarioById(1L)).thenReturn(usuarioResponse());

        mockMvc.perform(get("/api/v1/usuarios/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("GET /api/v1/usuarios/{id} responde 404 cuando no existe")
    void getUsuarioById_returnsNotFound() throws Exception {
        when(usuarioService.findUsuarioById(6L)).thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(get("/api/v1/usuarios/{id}", 6L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/usuarios responde 201 con el usuario creado")
    void createUsuario_returnsCreated() throws Exception {
        Usuario saved = usuario();
        saved.setId(1L);
        when(usuarioService.saveUsuario(any(Usuario.class))).thenReturn(saved);
        when(usuarioService.findUsuarioById(saved.getId())).thenReturn(usuarioResponse());

        mockMvc.perform(post("/api/v1/usuarios")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(usuarioPayload())))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.id").value(1L));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id} responde 200 con el usuario actualizado")
    void updateUsuario_returnsOk() throws Exception {
        Usuario actualizado = usuario();
        actualizado.setId(1L);
        when(usuarioService.updateUsuario(any(Long.class), any(Usuario.class))).thenReturn(actualizado);
        when(usuarioService.findUsuarioById(actualizado.getId())).thenReturn(usuarioResponse());

        mockMvc.perform(put("/api/v1/usuarios/{id}", 1L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(usuarioPayload())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo").value("ana@demo.com"));
    }

    @Test
    @DisplayName("PUT /api/v1/usuarios/{id} responde 404 cuando no existe")
    void updateUsuario_returnsNotFound() throws Exception {
        when(usuarioService.updateUsuario(any(Long.class), any(Usuario.class)))
            .thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/usuarios/{id}", 7L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(usuarioPayload())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/usuarios/{id} responde 204 cuando se elimina correctamente")
    void deleteUsuario_returnsNoContent() throws Exception {
        mockMvc.perform(delete("/api/v1/usuarios/{id}", 3L))
            .andExpect(status().isNoContent());
    }

    @Test
    @DisplayName("DELETE /api/v1/usuarios/{id} responde 404 cuando no existe")
    void deleteUsuario_returnsNotFound() throws Exception {
        doThrow(new EntityNotFoundException("no existe")).when(usuarioService).deleteUsuarioById(9L);

        mockMvc.perform(delete("/api/v1/usuarios/{id}", 9L))
            .andExpect(status().isNotFound());
    }

    private Usuario usuario() {
        Usuario usuario = new Usuario();
        usuario.setNombre("Ana");
        usuario.setApellido("Gómez");
        usuario.setCorreo("ana@demo.com");
        usuario.setTelefono("+56911111111");
        usuario.setFechaNacimiento(LocalDate.of(1995, 3, 15));
        usuario.setContrasena("secreta");
        usuario.setRol(rol("paciente"));
        return usuario;
    }

    private Usuario usuarioPayload() {
        Usuario usuario = usuario();
        usuario.setId(null);
        return usuario;
    }

    private Rol rol(String nombre) {
        Rol rol = new Rol();
        rol.setId(2L);
        rol.setNombre(nombre);
        return rol;
    }

    private UsuarioResponse usuarioResponse() {
        UsuarioResponse response = new UsuarioResponse();
        response.setId(1L);
        response.setNombre("Ana");
        response.setApellido("Gómez");
        response.setCorreo("ana@demo.com");
        response.setTelefono("+56911111111");
        response.setRol("paciente");
        return response;
    }
}
