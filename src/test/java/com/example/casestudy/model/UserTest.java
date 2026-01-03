package com.example.casestudy.model;

import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserTest {

    @Test
    void allArgsConstructorTest() {
        UUID id = UUID.randomUUID();
        User user = new User(id, "testUser", "hashedPassword", "randomSalt");

        assertThat(user.getId()).isEqualTo(id);
        assertThat(user.getUsername()).isEqualTo("testUser");
        assertThat(user.getPasswordHash()).isEqualTo("hashedPassword");
        assertThat(user.getSalt()).isEqualTo("randomSalt");
    }

    @Test
    void noArgsConstructorTest() {
        User user = new User();

        assertThat(user.getId()).isNull();
        assertThat(user.getUsername()).isNull();
        assertThat(user.getPasswordHash()).isNull();
        assertThat(user.getSalt()).isNull();
    }

    @Test
    void equalsAndHashCodeTest() {
        UUID id = UUID.randomUUID();

        User user1 = new User(id, "testUser", "hash", "salt");
        User user2 = new User(id, "testUser", "hash", "salt");

        assertThat(user1).isEqualTo(user2);
        assertThat(user1.hashCode()).hasSameHashCodeAs(user2.hashCode());
    }
}