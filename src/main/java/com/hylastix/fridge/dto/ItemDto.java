package com.hylastix.fridge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class ItemDto {
    private Long id;
    private String name;
    private String category;
    private double volume;
    private LocalDateTime storedAt;
    private LocalDate bestBefore;
    private Long ownerId;
} 