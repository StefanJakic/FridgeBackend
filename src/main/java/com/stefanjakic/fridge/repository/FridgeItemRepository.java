package com.stefanjakic.fridge.repository;

import com.stefanjakic.fridge.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;

public interface FridgeItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByFridgeId(Long fridgeId);
    
    List<Item> findByBestBeforeBeforeAndBestBeforeAfter(LocalDate before, LocalDate after);
}
