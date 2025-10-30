package com.example.casestudy.service;

import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.InvalidCredentialsException;
import com.example.casestudy.exception.InvalidInputException;
import com.example.casestudy.exception.UserAlreadyExistsException;
import com.example.casestudy.model.User;
import com.example.casestudy.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private UserService userService;

    private UserRequest userRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userRequest = new UserRequest();
        userRequest.setUsername("TestUser");
        userRequest.setPassword("password123");

        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPasswordHash("hashedpass");
        existingUser.setSalt(Base64.getEncoder().encodeToString("somesalt".getBytes()));
    }

    @Test
    void testRegisterUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = userService.registerUser(userRequest);

        assertNotNull(savedUser);
        assertEquals("TestUser", savedUser.getUsername());
        assertNotNull(savedUser.getPasswordHash());
        assertNotNull(savedUser.getSalt());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void testRegisterUser_UserAlreadyExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> userService.registerUser(userRequest));

        verify(userRepository, never()).save(any());
    }

    @Test
    void testAuthenticateUser_Success() throws Exception {
        User storedUser = new User();
        storedUser.setUsername("testuser");

        byte[] saltBytes = "1234567890123456".getBytes();
        String saltBase64 = Base64.getEncoder().encodeToString(saltBytes);
        storedUser.setSalt(saltBase64);

        var method = UserService.class.getDeclaredMethod("hashPassword", String.class, byte[].class);
        method.setAccessible(true);
        String correctHash = (String) method.invoke(userService, "password123", saltBytes);

        storedUser.setPasswordHash(correctHash);

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(storedUser));

        User authenticatedUser = userService.authenticateUser("testuser", "password123");

        assertEquals("testuser", authenticatedUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
    }



    @Test
    void testAuthenticateUser_InvalidUsername() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());
        assertThrows(InvalidCredentialsException.class, () -> userService.authenticateUser("unknown", "pass"));
    }

    @Test
    void testAuthenticateUser_WrongPassword() {
        existingUser.setPasswordHash("correctHash");
        existingUser.setSalt(Base64.getEncoder().encodeToString("1234567890123456".getBytes()));

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThrows(InvalidCredentialsException.class,
                () -> userService.authenticateUser("testuser", "wrongpass"));
    }

    @Test
    void testAuthenticateUser_Base64DecodeError() {
        User user = new User();
        user.setUsername("testuser");
        user.setSalt("invalid!!base64###");
        user.setPasswordHash("hash");

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(user));

        assertThrows(InvalidInputException.class,
                () -> userService.authenticateUser("testuser", "password123"));
    }
}

