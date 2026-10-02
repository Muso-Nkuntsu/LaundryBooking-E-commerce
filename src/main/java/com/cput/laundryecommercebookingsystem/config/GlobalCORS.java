package com.cput.laundryecommercebookingsystem.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**Muso Nkuntsu -231223722
 *
 * Lets the React frontend (a different address from the backend) call every endpoint.
 * Registered through WebMvcConfigurer so Spring MVC applies it without Spring Security.
 **/
@Configuration
public class GlobalCORS implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                // Vite dev server (5173) and "vite preview" (4173)
                .allowedOrigins("http://localhost:5173", "http://localhost:4173")
                .allowedMethods("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}
