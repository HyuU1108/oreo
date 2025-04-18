package com.oreo.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.NonNull;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    @Value("${file.resource-handler}")
    private String resourceHandler;

    @Value("${file.resource-locations}")
    private String resourceLocations;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        System.out.println("Configuring resource handler...");
        System.out.println("Resource Handler Path: " + resourceHandler);
        System.out.println("Resource Locations: " + resourceLocations);

        if (resourceHandler != null && !resourceHandler.isBlank() &&
            resourceLocations != null && !resourceLocations.isBlank()) {

            if (!resourceLocations.startsWith("file:") && !resourceLocations.startsWith("classpath:")) {
                 System.err.println("Warning: resource-locations should start with 'file:' or 'classpath:'. Please check your configuration.");
            }
            if (!resourceHandler.endsWith("/**")) {
                 System.err.println("Warning: resource-handler should end with '/**'. Please check your configuration.");
            }

            System.out.println("Adding resource handler mapping: '" + resourceHandler + "' -> '" + resourceLocations + "'");
            registry.addResourceHandler(resourceHandler)
                    .addResourceLocations(resourceLocations);
        } else {
            System.err.println("Resource handler configuration is incomplete or invalid. File serving for uploads might not work. Check 'file.resource-handler' and 'file.resource-locations' properties.");
        }
    }
}