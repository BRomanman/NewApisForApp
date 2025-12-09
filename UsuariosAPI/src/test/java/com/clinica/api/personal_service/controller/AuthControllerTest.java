package com.clinica.api.personal_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.personal_service.dto.LoginRequest;
import com.clinica.api.personal_service.dto.LoginResponse;
import com.clinica.api.personal_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(AuthController.class)
@AutoConfigureMockMvc(addFilters = false)
class AuthControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AuthService authService;

    @Test
    @DisplayName("POST /api/v1/auth/login responde 200 con los datos del usuario")
    void login_returnsOk() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenReturn(loginResponse());

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest())))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.role").value("doctor"))
            .andExpect(jsonPath("$.doctorId").value(5L))
            .andExpect(jsonPath("$.token").value("token.value"));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login responde 401 cuando las credenciales son inválidas")
    void login_returnsUnauthorized() throws Exception {
        when(authService.login(any(LoginRequest.class))).thenThrow(new BadCredentialsException("credenciales"));

        mockMvc.perform(post("/api/v1/auth/login")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(loginRequest())))
            .andExpect(status().isUnauthorized());
    }

    private LoginRequest loginRequest() {
        LoginRequest request = new LoginRequest();
        request.setCorreo("user@demo.com");
        request.setContrasena("123");
        return request;
    }

    private LoginResponse loginResponse() {
        LoginResponse response = new LoginResponse();
        response.setUserId(2L);
        response.setRole("doctor");
        response.setDoctorId(5L);
        response.setNombre("Ana");
        response.setApellido("Gómez");
        response.setCorreo("user@demo.com");
        response.setToken("token.value");
        return response;
    }
}
