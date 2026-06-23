package org.springboot.insurancemanagementsystem.service;

public interface TokenBlacklistService {

    void blacklist(String token);

    boolean isBlacklisted(String token);
}
