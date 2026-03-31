package com.example.casestudy.util;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;

/**
 * Utility class for managing refresh token cookies.
 */
@Component
public class CookieUtil {

    private static final String REFRESH_TOKEN_COOKIE_NAME = "refreshToken";
    private static final int REFRESH_TOKEN_COOKIE_MAX_AGE = 7 * 24 * 60 * 60; // 7 days in seconds

    /**
     * Sets the refresh token in an HTTP-only cookie.
     * 
     * @param response HTTP response
     * @param refreshToken The refresh token to set
     */
    public void setRefreshTokenCookie(HttpServletResponse response, String refreshToken) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, refreshToken, REFRESH_TOKEN_COOKIE_MAX_AGE);
        response.addCookie(cookie);
    }

    /**
     * Clears the refresh token cookie.
     * 
     * @param response HTTP response
     */
    public void clearRefreshTokenCookie(HttpServletResponse response) {
        Cookie cookie = createCookie(REFRESH_TOKEN_COOKIE_NAME, "", 0);
        response.addCookie(cookie);
    }

    /**
     * Creates a cookie with consistent security settings.
     * 
     * This centralized method ensures all cookies have the same security configuration:
     * - HttpOnly: true (prevents JavaScript access)
     * - Secure: false (for local development; should be true in production)
     * - SameSite: Strict (prevents CSRF attacks)
     * - Path: /users (limits cookie scope)
     * 
     * @param name Cookie name
     * @param value Cookie value
     * @param maxAge Cookie max age in seconds (0 to delete)
     * @return Configured Cookie instance
     */
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