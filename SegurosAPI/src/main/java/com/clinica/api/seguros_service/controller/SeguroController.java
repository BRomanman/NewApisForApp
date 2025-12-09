package com.clinica.api.seguros_service.controller;

import com.clinica.api.seguros_service.model.Seguro;
import com.clinica.api.seguros_service.service.SeguroService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.persistence.EntityNotFoundException;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/seguros")
@Tag(name = "Seguros", description = "Operaciones para administrar los planes de seguros médicos ofrecidos por la clínica.")
public class SeguroController {

    private final SeguroService seguroService;

    public SeguroController(SeguroService seguroService) {
        this.seguroService = seguroService;
    }

    @GetMapping
    @Operation(
        summary = "Lista todos los seguros disponibles.",
        description = "Devuelve el catálogo vigente de seguros con su información esencial. "
            + "Responde 204 No Content cuando no existen planes configurados."
    )
    public ResponseEntity<List<Seguro>> listarSeguros() {
        List<Seguro> seguros = seguroService.findAllSeguros();
        if (seguros.isEmpty()) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(seguros);
    }

    @GetMapping("/{id}")
    @Operation(
        summary = "Obtiene los detalles de un seguro por su ID.",
        description = "Permite inspeccionar un plan particular y responde 404 cuando el identificador no pertenece a ningún seguro."
    )
    public ResponseEntity<Seguro> obtenerSeguro(@PathVariable("id") Long id) {
        try {
            return ResponseEntity.ok(seguroService.findSeguroById(id));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @PostMapping
    @Operation(
        summary = "Crea un nuevo seguro.",
        description = "Registra un plan de seguro con la información proporcionada y devuelve 201 con el recurso almacenado."
    )
    public ResponseEntity<Seguro> crearSeguro(@RequestBody Seguro seguro) {
        return ResponseEntity.status(HttpStatus.CREATED).body(seguroService.createSeguro(seguro));
    }

    @PutMapping("/{id}")
    @Operation(
        summary = "Actualiza la información del seguro.",
        description = "Reemplaza los atributos fundamentales del plan (nombre, descripción y valor) para mantenerlo al día. "
            + "Cuando el ID no existe se responde 404."
    )
    public ResponseEntity<Seguro> actualizarSeguro(
        @PathVariable("id") Long id,
        @RequestBody Seguro seguro
    ) {
        try {
            return ResponseEntity.ok(seguroService.updateSeguro(id, seguro));
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }

    @DeleteMapping("/{id}")
    @Operation(
        summary = "Elimina un seguro.",
        description = "Borra definitivamente el plan indicado y responde 204 al completar la operación. "
            + "Un identificador inexistente provoca una respuesta 404."
    )
    public ResponseEntity<Void> eliminarSeguro(@PathVariable("id") Long id) {
        try {
            seguroService.deleteSeguro(id);
            return ResponseEntity.noContent().build();
        } catch (EntityNotFoundException ex) {
            return ResponseEntity.notFound().build();
        }
    }
}
