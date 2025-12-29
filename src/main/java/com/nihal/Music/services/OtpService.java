package com.nihal.Music.services;

import com.nihal.Music.dtos.OtpRequest;

public interface OtpService {

    public String genOtp();

    public void verifyOtp(String otp, String userEmail);

    public boolean sendOtp(OtpRequest otpRequest);
}
