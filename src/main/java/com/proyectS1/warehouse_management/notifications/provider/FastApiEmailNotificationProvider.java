package com.proyectS1.warehouse_management.notifications.provider;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
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
        MultiValueMap<String, Object> formData = new LinkedMultiValueMap<>();
        formData.add("user_id", String.valueOf(command.userId()));
        formData.add("recipient", command.recipient());
        formData.add("subject", command.subject());
        formData.add("body", command.body());

        restClientBuilder
            .baseUrl(properties.getFastapi().getBaseUrl())
            .build()
            .post()
            .uri(properties.getFastapi().getSendPath())
            .contentType(MediaType.MULTIPART_FORM_DATA)
            .body(formData)
            .retrieve()
            .toBodilessEntity();
    }
}
