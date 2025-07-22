package com.hylastix.fridge.repository;

import com.hylastix.fridge.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.Set;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    @Query("SELECT u FROM User u WHERE :fridgeId NOT IN (SELECT f.id FROM u.fridges f)")
    Set<User> findUsersNotInFridge(@Param("fridgeId") Long fridgeId);
}
