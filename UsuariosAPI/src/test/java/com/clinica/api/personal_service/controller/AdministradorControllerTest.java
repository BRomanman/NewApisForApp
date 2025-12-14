package com.clinica.api.personal_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.personal_service.dto.AdminPasswordChangeRequest;
import com.clinica.api.personal_service.dto.AdministradorDto;
import com.clinica.api.personal_service.dto.AdministradorUpdateRequestDto;
import com.clinica.api.personal_service.exception.PersonalServiceExceptionHandler;
import com.clinica.api.personal_service.service.AdminPasswordService;
import com.clinica.api.personal_service.service.AdministradorService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AdministradorControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AdministradorService administradorService;

    @Mock
    private AdminPasswordService adminPasswordService;

    @InjectMocks
    private AdministradorController administradorController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(administradorController)
            .setControllerAdvice(new PersonalServiceExceptionHandler())
            .build();
    }

    @Test
    @DisplayName("GET /api/v1/administradores/{id} responde 200 cuando existe")
    void getAdminById_returnsOk() throws Exception {
        when(administradorService.findByIdDto(1L)).thenReturn(java.util.Optional.of(adminDto()));

        mockMvc.perform(get("/api/v1/administradores/{id}", 1L))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.correo").value("admin@demo.com"));
    }

    @Test
    @DisplayName("GET /api/v1/administradores/{id} responde 404 cuando no existe")
    void getAdminById_returnsNotFound() throws Exception {
        when(administradorService.findByIdDto(5L)).thenReturn(java.util.Optional.empty());

        mockMvc.perform(get("/api/v1/administradores/{id}", 5L))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PUT /api/v1/administradores/{id} responde 200 al actualizar")
    void updateAdmin_returnsOk() throws Exception {
        when(administradorService.update(any(Long.class), any(AdministradorUpdateRequestDto.class)))
            .thenReturn(adminDto());

        mockMvc.perform(put("/api/v1/administradores/{id}", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.nombre").value("Ana"));
    }

    @Test
    @DisplayName("PUT /api/v1/administradores/{id} responde 404 cuando no existe")
    void updateAdmin_returnsNotFound() throws Exception {
        when(administradorService.update(any(Long.class), any(AdministradorUpdateRequestDto.class)))
            .thenThrow(new EntityNotFoundException("no existe"));

        mockMvc.perform(put("/api/v1/administradores/{id}", 9L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(updateRequest())))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/administradores/{id}/foto-perfil responde 204")
    void actualizarFotoPerfilAdmin_returnsNoContent() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[] {1, 2});

        mockMvc.perform(multipart("/api/v1/administradores/{id}/foto-perfil", 3L)
                .file(file))
            .andExpect(status().isNoContent());

        verify(administradorService).actualizarFotoPerfilAdmin(3L, file);
    }

    @Test
    @DisplayName("POST /api/v1/administradores/{id}/foto-perfil responde 404 cuando no existe")
    void actualizarFotoPerfilAdmin_returnsNotFound() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "foto.jpg", "image/jpeg", new byte[] {1, 2});
        doThrow(new EntityNotFoundException("no existe")).when(administradorService).actualizarFotoPerfilAdmin(4L, file);

        mockMvc.perform(multipart("/api/v1/administradores/{id}/foto-perfil", 4L)
                .file(file))
            .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("GET /api/v1/administradores/{id}/foto-perfil responde 200 con imagen")
    void obtenerFotoPerfilAdmin_returnsOk() throws Exception {
        when(administradorService.obtenerFotoPerfilAdmin(1L)).thenReturn(new byte[] {9, 8, 7});

        mockMvc.perform(get("/api/v1/administradores/{id}/foto-perfil", 1L))
            .andExpect(status().isOk())
            .andExpect(content().contentType(MediaType.IMAGE_JPEG))
            .andExpect(content().bytes(new byte[] {9, 8, 7}));
    }

    @Test
    @DisplayName("PUT /api/v1/administradores/{id}/contrasena responde 204")
    void cambiarContrasena_returnsNoContent() throws Exception {
        AdminPasswordChangeRequest request = new AdminPasswordChangeRequest();
        request.setCurrentPassword("oldPass1");
        request.setNewPassword("NewPass12");

        // Evita que el mock genere side effects o excepciones inesperadas.
        org.mockito.Mockito.doNothing().when(adminPasswordService).changePassword(2L, "oldPass1", "NewPass12");

        mockMvc.perform(put("/api/v1/administradores/{id}/contrasena", 2L)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsBytes(request)))
            .andExpect(status().isNoContent());

        verify(adminPasswordService).changePassword(2L, "oldPass1", "NewPass12");
    }

    private AdministradorDto adminDto() {
        AdministradorDto dto = new AdministradorDto();
        dto.setId(1L);
        dto.setNombre("Ana");
        dto.setApellido("Gomez");
        dto.setCorreo("admin@demo.com");
        dto.setTelefono("+56911111111");
        dto.setSueldo(1200000L);
        dto.setActivo(true);
        dto.setRol("administrador");
        return dto;
    }

    private AdministradorUpdateRequestDto updateRequest() {
        AdministradorUpdateRequestDto request = new AdministradorUpdateRequestDto();
        request.setNombre("Ana");
        request.setApellido("Gomez");
        return request;
    }
}
