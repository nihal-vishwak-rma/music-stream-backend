package com.nihal.Music.exception;

public class SongFetchException  extends RuntimeException{

    public SongFetchException(String videoId, Throwable cause){
        super("Failed to fetch song with videoId: " + videoId, cause);
    }
}
