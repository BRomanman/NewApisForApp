package com.clinica.api.seguros_service.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Seguros API",
        version = "1.0",
        description = "Documentación de los endpoints para la administración de seguros médicos y contratos.\n"
            + "Códigos: 200/201 en respuestas correctas, 204 sin datos, 400 payload inválido, 404 inexistente, "
            + "409 conflictos (FK/duplicados) y 500 para fallos internos. Incluye esquemas Seguro, ContratoSeguro y Beneficiarios.",
        contact = @Contact(name = "Equipo Clínicas", email = "contacto@clinica.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig {
    // Configuración realizada mediante anotaciones.
}
