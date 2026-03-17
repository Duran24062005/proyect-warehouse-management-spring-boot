package com.proyectS1.warehouse_management.audit;

import java.util.Map;

public record AuditSnapshot(
    Map<String, Object> payload,
    String passwordFingerprint
) {
}
