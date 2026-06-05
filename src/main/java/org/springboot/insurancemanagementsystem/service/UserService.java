package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.UserResponseDto;
import org.springframework.data.domain.Page;

public interface UserService {

    Page<UserResponseDto> getAllUsers(int page, int size);

    UserResponseDto getUserById(Long id);

    void activateUser(Long id);

    void deactivateUser(Long id);

}
