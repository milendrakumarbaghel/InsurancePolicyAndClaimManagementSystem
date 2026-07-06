package org.springboot.insurancemanagementsystem.security.util;

import org.springframework.security.core.Authentication;

public class SecurityUtil {

    private SecurityUtil() {
        // Utility class - prevent instantiation
    }

    public static String extractRoleFromAuthentication(Authentication authentication) {
        if (authentication == null || authentication.getAuthorities() == null) {
            return "";
        }

        return authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");
    }
}
