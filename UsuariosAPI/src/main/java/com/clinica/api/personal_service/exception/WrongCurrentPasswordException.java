package com.clinica.api.personal_service.exception;

public class WrongCurrentPasswordException extends RuntimeException {
    public WrongCurrentPasswordException() {
        super("WRONG_CURRENT_PASSWORD");
    }
}
