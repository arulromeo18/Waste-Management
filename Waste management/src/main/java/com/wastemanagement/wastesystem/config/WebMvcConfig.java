package com.wastemanagement.wastesystem.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Path;
import java.nio.file.Paths;

/**
 * Maps "/uploads/**" HTTP requests to the physical directory where
 * FileStorageService writes uploaded images, so the URL paths stored in
 * Complaint.imageUrls / CollectionRecord.imageUrls (e.g.
 * "/uploads/complaints/3f2a1c9e-....jpg") actually resolve to a viewable
 * image when the React frontend renders an <img src="..."> tag.
 *
 * Without this mapping, FileStorageService would successfully write files
 * to disk, but every stored imageUrl would 404 when requested — the file
 * would exist, but nothing would tell Spring where to find it for an
 * incoming GET request under that path.
 *
 * This is what SecurityConfig's existing "/uploads/**" permitAll() matcher
 * was already anticipating (added back in the Security phase, before
 * FileStorageService existed) — the security rule and this resource
 * mapping are two halves of the same feature, now both in place.
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir}")
    private String uploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        Path uploadPath = Paths.get(uploadDir).toAbsolutePath().normalize();
        String resourceLocation = "file:" + uploadPath + "/";

        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(resourceLocation);
    }
}