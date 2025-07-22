package com.hylastix.fridge.config;

import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import jakarta.persistence.OptimisticLockException;

@ControllerAdvice
public class OptimisticLockingExceptionHandler {
    @ExceptionHandler({OptimisticLockException.class, OptimisticLockingFailureException.class})
    public ResponseEntity<String> handleOptimisticLocking(Exception ex) {
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Conflict: The resource was modified by another user. Please reload and try again.");
    }
} 