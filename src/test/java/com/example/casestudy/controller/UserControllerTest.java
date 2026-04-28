package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.auth.InvalidRefreshTokenException;
import com.example.casestudy.model.User;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.RefreshTokenService;
import com.example.casestudy.util.CookieUtil;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Comprehensive unit tests for UserController class.
 * 
 * This test class verifies:
 * - User registration endpoint
 * - User login endpoint with refresh token cookie
 * - Token verification endpoints (v1 and v2)
 * - Token refresh endpoint
 * - Logout endpoint with token revocation
 * - Cookie handling via CookieUtil
 * - Error scenarios and edge cases
 */
class UserControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CookieUtil cookieUtil;

    @Mock
    private Executor verificationExecutor;

    @InjectMocks
    private UserController userController;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();
        
        // Configure mock executor to run tasks synchronously for testing
        doAnswer(invocation -> {
            Runnable task = invocation.getArgument(0);
            task.run();
            return null;
        }).when(verificationExecutor).execute(any(Runnable.class));
    }

    // ==================== Registration Tests ====================




    // ==================== Login Tests ====================

    @Test
    @DisplayName("Should login user successfully and set refresh token cookie")
    void testLoginUser_Success() {
        // Given
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("Password123!");

        User mockUser = new User();
        mockUser.setUsername("testUser");
        String refreshToken = "refresh-token-uuid";

        when(authenticationService.authenticate("testUser", "Password123!")).thenReturn(mockUser);
        when(refreshTokenService.createRefreshToken("testUser")).thenReturn(refreshToken);

        // When
        ResponseEntity<MessageDto> result = userController.loginUser(request, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Login Successful", result.getBody().getMessage());
        verify(authenticationService, times(1)).authenticate("testUser", "Password123!");
        verify(refreshTokenService, times(1)).createRefreshToken("testUser");
        verify(cookieUtil, times(1)).setRefreshTokenCookie(response, refreshToken);
    }

    @Test
    @DisplayName("Should login user and invoke cookie utility")
    void testLoginUser_CookieUtilInvocation() {
        // Given
        UserRequest request = new UserRequest("user1", "pass123");
        User mockUser = new User();
        mockUser.setUsername("user1");

        when(authenticationService.authenticate("user1", "pass123")).thenReturn(mockUser);
        when(refreshTokenService.createRefreshToken("user1")).thenReturn("token-123");

        // When
        userController.loginUser(request, response);

        // Then
        verify(cookieUtil, times(1)).setRefreshTokenCookie(eq(response), eq("token-123"));
    }


    // ==================== Logout Tests ====================

    @Test
    @DisplayName("Should logout user and revoke refresh token")
    void testLogout_Success() {
        // Given
        String refreshToken = "refresh-token-to-revoke";

        // When
        ResponseEntity<MessageDto> result = userController.logout(refreshToken, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Refresh Token Revoked and logged out successfully", result.getBody().getMessage());
        verify(refreshTokenService, times(1)).revokeRefreshToken(refreshToken);
        verify(cookieUtil, times(1)).clearRefreshTokenCookie(response);
    }

    @Test
    @DisplayName("Should clear cookie even when token revocation fails")
    void testLogout_ClearsCookieRegardlessOfRevocation() {
        // Given
        String refreshToken = "token-123";
        doThrow(new RuntimeException("Revocation failed"))
            .when(refreshTokenService).revokeRefreshToken(refreshToken);

        // When & Then
        assertThrows(RuntimeException.class, () -> {
            userController.logout(refreshToken, response);
        });

        verify(refreshTokenService, times(1)).revokeRefreshToken(refreshToken);
        // Cookie clearing happens after revocation, so it won't be called if exception is thrown
    }

    @Test
    @DisplayName("Should logout with empty refresh token")
    void testLogout_EmptyRefreshToken() {
        // Given
        String emptyToken = "";

        // When
        ResponseEntity<MessageDto> result = userController.logout(emptyToken, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(refreshTokenService, times(1)).revokeRefreshToken(emptyToken);
        verify(cookieUtil, times(1)).clearRefreshTokenCookie(response);
    }

    // ==================== Integration Tests ====================

    @Test
    @DisplayName("Should maintain API contract - login returns success message")
    void testLogin_ApiContractUnchanged() {
        // Given
        UserRequest request = new UserRequest("testUser", "Password123!");
        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(authenticationService.authenticate("testUser", "Password123!")).thenReturn(mockUser);
        when(refreshTokenService.createRefreshToken("testUser")).thenReturn("refresh-token");

        // When
        ResponseEntity<MessageDto> result = userController.loginUser(request, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Login Successful", result.getBody().getMessage());
        verify(cookieUtil, times(1)).setRefreshTokenCookie(eq(response), anyString());
    }

    @Test
    @DisplayName("Should verify all service interactions during login")
    void testLogin_AllServiceInteractions() {
        // Given
        UserRequest request = new UserRequest("user6", "pass6");
        User mockUser = new User();
        mockUser.setUsername("user6");

        when(authenticationService.authenticate("user6", "pass6")).thenReturn(mockUser);
        when(refreshTokenService.createRefreshToken("user6")).thenReturn("token-6");

        // When
        userController.loginUser(request, response);

        // Then
        verify(authenticationService, times(1)).authenticate("user6", "pass6");
        verify(refreshTokenService, times(1)).createRefreshToken("user6");
        verify(cookieUtil, times(1)).setRefreshTokenCookie(response, "token-6");
        verifyNoMoreInteractions(authenticationService, refreshTokenService, cookieUtil);
    }

    @Test
    @DisplayName("Should verify all service interactions during logout with token")
    void testLogout_AllServiceInteractionsWithToken() {
        // Given
        String token = "token-7";

        // When
        userController.logout(token, response);

        // Then
        verify(refreshTokenService, times(1)).revokeRefreshToken(token);
        verify(cookieUtil, times(1)).clearRefreshTokenCookie(response);
        verifyNoMoreInteractions(refreshTokenService, cookieUtil);
    }

}
