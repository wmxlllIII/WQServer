package com.example.test.server.service;

public interface EmailService {
    void sendVerificationCode(String ToEmail,String code);
}
