package com.example.CaseStudy.controller;

import com.example.CaseStudy.dto.Message;
import com.example.CaseStudy.dto.UserRequest;
import com.example.CaseStudy.exception.InvalidCredentialsException;
import com.example.CaseStudy.exception.TokenValidationException;
import com.example.CaseStudy.exception.UserAlreadyExistsException;
import com.example.CaseStudy.model.User;
import com.example.CaseStudy.service.UserService;
import com.example.CaseStudy.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    @Autowired
    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<Message> registerUser(@Valid @RequestBody UserRequest request) {
        User newUser = userService.registerUser(request);
        if (newUser != null) {
            return new ResponseEntity<>(new Message("User Registered Successfully"), HttpStatus.CREATED);
        } else {
            throw new UserAlreadyExistsException("User registration failed. Username already exists.");
        }
    }

    @PostMapping("/login")
    public ResponseEntity<String> loginUser(@RequestBody UserRequest request) {
        if (userService.authenticateUser(request.getUsername(), request.getPassword()).isPresent()) {
            String token = JwtUtil.generateToken(request.getUsername());
            return new ResponseEntity<>(token, HttpStatus.OK);
        } else {
            throw new InvalidCredentialsException("Invalid username or password.");
        }
    }

    @GetMapping("/verify")
    public ResponseEntity<String> verify(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        try {
            String username = JwtUtil.validateToken(token);
            return ResponseEntity.ok("Successfully verified user: " + username);
        } catch (Exception e) {
            throw new TokenValidationException("Invalid/Expired token");
        }
    }
}

