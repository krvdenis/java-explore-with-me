package ru.practicum.security;

import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.HandlerExceptionResolver;
import ru.practicum.security.jwt.JwtAuthenticationFilter;
import ru.practicum.security.jwt.JwtUtils;
import ru.practicum.user.admin.AdminUserService;
import ru.practicum.user.model.UserRole;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class SecurityConfig {
    AdminUserService userService;
    AuthenticationConfiguration authenticationConfiguration;
    HandlerExceptionResolver resolver;
    JwtUtils jwtUtils;
    JwtAuthenticationFilter jwtAuthFilter;

    public SecurityConfig(AuthenticationConfiguration authenticationConfiguration,
                          AdminUserService userService,
                          @Qualifier("handlerExceptionResolver") HandlerExceptionResolver resolver,
                          JwtUtils jwtUtils,
                          JwtAuthenticationFilter jwtAuthFilter) {
        this.authenticationConfiguration = authenticationConfiguration;
        this.userService = userService;
        this.resolver = resolver;
        this.jwtUtils = jwtUtils;
        this.jwtAuthFilter = jwtAuthFilter;
    }

    @Bean
    public AuthenticationManager authenticationManager() throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http,
                                                   AuthenticationManager authenticationManager) throws Exception {
        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/login").permitAll()
                        .requestMatchers("/", "/compilations", "/compilations/**", "/categories",
                                "/categories/**", "/events", "/events/**", "/registration").permitAll()
                        .requestMatchers("/admin/**").hasAnyRole(UserRole.ADMIN.name(),
                                UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/super-admin/**").hasRole(UserRole.SUPER_ADMIN.name())
                        .requestMatchers("/users/**").hasAnyRole(UserRole.USER.name(), UserRole.ADMIN.name(),
                                UserRole.SUPER_ADMIN.name())
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAt(
                        new JsonUserPasswordAuthenticationFilter(authenticationManager, jwtUtils),
                        UsernamePasswordAuthenticationFilter.class
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .clearAuthentication(true)
                        .logoutSuccessHandler((request, response, authentication) -> {
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_OK);
                            response.getWriter().write("{\"message\": \"Logout successful. Please delete your " +
                                    "token from client storage.\"}");
                        })
                )
                .exceptionHandling(exception -> exception
                        .authenticationEntryPoint((request, response, authException) ->
                                resolver.resolveException(request, response, null, authException))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                resolver.resolveException(request, response, null, accessDeniedException)))
                .build();
    }
}
