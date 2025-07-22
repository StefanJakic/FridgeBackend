package com.hylastix.fridge.repository;

import com.hylastix.fridge.entity.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FridgeRepository extends JpaRepository<Fridge, Long> {
    List<Fridge> findByUsers_Id(Long userId);

    Optional<Fridge> findByName(String name);
}
