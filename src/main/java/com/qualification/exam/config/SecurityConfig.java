package com.qualification.exam.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories
                .createDelegatingPasswordEncoder();
    }

    @Bean
    public UserDetailsService userDetailsService(
            @Value("${app.security.username}") String username,
            @Value("${app.security.password}") String password,
            PasswordEncoder passwordEncoder
    ) {
        return new InMemoryUserDetailsManager(
                User.withUsername(username)
                        .password(passwordEncoder.encode(password))
                        .roles("API_USER")
                        .build()
        );
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            ApiAuthenticationEntryPoint authenticationEntryPoint
    ) throws Exception {
        http
        .authorizeHttpRequests(authorize ->
				authorize
						.requestMatchers("/", 
								"/projects/**", 
								"/css/**", 
								"/error", 
								"/v3/api-docs/**", 
								"/swagger-ui/**",
								"/swagger-ui.html")
						.permitAll().requestMatchers("/api/**")
						.hasRole("API_USER")
						.anyRequest().authenticated()
						)
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers("/api/**")
                )
                .httpBasic(httpBasic ->
                        httpBasic.authenticationEntryPoint(
                                authenticationEntryPoint
                        )
                );

        return http.build();
    }
}