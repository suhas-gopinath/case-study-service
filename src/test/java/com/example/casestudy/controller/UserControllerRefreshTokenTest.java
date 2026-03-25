package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.auth.InvalidRefreshTokenException;
import com.example.casestudy.model.User;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.AccessTokenService;
import com.example.casestudy.service.token.RefreshTokenService;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for UserController refresh token functionality.
 * 
 * This test class verifies:
 * - Modified /login endpoint with refresh token cookie
 * - /refresh endpoint for token refresh
 * - /logout endpoint for token revocation
 * - /me endpoint for user information
 * - Cookie handling (HttpOnly, Secure, Path, MaxAge)
 */
class UserControllerRefreshTokenTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @InjectMocks
    private UserController userController;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();
    }

    @Test
    @DisplayName("Should set refresh token cookie on successful login")
    void testLogin_WithRefreshTokenCookie() {
        UserRequest request = new UserRequest("testUser", "Password123!");
        User mockUser = new User();
        mockUser.setUsername("testUser");
        String accessToken = "jwt-access-token";
        String refreshToken = "refresh-token-uuid";

        when(authenticationService.authenticate("testUser", "Password123!")).thenReturn(mockUser);
        when(accessTokenService.generateAccessToken("testUser")).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken("testUser")).thenReturn(refreshToken);

        ResponseEntity<MessageDto> result = userController.loginUser(request, response);

        // Verify response
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(accessToken, result.getBody().getMessage());

        // Verify cookie was set
        Cookie cookie = response.getCookie("refreshToken");
        assertNotNull(cookie);
        assertEquals(refreshToken, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertFalse(cookie.getSecure()); // false for local dev
        assertEquals("/users", cookie.getPath());
        assertEquals(7 * 24 * 60 * 60, cookie.getMaxAge()); // 7 days

        verify(authenticationService, times(1)).authenticate("testUser", "Password123!");
        verify(accessTokenService, times(1)).generateAccessToken("testUser");
        verify(refreshTokenService, times(1)).createRefreshToken("testUser");
    }

    @Test
    @DisplayName("Should refresh access token using valid refresh token")
    void testRefresh_Success() {
        String refreshToken = "valid-refresh-token";
        String username = "testUser";
        String newAccessToken = "new-jwt-access-token";

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(username);
        when(accessTokenService.generateAccessToken(username)).thenReturn(newAccessToken);

        ResponseEntity<MessageDto> result = userController.refreshToken(refreshToken);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(newAccessToken, result.getBody().getMessage());

        verify(refreshTokenService, times(1)).validateRefreshToken(refreshToken);
        verify(accessTokenService, times(1)).generateAccessToken(username);
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void testRefresh_InvalidToken() {
        String invalidToken = "invalid-refresh-token";

        when(refreshTokenService.validateRefreshToken(invalidToken))
            .thenThrow(new InvalidRefreshTokenException());

        assertThrows(InvalidRefreshTokenException.class, () -> {
            userController.refreshToken(invalidToken);
        });

        verify(refreshTokenService, times(1)).validateRefreshToken(invalidToken);
        verify(accessTokenService, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Should revoke refresh token and clear cookie on logout")
    void testLogout_Success() {
        String refreshToken = "refresh-token-to-revoke";

        ResponseEntity<MessageDto> result = userController.logout(refreshToken, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Logged out successfully", result.getBody().getMessage());

        // Verify token was revoked
        verify(refreshTokenService, times(1)).revokeRefreshToken(refreshToken);

        // Verify cookie was cleared
        Cookie cookie = response.getCookie("refreshToken");
        assertNotNull(cookie);
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge()); // MaxAge 0 deletes cookie
        assertTrue(cookie.isHttpOnly());
        assertEquals("/users", cookie.getPath());
    }

    @Test
    @DisplayName("Should handle logout without refresh token gracefully")
    void testLogout_NoRefreshToken() {
        ResponseEntity<MessageDto> result = userController.logout(null, response);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Logged out successfully", result.getBody().getMessage());

        // Verify revoke was not called
        verify(refreshTokenService, never()).revokeRefreshToken(anyString());

        // Verify cookie was still cleared
        Cookie cookie = response.getCookie("refreshToken");
        assertNotNull(cookie);
        assertEquals(0, cookie.getMaxAge());
    }

    @Test
    @DisplayName("Should return current user information from JWT")
    void testverifyV2_Success() {
        String authHeader = "Bearer jwt-access-token";
        String username = "testUser";

        when(accessTokenService.validateAccessToken("jwt-access-token")).thenReturn(username);

        ResponseEntity<MessageDto> result = userController.verifyV2(authHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(username, result.getBody().getMessage());

        verify(accessTokenService, times(1)).validateAccessToken("jwt-access-token");
    }

    @Test
    @DisplayName("Should handle missing Bearer prefix in Authorization header")
    void testverifyV2_WithoutBearerPrefix() {
        String authHeader = "jwt-access-token"; // Missing "Bearer " prefix
        String username = "testUser";

        when(accessTokenService.validateAccessToken("jwt-access-token")).thenReturn(username);

        ResponseEntity<MessageDto> result = userController.verifyV2(authHeader);

        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals(username, result.getBody().getMessage());
    }

    @Test
    @DisplayName("Should maintain API contract - login still returns JWT in MessageDto")
    void testLogin_ApiContractUnchanged() {
        UserRequest request = new UserRequest("testUser", "Password123!");
        User mockUser = new User();
        mockUser.setUsername("testUser");
        String accessToken = "jwt-access-token";

        when(authenticationService.authenticate("testUser", "Password123!")).thenReturn(mockUser);
        when(accessTokenService.generateAccessToken("testUser")).thenReturn(accessToken);
        when(refreshTokenService.createRefreshToken("testUser")).thenReturn("refresh-token");

        ResponseEntity<MessageDto> result = userController.loginUser(request, response);

        // API contract: JWT access token in MessageDto
        assertEquals(accessToken, result.getBody().getMessage());
        assertEquals(HttpStatus.OK, result.getStatusCode());
        
        // Refresh token is NOT in response body (only in cookie)
        assertNotEquals("refresh-token", result.getBody().getMessage());
    }
}
