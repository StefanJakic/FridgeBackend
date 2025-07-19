package com.hylastix.fridge.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CreateItemRequest {
    @NotBlank(message = "Item name must not be blank")
    private String name;
    @NotBlank(message = "Category must not be blank")
    private String category;
    @Min(value = 1, message = "Item volume must be greater than zero")
    private double volume;
    private LocalDate bestBefore;
} 