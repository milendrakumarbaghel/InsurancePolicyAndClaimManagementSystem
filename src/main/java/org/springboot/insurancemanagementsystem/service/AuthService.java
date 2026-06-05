package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.LoginRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginResponseDto;
import org.springboot.insurancemanagementsystem.dto.RegisterRequestDto;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);

    UserResponseDto createAgent(RegisterRequestDto request);

}
