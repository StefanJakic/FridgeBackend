package com.hylastix.fridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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