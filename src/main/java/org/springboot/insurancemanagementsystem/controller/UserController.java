package org.springboot.insurancemanagementsystem.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class UserController {

    private final UserService userService;

    @GetMapping("/customers")
    @PreAuthorize("hasRole('ADMIN')")
    public List<User> getCustomers() {
        return userService.getCustomers();
    }

    @GetMapping("/agents")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponseDto>> getAgents() {
        log.info("Admin requested agent list");

        List<UserResponseDto> agents = userService.getAgents();

        log.info("Retrieved {} agents", agents.size());

        return ResponseEntity.ok(agents);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserResponseDto>> getAllUsers(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size) {

        log.info("Admin requested user list. Page: {}, Size: {}", page, size);

        Page<UserResponseDto> users =
                userService.getAllUsers(page, size);

        log.info("Retrieved {} users", users.getNumberOfElements());

        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponseDto> getUserById(
            @PathVariable Long id) {

        log.info("Admin requested user details for ID: {}", id);

        UserResponseDto user =
                userService.getUserById(id);

        log.info("User details retrieved successfully for ID: {}", id);

        return ResponseEntity.ok(user);
    }

    @PatchMapping("/{id}/activate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> activateUser(
            @PathVariable Long id) {

        log.info("Admin attempting to activate user ID: {}", id);

        userService.activateUser(id);

        log.info("User activated successfully. ID: {}", id);

        return ResponseEntity.ok(
                "User activated successfully"
        );
    }

    @PatchMapping("/{id}/deactivate")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deactivateUser(
            @PathVariable Long id) {

        log.info("Admin attempting to deactivate user ID: {}", id);

        userService.deactivateUser(id);

        log.info("User deactivated successfully. ID: {}", id);

        return ResponseEntity.ok(
                "User deactivated successfully"
        );
    }
}
