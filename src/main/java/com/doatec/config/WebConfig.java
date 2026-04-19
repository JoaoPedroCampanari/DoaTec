package com.doatec.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${upload.path:uploads}")
    private String uploadPath;

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        // Cria o caminho absoluto para a pasta de uploads
        Path uploadDir = Paths.get(uploadPath).toAbsolutePath().normalize();

        // Cria a pasta se não existir
        if (!Files.exists(uploadDir)) {
            try {
                Files.createDirectories(uploadDir);
            } catch (Exception e) {
                throw new RuntimeException("Não foi possível criar a pasta de uploads: " + uploadDir, e);
            }
        }

        // Mapeia URL /uploads/** para a pasta física
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:" + uploadDir + "/");
    }
}