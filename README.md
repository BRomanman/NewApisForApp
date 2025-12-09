# APIs móviles de la clínica

Este repositorio contiene los microservicios que usa la app móvil de la clínica para administrar usuarios, doctores, historiales médicos, citas y seguros. Cada servicio es independiente, pero comparten un estilo REST y convenciones de respuesta coherentes (200 para lecturas exitosas, 201 en creaciones, 204 cuando no hay contenido y 4xx ante errores de negocio).

> Los ejemplos a continuación usan `curl` contra puertos de desarrollo sugeridos. Ajusta host/puerto según el servicio que estés ejecutando.

## Servicios disponibles

| Servicio     | Carpeta        | Puerto | Base URL sugerida                        | Swagger UI                                     | Funcionalidad principal        |
| ------------ | -------------- | ------ | ---------------------------------------- | ---------------------------------------------- | ------------------------------ |
| HistorialAPI | `HistorialAPI` | `8083` | `http://localhost:8083/api/v1/historial` | [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html) | Consulta historiales clínicos |
| CitasAPI     | `CitasAPI`     | `8080` | `http://localhost:8080/api/v1/citas`     | [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html) | CRUD de citas médicas         |
| SegurosAPI   | `SegurosAPI`   | `8081` | `http://localhost:8081/api/v1/seguros`   | [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html) | Gestión de seguros médicos   |
| UsuariosAPI  | `UsuariosAPI`  | `8082` | `http://localhost:8082/api/v1`           | [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html) | Usuarios, doctores y login     |

---

## HistorialAPI

Permite consultar historiales médicos ya registrados. No expone creación/edición porque esos datos provienen de otros flujos clínicos.

**Base URL:** `http://localhost:8083/api/v1/historial`  
**Swagger UI:** [http://localhost:8083/swagger-ui/index.html](http://localhost:8083/swagger-ui/index.html)

**Panorama funcional**
- Expone únicamente consultas (`GET /usuario/{usuarioId}` y `GET /{id}`) sobre la entidad `Historial`, que serializa la fecha en formato `yyyy-MM-dd` y anida los datos mínimos del paciente.
- `HistorialService` encapsula las consultas a la base de datos (`findHistorialesByUsuarioId`, `findHistorialById`) y lanza `EntityNotFoundException` para mapearlo a 404 cuando corresponde.
- Las respuestas devuelven `204 No Content` cuando no existen antecedentes, lo que simplifica la lógica de la app móvil al diferenciar entre "sin historial" y "usuario inexistente".

**Testing**
- `HistorialAPI/src/test/java/com/clinica/api/historial_service/controller/HistorialControllerTest.java` cubre ambos endpoints con `@WebMvcTest`, validando respuestas 200, 204 y 404 más el shape del JSON.
- `HistorialAPI/src/test/java/com/clinica/api/historial_service/service/HistorialServiceTest.java` usa Mockito para verificar que la capa de servicio delega correctamente en el repositorio y lanza las excepciones esperadas.

### `GET /usuario/{usuarioId}` – Historiales por usuario

Devuelve todos los historiales asociados a un paciente. Responde `204 No Content` cuando la lista está vacía.

```bash
curl -X GET "http://localhost:8083/api/v1/historial/usuario/15"
```

**Respuesta 200**

```json
[
  {
    "id": 71,
    "usuario": {
      "id": 15,
      "nombre": "Laura",
      "apellido": "Rivas",
      "correo": "laura.rivas@example.com"
    },
    "fechaConsulta": "2024-09-10",
    "diagnostico": "Hipertensión controlada",
    "observaciones": "Paciente estable, control en 3 meses"
  }
]
```

### `GET /{id}` – Historial específico

Consulta un historial concreto; si no existe, se responde 404.

```bash
curl -X GET "http://localhost:8083/api/v1/historial/98"
```

**Respuesta 200**

```json
{
  "id": 98,
  "usuario": {
    "id": 22,
    "nombre": "Pablo",
    "apellido": "Maldonado"
  },
  "fechaConsulta": "2024-11-01",
  "diagnostico": "Gastritis",
  "observaciones": "Controlar dieta y recetar inhibidor de bomba"
}
```

---

## CitasAPI

Gestiona el ciclo completo de las citas médicas. Trabaja con entidades `Cita`, `Usuario` y `Doctor`.

**Base URL:** `http://localhost:8080/api/v1/citas`  
**Swagger UI:** [http://localhost:8080/swagger-ui/index.html](http://localhost:8080/swagger-ui/index.html)

**Panorama funcional**
- El modelo `Cita` utiliza asociaciones JPA con `Usuario` y `Doctor`, pero las expone como `idUsuario`/`idDoctor` vía `@JsonProperty` para que el cliente móvil solo trabaje con identificadores.
- La capa de servicio (`CitaService`) crea citas en estado `CONFIRMADA` cuando el `id` llega nulo, valida que exista antes de actualizar/eliminar y calcula las "próximas" citas comparando `fechaCita` con `now()`.
- Todas las colecciones retornan `204 No Content` cuando están vacías, reforzando una semántica consistente con el resto de microservicios.

**Testing**
- `CitasAPI/src/test/java/citas_service_nuevo/controller/CitaControllerTest.java` valida con `MockMvc` los flujos felices y los errores (404 en búsquedas/updates, 204 en listas vacías, etc.).
- `CitasAPI/src/test/java/citas_service_nuevo/service/CitaServiceTest.java` usa Mockito para comprobar reglas de negocio como el estado por defecto, las excepciones en `deleteById` y el uso de `LocalDateTime.now()` para filtrar próximas citas.

### `GET /` – Listar todas las citas

```bash
curl -X GET "http://localhost:8080/api/v1/citas"
```

**Respuesta 200**

```json
[
  {
    "id": 42,
    "fechaCita": "2025-02-10T09:30:00",
    "estado": "CONFIRMADA",
    "idUsuario": 15,
    "idDoctor": 3,
    "idConsulta": 120
  }
]
```

Si no hay registros, devuelve 204.

### `GET /{id}` – Detalle de cita

```bash
curl -X GET "http://localhost:8080/api/v1/citas/42"
```

**Respuesta 200**

```json
{
  "id": 42,
  "fechaCita": "2025-02-10T09:30:00",
  "estado": "CONFIRMADA",
  "idUsuario": 15,
  "idDoctor": 3,
  "idConsulta": 120
}
```

### `GET /usuario/{idUsuario}` – Citas de un usuario

```bash
curl -X GET "http://localhost:8080/api/v1/citas/usuario/15"
```

**Respuesta 200** (lista filtrada) o 204 si el paciente no tiene citas.

### `POST /` – Crear cita

Si el `id` llega nulo, la capa de servicio marca el estado como `CONFIRMADA` por defecto.

```bash
curl -X POST "http://localhost:8080/api/v1/citas" \
  -H "Content-Type: application/json" \
  -d '{
        "fechaCita": "2025-03-02T11:00:00",
        "estado": "PENDIENTE",
        "idUsuario": 15,
        "idDoctor": 3,
        "idConsulta": 128
      }'
```

**Respuesta 201**

```json
{
  "id": 84,
  "fechaCita": "2025-03-02T11:00:00",
  "estado": "CONFIRMADA",
  "idUsuario": 15,
  "idDoctor": 3,
  "idConsulta": 128
}
```

### `PUT /{id}` – Actualizar cita existente

```bash
curl -X PUT "http://localhost:8080/api/v1/citas/84" \
  -H "Content-Type: application/json" \
  -d '{
        "fechaCita": "2025-03-05T08:30:00",
        "idDoctor": 4,
        "idConsulta": 140
      }'
```

**Respuesta 200** devuelve la cita con los cambios aplicados.

### `DELETE /{id}` – Eliminar cita

```bash
curl -X DELETE "http://localhost:8080/api/v1/citas/84"
```

**Respuesta 204** si la eliminación fue exitosa; 404 si el id no existe.

### `GET /usuario/{idUsuario}/proximas` – Próximas citas

Obtiene solo las que tienen `fechaCita` mayor a `now()`.

```bash
curl -X GET "http://localhost:8080/api/v1/citas/usuario/15/proximas"
```

**Respuesta 200**: misma estructura que la lista general.

---

## SegurosAPI

Ofrece altas, consultas, actualizaciones, cancelaciones y bajas lógicas para seguros médicos. Las reglas de negocio (nombres únicos por usuario, evitar cancelaciones repetidas, etc.) se concentraron en `SeguroService`, que lanza `EntityNotFoundException` cuando corresponde para que el controlador devuelva 404/204 coherentes.

**Base URL:** `http://localhost:8081/api/v1/seguros`  
**Swagger UI:** [http://localhost:8081/swagger-ui/index.html](http://localhost:8081/swagger-ui/index.html)

**Panorama funcional**
- El modelo `Seguro` mantiene el estado (`ACTIVO`/`CANCELADO`), el `usuarioId` y marcas de tiempo (`fechaCreacion`, `fechaCancelacion`) usando `@PrePersist` para completar campos automáticamente.
- `SeguroService.create` limpia el identificador entrante, fija `estado=ACTIVO` y quita la fecha de cancelación; `cancel` marca la fecha actual y opcionalmente agrega el motivo en la descripción, mientras que `delete` requiere que el registro exista antes de borrarlo.
- `PATCH /{id}/cancelacion` se usa para baja lógica y `DELETE /{id}` elimina definitivamente; la API devuelve 204 cuando las colecciones vienen vacías para mantener consistencia con el resto del ecosistema.

**Testing**
- `SegurosAPI/src/test/java/com/clinica/api/seguros_service/controller/SeguroControllerTest.java` verifica cada endpoint con `MockMvc`, cubriendo casos felices y errores 404, además de probar el `PATCH` con motivo opcional.
- `SegurosAPI/src/test/java/com/clinica/api/seguros_service/service/SeguroServiceTest.java` valida reglas como el estado por defecto, la mutación de descripción al cancelar y las excepciones por registros inexistentes.

### `POST /` – Tomar un seguro

```bash
curl -X POST "http://localhost:8081/api/v1/seguros" \
  -H "Content-Type: application/json" \
  -d '{
        "nombreSeguro": "Plan Platino",
        "descripcion": "Cobertura dental + oftalmológica",
        "usuarioId": 15
      }'
```

**Respuesta 201**

```json
{
  "id": 12,
  "nombreSeguro": "Plan Platino",
  "descripcion": "Cobertura dental + oftalmológica",
  "estado": "ACTIVO",
  "fechaCreacion": "2024-11-13T18:30:12.120987",
  "fechaCancelacion": null,
  "usuarioId": 15,
  "usuarioNombre": "Laura Rivas"
}
```

### `GET /` – Listar seguros

```bash
curl -X GET "http://localhost:8081/api/v1/seguros"
```

Devuelve un arreglo de `SeguroResponse` o 204 si no existen registros.

### `GET /usuario/{usuarioId}` – Seguros por titular

```bash
curl -X GET "http://localhost:8081/api/v1/seguros/usuario/15"
```

Responde 200 con la lista filtrada o 204 si el usuario aún no contrata seguros.

### `GET /{id}` – Seguro específico

```bash
curl -X GET "http://localhost:8081/api/v1/seguros/12"
```

Devuelve el `SeguroResponse` o 404 si no se encuentra.

### `PUT /{id}` – Actualizar seguro

Puede cambiar nombre, descripción o transferir el seguro a otro usuario siempre que no rompa la regla de nombres únicos por titular.

```bash
curl -X PUT "http://localhost:8081/api/v1/seguros/12" \
  -H "Content-Type: application/json" \
  -d '{
        "nombreSeguro": "Plan Platino Plus",
        "descripcion": "Incluye telemedicina",
        "usuarioId": 16
      }'
```

**Respuesta 200** con el seguro actualizado.

### `PATCH /{id}/cancelacion` – Cancelar seguro

El cuerpo es opcional; si se envía `motivo`, se agrega a la descripción como bitácora.

```bash
curl -X PATCH "http://localhost:8081/api/v1/seguros/12/cancelacion" \
  -H "Content-Type: application/json" \
  -d '{ "motivo": "Paciente migra a otro plan" }'
```

**Respuesta 200** muestra `estado="CANCELADO"` y `fechaCancelacion`. Si ya estaba cancelado, devuelve 409.

### `DELETE /{id}` – Borrar seguro

```bash
curl -X DELETE "http://localhost:8081/api/v1/seguros/12"
```

Elimina definitivamente y responde 204. Si el id no existe, el handler devuelve 404.

---

## UsuariosAPI

Expone controladores para usuarios finales (`/api/v1/usuarios`), doctores (`/api/v1/doctores`) y autenticación (`/api/v1/auth`). La capa de servicio bloquea operaciones sobre usuarios con rol `administrador` para proteger cuentas maestras.

**Base URL:** `http://localhost:8082/api/v1`  
**Swagger UI:** [http://localhost:8082/swagger-ui/index.html](http://localhost:8082/swagger-ui/index.html)

**Panorama funcional**
- `UsuarioController` expone únicamente usuarios no administradores y devuelve `UsuarioResponse`, que agrega datos del doctor asociado (si existe) consultando `DoctorRepository`.
- `DoctorController` opera sobre `PersonalService`, el cual implementa bajas lógicas (`activo=false`) y autocompleta `activo=true` en altas para mantener consistencia con el front.
- `AuthController` recibe `LoginRequest`, delega la validación de credenciales a `UsuarioService.login` (que consulta correo, compara contraseña y adjunta `doctorId` si corresponde) y responde 200/401 según resultado.

**Testing**
- `UsuariosAPI/src/test/java/com/clinica/api/personal_service/controller/UsuarioControllerTest.java`, `DoctorControllerTest.java` y `AuthControllerTest.java` cubren los endpoints con `@WebMvcTest`, asegurando códigos 200/201/204/404/401 y shape de las respuestas.
- `UsuariosAPI/src/test/java/com/clinica/api/personal_service/service/UsuarioServiceTest.java` y `PersonalServiceTest.java` usan Mockito para verificar reglas como el filtro de administradores, la restitución de `doctorId` en login y la baja lógica de doctores.

### Gestión de usuarios (`/api/v1/usuarios`)

#### `GET /` – Listar usuarios visibles

```bash
curl -X GET "http://localhost:8082/api/v1/usuarios"
```

Responde con `UsuarioResponse` (sin administradores) o 204.

#### `GET /{id}` – Usuario por id

```bash
curl -X GET "http://localhost:8082/api/v1/usuarios/25"
```

Devuelve el usuario si no es administrador; caso contrario responde 404 para evitar filtraciones.

#### `POST /` – Crear usuario

```bash
curl -X POST "http://localhost:8082/api/v1/usuarios" \
  -H "Content-Type: application/json" \
  -d '{
        "nombre": "Ana",
        "apellido": "Guzmán",
        "fechaNacimiento": "1992-05-01T00:00:00",
        "correo": "ana.guzman@example.com",
        "telefono": "+56911112222",
        "contrasena": "clave123",
        "rol": { "id": 2 }
      }'
```

**Respuesta 201**

```json
{
  "id": 25,
  "nombre": "Ana",
  "apellido": "Guzmán",
  "fechaNacimiento": "1992-05-01T00:00:00",
  "correo": "ana.guzman@example.com",
  "telefono": "+56911112222",
  "rol": "paciente",
  "doctor": null
}
```

#### `PUT /{id}` – Actualizar usuario

```bash
curl -X PUT "http://localhost:8082/api/v1/usuarios/25" \
  -H "Content-Type: application/json" \
  -d '{
        "nombre": "Ana Carolina",
        "apellido": "Guzmán",
        "fechaNacimiento": "1992-05-01T00:00:00",
        "correo": "aca.guzman@example.com",
        "telefono": "+56999998888",
        "contrasena": "clave456",
        "rol": { "id": 3 }
      }'
```

Responde 200 con la versión actualizada, siempre que el usuario objetivo y el payload no correspondan a un administrador.

#### `DELETE /{id}` – Eliminar usuario

```bash
curl -X DELETE "http://localhost:8082/api/v1/usuarios/25"
```

Responde 204 si se elimina y 404 si el id no existe o estaba reservado para administración.

### Gestión de doctores (`/api/v1/doctores`)

#### `GET /` – Listar doctores activos

```bash
curl -X GET "http://localhost:8082/api/v1/doctores"
```

Devuelve un arreglo de `DoctorResponse` o 204 si no hay doctores activos (la eliminación se maneja como baja lógica).

#### `GET /{id}` – Detalle de doctor

```bash
curl -X GET "http://localhost:8082/api/v1/doctores/5"
```

Obtiene sueldo, bono, tarifa y datos básicos del usuario asociado.

#### `POST /` – Crear doctor

```bash
curl -X POST "http://localhost:8082/api/v1/doctores" \
  -H "Content-Type: application/json" \
  -d '{
        "tarifaConsulta": 35000,
        "sueldo": 1200000,
        "bono": 200000,
        "usuario": { "id": 18 }
      }'
```

**Respuesta 201** devuelve el `DoctorResponse` resultante.

#### `PUT /{id}` – Actualizar doctor

```bash
curl -X PUT "http://localhost:8082/api/v1/doctores/5" \
  -H "Content-Type: application/json" \
  -d '{
        "tarifaConsulta": 38000,
        "sueldo": 1250000,
        "bono": 250000,
        "usuario": { "id": 18 }
      }'
```

Responde 200 con los nuevos valores. Si el doctor no existe o fue dado de baja, responde 404.

#### `DELETE /{id}` – Baja lógica de doctor

```bash
curl -X DELETE "http://localhost:8082/api/v1/doctores/5"
```

Marca `activo=false` y responde 204.

### Autenticación (`/api/v1/auth`)

#### `POST /login` – Iniciar sesión

```bash
curl -X POST "http://localhost:8082/api/v1/auth/login" \
  -H "Content-Type: application/json" \
  -d '{
        "correo": "ana.guzman@example.com",
        "contrasena": "clave456"
      }'
```

**Respuesta 200**

```json
{
  "userId": 25,
  "role": "paciente",
  "doctorId": null,
  "nombre": "Ana Carolina",
  "apellido": "Guzmán",
  "correo": "ana.guzman@example.com"
}
```

Si las credenciales son incorrectas, devuelve 401.

---

## Estrategia de pruebas automatizadas

- Todos los microservicios comparten la misma pila (`spring-boot-starter-test`, `JUnit 5`, `Mockito`, `MockMvc` y `H2` como runtime in-memory), lo que permite ejecutar las pruebas con `./gradlew test` desde cada carpeta sin dependencias externas de base de datos.
- **HistorialAPI:** combina `WebMvcTest` (para validar respuestas 200/204/404) con pruebas unitarias de servicio que verifican interacción con `HistorialRepository`.
- **CitasAPI:** las pruebas de controlador garantizan que cada endpoint devuelva el código correcto y que el JSON respete los campos `idUsuario/idDoctor`, mientras que las de servicio cubren reglas como estado por defecto y validación de existencia.
- **SegurosAPI:** simula escenarios de cancelación, bajas definitivas y validaciones de entrada tanto en la capa web como en servicio, asegurando que la descripción se actualice con el motivo de cancelación.
- **UsuariosAPI:** los tests separan responsabilidades entre controladores (`Usuarios`, `Doctores`, `Auth`) y servicios (`UsuarioService`, `PersonalService`) para verificar el bloqueo de administradores, la propagación de `doctorId` y la baja lógica de doctores.
