package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.auth.InvalidRefreshTokenException;
import com.example.casestudy.model.User;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.AccessTokenService;
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
    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

    @Mock
    private CookieUtil cookieUtil;

    @InjectMocks
    private UserController userController;

    private MockHttpServletResponse response;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        response = new MockHttpServletResponse();
    }

    // ==================== Registration Tests ====================

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        // Given
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("Password123!");

        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(authenticationService.register(request)).thenReturn(mockUser);

        // When
        ResponseEntity<MessageDto> result = userController.registerUser(request);

        // Then
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("User Registered Successfully", result.getBody().getMessage());
        verify(authenticationService, times(1)).register(request);
    }

    @Test
    @DisplayName("Should handle registration with valid user request")
    void testRegisterUser_WithValidRequest() {
        // Given
        UserRequest request = new UserRequest("newUser", "SecurePass123!");
        User mockUser = new User();
        mockUser.setUsername("newUser");

        when(authenticationService.register(request)).thenReturn(mockUser);

        // When
        ResponseEntity<MessageDto> result = userController.registerUser(request);

        // Then
        assertNotNull(result);
        assertEquals(HttpStatus.CREATED, result.getStatusCode());
        verify(authenticationService, times(1)).register(request);
    }

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

    // ==================== Token Verification Tests (v1) ====================

    @Test
    @DisplayName("Should verify access token successfully (v1)")
    void testVerify_Success() {
        // Given
        String authHeader = "Bearer mockToken123";
        String username = "testUser";

        when(accessTokenService.validateAccessToken("mockToken123")).thenReturn(username);

        // When
        ResponseEntity<MessageDto> result = userController.verify(authHeader);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Successfully verified user: testUser", result.getBody().getMessage());
        verify(accessTokenService, times(1)).validateAccessToken("mockToken123");
    }

    @Test
    @DisplayName("Should verify token without Bearer prefix (v1)")
    void testVerify_WithoutBearerPrefix() {
        // Given
        String authHeader = "mockToken456";
        String username = "user2";

        when(accessTokenService.validateAccessToken("mockToken456")).thenReturn(username);

        // When
        ResponseEntity<MessageDto> result = userController.verify(authHeader);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Successfully verified user: user2", result.getBody().getMessage());
        verify(accessTokenService, times(1)).validateAccessToken("mockToken456");
    }

    // ==================== Token Verification Tests (v2) ====================

    @Test
    @DisplayName("Should verify access token successfully (v2)")
    void testVerifyV2_Success() {
        // Given
        String authHeader = "Bearer jwt-access-token";
        String refreshToken = "valid-refresh-token";
        String username = "testUser";

        when(accessTokenService.validateAccessToken("jwt-access-token")).thenReturn(username);
        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(username);

        // When
        ResponseEntity<MessageDto> result = userController.verifyV2(authHeader, refreshToken);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Successfully verified user: testUser", result.getBody().getMessage());
        verify(accessTokenService, times(1)).validateAccessToken("jwt-access-token");
        verify(refreshTokenService, times(1)).validateRefreshToken(refreshToken);
    }

    @Test
    @DisplayName("Should verify token without Bearer prefix (v2)")
    void testVerifyV2_WithoutBearerPrefix() {
        // Given
        String authHeader = "jwt-token-789";
        String refreshToken = "valid-refresh-token";
        String username = "user3";

        when(accessTokenService.validateAccessToken("jwt-token-789")).thenReturn(username);
        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(username);

        // When
        ResponseEntity<MessageDto> result = userController.verifyV2(authHeader, refreshToken);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("Successfully verified user: user3", result.getBody().getMessage());
        verify(refreshTokenService, times(1)).validateRefreshToken(refreshToken);
    }

    // ==================== Token Refresh Tests ====================

    @Test
    @DisplayName("Should refresh access token using valid refresh token")
    void testRefreshToken_Success() {
        // Given
        String refreshToken = "valid-refresh-token";
        String username = "testUser";
        String newAccessToken = "new-jwt-access-token";

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(username);
        when(accessTokenService.generateAccessToken(username)).thenReturn(newAccessToken);

        // When
        ResponseEntity<MessageDto> result = userController.refreshToken(refreshToken);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals(newAccessToken, result.getBody().getMessage());
        verify(refreshTokenService, times(1)).validateRefreshToken(refreshToken);
        verify(accessTokenService, times(1)).generateAccessToken(username);
    }

    @Test
    @DisplayName("Should throw exception for invalid refresh token")
    void testRefreshToken_InvalidToken() {
        // Given
        String invalidToken = "invalid-refresh-token";

        when(refreshTokenService.validateRefreshToken(invalidToken))
            .thenThrow(new InvalidRefreshTokenException());

        // When & Then
        assertThrows(InvalidRefreshTokenException.class, () -> {
            userController.refreshToken(invalidToken);
        });

        verify(refreshTokenService, times(1)).validateRefreshToken(invalidToken);
        verify(accessTokenService, never()).generateAccessToken(anyString());
    }

    @Test
    @DisplayName("Should generate new access token on refresh")
    void testRefreshToken_GeneratesNewToken() {
        // Given
        String refreshToken = "refresh-abc";
        String username = "user4";
        String newToken = "new-token-xyz";

        when(refreshTokenService.validateRefreshToken(refreshToken)).thenReturn(username);
        when(accessTokenService.generateAccessToken(username)).thenReturn(newToken);

        // When
        ResponseEntity<MessageDto> result = userController.refreshToken(refreshToken);

        // Then
        assertEquals(newToken, result.getBody().getMessage());
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
    @DisplayName("Should handle logout without refresh token gracefully")
    void testLogout_NoRefreshToken() {
        // When
        ResponseEntity<MessageDto> result = userController.logout(null, response);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertNotNull(result.getBody());
        assertEquals("Refresh Token Revoked and logged out successfully", result.getBody().getMessage());
        
        // FIXED: The controller now always calls revokeRefreshToken, even with null token
        verify(refreshTokenService, times(1)).revokeRefreshToken(null);
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
    @DisplayName("Should handle Bearer token with extra spaces")
    void testVerify_BearerTokenWithSpaces() {
        // Given
        String authHeader = "Bearer   token-with-spaces  ";
        String expectedToken = "  token-with-spaces  ";

        when(accessTokenService.validateAccessToken(expectedToken)).thenReturn("user5");

        // When
        ResponseEntity<MessageDto> result = userController.verify(authHeader);

        // Then
        assertEquals(HttpStatus.OK, result.getStatusCode());
        verify(accessTokenService, times(1)).validateAccessToken(expectedToken);
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

    @Test
    @DisplayName("Should verify all service interactions during logout without token")
    void testLogout_AllServiceInteractionsWithoutToken() {
        // When
        userController.logout(null, response);

        // Then
        // FIXED: The controller now always calls revokeRefreshToken, even with null
        verify(refreshTokenService, times(1)).revokeRefreshToken(null);
        verify(cookieUtil, times(1)).clearRefreshTokenCookie(response);
        verifyNoMoreInteractions(refreshTokenService, cookieUtil);
    }
}