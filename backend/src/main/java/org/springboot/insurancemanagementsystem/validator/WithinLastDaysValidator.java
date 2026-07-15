package org.springboot.insurancemanagementsystem.validator;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import java.time.LocalDate;

public class WithinLastDaysValidator implements ConstraintValidator<WithinLastDays, LocalDate> {
    private int days;

    @Override
    public void initialize(WithinLastDays constraintAnnotation) {
        this.days = constraintAnnotation.days();
    }

    @Override
    public boolean isValid(LocalDate value, ConstraintValidatorContext context) {
        if (value == null) {
            return true; // Covered by @NotNull if required
        }
        LocalDate today = LocalDate.now();
        LocalDate limit = today.minusDays(days);
        return !value.isBefore(limit) && !value.isAfter(today);
    }
}