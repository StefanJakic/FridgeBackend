package com.hylastix.fridge.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

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

    public double getCurrentVolume() {
        return Math.round(currentVolume * 100.0) / 100.0;
    }
} 