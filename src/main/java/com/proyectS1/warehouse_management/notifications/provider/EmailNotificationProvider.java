package com.proyectS1.warehouse_management.notifications.provider;

import com.proyectS1.warehouse_management.notifications.model.EmailNotificationCommand;

public interface EmailNotificationProvider {
    String getProviderKey();
    void send(EmailNotificationCommand command);
}
