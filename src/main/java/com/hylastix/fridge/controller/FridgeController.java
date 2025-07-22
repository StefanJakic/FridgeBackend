package com.hylastix.fridge.controller;

import com.hylastix.fridge.dto.CreateFridgeRequest;
import com.hylastix.fridge.dto.CreateItemRequest;
import com.hylastix.fridge.dto.FridgeDto;
import com.hylastix.fridge.dto.ItemDto;
import com.hylastix.fridge.dto.GiveItemsRequest;
import com.hylastix.fridge.entity.Fridge;
import com.hylastix.fridge.entity.Item;
import com.hylastix.fridge.entity.User;
import com.hylastix.fridge.service.FridgeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;



import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;

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
            Fridge created = fridgeService.createMyFridge(fridge, user.getId());
            FridgeDto dto = new FridgeDto(created.getId(), created.getName(), created.getCapacity(), created.getCurrentVolume(), created.getCurrentNumberOfItems());
            return ResponseEntity.ok(dto);
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
            Item created = fridgeService.addMyItem(fridgeId, user.getId(), item);
            ItemDto dto = new ItemDto(created.getId(), created.getName(), created.getCategory(), created.getVolume(), created.getStoredAt(), created.getBestBefore(), created.getOwner().getId());
            return ResponseEntity.ok(dto);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getAllMyItemsFromFridge")
    public ResponseEntity<?> getAllMyItemsFromFridge(@RequestParam Long fridgeId, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            List<ItemDto> dtos = fridgeService.getAllMyItemsFromFridge(fridgeId, user).stream()
                .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getMyItemsSortedByBestBefore")
    public ResponseEntity<?> getMyItemsSortedByBestBefore(@RequestParam Long fridgeId, @RequestParam(defaultValue = "asc") String order, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            List<Item> items = fridgeService.getMyItemsSortedByBestBefore(fridgeId, user, order);
            List<ItemDto> dtos = items.stream()
                .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getMyItemsSortedByStoredAt")
    public ResponseEntity<?> getMyItemsSortedByByStoredAt(@RequestParam Long fridgeId, @RequestParam(defaultValue = "asc") String order, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            List<Item> items = fridgeService.getMyItemsSortedByStoredAt(fridgeId, user, order);
            List<ItemDto> dtos = items.stream()
                .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @DeleteMapping("/deleteMyItems")
    public ResponseEntity<?> deleteMyItemsFromFridge(@RequestParam List<Long> itemIds, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            fridgeService.deleteMyItems(itemIds, user.getId());
            return ResponseEntity.ok("Item deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }
    

    @PostMapping("/giveMyItemsToUser")
    public ResponseEntity<?> giveMyItemsToFridgeMember(@RequestBody GiveItemsRequest req, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            fridgeService.giveMyItemsToUser(req.getItemIds(), req.getNewOwnerId(), user);
            return ResponseEntity.ok("Items transferred successfully");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @PostMapping("/leaveMyFridge")
    public ResponseEntity<?> leaveMyFridge(@RequestParam Long fridgeId, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            List<Item> deletedItems = fridgeService.leaveMyFridge(fridgeId, user.getId());
            List<ItemDto> dtos = deletedItems.stream()
                .map(i -> new ItemDto(i.getId(), i.getName(), i.getCategory(), i.getVolume(), i.getStoredAt(), i.getBestBefore(), i.getOwner().getId()))
                .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }


    @GetMapping("/getMyFridges")
    public ResponseEntity<?> getMyFridges(Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            return ResponseEntity.ok(fridgeService.getMyFridges(user)); // Already returns DTOs
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getMyItemsFromFridge")
    public ResponseEntity<?> getMyItemsFromFridge(@RequestParam Long fridgeId, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            return ResponseEntity.ok(fridgeService.getMyItemsFromFridge(fridgeId, user));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    @GetMapping("/getMyUsersFromFridge")
    public ResponseEntity<?> getMyFridgeMembers(@RequestParam Long fridgeId, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            return ResponseEntity.ok(
                fridgeService.getMyUsersFromFridge(fridgeId, user).stream()
                    .map(u -> new com.hylastix.fridge.dto.UserDto(u.getId(), u.getEmail(), u.getRole()))
                    .collect(java.util.stream.Collectors.toList())
            );
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

    //TODO maybe always call fridges?
    @GetMapping("/getMyFridge")
    public ResponseEntity<?> getMyFridge(@RequestParam Long fridgeId, Authentication authentication) {
        try {
            String email = ((User) authentication.getPrincipal()).getEmail();
            User user = fridgeService.getUserByEmail(email);
            return ResponseEntity.ok(fridgeService.getMyFridge(user, fridgeId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(e.getMessage());
        }
    }

}
