package com.cuet.ghoorni.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.upload-dir:resources/uploads}")
    private String fileUploadDir;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        try {
            // File uploads directory
            Path fileUploadPath = Paths.get(fileUploadDir).toAbsolutePath().normalize();

            // Create directory if it doesn't exist
            if (!Files.exists(fileUploadPath)) {
                Files.createDirectories(fileUploadPath);
            }

            String fileUploadLocation = fileUploadPath.toString();
            registry.addResourceHandler("/uploads/**")
                    .addResourceLocations("file:" + fileUploadLocation + "/");

            System.out.println("Resource handlers configured:");
            System.out.println("File uploads path: " + fileUploadLocation);
        } catch (Exception e) {
            System.err.println("Failed to configure resource handlers: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
