package com.proyectS1.warehouse_management.notifications.provider;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.proyectS1.warehouse_management.notifications.config.EmailNotificationProperties;
import com.proyectS1.warehouse_management.notifications.model.EmailNotificationCommand;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class FastApiEmailNotificationProvider implements EmailNotificationProvider {

    private final EmailNotificationProperties properties;
    private final RestClient.Builder restClientBuilder;

    @Override
    public String getProviderKey() {
        return "fastapi";
    }

    @Override
    public void send(EmailNotificationCommand command) {
        restClientBuilder
            .baseUrl(properties.getFastapi().getBaseUrl())
            .build()
            .post()
            .uri(properties.getFastapi().getSendPath())
            .contentType(MediaType.APPLICATION_JSON)
            .body(
                new FastApiEmailRequest(
                    command.userId(),
                    command.recipient(),
                    command.subject(),
                    command.templateName(),
                    command.templateData()
                )
            )
            .retrieve()
            .toBodilessEntity();
    }

    private record FastApiEmailRequest(
        int user_id,
        String recipient,
        String subject,
        String template_name,
        Object template_data
    ) {
    }
}
