package com.example.casestudy.service.auth;

import com.example.casestudy.dto.UserRequest;



import com.example.casestudy.exception.auth.InvalidCredentialsException;
import com.example.casestudy.exception.common.InvalidInputException;
import com.example.casestudy.exception.user.UserAlreadyExistsException;
import com.example.casestudy.model.User;
import com.example.casestudy.repository.UserRepository;
import com.example.casestudy.service.password.PBKDF2PasswordService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.Base64;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PBKDF2PasswordService passwordService;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private UserRequest userRequest;
    private User existingUser;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        userRequest = new UserRequest();
        userRequest.setUsername("TestUser");
        userRequest.setPassword("Password123!");

        existingUser = new User();
        existingUser.setUsername("testuser");
        existingUser.setPasswordHash("hashedPassword");
        existingUser.setSalt(Base64.getEncoder().encodeToString("somesalt".getBytes()));
    }

    @Test
    @DisplayName("Should register user successfully")
    void testRegisterUser_Success() {
        byte[] mockSalt = "1234567890123456".getBytes();
        String mockHash = "mockHashedPassword";

        when(userRepository.findByUsername("testuser")).thenReturn(Optional.empty());
        when(passwordService.generateSalt()).thenReturn(mockSalt);
        when(passwordService.hash("Password123!", mockSalt)).thenReturn(mockHash);
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        User savedUser = authenticationService.register(userRequest);

        assertNotNull(savedUser);
        assertEquals("TestUser", savedUser.getUsername());
        assertEquals(mockHash, savedUser.getPasswordHash());
        assertNotNull(savedUser.getSalt());

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, times(1)).save(any(User.class));
        verify(passwordService, times(1)).generateSalt();
        verify(passwordService, times(1)).hash("Password123!", mockSalt);
    }

    @Test
    @DisplayName("Should throw UserAlreadyExistsException when username exists")
    void testRegisterUser_UserAlreadyExists() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));

        assertThrows(UserAlreadyExistsException.class, () -> {
            authenticationService.register(userRequest);
        });

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(userRepository, never()).save(any());
    }

    @Test
    @DisplayName("Should authenticate user successfully")
    void testAuthenticateUser_Success() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordService.verify("Password123!", existingUser.getPasswordHash(), existingUser.getSalt()))
                .thenReturn(true);

        User authenticatedUser = authenticationService.authenticate("testuser", "Password123!");

        assertEquals("testuser", authenticatedUser.getUsername());
        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordService, times(1)).verify("Password123!", existingUser.getPasswordHash(), existingUser.getSalt());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for invalid username")
    void testAuthenticateUser_InvalidUsername() {
        when(userRepository.findByUsername("unknown")).thenReturn(Optional.empty());

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.authenticate("unknown", "Password123!");
        });

        verify(userRepository, times(1)).findByUsername("unknown");
        verify(passwordService, never()).verify(anyString(), anyString(), anyString());
    }

    @Test
    @DisplayName("Should throw InvalidCredentialsException for wrong password")
    void testAuthenticateUser_WrongPassword() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordService.verify("WrongPassword!", existingUser.getPasswordHash(), existingUser.getSalt()))
                .thenReturn(false);

        assertThrows(InvalidCredentialsException.class, () -> {
            authenticationService.authenticate("testuser", "WrongPassword!");
        });

        verify(userRepository, times(1)).findByUsername("testuser");
        verify(passwordService, times(1)).verify("WrongPassword!", existingUser.getPasswordHash(), existingUser.getSalt());
    }

    @Test
    @DisplayName("Should throw InvalidInputException on password verification error")
    void testAuthenticateUser_VerificationError() {
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(existingUser));
        when(passwordService.verify(anyString(), anyString(), anyString()))
                .thenThrow(new RuntimeException("Verification error"));

        assertThrows(InvalidInputException.class, () -> {
            authenticationService.authenticate("testuser", "Password123!");
        });
    }
}