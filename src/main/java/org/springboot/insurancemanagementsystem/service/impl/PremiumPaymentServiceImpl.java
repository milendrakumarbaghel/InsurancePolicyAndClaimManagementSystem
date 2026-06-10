package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springboot.insurancemanagementsystem.entitie.Policy;
import org.springboot.insurancemanagementsystem.entitie.PolicyPlan;
import org.springboot.insurancemanagementsystem.entitie.PremiumPayment;
import org.springboot.insurancemanagementsystem.enums.PaymentMode;
import org.springboot.insurancemanagementsystem.enums.PaymentStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.enums.Role;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
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

        log.info("Payment request received for policyNumber={}",
                request.getPolicyNumber());

        Policy policy =
                policyRepository.findByPolicyNumber(
                                request.getPolicyNumber())
                        .orElseThrow(() -> {
                            log.warn("Policy not found with number={}",
                                    request.getPolicyNumber());
                            return new ResourceNotFoundException(
                                    "Policy not found");
                        });

        Optional<PolicyPlan> planOptional =
                policyPlanRepository.findById(
                        request.getPolicyPlanId());

        if (planOptional.isEmpty()) {

            log.warn("Policy plan not found with id={}",
                    request.getPolicyPlanId());

            throw new ResourceNotFoundException(
                    "Policy plan not found with Id: "
                            + request.getPolicyPlanId());
        }

        PolicyPlan plan = planOptional.get();

        Double premiumAmount =
                plan.getPremiumAmount();

        if (!premiumAmount.equals(request.getAmount())) {

            log.warn(
                    "Invalid payment amount. Expected={}, Received={}",
                    premiumAmount,
                    request.getAmount());

            throw new BusinessException(
                    "Payment amount must be equal to Premium Amount: "
                            + premiumAmount);
        }

        String transactionRef =
                "TXN-" + System.currentTimeMillis();

        log.debug("Generated transaction reference={}",
                transactionRef);

        if (paymentRepository.existsByTransactionReference(
                transactionRef)) {

            log.warn("Duplicate transaction reference detected={}",
                    transactionRef);

            throw new BusinessException(
                    "Duplicate transaction reference");
        }

        Double totalPremiumPaid =
                policy.getTotalPremiumPaid();

        PremiumPayment payment =
                new PremiumPayment();

        payment.setPolicy(policy);

        payment.setAmount(
                request.getAmount());

        payment.setPaymentMode(
                PaymentMode.valueOf(
                        request.getPaymentMode()));

        payment.setTransactionReference(
                transactionRef);
        payment.setCreatedAt(LocalDateTime.now());

        PremiumPayment savedPayment =
                paymentRepository.save(payment);

        if (totalPremiumPaid <
                (savedPayment.getAmount() + totalPremiumPaid)) {

            payment.setStatus(
                    PaymentStatus.SUCCESS);

            log.info(
                    "Payment successful. TransactionRef={}, Amount={}",
                    transactionRef,
                    savedPayment.getAmount());

        } else {

            payment.setStatus(
                    PaymentStatus.FAILED);

            log.warn(
                    "Payment failed. TransactionRef={}",
                    transactionRef);
        }

        if (savedPayment.getStatus()
                == PaymentStatus.SUCCESS) {

            Double totalPaid =
                    policy.getTotalPremiumPaid()
                            + request.getAmount();

            policy.setTotalPremiumPaid(
                    totalPaid);

            if (policy.getStatus()
                    == PolicyStatus.PENDING_PAYMENT) {

                policy.setStatus(
                        PolicyStatus.ACTIVE);

                log.info(
                        "Policy activated after successful payment. PolicyNumber={}",
                        policy.getPolicyNumber());
            }

            policyRepository.save(policy);

            log.info(
                    "Policy payment updated. PolicyNumber={}, TotalPaid={}",
                    policy.getPolicyNumber(),
                    totalPaid);
        }

        return mapToResponseDto(savedPayment);
    }

    @Override
    public PaymentResponseDto getPaymentById(
            Long paymentId, String email) {

        log.debug("Fetching payment by id={}",
                paymentId);

        PremiumPayment payment =
                paymentRepository.findById(
                                paymentId)
                        .orElseThrow(() -> {
                            log.warn(
                                    "Payment not found with id={}",
                                    paymentId);

                            return new ResourceNotFoundException(
                                    "Payment not found");
                        });

        if (payment.getPolicy().getCustomer().getUser().getRole().equals(Role.CUSTOMER)) {
            if (payment.getPolicy().getCustomer().getUser().getEmail().equals(email)) {
                throw new BusinessException("Access denied. You can only view your own payment details.");
            }
        }

        return mapToResponseDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPolicyPayments(
            Long policyId, String email) {

        log.debug(
                "Fetching payment history for policyId={}",
                policyId);
        List<PremiumPayment> payments =
                paymentRepository.findByPolicy_Id(policyId);

        if (payments.isEmpty()) {
            throw new ResourceNotFoundException(
                    "No payments found for policy id: " + policyId);
        }

        PremiumPayment payment = payments.getFirst();

        if (payment.getPolicy()
                .getCustomer()
                .getUser()
                .getRole()
                .equals(Role.CUSTOMER)
                && !payment.getPolicy()
                .getCustomer()
                .getUser()
                .getEmail()
                .equals(email)) {

            throw new BusinessException(
                    "You are not authorized to access payments for this policy.");
        }

        return payments.stream()
                .map(this::mapToResponseDto)
                .collect(Collectors.toList());
    }

    @Override
    public Page<PaymentResponseDto> getAllPayments(
            int page,
            int size,
            String sortBy,
            String sortDir) {

        log.debug(
                "Fetching all payments. page={}, size={}, sortBy={}, sortDir={}",
                page,
                size,
                sortBy,
                sortDir);

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
        dto.setPaymentId(payment.getId());

        dto.setPolicyNumber(
                payment.getPolicy()
                        .getPolicyNumber());

        if (payment.getPaymentMode() != null) {

            dto.setPaymentMode(
                    payment.getPaymentMode()
                            .name());
        }

        if (payment.getStatus() != null) {

            dto.setStatus(
                    payment.getStatus()
                            .name());
        }

        return dto;
    }
}