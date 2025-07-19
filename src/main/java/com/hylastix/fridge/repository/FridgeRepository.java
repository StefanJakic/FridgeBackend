package com.hylastix.fridge.repository;

import com.hylastix.fridge.entity.Fridge;
import org.springframework.data.jpa.repository.JpaRepository;


public interface FridgeRepository extends JpaRepository<Fridge, Long> {

}
