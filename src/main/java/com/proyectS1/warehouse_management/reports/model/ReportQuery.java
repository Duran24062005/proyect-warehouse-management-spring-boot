package com.proyectS1.warehouse_management.reports.model;

import com.proyectS1.warehouse_management.model.enums.MovementType;
import com.proyectS1.warehouse_management.model.enums.ReportFormat;
import com.proyectS1.warehouse_management.model.enums.ReportType;

public record ReportQuery(
    ReportType type,
    ReportFormat format,
    Long productId,
    Long warehouseId,
    MovementType movementType,
    String category,
    Long managerUserId
) {
}
