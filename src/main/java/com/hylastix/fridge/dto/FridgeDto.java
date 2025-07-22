package com.hylastix.fridge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class FridgeDto {
    private Long id;
    private String name;
    private double capacity;
    private double currentVolume;
    private double currentNumberOfItems;

    // Custom getter for currentVolume (Lombok will NOT generate this one)
    public double getCurrentVolume() {
        return Math.round(currentVolume * 100.0) / 100.0;
    }
} 