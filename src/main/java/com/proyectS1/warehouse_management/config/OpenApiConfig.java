package com.proyectS1.warehouse_management.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;

@Configuration
public class OpenApiConfig {

    @Bean
    OpenAPI warehouseManagementOpenAPI(@Value("${server.port}") String serverPort) {
        return new OpenAPI()
            .info(new Info()
                .title("Warehouse Management API")
                .description("API para gestion de productos, bodegas y movimientos de inventario.")
                .version("v1")
                .contact(new Contact()
                    .name("Proyecto S1")
                    .email("admin@logitrack.com"))
                .license(new License()
                    .name("Apache 2.0")))
            .components(new Components().addSecuritySchemes("bearerAuth",
                new SecurityScheme()
                    .name("bearerAuth")
                    .type(SecurityScheme.Type.HTTP)
                    .scheme("bearer")
                    .bearerFormat("JWT")))
            .addServersItem(new Server()
                .url("http://localhost:" + serverPort)
                .description("Local environment"));
    }
}
