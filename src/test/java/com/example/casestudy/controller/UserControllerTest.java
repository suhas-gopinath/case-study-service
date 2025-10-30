package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.UserService;
import com.example.casestudy.util.JwtUtil;
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
    private UserService userService;

    @Mock
    private JwtUtil jwtUtil;

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

        when(userService.registerUser(request)).thenReturn(mockUser);

        ResponseEntity<MessageDto> response = userController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User Registered Successfully", response.getBody().getMessage());
        verify(userService, times(1)).registerUser(request);
    }

    @Test
    void testLoginUser_Success() {
        UserRequest request = new UserRequest();
        request.setUsername("testUser");
        request.setPassword("password");

        User mockUser = new User();
        mockUser.setUsername("testUser");

        when(userService.authenticateUser("testUser", "password")).thenReturn(mockUser);
        when(jwtUtil.generateToken("testUser")).thenReturn("mockToken123");

        ResponseEntity<MessageDto> response = userController.loginUser(request);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("mockToken123", response.getBody().getMessage());
        verify(userService, times(1)).authenticateUser("testUser", "password");
        verify(jwtUtil, times(1)).generateToken("testUser");
    }

    @Test
    void testVerify_Success() {
        String token = "Bearer mockToken123";
        when(jwtUtil.validateToken("mockToken123")).thenReturn("testUser");

        ResponseEntity<MessageDto> response = userController.verify(token);

        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertEquals("Successfully verified user: testUser", response.getBody().getMessage());
        verify(jwtUtil, times(1)).validateToken("mockToken123");
    }
}
