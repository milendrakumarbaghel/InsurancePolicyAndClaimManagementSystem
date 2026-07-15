package org.springboot.insurancemanagementsystem.validator;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

@Target({ElementType.FIELD})
@Retention(value = RetentionPolicy.RUNTIME)
@Constraint(validatedBy = WithinLastDaysValidator.class)
@Documented
public @interface WithinLastDays {
    String message() default "Incident date must be within the last 15 days.";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
    int days() default 15;
}