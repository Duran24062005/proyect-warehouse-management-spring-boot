package com.proyectS1.warehouse_management.model.enums;

public enum ReportFormat {
    JSON,
    CSV,
    TXT,
    PDF,
    IMG;

    public static ReportFormat fromToken(String value) {
        if (value == null || value.isBlank()) {
            return JSON;
        }

        try {
            return ReportFormat.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported report format: " + value, exception);
        }
    }
}
