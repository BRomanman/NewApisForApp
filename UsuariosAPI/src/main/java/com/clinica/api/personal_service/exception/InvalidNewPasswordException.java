package com.clinica.api.personal_service.exception;

public class InvalidNewPasswordException extends RuntimeException {
    public InvalidNewPasswordException(String message) {
        super(message);
    }
}
