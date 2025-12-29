package com.nihal.Music.services;

public interface EmailService {

    void sendOtpEmail(String toEmail, String subject, String otp);
}
