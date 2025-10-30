package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.UserService;
import com.example.casestudy.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserControllerTest {

    @Mock
    private UserService userService;

    @InjectMocks
    private UserController userController;

    @Captor
    private ArgumentCaptor<UserRequest> userRequestCaptor;

    private UserRequest request;
    private User user;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        request = new UserRequest();
        request.setUsername("testuser");
        request.setPassword("testpass");

        user = new User();
        user.setUsername("testuser");
        user.setPasswordHash("hashedPassword");
    }

    @Test
    void testRegisterUser_Success() {
        when(userService.registerUser(any(UserRequest.class))).thenReturn(user);

        ResponseEntity<MessageDto> response = userController.registerUser(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals("User Registered Successfully", response.getBody().getMessage());

        verify(userService, times(1)).registerUser(userRequestCaptor.capture());
        assertEquals("testuser", userRequestCaptor.getValue().getUsername());
    }

    @Test
    void testLoginUser_Success() {
        when(userService.authenticateUser("testuser", "testpass")).thenReturn(user);
        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.generateToken("testuser")).thenReturn("mockedToken");

            ResponseEntity<MessageDto> response = userController.loginUser(request);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("mockedToken", response.getBody().getMessage());
            verify(userService, times(1)).authenticateUser("testuser", "testpass");
        }
    }

    @Test
    void testVerify_Success() {
        String token = "mockedToken";
        String username = "testuser";

        try (MockedStatic<JwtUtil> mockedJwt = mockStatic(JwtUtil.class)) {
            mockedJwt.when(() -> JwtUtil.validateToken(token)).thenReturn(username);

            ResponseEntity<MessageDto> response = userController.verify("Bearer " + token);

            assertEquals(HttpStatus.OK, response.getStatusCode());
            assertEquals("Successfully verified user: testuser", response.getBody().getMessage());
            mockedJwt.verify(() -> JwtUtil.validateToken(token), times(1));
        }
    }
}
