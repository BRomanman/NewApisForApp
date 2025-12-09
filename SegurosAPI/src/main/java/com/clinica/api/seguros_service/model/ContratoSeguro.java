package com.clinica.api.seguros_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import java.time.LocalDateTime;

@Entity
@Table(name = "contratoseguro")
public class ContratoSeguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_contrato")
    private Long id;

    @Column(name = "id_seguro", nullable = false)
    private Long idSeguro;

    @Column(name = "id_usuario", nullable = false)
    private Long idUsuario;

    @Column(name = "rut_beneficiarios", nullable = false, length = 100)
    private String rutBeneficiarios;

    @Column(name = "nombre_beneficiarios", nullable = false, length = 200)
    private String nombreBeneficiarios;

    @Column(name = "fecha_nacimiento_beneficiarios", nullable = false, length = 80)
    private String fechaNacimientoBeneficiarios;

    @Column(name = "correo_contacto", nullable = false, length = 100)
    private String correoContacto;

    @Column(name = "telefono_contacto", nullable = false, length = 15)
    private String telefonoContacto;

    @Column(name = "metodo_pago", nullable = false, length = 50)
    private String metodoPago;

    @Column(name = "fecha_contratacion", nullable = false)
    private LocalDateTime fechaContratacion;

    @Column(name = "fecha_cancelacion")
    private LocalDateTime fechaCancelacion;

    @Column(name = "estado", nullable = false, length = 20)
    private String estado = "ACTIVO";

    @PrePersist
    void prePersist() {
        if (fechaContratacion == null) {
            fechaContratacion = LocalDateTime.now();
        }
        if (estado == null) {
            estado = "ACTIVO";
        }
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Long getIdSeguro() {
        return idSeguro;
    }

    public void setIdSeguro(Long idSeguro) {
        this.idSeguro = idSeguro;
    }

    public Long getIdUsuario() {
        return idUsuario;
    }

    public void setIdUsuario(Long idUsuario) {
        this.idUsuario = idUsuario;
    }

    public String getRutBeneficiarios() {
        return rutBeneficiarios;
    }

    public void setRutBeneficiarios(String rutBeneficiarios) {
        this.rutBeneficiarios = rutBeneficiarios;
    }

    public String getNombreBeneficiarios() {
        return nombreBeneficiarios;
    }

    public void setNombreBeneficiarios(String nombreBeneficiarios) {
        this.nombreBeneficiarios = nombreBeneficiarios;
    }

    public String getFechaNacimientoBeneficiarios() {
        return fechaNacimientoBeneficiarios;
    }

    public void setFechaNacimientoBeneficiarios(String fechaNacimientoBeneficiarios) {
        this.fechaNacimientoBeneficiarios = fechaNacimientoBeneficiarios;
    }

    public String getCorreoContacto() {
        return correoContacto;
    }

    public void setCorreoContacto(String correoContacto) {
        this.correoContacto = correoContacto;
    }

    public String getTelefonoContacto() {
        return telefonoContacto;
    }

    public void setTelefonoContacto(String telefonoContacto) {
        this.telefonoContacto = telefonoContacto;
    }

    public String getMetodoPago() {
        return metodoPago;
    }

    public void setMetodoPago(String metodoPago) {
        this.metodoPago = metodoPago;
    }

    public LocalDateTime getFechaContratacion() {
        return fechaContratacion;
    }

    public void setFechaContratacion(LocalDateTime fechaContratacion) {
        this.fechaContratacion = fechaContratacion;
    }

    public LocalDateTime getFechaCancelacion() {
        return fechaCancelacion;
    }

    public void setFechaCancelacion(LocalDateTime fechaCancelacion) {
        this.fechaCancelacion = fechaCancelacion;
    }

    public String getEstado() {
        return estado;
    }

    public void setEstado(String estado) {
        this.estado = estado;
    }
}
