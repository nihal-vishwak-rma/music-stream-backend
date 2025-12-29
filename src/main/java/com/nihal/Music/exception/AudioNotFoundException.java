package com.nihal.Music.exception;

public class AudioNotFoundException extends RuntimeException {
    public AudioNotFoundException(String message) {
        super(message);
    }
}
