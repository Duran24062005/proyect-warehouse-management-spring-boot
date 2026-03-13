package com.proyectS1.warehouse_management.notifications.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@Component
@ConfigurationProperties(prefix = "app.notifications.email")
public class EmailNotificationProperties {

    private boolean enabled = true;
    private boolean failOnError = false;
    private String provider = "fastapi";
    private final FastApi fastapi = new FastApi();

    @Getter
    @Setter
    public static class FastApi {
        private String baseUrl = "http://127.0.0.1:8000";
        private String sendPath = "/emails/send";
    }
}
