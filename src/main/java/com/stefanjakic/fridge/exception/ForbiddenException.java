package com.stefanjakic.fridge.exception;
 
public class ForbiddenException extends RuntimeException {
    public ForbiddenException(String message) {
        super(message);
    }
} 