package com.clinica.api.personal_service.controller;

import com.clinica.api.personal_service.dto.LoginRequest;
import com.clinica.api.personal_service.dto.LoginResponse;
import com.clinica.api.personal_service.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.Objects;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/auth")
@Tag(name = "Autenticación", description = "Endpoints para validar credenciales y recuperar información básica de la sesión.")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/login")
    @Operation(
        summary = "Valida las credenciales del usuario y retorna su rol.",
        description = "Verifica correo y contraseña contra la base de datos y entrega información del usuario y su rol para construir la sesión. "
            + "Responde 401 cuando las credenciales no son válidas."
    )
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginRequest safeRequest = Objects.requireNonNull(request, "Credenciales requeridas");
        try {
            LoginResponse resp = authService.login(safeRequest);
            return ResponseEntity.ok(resp);
        } catch (AuthenticationException ex) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
