package com.cmmslight.cmmsapi.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

/**
 * Autenticacao HTTP Basic local (sem dependencia de provedor externo de identidade),
 * com autorizacao por perfil (ADMIN, PLANNER, TECHNICIAN, REQUESTER).
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(basic -> {})
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/actuator/health/**").permitAll()

                .requestMatchers(HttpMethod.POST, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.PUT, "/api/users/**").hasRole("ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/users/**").hasRole("ADMIN")
                .requestMatchers("/api/audit-logs/**").hasRole("ADMIN")
                .requestMatchers("/api/backups/**").hasRole("ADMIN")

                .requestMatchers(HttpMethod.POST, "/api/work-orders").hasAnyRole("ADMIN", "PLANNER", "TECHNICIAN", "REQUESTER")
                .requestMatchers(HttpMethod.POST, "/api/work-orders/*/comments").hasAnyRole("ADMIN", "PLANNER", "TECHNICIAN", "REQUESTER")
                .requestMatchers(HttpMethod.POST, "/api/work-orders/**").hasAnyRole("ADMIN", "PLANNER", "TECHNICIAN")
                .requestMatchers(HttpMethod.PUT, "/api/work-orders/**").hasAnyRole("ADMIN", "PLANNER", "TECHNICIAN")
                .requestMatchers(HttpMethod.DELETE, "/api/work-orders/**").hasAnyRole("ADMIN", "PLANNER")

                .requestMatchers(HttpMethod.POST, "/api/assets/**", "/api/asset-types/**", "/api/maintenance-plans/**",
                        "/api/suppliers/**", "/api/parts/**", "/api/checklist-templates/**", "/api/sensor-threshold-rules/**")
                    .hasAnyRole("ADMIN", "PLANNER")
                .requestMatchers(HttpMethod.PUT, "/api/assets/**", "/api/asset-types/**", "/api/maintenance-plans/**",
                        "/api/suppliers/**", "/api/parts/**", "/api/checklist-templates/**", "/api/sensor-threshold-rules/**")
                    .hasAnyRole("ADMIN", "PLANNER")
                .requestMatchers(HttpMethod.DELETE, "/api/assets/**", "/api/asset-types/**", "/api/maintenance-plans/**",
                        "/api/suppliers/**", "/api/parts/**", "/api/checklist-templates/**", "/api/sensor-threshold-rules/**")
                    .hasAnyRole("ADMIN", "PLANNER")

                .requestMatchers("/api/**").authenticated()
                .anyRequest().authenticated()
            );
        return http.build();
    }
}
