package com.example.casestudy.service.database;

import com.example.casestudy.model.User;

import java.util.Optional;

public interface UserDatabaseService {

    Optional<User> findByUsername(String username);

    User save(User user);
}
