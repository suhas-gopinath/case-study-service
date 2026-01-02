package com.example.casestudy.repository;

import com.example.casestudy.model.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository userRepository;

    @Test
    @DisplayName("Should find user by username when user exists")
    void findByUsername_Success() {
        User user = new User(null, "test_user", "hashed_pass", "salt_value");
        userRepository.save(user);

        Optional<User> foundUser = userRepository.findByUsername("test_user");

        assertTrue(foundUser.isPresent());
        assertEquals("test_user", foundUser.get().getUsername());
        assertNotNull(foundUser.get().getId());
    }

    @Test
    @DisplayName("Should return empty Optional when user does not exist")
    void findByUsername_NotFound() {
        Optional<User> foundUser = userRepository.findByUsername("non_existent_user");
        assertFalse(foundUser.isPresent());
    }
}