package com.hylastix.fridge.dto;

import lombok.Getter;
import lombok.Setter;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class GiveItemsRequest {
    private List<Long> itemIds;
    private Long newOwnerId;
} 