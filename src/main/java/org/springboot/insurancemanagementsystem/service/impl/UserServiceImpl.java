package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springboot.insurancemanagementsystem.entitie.User;
import org.springboot.insurancemanagementsystem.enums.Role;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.UserRepository;
import org.springboot.insurancemanagementsystem.service.UserService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public Page<UserResponseDto> getAllUsers(
            int page,
            int size) {

        log.debug(
                "Fetching all users. page={}, size={}",
                page,
                size);

        Pageable pageable = PageRequest.of(
                page,
                size,
                Sort.by("createdAt").descending());

        return userRepository.findAll(pageable)
                .map(this::mapToResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public UserResponseDto getUserById(
            Long id) {

        log.debug(
                "Fetching user by id={}",
                id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "User not found with given id={}",
                            id);

                    return new ResourceNotFoundException(
                            "User not found with id : " + id);
                });

        return mapToResponse(user);
    }

    @Override
    public void activateUser(
            Long id) {

        log.info(
                "User activation requested. userId={}",
                id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "User not found with the id={}",
                            id);

                    return new ResourceNotFoundException(
                            "User not found with id : " + id);
                });

        user.setActive(true);

        userRepository.save(user);

        log.info(
                "User activated successfully. email={}",
                user.getEmail());
    }

    @Override
    public void deactivateUser(
            Long id) {

        log.info(
                "User deactivation requested. userId={}",
                id);

        User user = userRepository.findById(id)
                .orElseThrow(() -> {
                    log.warn(
                            "User not found with id={}",
                            id);

                    return new ResourceNotFoundException(
                            "User not found with id : " + id);
                });

        user.setActive(false);

        userRepository.save(user);

        log.info(
                "User deactivated successfully. email={}",
                user.getEmail());
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getCustomers() {
        return userRepository.findByRole(Role.CUSTOMER)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    @Override
    @Transactional(readOnly = true)
    public List<UserResponseDto> getAgents() {
        return userRepository.findByRole(Role.AGENT)
                .stream()
                .map(this::mapToResponse)
                .toList();
    }

    private UserResponseDto mapToResponse(
            User user) {

        return UserResponseDto.builder()
                .userId(user.getId())
                .fullName(user.getFullName())
                .email(user.getEmail())
                .mobileNumber(user.getMobileNumber())
                .role(user.getRole().name())
                .active(user.isActive())
                .build();
    }
}
