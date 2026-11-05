package com.ProyectoAula.GymAssist.mobile;

import java.util.List;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

/**
 * Seguridad de la API para la app Flutter: todo lo que empieza por /api/m/.
 * Sin sesión ni formulario: cada petición trae su token JWT. Las páginas web siguen
 * usando la configuración de SecurityConfig (se evalúa después de esta).
 */
@Configuration
public class MobileSecurityConfig {

    @Bean
    @Order(1)
    public SecurityFilterChain mobileFilterChain(HttpSecurity http, JwtService jwt) throws Exception {
        CorsConfiguration cors = new CorsConfiguration();
        cors.setAllowedOriginPatterns(List.of("*"));
        cors.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));
        cors.setAllowedHeaders(List.of("*"));
        cors.setAllowCredentials(false);
        UrlBasedCorsConfigurationSource corsSource = new UrlBasedCorsConfigurationSource();
        corsSource.registerCorsConfiguration("/api/m/**", cors);

        http.securityMatcher("/api/m/**")
                .csrf(c -> c.disable())
                .cors(c -> c.configurationSource(corsSource))
                .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .formLogin(f -> f.disable())
                .httpBasic(h -> h.disable())
                .logout(l -> l.disable())
                .authorizeHttpRequests(a -> a
                        .requestMatchers(HttpMethod.OPTIONS, "/api/m/**").permitAll()
                        .requestMatchers("/api/m/auth/login", "/api/m/auth/register", "/api/m/imagenes").permitAll()
                        .requestMatchers("/api/m/admin/**").hasRole("ADMIN")
                        .requestMatchers("/api/m/cliente/**").hasRole("CLIENTE")
                        .anyRequest().authenticated())
                .exceptionHandling(e -> e.authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED)))
                .addFilterBefore(new JwtAuthFilter(jwt), UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }
}
