package com.clinica.api.personal_service.dto;

import java.time.LocalDate;

public class DoctorResponse {
    private Long id;
    private Integer tarifaConsulta;
    private Long sueldo;
    private Long bono;
    private UsuarioInfo usuario;
    
    // [CORRECTO] El campo va aquí, en la raíz de la respuesta
    private String especialidad; 

    public static class UsuarioInfo {
        private Long id;
        private String nombre;
        private String apellido;
        private LocalDate fechaNacimiento;
        private String correo;
        private String telefono;
        private String rol;

        // [CORRECCIÓN] Quitamos 'especialidad' de aquí, no pertenece a los datos personales

        public Long getId() { return id; }
        public void setId(Long id) { this.id = id; }
        public String getNombre() { return nombre; }
        public void setNombre(String nombre) { this.nombre = nombre; }
        public String getApellido() { return apellido; }
        public void setApellido(String apellido) { this.apellido = apellido; }
        public LocalDate getFechaNacimiento() { return fechaNacimiento; }
        public void setFechaNacimiento(LocalDate fechaNacimiento) { this.fechaNacimiento = fechaNacimiento; }
        public String getCorreo() { return correo; }
        public void setCorreo(String correo) { this.correo = correo; }
        public String getTelefono() { return telefono; }
        public void setTelefono(String telefono) { this.telefono = telefono; }
        public String getRol() { return rol; }
        public void setRol(String rol) { this.rol = rol; }
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getTarifaConsulta() { return tarifaConsulta; }
    public void setTarifaConsulta(Integer tarifaConsulta) { this.tarifaConsulta = tarifaConsulta; }
    public Long getSueldo() { return sueldo; }
    public void setSueldo(Long sueldo) { this.sueldo = sueldo; }
    public Long getBono() { return bono; }
    public void setBono(Long bono) { this.bono = bono; }
    public UsuarioInfo getUsuario() { return usuario; }
    public void setUsuario(UsuarioInfo usuario) { this.usuario = usuario; }

    // [CORRECCIÓN] ¡Faltaban estos! Sin esto, el controlador falla.
    public String getEspecialidad() { return especialidad; }
    public void setEspecialidad(String especialidad) { this.especialidad = especialidad; }
}
