package com.example.casestudy.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CookieUtil {

    public static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";

    @Value("${refresh.token.ttl}")
    private long refreshTokenTtlMillis;

    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        int maxAgeSeconds = (int) (refreshTokenTtlMillis / 1000);
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, maxAgeSeconds);
        response.addCookie(cookie);
    }

    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0);
        response.addCookie(cookie);
    }

    private Cookie createCookie(String name, String value, int maxAge) {
        Cookie cookie = new Cookie(name, value);
        cookie.setHttpOnly(true);
        cookie.setSecure(false);
        cookie.setAttribute("SameSite", "Strict");
        cookie.setPath("/users");
        cookie.setMaxAge(maxAge);
        return cookie;
    }
}