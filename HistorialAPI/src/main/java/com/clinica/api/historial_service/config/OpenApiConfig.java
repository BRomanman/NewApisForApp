package com.clinica.api.historial_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Historial API",
        version = "1.0",
        description = "Servicio REST para exponer el historial clínico de los usuarios.",
        contact = @Contact(name = "Equipo Clínicas", email = "contacto@clinica.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig {
    // La configuración se realiza únicamente con las anotaciones.
}
