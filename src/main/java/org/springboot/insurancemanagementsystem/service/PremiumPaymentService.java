package org.springboot.insurancemanagementsystem.service;

import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springframework.data.domain.Page;

import java.util.List;

public interface PremiumPaymentService {

    PaymentResponseDto recordPayment(PaymentRequestDto request);

    PaymentResponseDto getPaymentById(Long paymentId);

    List<PaymentResponseDto> getPolicyPayments(Long policyId);

    Page<PaymentResponseDto> getAllPayments(int page, int size, String sortBy, String sortDir);
}
