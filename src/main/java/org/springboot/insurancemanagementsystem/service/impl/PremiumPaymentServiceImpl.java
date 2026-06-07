package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springboot.insurancemanagementsystem.entitie.PremiumPayment;
import org.springboot.insurancemanagementsystem.enums.PaymentMode;
import org.springboot.insurancemanagementsystem.enums.PaymentStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.PolicyPlanRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.repository.PremiumPaymentRepository;
import org.springboot.insurancemanagementsystem.service.PremiumPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PremiumPaymentServiceImpl
        implements PremiumPaymentService {

    private final PremiumPaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final PolicyPlanRepository policyPlanRepository;
    private final ModelMapper modelMapper;

    @Override
    @Transactional
    public PaymentResponseDto recordPayment(
            PaymentRequestDto request) {

        Policy policy =
                policyRepository.findByPolicyNumber(
                                request.getPolicyNumber())
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Policy not found"));
        Optional<PolicyPlan> byId = policyPlanRepository.findById(request.getPolicyPlanId());
        if (byId.isEmpty()) {
            throw new ResourceNotFoundException("Policy plan not found with Id: " + request.getPolicyPlanId());
        }

        Double premiumAmount = byId.get().getPremiumAmount();
        if (premiumAmount > request.getAmount()
                || premiumAmount < request.getAmount()) {
            throw new BusinessException("Payment amount must be equal to Premium Amount: " + premiumAmount);
        }

        String transactionRef =
                "TXN-" + System.currentTimeMillis();

        if (paymentRepository.existsByTransactionReference(
                transactionRef)) {

            throw new BusinessException(
                    "Duplicate transaction reference");
        }

        Double totalPremiumPaid = policy.getTotalPremiumPaid();
        PremiumPayment payment =
                new PremiumPayment();

        payment.setPolicy(policy);
        payment.setAmount(request.getAmount());
        payment.setPaymentMode(
                PaymentMode.valueOf(request.getPaymentMode()));


        payment.setTransactionReference(
                transactionRef);

        PremiumPayment savedPayment =
                paymentRepository.save(payment);

        if (totalPremiumPaid < (savedPayment.getAmount() + totalPremiumPaid)) {
            payment.setStatus(
                    PaymentStatus.valueOf("SUCCESS"));
        } else {
            payment.setStatus(PaymentStatus.valueOf("FAILED"));
        }


        if (savedPayment.getStatus()
                == PaymentStatus.SUCCESS) {

            Double totalPaid = policy.getTotalPremiumPaid() + request.getAmount();

            policy.setTotalPremiumPaid(totalPaid);

            if (policy.getStatus()
                    == PolicyStatus.PENDING_PAYMENT) {

                policy.setStatus(
                        PolicyStatus.ACTIVE);
            }

            policyRepository.save(policy);
        }

        return mapToResponseDto(savedPayment);
    }

    @Override
    public PaymentResponseDto getPaymentById(
            Long paymentId) {

        PremiumPayment payment =
                paymentRepository.findById(
                                paymentId)
                        .orElseThrow(() ->
                                new ResourceNotFoundException(
                                        "Payment not found"));

        return mapToResponseDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPolicyPayments(
            Long policyId) {

        return paymentRepository
                .findByPolicy_Id(policyId)
                .stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PaymentResponseDto> getAllPayments(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        Sort sort =
                sortDir.equalsIgnoreCase("asc")
                        ? Sort.by(sortBy).ascending()
                        : Sort.by(sortBy).descending();

        Pageable pageable =
                PageRequest.of(page, size, sort);

        return paymentRepository
                .findAll(pageable)
                .map(this::mapToResponseDto);
    }

    private PaymentResponseDto mapToResponseDto(
            PremiumPayment payment) {

        PaymentResponseDto dto =
                modelMapper.map(
                        payment,
                        PaymentResponseDto.class);

        dto.setPolicyNumber(
                payment.getPolicy().getPolicyNumber());

        if (payment.getPaymentMode() != null) {
            dto.setPaymentMode(
                    payment.getPaymentMode().name());
        }

        if (payment.getStatus() != null) {
            dto.setStatus(
                    payment.getStatus().name());
        }

        return dto;
    }
}
