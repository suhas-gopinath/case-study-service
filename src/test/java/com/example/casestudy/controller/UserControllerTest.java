package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.service.token.AccessTokenService;
import com.example.casestudy.service.token.RefreshTokenService;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class UserControllerTest {

    @Mock
    private AuthenticationService authenticationService;

    @Mock

    private AccessTokenService accessTokenService;

    @Mock
    private RefreshTokenService refreshTokenService;

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
        MockHttpServletResponse response = new MockHttpServletResponse();

        when(authenticationService.authenticate("testUser", "password")).thenReturn(mockUser);

        when(accessTokenService.generateAccessToken("testUser")).thenReturn("mockToken123");
        when(refreshTokenService.createRefreshToken("testUser")).thenReturn("refresh-token-uuid");

        ResponseEntity<MessageDto> result = userController.loginUser(request, response);





        assertEquals(HttpStatus.OK, result.getStatusCode());
        assertEquals("mockToken123", result.getBody().getMessage());
        verify(authenticationService, times(1)).authenticate("testUser", "password");

        verify(accessTokenService, times(1)).generateAccessToken("testUser");
        verify(refreshTokenService, times(1)).createRefreshToken("testUser");
    }


    @Test
    void testVerify_Success() {
        String token = "Bearer mockToken123";

        when(accessTokenService.validateAccessToken("mockToken123")).thenReturn("testUser");


        ResponseEntity<MessageDto> response = userController.verify(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully verified user: testUser", response.getBody().getMessage());

        verify(accessTokenService, times(1)).validateAccessToken("mockToken123");
    }

}
