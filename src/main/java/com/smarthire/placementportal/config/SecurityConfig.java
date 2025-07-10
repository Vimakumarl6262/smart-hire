package com.smarthire.placementportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable()) // Disable CSRF (enable in prod!)
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/admin/**").authenticated() // Admin protected
                .anyRequest().permitAll() // Public for all others
            )
            .formLogin(form -> form
                .loginPage("/login") // Custom login page
                .loginProcessingUrl("/login") // Where Spring posts login form
                .defaultSuccessUrl("/admin/dashboard", true) // Redirect after login
                .failureUrl("/login?error=true") // Error message
                .permitAll()
            )
            .logout(logout -> logout
                .logoutUrl("/logout")
                .logoutSuccessUrl("/login?logout=true")
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .permitAll()
            );

        return http.build();
    }
}
