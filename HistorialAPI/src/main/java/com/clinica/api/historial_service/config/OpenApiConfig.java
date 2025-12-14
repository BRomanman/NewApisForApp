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
        description = "Servicio REST para exponer historiales clínicos de usuarios y doctores.\n"
            + "Códigos: 200 cuando existen registros, 204 sin historiales, 404 si el ID no existe y 500 para fallos internos. "
            + "Los esquemas incluyen fechas, horas y diagnósticos/observaciones.",
        contact = @Contact(name = "Equipo Clínicas", email = "contacto@clinica.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig {
    // La configuración se realiza únicamente con las anotaciones.
}
