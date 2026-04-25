package com.example.casestudy.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class JwtAuthenticationEntryPoint {

    public void handle(HttpServletResponse response, String reason) throws IOException {

        response.setContentType("application/json");
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);

        String message = switch (reason) {
            case "TOKEN_EXPIRED" -> "Token expired";
            case "INVALID_TOKEN" -> "Invalid token";
            default -> "Unauthorized";
        };

        response.getWriter().write("{\"error\":\"" + message + "\"}");
    }
}