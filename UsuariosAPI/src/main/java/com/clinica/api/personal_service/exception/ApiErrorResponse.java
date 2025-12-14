package com.clinica.api.personal_service.exception;

/** Representa el cuerpo que se devuelve cuando hay un error en la peticion. **/
public class ApiErrorResponse {

    private String codigo;
    private String mensaje;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(String codigo, String mensaje) {
        this.codigo = codigo;
        this.mensaje = mensaje;
    }

    public String getCodigo() {
        return codigo;
    }

    public void setCodigo(String codigo) {
        this.codigo = codigo;
    }

    public String getMensaje() {
        return mensaje;
    }

    public void setMensaje(String mensaje) {
        this.mensaje = mensaje;
    }
}
