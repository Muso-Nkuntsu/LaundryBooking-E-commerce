package com.cput.laundryecommercebookingsystem.config;

import com.cput.laundryecommercebookingsystem.repository.IStudentRepository;
import com.cput.laundryecommercebookingsystem.security.JwtAuthFilter;
import com.cput.laundryecommercebookingsystem.security.JwtService;
import com.cput.laundryecommercebookingsystem.security.RateLimitFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/*SecurityConfig.java
 * Who may call which endpoint. The first rule that matches wins.
 * "Is this booking/order yours?" is checked in the controllers with CurrentStudent.
 * Replaces GlobalCORS.java (CORS is set here now).
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    /** Reference data students read; only an admin may change it. */
    private static final String[] CATALOGUE = {
            "/product/**", "/laundry-service/**", "/laundrymachine/**", "/laundry-room/**", "/timeslot/**"};

    private final JwtService jwtService;
    private final IStudentRepository studentRepository;

    @Value("${app.cors.allowed-origins:http://localhost:5173,http://localhost:4173}")
    private String allowedOrigins;

    public SecurityConfig(JwtService jwtService, IStudentRepository studentRepository) {
        this.jwtService = jwtService;
        this.studentRepository = studentRepository;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // The token travels in a header, not a cookie, so CSRF attacks don't apply.
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> basic.disable())
            .formLogin(form -> form.disable())
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/error").permitAll()

                // ---- anyone, no login ----
                .requestMatchers(HttpMethod.POST, "/student/create", "/student/login",
                        "/student/verify-email", "/student/resend-verification",
                        "/student/forgot-password", "/student/reset-password").permitAll()
                .requestMatchers(HttpMethod.GET, "/product/getall", "/product/read/**",
                        "/laundry-service/getall", "/laundry-service/read/**",
                        "/review/getall", "/review/read/**", "/review/service/**", "/review/summary",
                        "/uploads/**").permitAll() // product pictures

                // ---- ADMIN only ----
                .requestMatchers("/api/admin/**").hasRole("ADMIN")
                .requestMatchers("/student/getall", "/student/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.GET, "/api/bookings", "/api/orders", "/api/orders/status/**",
                        "/payment/all", "/payment/status/**", "/order-item/getall").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/bookings/*/status",
                        "/payment/*/complete", "/payment/*/fail").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, "/api/orders/*/place").hasRole("ADMIN") // marks an order PAID
                .requestMatchers(HttpMethod.DELETE, "/api/bookings/*", "/api/orders/*").hasRole("ADMIN")
                .requestMatchers("/order-item/create", "/order-item/update", "/order-item/delete/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.POST, CATALOGUE).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, CATALOGUE).hasRole("ADMIN")
                .requestMatchers(HttpMethod.PATCH, CATALOGUE).hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, CATALOGUE).hasRole("ADMIN")

                // ---- everything else: logged in (controllers check ownership) ----
                .anyRequest().authenticated()
            )
            .exceptionHandling(e -> e
                .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
                .accessDeniedHandler((request, response, ex) -> {
                    response.setStatus(HttpStatus.FORBIDDEN.value());
                    response.setContentType("text/plain;charset=UTF-8");
                    response.getWriter().write("You do not have permission to do that.");
                })
            )
            .addFilterBefore(new RateLimitFilter(), UsernamePasswordAuthenticationFilter.class)
            .addFilterBefore(new JwtAuthFilter(jwtService, studentRepository), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    /** Only the frontend addresses in ALLOWED_ORIGINS may call the API from a browser. */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.stream(allowedOrigins.split(","))
                .map(String::trim)
                .filter(origin -> !origin.isEmpty())
                .toList());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("Authorization", "Content-Type"));
        config.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * Logins are handled by StudentController with our own tokens. This empty bean stops Spring Boot
     * from creating its default "user" account and printing a generated password at startup.
     */
    @Bean
    public UserDetailsService noDefaultUser() {
        return username -> {
            throw new UsernameNotFoundException("Not used");
        };
    }
}
