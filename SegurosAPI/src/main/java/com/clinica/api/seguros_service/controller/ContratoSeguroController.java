package com.clinica.api.seguros_service.controller;

import com.clinica.api.seguros_service.model.ContratoSeguro;
import com.clinica.api.seguros_service.service.SeguroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seguros/contratos")
@Tag(name = "Contratos de Seguros", description = "Gestión de los contratos emitidos para los distintos planes, incluyendo consulta, creación y cancelación.")
public class ContratoSeguroController {

    private final SeguroService seguroService;

    public ContratoSeguroController(SeguroService seguroService) {
        this.seguroService = seguroService;
    }

    @GetMapping("/usuario/{idUsuario}")
    @Operation(
        summary = "Obtiene los contratos de seguro asociados a un usuario.",
        description = "Devuelve todos los contratos vigentes o históricos de un beneficiario. "
            + "Responde 204 cuando el usuario aún no ha contratado ningún plan."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Contratos encontrados para el usuario.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ContratoSeguro.class))
            )
        ),
        @ApiResponse(responseCode = "204", description = "El usuario no tiene contratos asociados.")
    })
    public ResponseEntity<List<ContratoSeguro>> listarContratosPorUsuario(@PathVariable("idUsuario") Long idUsuario) {
        List<ContratoSeguro> contratos = seguroService.findContratosByUsuario(idUsuario);
        if (contratos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/seguro/{idSeguro}")
    @Operation(
        summary = "Obtiene los contratos de un seguro específico.",
        description = "Permite inspeccionar quiénes han suscrito un plan concreto, útil para métricas comerciales. "
            + "Retorna 204 si el plan no tiene contratos."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Contratos encontrados para el plan.",
            content = @Content(
                mediaType = "application/json",
                array = @ArraySchema(schema = @Schema(implementation = ContratoSeguro.class))
            )
        ),
        @ApiResponse(responseCode = "204", description = "El seguro no tiene contratos registrados.")
    })
    public ResponseEntity<List<ContratoSeguro>> listarContratosPorSeguro(@PathVariable("idSeguro") Long idSeguro) {
        List<ContratoSeguro> contratos = seguroService.findContratosBySeguro(idSeguro);
        if (contratos.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(contratos);
    }

    @GetMapping("/{id_contrato}")
    @Operation(
        summary = "Obtiene un contrato por su ID.",
        description = "Entrega todos los campos del contrato solicitado, incluyendo beneficiarios y medios de contacto. "
            + "Si el ID es inválido responde 404."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Contrato encontrado y retornado.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContratoSeguro.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "No existe un contrato con el ID indicado.")
    })
    public ResponseEntity<ContratoSeguro> obtenerContrato(@PathVariable("id_contrato") Long id) {
        try {
            return ResponseEntity.ok(seguroService.findContratoById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo contrato de seguro.",
        description = "Registra la contratación de un plan, validando los campos obligatorios y devolviendo 201 con el contrato generado."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "201",
            description = "Contrato creado correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContratoSeguro.class)
            )
        )
    })
    public ResponseEntity<ContratoSeguro> crearContrato(@RequestBody ContratoSeguro contrato) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seguroService.createContrato(contrato));
    }

    @PostMapping("/{id}/cancelar")
    @Operation(
        summary = "Cancela un contrato de seguro.",
        description = "Marca el contrato como CANCELADO y fija la fecha de término. Si el contrato no existe, la respuesta es 404."
    )
    @ApiResponses({
        @ApiResponse(
            responseCode = "200",
            description = "Contrato cancelado correctamente.",
            content = @Content(
                mediaType = "application/json",
                schema = @Schema(implementation = ContratoSeguro.class)
            )
        ),
        @ApiResponse(responseCode = "404", description = "Contrato no encontrado para cancelar.")
    })
    public ResponseEntity<ContratoSeguro> cancelarContrato(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(seguroService.cancelarContrato(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
