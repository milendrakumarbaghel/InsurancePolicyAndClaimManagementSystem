package org.springboot.insurancemanagementsystem.exception;

import lombok.Getter;

@Getter
public class UserInactiveException extends RuntimeException {

    private final boolean emailVerified;
    private final boolean mobileVerified;

    public UserInactiveException(String message) {
        super(message);
        this.emailVerified = true;
        this.mobileVerified = true;
    }

    public UserInactiveException(String message, boolean emailVerified, boolean mobileVerified) {
        super(message);
        this.emailVerified = emailVerified;
        this.mobileVerified = mobileVerified;
    }
}
