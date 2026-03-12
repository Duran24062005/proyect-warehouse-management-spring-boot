package com.proyectS1.warehouse_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    private static final String SECURITY_SCHEME_NAME = "bearerAuth";

    @Bean
    OpenAPI warehouseManagementOpenAPI(@Value("${server.port}") String serverPort) {
        return new OpenAPI()
            .info(new Info()
                .title("Warehouse Management API")
                .description("API para gestion de usuarios, autenticacion, productos, bodegas y movimientos de inventario.")
                .version("v1")
                .contact(new Contact()
                    .name("Proyecto S1")
                    .email("admin@logitrack.com"))
                .license(new License()
                    .name("Apache 2.0")))
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME, new SecurityScheme()
                    .name(SECURITY_SCHEME_NAME)
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
            .addServersItem(new Server()
                .url("http://localhost:" + serverPort)
                .description("Local environment"));
    }
}
