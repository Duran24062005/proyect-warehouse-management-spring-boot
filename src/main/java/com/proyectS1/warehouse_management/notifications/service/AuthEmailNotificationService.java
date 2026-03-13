package com.proyectS1.warehouse_management.notifications.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import static org.springframework.http.HttpStatus.BAD_GATEWAY;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.model.AppUser;
import com.proyectS1.warehouse_management.notifications.config.EmailNotificationProperties;
import com.proyectS1.warehouse_management.notifications.model.EmailNotificationCommand;
import com.proyectS1.warehouse_management.notifications.provider.EmailNotificationProviderFactory;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AuthEmailNotificationService {

    private static final Logger LOGGER = LoggerFactory.getLogger(AuthEmailNotificationService.class);
    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private final EmailNotificationProperties properties;
    private final EmailNotificationProviderFactory providerFactory;

    public void sendRegistrationEmail(AppUser user) {
        send(
            buildCommand(
                user,
                "Bienvenido a logiTrack",
                "auth-register.html",
                Map.of(
                    "nombre", user.getFirstName(),
                    "empresa", "logiTrack",
                    "correo", user.getEmail(),
                    "fecha", DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                    "evento", "registro"
                )
            ),
            "registration"
        );
    }

    public void sendLoginEmail(AppUser user) {
        send(
            buildCommand(
                user,
                "Nuevo acceso a logiTrack",
                "auth-login.html",
                Map.of(
                    "nombre", user.getFirstName(),
                    "empresa", "logiTrack",
                    "correo", user.getEmail(),
                    "fecha", DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                    "evento", "login"
                )
            ),
            "login"
        );
    }

    private EmailNotificationCommand buildCommand(
        AppUser user,
        String subject,
        String templateName,
        Map<String, Object> templateData
    ) {
        return new EmailNotificationCommand(
            3,
            user.getEmail(),
            subject,
            templateName,
            templateData
        );
    }

    private void send(EmailNotificationCommand command, String context) {
        if (!properties.isEnabled()) {
            return;
        }

        try {
            providerFactory.getProvider().send(command);
        } catch (Exception exception) {
            LOGGER.error("Email notification failed during {} flow for recipient {}", context, command.recipient(), exception);
            if (properties.isFailOnError()) {
                throw new ResponseStatusException(BAD_GATEWAY, "Unable to send " + context + " email notification");
            }
        }
    }
}
