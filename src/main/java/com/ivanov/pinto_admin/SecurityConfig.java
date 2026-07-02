package com.ivanov.pinto_admin;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@EnableWebSecurity
@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                .authorizeHttpRequests(auth -> auth
                        // управление учётными записями и правами бота — только главные администраторы
                        .requestMatchers("/accounts/**").hasRole("MAIN")
                        // управление дожимами — только главные администраторы
                        .requestMatchers("/push-messages/**").hasRole("MAIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/setrights").hasRole("MAIN")
                        .requestMatchers(org.springframework.http.HttpMethod.POST, "/api/users/changestatus").hasRole("MAIN")
                        // API и всё остальное — любая роль
                        .anyRequest().hasAnyRole("MAIN", "REGULAR")
                )
                .httpBasic(Customizer.withDefaults())
                // /api/** — stateless HTTP Basic, CSRF не нужен
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"))
                .build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }
}
