package com.proyectS1.warehouse_management.notifications.model;

import java.util.Map;

public record EmailNotificationCommand(
    int userId,
    String recipient,
    String subject,
    String templateName,
    Map<String, Object> templateData
) {
}
