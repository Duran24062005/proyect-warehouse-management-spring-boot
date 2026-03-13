package com.proyectS1.warehouse_management.notifications.model;

public record EmailNotificationCommand(
    int userId,
    String recipient,
    String subject,
    String body
) {
}
