package com.cput.laundryecommercebookingsystem.config;

import com.cput.laundryecommercebookingsystem.service.admin.ImageStorageService;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/*WebConfig.java
 * Serves uploaded product pictures at /uploads/... (read-only; anyone may view them, see SecurityConfig).
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ImageStorageService imageStorage;

    public WebConfig(ImageStorageService imageStorage) {
        this.imageStorage = imageStorage;
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations(imageStorage.getRootDir().toUri().toString())
                .setCachePeriod(3600);
    }
}
