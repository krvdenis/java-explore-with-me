package ru.practicum.security;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.AccessLevel;
import lombok.experimental.FieldDefaults;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationServiceException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import ru.practicum.security.jwt.JwtUtils;
import ru.practicum.user.dto.LoginRequest;

import java.io.IOException;
import java.time.format.DateTimeFormatter;
import java.util.Map;

@Slf4j
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class JsonUserPasswordAuthenticationFilter extends UsernamePasswordAuthenticationFilter {
    ObjectMapper objectMapper = new ObjectMapper();
    JwtUtils jwtUtils;

    public JsonUserPasswordAuthenticationFilter(AuthenticationManager authenticationManager, JwtUtils jwtUtils) {
        super(authenticationManager);
        this.jwtUtils = jwtUtils;
        setFilterProcessesUrl("/login");

        setAuthenticationSuccessHandler((request, response, authentication) -> {
            log.info("Пользователь [{}] успешно вошел. Генерация JWT...", authentication.getName());

            String token = jwtUtils.generateToken(authentication.getName());
            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_OK);

            response.getWriter().write(String.format(
                    "{\"message\": \"Login successful\", \"token\": \"%s\"}",
                    token
            ));
        });

        setAuthenticationFailureHandler(((request, response, exception) -> {
            log.warn("Аутентификация отклонена: {}", exception.getMessage());

            response.setContentType("application/json;charset=UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

            Map<String, Object> errorDetails = new java.util.HashMap<>();
            errorDetails.put("status", "UNAUTHORIZED");
            errorDetails.put("reason", "User authentication failed.");
            errorDetails.put("message", exception.getMessage());
            errorDetails.put("timestamp", java.time.LocalDateTime.now()
                    .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));

            objectMapper.writeValue(response.getWriter(), errorDetails);
        }));
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request, HttpServletResponse response)
            throws AuthenticationException {
        if (!request.getMethod().equals("POST")) {
            log.warn("Попытка входа с некорректным HTTP-методом: {}", request.getMethod());
            throw new AuthenticationServiceException("Authentication method not supported: " + request.getMethod());
        }

        try {
            LoginRequest loginRequest = objectMapper.readValue(request.getInputStream(), LoginRequest.class);

            log.debug("Попытка аутентификации для email: {}", loginRequest.getEmail());

            UsernamePasswordAuthenticationToken authRequest = new UsernamePasswordAuthenticationToken(
                    loginRequest.getEmail(),
                    loginRequest.getPassword()
            );

            setDetails(request, authRequest);
            return this.getAuthenticationManager().authenticate(authRequest);

        } catch (IOException e) {
            log.error("Ошибка при чтении JSON-тела запроса авторизации", e);
            throw new AuthenticationServiceException("Failed to parse authentication request body", e);
        }
    }
}
