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
        description = "Endpoints para autenticación, pacientes, doctores, administradores y especialidades.\n"
            + "Códigos típicos: 200/201 para respuestas exitosas, 204 cuando no hay contenido, 400 por payload inválido, "
            + "401 credenciales erróneas, 404 recurso no encontrado, 409 conflictos de datos y 500 errores inesperados.\n"
            + "Incluye subida/descarga de fotos de perfil (image/jpeg) y DTOs de dominio para swagger.",
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
