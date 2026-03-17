package com.proyectS1.warehouse_management.model.enums;

public enum ReportType {
    MOVEMENTS,
    PRODUCTS,
    WAREHOUSES;

    public static ReportType fromToken(String value) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException("Report type is required");
        }

        try {
            return ReportType.valueOf(value.trim().toUpperCase());
        } catch (IllegalArgumentException exception) {
            throw new IllegalArgumentException("Unsupported report type: " + value, exception);
        }
    }
}
