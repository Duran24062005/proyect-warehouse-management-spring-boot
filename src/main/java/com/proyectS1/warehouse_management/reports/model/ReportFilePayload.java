package com.proyectS1.warehouse_management.reports.model;

import org.springframework.http.MediaType;

public record ReportFilePayload(
    byte[] content,
    MediaType mediaType,
    String filename
) {
}
