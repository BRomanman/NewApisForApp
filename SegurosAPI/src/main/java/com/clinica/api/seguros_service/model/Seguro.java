package com.clinica.api.seguros_service.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

@Entity
@Table(name = "Seguros")
public class Seguro {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id_seguro")
    private Long id;

    @Column(name = "nombre_seguro", nullable = false, length = 100)
    private String nombreSeguro;

    @Column(length = 200)
    private String descripcion;

    @Column(nullable = false)
    private Integer valor;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getNombreSeguro() {
        return nombreSeguro;
    }

    public void setNombreSeguro(String nombreSeguro) {
        this.nombreSeguro = nombreSeguro;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public void setDescripcion(String descripcion) {
        this.descripcion = descripcion;
    }

    public Integer getValor() {
        return valor;
    }

    public void setValor(Integer valor) {
        this.valor = valor;
    }
}
