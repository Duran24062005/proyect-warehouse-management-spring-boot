package com.proyectS1.warehouse_management.notifications.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

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
                "Registro exitoso en logiTrack",
                """
                Hola %s,

                tu cuenta en logiTrack fue registrada correctamente el %s.

                Ya puedes ingresar al sistema con el correo %s.

                Saludos,
                Equipo logiTrack
                """.formatted(
                    user.getFirstName(),
                    DATE_TIME_FORMATTER.format(LocalDateTime.now()),
                    user.getEmail()
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
                """
                Hola %s,

                detectamos un inicio de sesion en tu cuenta de logiTrack el %s.

                Si fuiste tu, no necesitas realizar ninguna accion.

                Saludos,
                Equipo logiTrack
                """.formatted(
                    user.getFirstName(),
                    DATE_TIME_FORMATTER.format(LocalDateTime.now())
                )
            ),
            "login"
        );
    }

    private EmailNotificationCommand buildCommand(AppUser user, String subject, String body) {
        return new EmailNotificationCommand(
            3,
            user.getEmail(),
            subject,
            body
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
