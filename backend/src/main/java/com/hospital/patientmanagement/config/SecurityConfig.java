package com.hospital.patientmanagement.config;

import com.hospital.patientmanagement.security.CustomUserDetailsService;
import com.hospital.patientmanagement.security.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration for the REST API.
 *
 * <ul>
 *   <li>CSRF disabled — REST APIs are stateless; CSRF protection is for
 *       session-cookie flows, not Bearer tokens.</li>
 *   <li>Sessions disabled — JWT carries all state; no server-side session
 *       is created or consulted.</li>
 *   <li>CORS configured to allow the React dev server (and any origins
 *       supplied via the {@code CORS_ORIGINS} env var).</li>
 *   <li>{@code @EnableMethodSecurity} activates {@code @PreAuthorize} /
 *       {@code @PostAuthorize} on service/controller methods.</li>
 * </ul>
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter    jwtAuthenticationFilter;
    private final CustomUserDetailsService   userDetailsService;

    @Value("${app.cors.allowed-origins}")
    private String allowedOrigins;

    // ------------------------------------------------------------------ //
    //  HTTP Security
    // ------------------------------------------------------------------ //

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm ->
                sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                // Public auth endpoints
                .requestMatchers(HttpMethod.POST, "/api/auth/login").permitAll()
                // Swagger / actuator (optional — remove if not used)
                .requestMatchers("/actuator/**", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                // Everything else requires a valid JWT
                .anyRequest().authenticated()
            )
            .authenticationProvider(authenticationProvider())
            .addFilterBefore(jwtAuthenticationFilter,
                             UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    // ------------------------------------------------------------------ //
    //  CORS
    // ------------------------------------------------------------------ //

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(Arrays.asList(allowedOrigins.split(",")));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    // ------------------------------------------------------------------ //
    //  Authentication infrastructure
    // ------------------------------------------------------------------ //

    @Bean
    public AuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider();
        provider.setUserDetailsService(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // BCrypt cost factor 12 — good balance of security vs. latency (~250 ms/hash).
        return new BCryptPasswordEncoder(12);
    }
}
