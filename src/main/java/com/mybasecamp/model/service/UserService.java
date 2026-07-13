package com.mybasecamp.model.service;

import com.mybasecamp.model.entity.User;
import com.mybasecamp.model.repository.UserRepository;
import com.mybasecamp.util.TokenGenerator;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final SessionService sessionService;
    private static final int SESSION_EXPIRY_HOURS = 24;

    public UserService(UserRepository userRepository, SessionService sessionService) {
        this.userRepository = userRepository;
        this.passwordEncoder = new BCryptPasswordEncoder(10);
        this.sessionService = sessionService;
    }

    public Map<String, Object> createUser(String name, String email, String password, String passwordConfirm) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (name == null || name.isBlank() || email == null || email.isBlank() || password == null || password.isBlank() || passwordConfirm == null || passwordConfirm.isBlank()) {
                throw new IllegalArgumentException("All fields are required");
            }
            if (password.length() < 6) {
                throw new IllegalArgumentException("Password must contain at least 6 characters");
            }
            if (!password.equals(passwordConfirm)) {
                throw new IllegalArgumentException("Passwords do not match");
            }
            if (userRepository.existsByEmail(email)) {
                throw new IllegalArgumentException("User with this email already exists");
            }
            User user = new User();
            user.setName(name);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setAdmin(0);
            userRepository.save(user);
            result.put("success", true);
            result.put("message", "User created successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getUserById(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            Map<String, Object> userWithoutPassword = new HashMap<>();
            userWithoutPassword.put("id", user.getId());
            userWithoutPassword.put("name", user.getName());
            userWithoutPassword.put("email", user.getEmail());
            userWithoutPassword.put("admin", user.getAdmin() == 1);
            userWithoutPassword.put("created_at", user.getCreatedAt());
            result.put("success", true);
            result.put("data", userWithoutPassword);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> getAllUsersService() {
        Map<String, Object> result = new HashMap<>();
        try {
            var users = userRepository.findAll();
            var userData = users.stream().map(user -> {
                Map<String, Object> m = new HashMap<>();
                m.put("id", user.getId());
                m.put("name", user.getName());
                m.put("email", user.getEmail());
                m.put("admin", user.getAdmin() == 1);
                m.put("created_at", user.getCreatedAt());
                return m;
            }).toList();
            result.put("success", true);
            result.put("data", userData);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> deleteUser(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            if (!userRepository.existsById(userId)) {
                throw new EntityNotFoundException("User not found");
            }
            userRepository.deleteById(userId);
            result.put("success", true);
            result.put("message", "User deleted successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> makeUserAdmin(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            if (user.getAdmin() == 1) {
                throw new IllegalArgumentException("User is already an admin");
            }
            user.setAdmin(1);
            userRepository.save(user);
            result.put("success", true);
            result.put("message", "User promoted to admin successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> removeUserAdminService(Integer userId) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (userId == null) {
                throw new IllegalArgumentException("User ID is required");
            }
            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new EntityNotFoundException("User not found"));
            if (user.getAdmin() == 0) {
                throw new IllegalArgumentException("User is not an admin");
            }
            user.setAdmin(0);
            userRepository.save(user);
            result.put("success", true);
            result.put("message", "Admin privileges removed successfully");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> loginUser(String email, String password, Boolean remember) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (email == null || email.isBlank() || password == null || password.isBlank()) {
                throw new IllegalArgumentException("Email and password are required");
            }
            User user = userRepository.findByEmail(email)
                    .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));
            if (!passwordEncoder.matches(password, user.getPassword())) {
                throw new IllegalArgumentException("Invalid email or password");
            }
            String token = TokenGenerator.generateSessionToken();
            int expiryHours = (remember != null && remember) ? 30 * 24 : SESSION_EXPIRY_HOURS;
            LocalDateTime expiresAt = LocalDateTime.now().plusHours(expiryHours);
            sessionService.createSession(user.getId(), token, expiresAt);

            Map<String, Object> userWithoutPassword = new HashMap<>();
            userWithoutPassword.put("id", user.getId());
            userWithoutPassword.put("name", user.getName());
            userWithoutPassword.put("email", user.getEmail());
            userWithoutPassword.put("admin", user.getAdmin() == 1);
            userWithoutPassword.put("created_at", user.getCreatedAt());

            Map<String, Object> data = new HashMap<>();
            data.put("user", userWithoutPassword);
            data.put("token", token);

            result.put("success", true);
            result.put("message", "Login successful");
            result.put("data", data);
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public Map<String, Object> logoutUser(String token) {
        Map<String, Object> result = new HashMap<>();
        try {
            if (token == null || token.isBlank()) {
                throw new IllegalArgumentException("Token is required");
            }
            if (!sessionService.getSessionByToken(token).isPresent()) {
                throw new IllegalArgumentException("Session not found");
            }
            sessionService.deleteSession(token);
            result.put("success", true);
            result.put("message", "Logout successful");
        } catch (Exception err) {
            System.err.println(err.getMessage());
            result.put("success", false);
            result.put("message", err.getMessage());
        }
        return result;
    }

    public com.mybasecamp.model.entity.User findUserByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}
