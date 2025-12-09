package com.clinica.api.personal_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Usuarios API",
        description = "Endpoints para gestionar usuarios, doctores y autenticación.",
        version = "1.0",
        contact = @Contact(name = "Equipo Clínicas", email = "contacto@clinica.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig {
    // Configuración provista mediante anotaciones.
}
