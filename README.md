# APIs moviles de la clinica

Monorepo con 4 microservicios Spring Boot (Java 21) para la app movil: Citas, Usuarios, Historial y Seguros. Cada servicio se ejecuta de forma independiente con su propio Gradle Wrapper y expone Swagger UI.

## Servicios
| Servicio     | Carpeta        | Puerto | Base URL                         | Swagger UI                                    |
| ------------ | -------------- | ------ | -------------------------------- | --------------------------------------------- |
| CitasAPI     | `CitasAPI`     | 8080   | http://localhost:8080/api/v1/citas | http://localhost:8080/swagger-ui/index.html   |
| UsuariosAPI  | `UsuariosAPI`  | 8082   | http://localhost:8082/api/v1       | http://localhost:8082/swagger-ui/index.html   |
| HistorialAPI | `HistorialAPI` | 8083   | http://localhost:8083/api/v1/historial | http://localhost:8083/swagger-ui/index.html |
| SegurosAPI   | `SegurosAPI`   | 8084   | http://localhost:8084/api/v1/seguros | http://localhost:8084/swagger-ui/index.html |

## Prerrequisitos
- JDK 21
- MySQL local; usuario `root` sin password por defecto (editable en `*/src/main/resources/application.properties`)
- Bases de datos: `citas_api`, `usuarios_api`, `historial_api`, `seguros_api`
- Puertos libres 8080, 8082, 8083, 8084

## Como ejecutar
1) Arranca MySQL y crea las BDs indicadas.
2) Desde la carpeta del servicio, ejecutar:
```bash
cd CitasAPI && ./gradlew bootRun
cd UsuariosAPI && ./gradlew bootRun
cd HistorialAPI && ./gradlew bootRun
cd SegurosAPI && ./gradlew bootRun
```
3) Explora la documentacion interactiva en Swagger UI (rutas en la tabla de servicios).

## Autenticacion y seguridad
- UsuariosAPI expone `POST /api/v1/auth/login` que devuelve un JWT con claims `userId`, `role`, `doctorId`, `nombre`, `apellido`, `correo`. Llaves configurables via `jwt.secret` y `jwt.expiration-ms` en `UsuariosAPI/src/main/resources/application.properties`.
- Configuracion actual de seguridad en UsuariosAPI permite todas las rutas (`permitAll`). El JWT se emite pero no se exige en los endpoints.
- Hash de contrasenas en UsuariosAPI: SHA-256 sin salt (ver `Sha256PasswordEncoder`); considera reemplazar por bcrypt/argon2 en entornos productivos.
- CORS habilitado para `http://localhost:5173` y `http://127.0.0.1:5173` en cada servicio.

## Endpoints destacados
- **CitasAPI** (`/api/v1/citas`): listar, detalle, filtros por usuario/doctor/fecha, verificar disponibilidad, reservar (`PUT /{id}/reservar`), cancelar (`PATCH /{id}/cancelar`), eliminar.
- **UsuariosAPI** (`/api/v1`): 
  - `/usuarios`: CRUD de usuarios no administradores y foto de perfil.
  - `/doctores`: alta/baja/actualizacion de doctores, foto de perfil.
  - `/especialidades`: catalogo y asignacion a doctores.
  - `/administradores`: datos basicos, foto y cambio de contrasena.
  - `/auth/login`: emision de JWT.
- **HistorialAPI** (`/api/v1/historial`): consultas por usuario, doctor o id de historial.
- **SegurosAPI** (`/api/v1/seguros`): CRUD de seguros; `/api/v1/seguros/contratos` para crear, listar y cancelar contratos.

## Datos y esquema
- Dialecto MySQL configurado en todos los servicios.
- `spring.jpa.hibernate.ddl-auto=update` habilitado: ajusta a tu estrategia de migraciones antes de usar en produccion.

## Pruebas
- Cada modulo incluye pruebas con H2 en `*/src/test/java`.
- Ejecuta los tests de un servicio con:
```bash
./gradlew test
```
(desde la carpeta del servicio)

## Subida de imagenes
- UsuariosAPI permite subir JPEG hasta 5 MB para fotos de usuario, doctor y administrador (`spring.servlet.multipart.*`).
