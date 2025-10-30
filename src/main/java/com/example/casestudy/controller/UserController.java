package com.example.casestudy.controller;

import com.example.casestudy.dto.MessageDto;
import com.example.casestudy.dto.UserRequest;
import com.example.casestudy.model.User;
import com.example.casestudy.service.UserService;
import com.example.casestudy.util.JwtUtil;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;
    private static final Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<MessageDto> registerUser(@Valid @RequestBody UserRequest request) {
        logger.info("Received registration request for username: {}", request.getUsername());
        userService.registerUser(request);
        return new ResponseEntity<>(new MessageDto("User Registered Successfully"), HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<MessageDto> loginUser(@RequestBody UserRequest request) {
        logger.info("Received login request for username: {}", request.getUsername());
        User user = userService.authenticateUser(request.getUsername(), request.getPassword());
        String token = JwtUtil.generateToken(user.getUsername());
        return new ResponseEntity<>(new MessageDto(token), HttpStatus.OK);
    }

    @GetMapping("/verify")
    public ResponseEntity<MessageDto> verify(@RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String username = JwtUtil.validateToken(token);
        return ResponseEntity.ok(new MessageDto("Successfully verified user: " + username));
    }
}
