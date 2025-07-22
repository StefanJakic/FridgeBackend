package com.hylastix.fridge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateFridgeRequest {
    @NotBlank(message = "Fridge name must not be blank")
    private String name;
    @Min(value = 1, message = "Fridge capacity must be greater than zero")
    private double capacity;
} 