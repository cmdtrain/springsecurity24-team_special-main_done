package com.softserve.itacademy.config.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.HttpStatusEntryPoint;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {


    @Bean
    public SecurityFilterChain apiFilterChain(HttpSecurity http) throws Exception {
        http.formLogin(withDefaults())
            .httpBasic(withDefaults());

        http.exceptionHandling(customizer -> customizer
            .authenticationEntryPoint(new HttpStatusEntryPoint(HttpStatus.UNAUTHORIZED))
            .accessDeniedHandler((request, response, accessDeniedException) -> {
                response.setStatus(HttpStatus.FORBIDDEN.value());
                request.getRequestDispatcher("/access-denied").forward(request, response);
            })
        );

        http.logout(logout -> logout
            .invalidateHttpSession(true)
            .deleteCookies("JSESSIONID")
            .logoutSuccessUrl("/login")
        );

        http.authorizeHttpRequests(authorize -> authorize
            .requestMatchers(
                "/", "/login", "/access-denied",
                "/css/**", "/js/**", "/img/**", "/static/**", "/webjars/**"
            ).permitAll()
            .anyRequest().authenticated()
        );

        return http.build();
    }
}
