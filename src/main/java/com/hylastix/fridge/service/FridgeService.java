package com.hylastix.fridge.service;

import com.hylastix.fridge.entity.Fridge;
import com.hylastix.fridge.entity.Item;
import com.hylastix.fridge.entity.User;
import com.hylastix.fridge.repository.FridgeItemRepository;
import com.hylastix.fridge.repository.FridgeRepository;
import com.hylastix.fridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final UserRepository userRepository;
    private final FridgeItemRepository itemRepository;

    public void save(Fridge fridge) {
        fridgeRepository.save(fridge);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).get();
    }


    public void saveItem(Long fridgeId, Long ownerId, Item item) {
        Fridge fridge = fridgeRepository.findById(fridgeId).get();
        fridge.incrementNumberOfItems();
        fridge.incrementVolume(item.getVolume());
        User user = userRepository.findById(ownerId).get();

        item.setStoredAt(LocalDateTime.now());
        item.setOwner(user);
        fridge.getItems().add(item);

       itemRepository.save(item);
    }


    //@ADMIN
    public List<Fridge> getAllFridges() {
        return fridgeRepository.findAll();
    }

}
