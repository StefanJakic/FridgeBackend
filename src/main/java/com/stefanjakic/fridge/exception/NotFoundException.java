package com.stefanjakic.fridge.exception;
 
public class NotFoundException extends RuntimeException {
    public NotFoundException(String message) {
        super(message);
    }
} 