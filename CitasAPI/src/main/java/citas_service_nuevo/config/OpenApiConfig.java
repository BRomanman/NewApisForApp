package citas_service_nuevo.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "Citas API",
        description = "Catálogo de endpoints para administrar las citas médicas.",
        version = "1.0",
        contact = @Contact(name = "Equipo Clínicas", email = "contacto@clinica.com")
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local")
    }
)
public class OpenApiConfig {
    // Configuración basada en anotaciones
}
