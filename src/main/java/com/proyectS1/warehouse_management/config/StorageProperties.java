package com.proyectS1.warehouse_management.config;

import java.nio.file.Path;
import java.nio.file.Paths;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "app.storage")
@Getter
@Setter
public class StorageProperties {

    private String profilePhotosDir = "./uploads/profile-images";

    public Path getResolvedProfilePhotosDir() {
        return Paths.get(profilePhotosDir).toAbsolutePath().normalize();
    }
}
