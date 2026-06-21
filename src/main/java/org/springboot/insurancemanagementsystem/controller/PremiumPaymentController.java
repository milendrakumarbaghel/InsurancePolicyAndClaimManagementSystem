package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springboot.insurancemanagementsystem.service.PremiumPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Slf4j
@CrossOrigin(origins = "http://localhost:5173")
public class PremiumPaymentController {

    private final PremiumPaymentService premiumPaymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> recordPayment(
            @Valid @RequestBody PaymentRequestDto request) {

        log.info(
                "Payment request received for policy number: {}",
                request.getPolicyNumber()
        );

        PaymentResponseDto response =
                premiumPaymentService.recordPayment(request);

        log.info(
                "Payment recorded successfully. Payment ID: {}, Transaction Ref: {}",
                response.getPaymentId(),
                response.getTransactionReference()
        );

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> getPaymentById(
            @PathVariable Long paymentId,  Authentication authentication) {

        log.info(
                "Fetching payment details for paymentId: {}",
                paymentId
        );

        PaymentResponseDto response =
                premiumPaymentService.getPaymentById(paymentId, authentication.getName());

        log.info(
                "Payment details retrieved successfully for paymentId: {}",
                paymentId
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<List<PaymentResponseDto>> getPolicyPayments(
            @PathVariable Long policyId,Authentication authentication) {

        log.info(
                "Fetching payment history for policyId: {}",
                policyId
        );

        List<PaymentResponseDto> payments =
                premiumPaymentService.getPolicyPayments(policyId,authentication.getName());

        log.info(
                "Retrieved {} payment records for policyId: {}",
                payments.size(),
                policyId
        );

        return ResponseEntity.ok(payments);
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir,
            Authentication authentication) {

        log.info(
                "Fetching all payments | page: {}, size: {}, sortBy: {}, sortDir: {}",
                page,
                size,
                sortBy,
                sortDir
        );

        String email = authentication.getName();
        String role = authentication.getAuthorities().stream()
                .map(grantedAuthority -> grantedAuthority.getAuthority().replace("ROLE_", ""))
                .findFirst()
                .orElse("");

        Page<PaymentResponseDto> paymentsPage = premiumPaymentService.getAllPayments(
                page, size, sortBy, sortDir, email, role);

        log.info(
                "Retrieved {} payment records",
                paymentsPage.getNumberOfElements()
        );

        return ResponseEntity.ok(paymentsPage);
    }
}