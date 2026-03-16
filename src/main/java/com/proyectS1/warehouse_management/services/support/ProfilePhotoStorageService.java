package com.proyectS1.warehouse_management.services.support;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.Instant;
import java.util.Map;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.server.ResponseStatusException;

import com.proyectS1.warehouse_management.config.StorageProperties;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProfilePhotoStorageService {

    private static final long MAX_FILE_SIZE = 5L * 1024L * 1024L;
    private static final String PUBLIC_PREFIX = "/uploads/profile-images/";
    private static final Map<String, String> ALLOWED_TYPES = Map.of(
        "image/jpeg", "jpg",
        "image/png", "png",
        "image/webp", "webp"
    );

    private final StorageProperties storageProperties;

    public String storeProfilePhoto(Long userId, MultipartFile file, String previousFilename) {
        validateFile(file);

        String extension = ALLOWED_TYPES.get(file.getContentType());
        String filename = "user-" + userId + "-" + Instant.now().toEpochMilli() + "-"
            + UUID.randomUUID().toString().replace("-", "").substring(0, 8) + "." + extension;

        Path targetDirectory = storageProperties.getResolvedProfilePhotosDir();
        Path targetPath = targetDirectory.resolve(filename);

        try {
            Files.createDirectories(targetDirectory);
            Files.copy(file.getInputStream(), targetPath, StandardCopyOption.REPLACE_EXISTING);
            deletePreviousPhoto(previousFilename, filename);
            return filename;
        } catch (IOException exception) {
            throw new ResponseStatusException(INTERNAL_SERVER_ERROR, "Unable to store profile photo");
        }
    }

    public String toPublicUrl(String filename) {
        if (filename == null || filename.isBlank()) {
            return null;
        }

        return PUBLIC_PREFIX + filename;
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ResponseStatusException(BAD_REQUEST, "Profile photo file is required");
        }

        if (!ALLOWED_TYPES.containsKey(file.getContentType())) {
            throw new ResponseStatusException(BAD_REQUEST, "Only JPG, PNG and WEBP images are allowed");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new ResponseStatusException(BAD_REQUEST, "Profile photo must be 5 MB or smaller");
        }
    }

    private void deletePreviousPhoto(String previousFilename, String currentFilename) throws IOException {
        if (previousFilename == null || previousFilename.isBlank() || previousFilename.equals(currentFilename)) {
            return;
        }

        Path previousPath = storageProperties.getResolvedProfilePhotosDir().resolve(previousFilename).normalize();
        Files.deleteIfExists(previousPath);
    }
}
