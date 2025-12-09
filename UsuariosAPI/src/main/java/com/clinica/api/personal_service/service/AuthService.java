package com.clinica.api.personal_service.service;

import com.clinica.api.personal_service.dto.LoginRequest;
import com.clinica.api.personal_service.dto.LoginResponse;
import com.clinica.api.personal_service.security.CustomUserDetails;
import com.clinica.api.personal_service.security.JwtService;
import jakarta.transaction.Transactional;
import java.util.Objects;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class AuthService {

    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthService(AuthenticationManager authenticationManager, JwtService jwtService) {
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
    }

    public LoginResponse login(LoginRequest request) {
        LoginRequest safeRequest = Objects.requireNonNull(request, "Credenciales requeridas");
        UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
            safeRequest.getCorreo(),
            safeRequest.getContrasena()
        );
        CustomUserDetails principal = (CustomUserDetails) authenticationManager.authenticate(authRequest).getPrincipal();
        String token = jwtService.generateToken(principal);

        LoginResponse response = new LoginResponse();
        response.setUserId(principal.getUserId());
        response.setRole(principal.getRoleName());
        response.setDoctorId(principal.getDoctorId());
        response.setNombre(principal.getNombre());
        response.setApellido(principal.getApellido());
        response.setCorreo(principal.getUsername());
        response.setToken(token);
        return response;
    }
}
