package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.LoginRequestDto;
import org.springboot.insurancemanagementsystem.dto.LoginResponseDto;
import org.springboot.insurancemanagementsystem.dto.LogoutRequestDto;
import org.springboot.insurancemanagementsystem.dto.ForgotPasswordRequestDto;
import org.springboot.insurancemanagementsystem.dto.RefreshTokenRequestDto;
import org.springboot.insurancemanagementsystem.dto.RegisterRequestDto;
import org.springboot.insurancemanagementsystem.dto.ResetPasswordRequestDto;
import org.springboot.insurancemanagementsystem.dto.UserResponseDto;

public interface AuthService {

    UserResponseDto register(RegisterRequestDto request);

    LoginResponseDto login(LoginRequestDto request);

    LoginResponseDto refreshToken(RefreshTokenRequestDto request);

    void logout(String authorizationHeader, LogoutRequestDto request);

    void forgotPassword(ForgotPasswordRequestDto request);

    void resetPassword(ResetPasswordRequestDto request);

    UserResponseDto createAgent(RegisterRequestDto request);

}
