package com.hylastix.fridge.controller;

import com.hylastix.fridge.dto.FridgeDto;
import com.hylastix.fridge.dto.UserDto;
import com.hylastix.fridge.service.FridgeService;
import com.hylastix.fridge.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/admin")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final UserService userService;
    private final FridgeService fridgeService;


    // just in SWAGGER
    @GetMapping("/users")
    public ResponseEntity<?> getAllUsers() {
        try {
            List<UserDto> dtos = userService.getAllUsers().stream()
                .map(u -> new UserDto(u.getId(), u.getEmail(), u.getRole()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/fridges")
    public ResponseEntity<?> getAllFridges() {
        try {
            List<FridgeDto> dtos = fridgeService.getAllFridges().stream()
                    .map(f -> new FridgeDto(f.getId(), f.getName(), f.getCapacity(), f.getCurrentVolume(), f.getCurrentNumberOfItems()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
