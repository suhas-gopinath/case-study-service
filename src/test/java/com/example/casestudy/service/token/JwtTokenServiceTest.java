package com.example.casestudy.service.token;

import com.example.casestudy.config.JwtConfig;
import com.example.casestudy.exception.TokenValidationException;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;



import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;

import static org.mockito.Mockito.when;

class JwtTokenServiceTest {





    private JwtTokenService tokenService;
    private JwtConfig jwtConfig;

    @BeforeEach
    void setUp() {

        jwtConfig = Mockito.mock(JwtConfig.class);
        String secret = "secretKeyForTestingThatIsLongEnoughForHS256";
        when(jwtConfig.getSecretKey()).thenReturn(secret);
        when(jwtConfig.getExpirationTime()).thenReturn(3600000L);

        tokenService = new JwtTokenService(jwtConfig);
    }

    @Test


    @DisplayName("Should generate a valid token and extract the correct username")
    void testGenerateAndValidateToken() {
        String username = "testUser";

        String token = tokenService.generateAccessToken(username);







        assertNotNull(token);
        String validatedUser = tokenService.validateAccessToken(token);
        assertEquals(username, validatedUser);
    }

    @Test




    @DisplayName("Should throw TokenValidationException when token is malformed")
    void testValidateMalformedToken() {
        String malformedToken = "not.a.real.token";


        TokenValidationException exception = assertThrows(TokenValidationException.class, () -> {
            tokenService.validateAccessToken(malformedToken);
        });





        assertEquals("Malformed JWT token", exception.getMessage());
    }

    @Test



    @DisplayName("Should throw TokenValidationException when token is expired")
    void testValidateExpiredToken() {
        // negative value for immediate expiration
        when(jwtConfig.getExpirationTime()).thenReturn(-1000L);



        String token = tokenService.generateAccessToken("expiredUser");



        TokenValidationException exception = assertThrows(TokenValidationException.class, () -> {
            tokenService.validateAccessToken(token);
        });


        assertEquals("JWT has expired", exception.getMessage());
    }

    @Test



    @DisplayName("Should fail when signed with a different key")
    void testValidateInvalidSignature() {
        String token = tokenService.generateAccessToken("user");



        JwtConfig wrongKeyConfig = Mockito.mock(JwtConfig.class);
        when(wrongKeyConfig.getSecretKey()).thenReturn("MTIzNDU2Nzg5MDEyMzQ1Njc4OTAxMjM0NTY3ODkwMTI");
        JwtTokenService secondTokenService = new JwtTokenService(wrongKeyConfig);



        assertThrows(TokenValidationException.class, () -> {
            secondTokenService.validateAccessToken(token);
        });



    }
}