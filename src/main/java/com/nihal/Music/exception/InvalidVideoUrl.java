package com.nihal.Music.exception;

public class InvalidVideoUrl extends RuntimeException {
    public InvalidVideoUrl(String message) {
        super(message);
    }
}
