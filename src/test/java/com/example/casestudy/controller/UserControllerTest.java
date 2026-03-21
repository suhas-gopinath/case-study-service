package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.TokenService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock
    private TokenService tokenService;

    @InjectMocks
    private UserController userController;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testRegisterUser_Success() {
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(authenticationService.register(request)).thenReturn(mockUser);

        ResponseEntity<MessageDto> response = userController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User Registered Successfully", response.getBody().getMessage());
        verify(authenticationService, times(1)).register(request);
    }

    @Test
    void testLoginUser_Success() {
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(authenticationService.authenticate("testUser", "password")).thenReturn(mockUser);
        when(tokenService.generateAccessToken("testUser")).thenReturn("mockToken123");

        ResponseEntity<MessageDto> response = userController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockToken123", response.getBody().getMessage());
        verify(authenticationService, times(1)).authenticate("testUser", "password");
        verify(tokenService, times(1)).generateAccessToken("testUser");
    }

    @Test
    void testVerify_Success() {
        String token = "Bearer mockToken123";
        when(tokenService.validateAccessToken("mockToken123")).thenReturn("testUser");

        ResponseEntity<MessageDto> response = userController.verify(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully verified user: testUser", response.getBody().getMessage());
        verify(tokenService, times(1)).validateAccessToken("mockToken123");
    }
}
