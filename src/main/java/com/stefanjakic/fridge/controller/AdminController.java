package com.stefanjakic.fridge.controller;

import com.stefanjakic.fridge.entity.Fridge;
import com.stefanjakic.fridge.entity.Item;
import com.stefanjakic.fridge.entity.User;
import com.stefanjakic.fridge.service.UserService;
import com.stefanjakic.fridge.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import static com.stefanjakic.fridge.service.ServiceMessages.*;
import jakarta.validation.Valid;
import com.stefanjakic.fridge.dto.CreateItemRequest;
import com.stefanjakic.fridge.dto.FridgeDto;
import com.stefanjakic.fridge.dto.ItemDto;
import com.stefanjakic.fridge.dto.UserDto;

import java.util.Map;
import java.util.Set;
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

    //TODO in frontend, for now it is just in SWAGGER
    @PatchMapping("/user/{userId}/role")
    public ResponseEntity<?> changeUserRole(@PathVariable Long userId, @RequestParam String role) {
        try {
            userService.updateUserRole(userId, role);
            return ResponseEntity.ok(Map.of("message", String.format(USER_ROLE_UPDATED, role)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @PostMapping("/addUserToFridge")
    public ResponseEntity<?> addUserToFridge(@RequestParam Long fridgeId, @RequestParam Long userId) {
        try {
            fridgeService.addUserToFridge(fridgeId, userId);
            return ResponseEntity.ok().body(Map.of("message", USER_ADDED_TO_FRIDGE));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //IS IT USED SOMEWHERE?
    @DeleteMapping("/fridge/{fridgeId}")
    public ResponseEntity<?> deleteFridge(@PathVariable Long fridgeId) {
        try {
            // FRIDGE MUST BE EMPTY!!!
            fridgeService.deleteFridge(fridgeId);
            return ResponseEntity.ok(FRIDGE_DELETED_SUCCESS);
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

    @PostMapping("/addItem")
    public ResponseEntity<?> addItem(@RequestParam Long fridgeId, @RequestParam Long ownerId, @Valid @RequestBody CreateItemRequest request) {
        try {
            Item item = new Item();
            item.setName(request.getName());
            item.setCategory(request.getCategory());
            item.setVolume(request.getVolume());
            item.setBestBefore(request.getBestBefore());
            Item created = fridgeService.addItem(fridgeId, ownerId, item);
            ItemDto dto = new ItemDto(created.getId(), created.getName(), created.getCategory(), created.getVolume(), created.getStoredAt(), created.getBestBefore(), created.getOwner().getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fridge/{fridgeId}/items")
    public ResponseEntity<?> getItemsByFridgeId(@PathVariable Long fridgeId) {
        try {
            List<ItemDto> dtos = fridgeService.getItemsByFridgeId(fridgeId).stream()
                    .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteItem/{itemId}")
    public ResponseEntity<?> deleteItems(@PathVariable List<Long> itemIds, @RequestParam Long ownerId) {
        try {
            fridgeService.deleteMyItems(itemIds, ownerId);
            return ResponseEntity.ok("Item deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fridge/{fridgeId}")
    public ResponseEntity<?> findById(@PathVariable Long fridgeId) {
        try {
            Fridge f = fridgeService.findById(fridgeId);
            FridgeDto dto = new FridgeDto(f.getId(), f.getName(), f.getCapacity(), f.getCurrentVolume(), f.getCurrentNumberOfItems());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/user/{userId}/fridges")
    public ResponseEntity<?> findByUserId(@PathVariable Long userId) {
        try {
            List<FridgeDto> dtos = fridgeService.findByUserId(userId).stream()
                    .map(f -> new FridgeDto(f.getId(), f.getName(), f.getCapacity(), f.getCurrentVolume(), f.getCurrentNumberOfItems()))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @DeleteMapping("/kickOutUserFromSystem/{userId}")
    public ResponseEntity<?> kickOutUserFromSystem(@PathVariable Long userId) {
        try {
            Map<Long, List<Item>> removed = fridgeService.kickOutUserFromSystem(userId);
            Map<Long, List<ItemDto>> dtoMap = removed.entrySet().stream()
                .collect(Collectors.toMap(
                    Map.Entry::getKey,
                    e -> e.getValue().stream()
                        .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                        .collect(Collectors.toList())
                ));

            userService.deleteUser(userId);
            return ResponseEntity.ok(dtoMap);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fridge/{fridgeId}/members")
    public ResponseEntity<?> getFridgeMembers(@PathVariable Long fridgeId) {
        try {
            Set<User> users = fridgeService.getFridgeMembers(fridgeId);
            Set<UserDto> usersDto = users.stream()
                    .map(u -> new UserDto(u.getId(), u.getEmail(), u.getRole()))
                    .collect(Collectors.toSet());
            return ResponseEntity.ok(usersDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/fridge/{fridgeId}/non-members")
    public ResponseEntity<?> getUsersNotInFridge(@PathVariable Long fridgeId) {
        try {
            Set<User> users = userService.getUsersNotInFridge(fridgeId);
            Set<UserDto> usersDto = users.stream()
                    .map(u -> new UserDto(u.getId(), u.getEmail(), u.getRole()))
                    .collect(Collectors.toSet());
            return ResponseEntity.ok(usersDto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/check-expiring-items")
    public ResponseEntity<?> checkExpiringItems() {
        try {
            fridgeService.checkAndNotifyExpiringItems();
            return ResponseEntity.ok(Map.of("message", "Expiring items check completed and notifications sent"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
}
