package com.nihal.Music.services.Impl;

import com.nihal.Music.dtos.OtpRequest;
import com.nihal.Music.entity.Otp;
import com.nihal.Music.exception.OtpException;
import com.nihal.Music.repositories.OtpRepository;
import com.nihal.Music.services.EmailService;
import com.nihal.Music.services.OtpService;
import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Instant;
import java.util.Optional;

@Service
@Slf4j
public class OtpServiceImpl implements OtpService {

    @Autowired
    private OtpRepository otpRepository;

    @Autowired
    private EmailService emailService;


    @Override
    public String genOtp() {

        SecureRandom random = new SecureRandom();

        int code = 100000 + random.nextInt(900000);

        String otp = String.valueOf(code);

        return otp;
    }


    @Override
    @Transactional
    public void verifyOtp(String otp, String userEmail) {

        Otp byEmail = otpRepository.findByEmail(userEmail).orElseThrow(() -> new OtpException("No OTP found for this email. Please request a new one."));

        if (byEmail.getExpiry().isBefore(Instant.now())) {
            otpRepository.delete(byEmail);
            throw new OtpException("Otp is expired. Please request a new one.");
        }

        if (!byEmail.getOtp().equals(otp)) {
            throw new OtpException("Invalid Otp.");
        }

        log.info("OTP verified for email: {}", userEmail);


        otpRepository.delete(byEmail);
    }

    @Override
    @Transactional
    public boolean sendOtp(OtpRequest otpRequest) {

        String otpGen = genOtp();

        emailService.sendOtpEmail(otpRequest.getEmail(), "Your one time password is : ", otpGen);

        Otp otp;

        Optional<Otp> byEmail = otpRepository.findByEmail(otpRequest.getEmail());

        if (byEmail.isPresent()) {
            otp = byEmail.get();
        } else {
            otp = new Otp();
            otp.setEmail(otpRequest.getEmail());
        }

        otp.setOtp(otpGen);
        otp.setExpiry(Instant.now().plusMillis(5 * 60 * 1000));
        otpRepository.save(otp);

        log.info("otp send from otp service : {}", otpRequest.getEmail());

        return true;
    }
}
