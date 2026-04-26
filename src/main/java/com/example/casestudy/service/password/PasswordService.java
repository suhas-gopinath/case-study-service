package com.example.casestudy.service.password;

public interface PasswordService {

    String hash(String password, byte[] salt);

    boolean verify(String rawPassword, String storedHash, String storedSalt);

    byte[] generateSalt();
}