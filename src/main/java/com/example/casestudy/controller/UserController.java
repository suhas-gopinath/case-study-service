package com.example.casestudy.controller;

import com.example.casestudy.annotation.IpRateLimit;
import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.exception.AppException;
import com.example.casestudy.exception.common.InvalidInputException;
import com.example.casestudy.model.User;
import com.example.casestudy.security.AccessTokenService;
import com.example.casestudy.service.token.RefreshTokenService;
import com.example.casestudy.service.auth.AuthenticationService;
import com.example.casestudy.util.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);
    private final AuthenticationService authenticationService;
    private final AccessTokenService accessTokenService;
    private final RefreshTokenService refreshTokenService;
    private final CookieUtil cookieUtil;

    public UserController(AuthenticationService authenticationService, 
                          AccessTokenService accessTokenService,
                          RefreshTokenService refreshTokenService,
                          CookieUtil cookieUtil) {
        this.authenticationService = authenticationService;
        this.accessTokenService = accessTokenService;
        this.refreshTokenService = refreshTokenService;
        this.cookieUtil = cookieUtil;
    }

    @PostMapping("/register")
    @IpRateLimit(limitForPeriod = 3, limitRefreshPeriodSeconds = 3600)
    public ResponseEntity<MessageDto> registerUser(@Valid @RequestBody UserRequest request) {
        logger.info("Received registration request for username: {}", request.getUsername());

        authenticationService.register(request);
        return new ResponseEntity<>(new MessageDto("User Registered Successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    @IpRateLimit(limitForPeriod = 5, limitRefreshPeriodSeconds = 300)
    public ResponseEntity<MessageDto> loginUser(@RequestBody UserRequest request, HttpServletResponse response) {
        logger.info("Received login request for username: {}", request.getUsername());

        User user = authenticationService.authenticate(request.getUsername(), request.getPassword());
        
        String refreshToken = refreshTokenService.createRefreshToken(user.getUsername());
        cookieUtil.setRefreshTokenCookie(response, refreshToken);
        
        logger.info("Login successful for user: {}", user.getUsername());
        return new ResponseEntity<>(new MessageDto("Login Successful"), HttpStatus.OK);
    }

    @GetMapping("/verify/v1")
    @IpRateLimit(limitForPeriod = 10, limitRefreshPeriodSeconds = 60)
    public ResponseEntity<MessageDto> verify(Authentication authentication) {
        String username = authentication.getName();
        return ResponseEntity.ok(new MessageDto("Successfully verified user: " + username));
    }

    @PostMapping("/refresh")
    @IpRateLimit(limitForPeriod = 5, limitRefreshPeriodSeconds = 60)
    public ResponseEntity<MessageDto> refreshToken(@CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        logger.info("Received token refresh request");
        
        String username = refreshTokenService.validateRefreshToken(refreshToken);
        String newAccessToken = accessTokenService.generateToken(username);
        
        logger.info("Token refreshed successfully for user: {}", username);
        return ResponseEntity.ok(new MessageDto(newAccessToken));
    }
    
    @PostMapping("/logout")
    @IpRateLimit(limitForPeriod = 5, limitRefreshPeriodSeconds = 60)
    public ResponseEntity<MessageDto> logout(
        @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME, required = false) String refreshToken,
        HttpServletResponse response) {
            logger.info("Received logout request");

            if(refreshToken!= null){
                refreshTokenService.revokeRefreshToken(refreshToken);
            }
            cookieUtil.clearRefreshTokenCookie(response);
            
            logger.info("Logout successful");
            return ResponseEntity.ok(new MessageDto("Refresh Token Revoked and logged out successfully"));
    }
    
     @GetMapping("/verify/v2")
     @IpRateLimit(limitForPeriod = 10, limitRefreshPeriodSeconds = 60)
    public ResponseEntity<MessageDto> verifyV2(
            Authentication authentication,
            @CookieValue(name = CookieUtil.REFRESH_TOKEN_COOKIE_NAME) String refreshToken) {
        
        logger.info("Received dual verification request (JWT + Refresh Token)");       
        
        try {
            String usernameFromJwt = authentication.getName();
            logger.debug("JWT validated for user: {}", usernameFromJwt);
            
            String usernameFromRefreshToken = refreshTokenService.validateRefreshToken(refreshToken);
            logger.debug("Refresh token validated for user: {}", usernameFromRefreshToken);
            
            if (!usernameFromJwt.equals(usernameFromRefreshToken)) {
                logger.warn("Token mismatch: JWT user '{}' does not match refresh token user '{}'", 
                        usernameFromJwt, usernameFromRefreshToken);
                throw new InvalidInputException("Token verification failed: user mismatch");
            }
            
            logger.info("Dual verification successful for user: {}", usernameFromJwt);
            return ResponseEntity.ok(new MessageDto("Successfully verified user: " + usernameFromJwt));
            
        } catch (AppException e) {
            throw e;
        } catch (Exception e) {
            logger.error("Token verification failed: {}", e.getMessage(), e);
            throw new InvalidInputException("Token verification failed");
        }
    }
}