package com.hylastix.fridge.repository;

import com.hylastix.fridge.entity.Item;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface FridgeItemRepository extends JpaRepository<Item, Long> {
    List<Item> findByFridgeId(Long fridgeId);
}
