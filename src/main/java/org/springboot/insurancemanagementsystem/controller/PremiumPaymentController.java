package org.springboot.insurancemanagementsystem.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springboot.insurancemanagementsystem.service.PremiumPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
public class PremiumPaymentController {

    private final PremiumPaymentService premiumPaymentService;

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> recordPayment(
            @Valid @RequestBody PaymentRequestDto request) {

        PaymentResponseDto response =
                premiumPaymentService.recordPayment(request);

        return new ResponseEntity<>(
                response,
                HttpStatus.CREATED);
    }

    @GetMapping("/{paymentId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<PaymentResponseDto> getPaymentById(
            @PathVariable Long paymentId) {

        return ResponseEntity.ok(
                premiumPaymentService.getPaymentById(paymentId));
    }

    @GetMapping("/policy/{policyId}")
    @PreAuthorize("hasAnyRole('ADMIN','AGENT','CUSTOMER')")
    public ResponseEntity<List<PaymentResponseDto>> getPolicyPayments(
            @PathVariable Long policyId) {

        return ResponseEntity.ok(
                premiumPaymentService.getPolicyPayments(policyId));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','AGENT')")
    public ResponseEntity<Page<PaymentResponseDto>> getAllPayments(

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size,

            @RequestParam(defaultValue = "id")
            String sortBy,

            @RequestParam(defaultValue = "desc")
            String sortDir) {

        return ResponseEntity.ok(
                premiumPaymentService.getAllPayments(
                        page,
                        size,
                        sortBy,
                        sortDir));
    }
}