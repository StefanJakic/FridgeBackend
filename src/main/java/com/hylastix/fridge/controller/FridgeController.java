package com.hylastix.fridge.controller;

import com.hylastix.fridge.dto.*;
import com.hylastix.fridge.entity.Fridge;
import com.hylastix.fridge.entity.Item;
import com.hylastix.fridge.entity.User;
import com.hylastix.fridge.service.FridgeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;


import java.util.HashSet;

@RestController
@RequestMapping("/fridge")
@RequiredArgsConstructor
public class FridgeController {

    private final FridgeService fridgeService;

    @PostMapping("/createMyFridge")
    public ResponseEntity<?> createMyFridge(@Valid @RequestBody CreateFridgeRequest request, Authentication authentication) {
        try {

            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);


            Fridge fridge = new Fridge();
            fridge.setName(request.getName());
            fridge.setCapacity(request.getCapacity());

            fridge.setCurrentNumberOfItems(0);
            fridge.setCurrentVolume(0);
            fridge.setUsers(new HashSet<>());
            fridge.getUsers().add(user);

            fridge.setItems(new HashSet<>());


            fridge.getUsers().add(user);
            user.getFridges().add(fridge);
            fridgeService.save(fridge);

            return ResponseEntity.ok("OK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



    @PostMapping("/addMyItem")
    public ResponseEntity<?> addMyItemToFridge(@RequestParam Long fridgeId, Authentication authentication, @Valid @RequestBody CreateItemRequest request) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            Item item = new Item();
            item.setName(request.getName());
            item.setCategory(request.getCategory());
            item.setVolume(request.getVolume());
            item.setBestBefore(request.getBestBefore());
            fridgeService.saveItem(fridgeId, user.getId(), item);
            return ResponseEntity.ok("OKK");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }



}
