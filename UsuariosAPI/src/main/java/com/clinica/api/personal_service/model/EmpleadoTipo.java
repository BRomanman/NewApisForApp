package com.clinica.api.personal_service.model;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

public enum EmpleadoTipo {
    DOCTOR("Doctor"),
    ADMINISTRADOR("Administrador");

    private final String dbValue;

    EmpleadoTipo(String dbValue) {
        this.dbValue = dbValue;
    }

    public String getDbValue() {
        return dbValue;
    }

    public static EmpleadoTipo fromDbValue(String value) {
        for (EmpleadoTipo tipo : values()) {
            if (tipo.dbValue.equalsIgnoreCase(value)) {
                return tipo;
            }
        }
        throw new IllegalArgumentException("Tipo de empleado desconocido: " + value);
    }

    @Converter(autoApply = true)
    public static class ConverterImpl implements AttributeConverter<EmpleadoTipo, String> {

        @Override
        public String convertToDatabaseColumn(EmpleadoTipo attribute) {
            return attribute != null ? attribute.dbValue : null;
        }

        @Override
        public EmpleadoTipo convertToEntityAttribute(String dbData) {
            return dbData != null ? EmpleadoTipo.fromDbValue(dbData) : null;
        }
    }
}
