package com.hylastix.fridge.service;

import com.hylastix.fridge.dto.ItemDto;
import com.hylastix.fridge.dto.FridgeDto;
import com.hylastix.fridge.entity.Fridge;
import com.hylastix.fridge.entity.Item;
import com.hylastix.fridge.entity.User;
import com.hylastix.fridge.repository.FridgeItemRepository;
import com.hylastix.fridge.repository.FridgeRepository;
import com.hylastix.fridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import static com.hylastix.fridge.service.ServiceMessages.*;
import com.hylastix.fridge.exception.NotFoundException;
import com.hylastix.fridge.exception.ForbiddenException;

@Service
@RequiredArgsConstructor
public class FridgeService {

    private final FridgeRepository fridgeRepository;
    private final UserRepository userRepository;
    private final FridgeItemRepository itemRepository;
    private final KafkaProducerService kafkaProducerService;


    //@USER
    public Item addMyItem(Long fridgeId, Long ownerId, Item item) {

        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new NotFoundException(FRIDGE_NOT_FOUND));
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (item.getBestBefore().isBefore(LocalDate.now())) {
            if (!owner.getRole().equals("ADMIN")) {
                throw new ForbiddenException(ITEM_EXPIRED);
            }
        }
        if (!isUserInFridge(fridge, owner))
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        return addItemCommon(fridge, ownerId, item);
    }

    //@ADMIN
    public Item addItem(Long fridgeId, Long ownerId, Item item) {
        // Admin može da doda i istekle, ili ima dodatne privilegije
        Fridge fridge = fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new NotFoundException(FRIDGE_NOT_FOUND));
        return addItemCommon(fridge, ownerId, item);
    }


    //Important(A): In this project every add should be done with this, since we want to centralize place for kafka sending
    //Important(B): Wrap this method with addItemByAdmin or addItemByUser
    @Transactional
    private Item addItemCommon(Fridge fridge, Long ownerId, Item item) {
        User owner = userRepository.findById(ownerId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (!isUserInFridge(fridge, owner))
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);

        if (fridge.getCurrentVolume() + item.getVolume() > fridge.getCapacity()) {
            throw new ForbiddenException(FRIDGE_CAPACITY_EXCEEDED);
        }

        fridge.incrementNumberOfItems();
        fridge.incrementVolume(item.getVolume());

        item.setStoredAt(LocalDateTime.now());
        item.setOwner(owner);
        addItemToFridgeBidirectional(item, fridge);

        // fridgeRepository.save(fridge); // REMOVE THIS
        Item savedItem = itemRepository.save(item);
        
        // Send kafka message after successful save
        kafkaProducerService.send("FridgeService: " + savedItem.getName());
        
        return savedItem;
    }


    //@ADMIN
    private Fridge getFridgeOrThrow(Long fridgeId) {
        return fridgeRepository.findById(fridgeId)
                .orElseThrow(() -> new NotFoundException(FRIDGE_NOT_FOUND));
    }

    //@ADMIN
    private User getUserOrThrow(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    //@ADMIN
    private Item getItemOrThrow(Long itemId) {
        return itemRepository.findById(itemId)
                .orElseThrow(() -> new NotFoundException(ITEM_NOT_FOUND));
    }

    private boolean isUserInFridge(Fridge fridge, Long userId) {
        return fridge.getUsers().stream().anyMatch(u -> u.getId().equals(userId));
    }

    private boolean isUserInFridge(Fridge fridge, User user) {
        return fridge.getUsers().contains(user);
    }

    private List<Item> filterItemsByOwner(Set<Item> items, Long userId) {
        return items.stream().filter(item -> item.getOwner().getId().equals(userId)).toList();
    }

    //@ADMIN // Admin can filter by users by fridge
    public List<Item> getItemsByFridgeId(Long fridgeId) {
        return itemRepository.findByFridgeId(fridgeId);
    }

    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
    }

    //@USER
    public List<Item> getAllMyItemsFromFridge(Long fridgeId, User user) {
        Fridge fridge = getFridgeOrThrow(fridgeId);
        if (!isUserInFridge(fridge, user)) {
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        }
        Set<Item> items = fridge.getItems();
        return new ArrayList<>(filterItemsByOwner(items, user.getId()));
    }


    //@USE
    public void giveMyItemsToUser(List<Long> itemIds, Long newOwnerId, User user) {
        for (Long itemId : itemIds) {
            Item item = getItemOrThrow(itemId);
            if (!item.getOwner().getId().equals(user.getId())) {
                throw new ForbiddenException(USER_NOT_IN_FRIDGE);
            }
            User owner = item.getOwner();
            User newOwner = userRepository.findById(newOwnerId)
                    .orElseThrow(() -> new NotFoundException(NEW_OWNER_NOT_FOUND));
            Fridge fridge = item.getFridge();
            if (!isUserInFridge(fridge, newOwner)) {
                throw new ForbiddenException("Target user is not a member of the fridge!");
            }
            if (!isUserInFridge(fridge, owner)) {
                throw new ForbiddenException("Owner is not a member of the fridge!");
            }
            item.setOwner(newOwner);
            itemRepository.save(item);
        }
    }

    //@USER
    public List<Item> getMyItemsSortedByBestBefore(Long fridgeId, User user, String order) {
        List<Item> items = getAllMyItemsFromFridge(fridgeId, user);
        if ("desc".equalsIgnoreCase(order)) {
            return items.stream().sorted(Comparator.comparing(Item::getBestBefore).reversed()).collect(Collectors.toList());
        } else {
            return items.stream().sorted(Comparator.comparing(Item::getBestBefore)).collect(Collectors.toList());
        }
    }

    //@USER
    public List<Item> getMyItemsSortedByStoredAt(Long fridgeId, User user, String order) {
        List<Item> items = getAllMyItemsFromFridge(fridgeId, user);
        if ("desc".equalsIgnoreCase(order)) {
            return items.stream().sorted(Comparator.comparing(Item::getStoredAt).reversed()).collect(Collectors.toList());
        } else {
            return items.stream().sorted(Comparator.comparing(Item::getStoredAt)).collect(Collectors.toList());
        }
    }


    //@USER
    @Transactional
    private void deleteMyItem(Long itemId, Long ownerId) {
        Item item = getItemOrThrow(itemId);
        if (!item.getOwner().getId().equals(ownerId)) {
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        }
        Fridge fridge = item.getFridge();
        fridge.decrementNumberOfItems();
        fridge.decrementVolume(item.getVolume());
        removeItemFromFridgeBidirectional(item, fridge);
        fridgeRepository.save(fridge);
//        itemRepository.deleteById(itemId);

    }

    //TODO optimize query, find all items, and then call JPA
    //@USER AND @ADMIN
    @Transactional
    public void deleteMyItems(List<Long> itemIds, Long ownerId) {
        //TODO maybe return items that were successfully deleted?
        List<ItemDto> items = new ArrayList<>();
        itemIds.forEach(itemId -> {
            deleteMyItem(itemId, ownerId);
        });

        //TODO here is kafka if everything is ok then call it in queue what user sent which items

    }


    //@ADMIN
    @Transactional
    public Map<Long, List<Item>> kickOutUserFromSystem(Long userId) {
        userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        Map<Long, List<Item>> allDeletedItemsFromALlFridgesAnd = new HashMap<>();
        List<Fridge> fridges = fridgeRepository.findByUsers_Id(userId);


        fridges.forEach(fridge -> {

            allDeletedItemsFromALlFridgesAnd.put(fridge.getId(), leaveMyFridge(fridge.getId(), userId));

        });
        return allDeletedItemsFromALlFridgesAnd;
    }


    //@USER
    @Transactional
    public List<Item> leaveMyFridge(Long fridgeId, Long userId) {
        Fridge fridge = getFridgeOrThrow(fridgeId);
        User user = getUserOrThrow(userId);
        List<Item> itemsToDelete = filterItemsByOwner(fridge.getItems(), userId);
        deleteMyItems(itemsToDelete.stream().map(Item::getId).collect(Collectors.toList()), userId);

        //TODO think do you have to call both save methods? //MAYBE THIS SHOULD BE IN USER SERVICE?
        removeUserFromFridgeBidirectional(user, fridge);

        fridgeRepository.save(fridge);
        userRepository.save(user);
        return itemsToDelete;
    }


     //TODO: Think more about the logic where only ADMIN can move items from one fridge to another
     {/*
     This is a complex scenario.
       In the future, check if the other fridge has space.
       What if only some items fit in the fridge? Should we try to sort them first(by size),
       add the many that fit, and return the rest back to the user?
       Should we return a list of items that could not be moved? Are they part of old fridge?
       This have to be consider that other users as well can add remove items, so it has to be Immutable
       Also, check if the target fridge has enough capacity before adding items.

     #ADMIN
     - Admin can transfer items between different fridges.
     - User can only transfer items to other users inside the SAME fridges.
      */}
//    @Transactional
//    public void giveMyItemToUserOfAnotherFridge(Long itemId, Long ownerId, Long newOwnerId, Long targetFridgeId) {
//
//        if (currentFridge.getId().equals(targetFridgeId)) {
//            if (targetFridge.getCurrentVolume() + item.getVolume() > targetFridge.getCapacity()) {
//                throw new ForbiddenException(FRIDGE_CAPACITY_EXCEEDED);
//            }
//            deleteItem(itemId);
//            item.setFridge(targetFridge);
//            addItemCommon(targetFridge, newOwnerId, item);
//        }
//    }



    //@USER
    public Fridge createMyFridge(Fridge fridge, Long userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        if (fridgeRepository.findByName(fridge.getName()).isPresent()) {
            throw new ForbiddenException(FRIDGE_NAME_EXISTS);
        }

        if (fridge.getCapacity() <= 0)
            throw new ForbiddenException(FRIDGE_CAPACITY_INVALID);
        fridge.setCurrentNumberOfItems(0);
        fridge.setCurrentVolume(0);
        fridge.setUsers(new HashSet<>());
        addUserToFridgeBidirectional(user, fridge);
        fridge.setItems(new HashSet<>());
        return fridgeRepository.save(fridge);
    }

    @Transactional
    public void deleteFridge(Long fridgeId) {
        Fridge fridge = findById(fridgeId);

        if (!fridge.getUsers().isEmpty()) {
            throw new ForbiddenException("Cannot delete fridge: fridge has users");
        }

        if (!fridge.getItems().isEmpty()) {
            throw new ForbiddenException("Cannot delete fridge: fridge has items");
        }

        fridgeRepository.deleteById(fridgeId);
    }


    //@ADMIN
    public List<Fridge> getAllFridges() {
        return fridgeRepository.findAll();
    }

    //@ADMIN
    public Fridge findById(Long fridgeId) {
        return fridgeRepository.findById(fridgeId).orElseThrow(() -> new NotFoundException(FRIDGE_NOT_FOUND));
    }

    //@ADMIN
    public Set<User> getFridgeMembers(Long fridgeId) {
        Fridge fridge = findById(fridgeId);
        return new HashSet<>(fridge.getUsers());
    }

    //@ADMIN
    public void addUserToFridge(Long fridgeId, Long userId) {
        Fridge fridge = findById(fridgeId);
        User user = userRepository.findById(userId).orElseThrow(() -> new NotFoundException(USER_NOT_FOUND));
        addUserToFridgeBidirectional(user, fridge);
        fridgeRepository.save(fridge);
        userRepository.save(user);
    }


    //@ADMIN
    public List<Fridge> findByUserId(Long userId) {
        return fridgeRepository.findByUsers_Id(userId);
    }


    //@USER
    public FridgeDto getMyFridge(User user, Long fridgeId) {
        Fridge fridge = findById(fridgeId);
        if (!isUserInFridge(fridge, user)) {
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        }
        return new FridgeDto(
            fridge.getId(),
            fridge.getName(),
            fridge.getCapacity(),
            fridge.getCurrentVolume(),
            fridge.getCurrentNumberOfItems()
        );
    }

    //@USER
    public List<FridgeDto> getMyFridges(User user) {
        List<FridgeDto> result = new ArrayList<>();
        for (Fridge fridge : user.getFridges()) {
            result.add(getMyFridge(user, fridge.getId()));
        }
        return result;
    }

    //@USER
    public List<Item> getMyItemsFromFridge(Long fridgeId, User user) {
        Fridge fridge = findById(fridgeId);
        if (!isUserInFridge(fridge, user)) {
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        }
        return fridge.getItems().stream().filter(item -> item.getOwner().getId().equals(user.getId())).collect(Collectors.toList());
    }


    //@USER
    public List<User> getMyUsersFromFridge(Long fridgeId, User user) {
        Fridge fridge = findById(fridgeId);
        if (!isUserInFridge(fridge, user)) {
            throw new ForbiddenException(USER_NOT_IN_FRIDGE);
        }
        return fridge.getUsers().stream().filter(u -> !u.getId().equals(user.getId())).collect(Collectors.toList());
    }

    private void addUserToFridgeBidirectional(User user, Fridge fridge) {
        if (!fridge.getUsers().contains(user)) {
            fridge.getUsers().add(user);
        }
        if (!user.getFridges().contains(fridge)) {
            user.getFridges().add(fridge);
        }
    }

    private void removeUserFromFridgeBidirectional(User user, Fridge fridge) {
        if (fridge.getUsers().contains(user)) {
            fridge.getUsers().remove(user);
        }
        if (user.getFridges().contains(fridge)) {
            user.getFridges().remove(fridge);
        }
    }

    private void addItemToFridgeBidirectional(Item item, Fridge fridge) {
        if (!fridge.getItems().contains(item)) {
            fridge.getItems().add(item);
        }
        item.setFridge(fridge);
    }
    private void removeItemFromFridgeBidirectional(Item item, Fridge fridge) {
        if (fridge.getItems().contains(item)) {
            fridge.getItems().remove(item);
        }
        item.setFridge(null);
    }
}
