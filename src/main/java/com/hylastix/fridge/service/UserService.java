package com.hylastix.fridge.service;

import com.hylastix.fridge.entity.User;
import com.hylastix.fridge.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.Set;

import static com.hylastix.fridge.service.ServiceMessages.*;


@Service
@RequiredArgsConstructor
public class UserService {


    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public void register(User user) {
        if (userRepository.findByEmail(user.getEmail()).isPresent()) {
            throw new RuntimeException(EMAIL_ALREADY_EXISTS);
        }
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        user.setRole(DEFAULT_ROLE);
        // user.setRole("ADMIN");
        userRepository.save(user);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public User findById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
    }

    public Optional<User> findByEmail(String email) {
        return userRepository.findByEmail(email);
    }

    public boolean validateCredentials(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException(USER_NOT_FOUND));
        return passwordEncoder.matches(password, user.getPassword());
    }

    public User updateUserRole(Long userId, String role) {
        User user = findById(userId);
        user.setRole(role.toUpperCase());
        return userRepository.save(user);
    }

    public Set<User> getUsersNotInFridge(Long fridgeId) {
        return userRepository.findUsersNotInFridge(fridgeId);
    }

    public void deleteUser(Long userId) {
        userRepository.deleteById(userId);
    }

    public boolean existsById(Long userId) {
        return userRepository.existsById(userId);
    }
}
