package com.clinica.api.personal_service.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.clinica.api.personal_service.dto.LoginRequest;
import com.clinica.api.personal_service.dto.LoginResponse;
import com.clinica.api.personal_service.exception.PersonalServiceExceptionHandler;
import com.clinica.api.personal_service.service.AuthService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

@ExtendWith(MockitoExtension.class)
class AuthControllerTest {

    private MockMvc mockMvc;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController)
            .setControllerAdvice(new PersonalServiceExceptionHandler())
            .build();
    }

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
