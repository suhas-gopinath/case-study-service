package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.AppException;
import com.example.casestudy.exception.common.InvalidInputException;
import com.example.casestudy.model.User;
import com.example.casestudy.service.token.AccessTokenService;
import com.example.casestudy.service.token.RefreshTokenService;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.util.CookieUtil;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
@RateLimiter(name = "globalRateLimiter")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthenticationService authenticationService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;
    private final Executor verificationExecutor;

    public UserController(AuthenticationService authenticationService, 
                          AccessTokenService accessTokenService,
                          RefreshTokenService refreshTokenService,
                          CookieUtil cookieUtil,
                          @Qualifier("verificationTaskExecutor") Executor verificationExecutor) {
        this.authenticationService = authenticationService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
        this.verificationExecutor = verificationExecutor;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageDto> registerUser(@Valid @RequestBody UserRequest request) {
        logger.info("Received registration request for username: {}", request.getUsername());

        authenticationService.register(request);
        return new ResponseEntity<>(new MessageDto("User Registered Successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<MessageDto> loginUser(@RequestBody UserRequest request, HttpServletResponse response) {
        logger.info("Received login request for username: {}", request.getUsername());

        User user = authenticationService.authenticate(request.getUsername(), request.getPassword());
        
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        cookieUtil.setRefreshTokenCookie(response, refreshToken);
        
        logger.info("Login successful for user: {}", user.getUsername());
        return new ResponseEntity<>(new MessageDto("Login Successful"), HttpStatus.OK);
    }

    @GetMapping("/verify/v1")
    public ResponseEntity<MessageDto> verify(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");

        String username = accessTokenService.validateAccessToken(token);
        return ResponseEntity.ok(new MessageDto("Successfully verified user: " + username));
    }

    @PostMapping("/refresh")
    public ResponseEntity<MessageDto> refreshToken(@CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        logger.info("Received token refresh request");
        
        String username = refreshTokenService.validateRefreshToken(refreshToken);
        String newAccessToken = accessTokenService.generateAccessToken(username);
        
        logger.info("Token refreshed successfully for user: {}", username);
        return ResponseEntity.ok(new MessageDto(newAccessToken));
    }
    
    @PostMapping("/logout")
    public ResponseEntity<MessageDto> logout(
        @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
        HttpServletResponse response) {
            logger.info("Received logout request");
            
            refreshTokenService.revokeRefreshToken(refreshToken);
            cookieUtil.clearRefreshTokenCookie(response);
            
            logger.info("Logout successful");
            return ResponseEntity.ok(new MessageDto("Refresh Token Revoked and logged out successfully"));
    }
    
     @GetMapping("/verify/v2")
    public CompletableFuture<ResponseEntity<MessageDto>> verifyV2(
            @RequestHeader("Authorization") String authHeader,
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        
        logger.info("Received dual verification request (JWT + Refresh Token) - truly non-blocking");
        
        String token = authHeader.replace("Bearer ", "");
        
        CompletableFuture<String> jwtValidation = CompletableFuture.supplyAsync(() -> {
            String username = accessTokenService.validateAccessToken(token);
            logger.debug("JWT validated for user: {}", username);
            return username;
        }, verificationExecutor);
        
        CompletableFuture<String> refreshTokenValidation = CompletableFuture.supplyAsync(() -> {
            String username = refreshTokenService.validateRefreshToken(refreshToken);
            logger.debug("Refresh token validated for user: {}", username);
            return username;
        }, verificationExecutor);
        
        return jwtValidation.thenCombine(refreshTokenValidation, (usernameFromJwt, usernameFromRefreshToken) -> {
            if (!usernameFromJwt.equals(usernameFromRefreshToken)) {
                logger.warn("Token mismatch: JWT user '{}' does not match refresh token user '{}'", 
                        usernameFromJwt, usernameFromRefreshToken);
                throw new InvalidInputException("Token verification failed: user mismatch");
            }
            
            logger.info("Dual verification successful for user: {}", usernameFromJwt);
            return ResponseEntity.ok(new MessageDto("Successfully verified user: " + usernameFromJwt));
        })
        .orTimeout(3000, TimeUnit.MILLISECONDS)
        .exceptionally(throwable -> {
            Throwable cause = throwable.getCause() != null ? throwable.getCause() : throwable;
            
            if (cause instanceof AppException) {
                throw (AppException) cause;
            }
            
            logger.error("Token verification failed: {}", cause.getMessage(), cause);
            throw new InvalidInputException("Token verification failed");
        });
    }
}