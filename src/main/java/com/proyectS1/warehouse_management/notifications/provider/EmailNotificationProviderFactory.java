package com.proyectS1.warehouse_management.notifications.provider;

import java.util.List;

import org.springframework.stereotype.Component;

import com.proyectS1.warehouse_management.notifications.config.EmailNotificationProperties;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class EmailNotificationProviderFactory {

    private final List<EmailNotificationProvider> providers;
    private final EmailNotificationProperties properties;

    public EmailNotificationProvider getProvider() {
        return providers.stream()
            .filter(provider -> provider.getProviderKey().equalsIgnoreCase(properties.getProvider()))
            .findFirst()
            .orElseThrow(() -> new IllegalStateException(
                "No email notification provider configured for key: " + properties.getProvider()
            ));
    }
}
