package com.proyectS1.warehouse_management.reports.model;

import com.proyectS1.warehouse_management.model.enums.MovementType;

public record MovementAnalyticsQuery(
    String window,
    Long productId,
    Long warehouseId,
    MovementType movementType
) {
}
