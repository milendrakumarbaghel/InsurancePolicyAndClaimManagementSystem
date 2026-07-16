package org.springboot.insurancemanagementsystem.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.modelmapper.ModelMapper;
import org.springboot.insurancemanagementsystem.dto.PaymentRequestDto;
import org.springboot.insurancemanagementsystem.dto.PaymentResponseDto;
import org.springboot.insurancemanagementsystem.entitie.*;
import org.springboot.insurancemanagementsystem.enums.PaymentMode;
import org.springboot.insurancemanagementsystem.enums.PaymentStatus;
import org.springboot.insurancemanagementsystem.enums.PolicyStatus;
import org.springboot.insurancemanagementsystem.exception.BusinessException;
import org.springboot.insurancemanagementsystem.exception.ResourceNotFoundException;
import org.springboot.insurancemanagementsystem.repository.CustomerRepository;
import org.springboot.insurancemanagementsystem.repository.PolicyRepository;
import org.springboot.insurancemanagementsystem.repository.PremiumPaymentRepository;
import org.springboot.insurancemanagementsystem.service.PremiumPaymentService;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class PremiumPaymentServiceImpl
        implements PremiumPaymentService {

    private final PremiumPaymentRepository paymentRepository;
    private final PolicyRepository policyRepository;
    private final CustomerRepository customerRepository;
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

        // Use the premium that was calculated and stored when this policy was purchased.
        // This is always consistent with what the customer agreed to.
        Double premiumAmount = policy.getCalculatedPremiumAmount();

        if (premiumAmount == null) {
            log.error("Policy {} has no calculatedPremiumAmount – data integrity issue.",
                    policy.getPolicyNumber());
            throw new BusinessException(
                    "Cannot determine the premium for this policy. Please contact support.");
        }

        if (!premiumAmount.equals(request.getAmount())) {

            log.warn(
                    "Invalid payment amount. Expected={}, Received={}",
                    premiumAmount,
                    request.getAmount());

            throw new BusinessException(
                    "Payment amount must be equal to Premium Amount: "
                            + premiumAmount);
        }

        // ── Prevent duplicate payment for the same installment period ──
        // Use the customer's chosen cycle (not the plan default)
        var effectivePremiumType = policy.getSelectedPremiumType() != null
                ? policy.getSelectedPremiumType()
                : policy.getPlan().getPremiumType();

        LocalDateTime[] period = computeInstallmentPeriod(effectivePremiumType, LocalDate.now());

        boolean alreadyPaid = paymentRepository
                .existsByPolicy_IdAndStatusAndPaymentDateBetween(
                        policy.getId(),
                        PaymentStatus.SUCCESS,
                        period[0], period[1]);

        if (alreadyPaid) {
            String cycleName = effectivePremiumType.name()
                    .replace("_", " ").toLowerCase();
            log.warn(
                    "Duplicate payment blocked for policyNumber={}, cycle={}",
                    request.getPolicyNumber(), cycleName);
            throw new BusinessException(
                    "A premium payment for the current "
                            + cycleName
                            + " installment has already been recorded for this policy.");
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
        payment.setPaymentDate(LocalDateTime.now());

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
            Long paymentId, String email, String role) {

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

        User user = payment.getPolicy().getCustomer().getUser();
        if ("CUSTOMER".equals(role) && !user.getEmail().equals(email)) {
            throw new BusinessException("Access denied. You can only view your own payment details.");
        }

        return mapToResponseDto(payment);
    }

    @Override
    public List<PaymentResponseDto> getPolicyPayments(
            Long policyId, String email, String role) {

        log.info("Fetching payment history for policyId={}", policyId);
        Policy policy = policyRepository.findById(policyId)
                .orElseThrow(() -> new ResourceNotFoundException("Policy not found"));

        if ("CUSTOMER".equals(role)
                && !policy.getCustomer().getUser().getEmail().equals(email)) {
            throw new BusinessException("You are not authorized to access payments for this policy.");
        }

        return paymentRepository.findByPolicy_Id(policyId)
                .stream()
                .map(this::mapToResponseDto)
                .toList();
    }

    @Override
    public Page<PaymentResponseDto> getAllPayments(int page, int size, String sortBy, String sortDir, String email, String role) {
        log.info("Fetching payments history block request by user context: {}", email);

        Sort sort = sortDir.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        Pageable pageable = PageRequest.of(page, size, sort);

        if ("CUSTOMER".equals(role)) {
            Customer customer = customerRepository.findByUserEmail(email)
                    .orElseThrow(() -> new ResourceNotFoundException("Customer profile not found"));

            return paymentRepository.findByPolicy_Customer_Id(customer.getId(), pageable)
                    .map(this::mapToResponseDto);
        }

        return paymentRepository.findAll(pageable).map(this::mapToResponseDto);
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

    /**
     * Returns a two-element array [periodStart, periodEnd] representing
     * the current installment window for the given premium type.
     */
    private LocalDateTime[] computeInstallmentPeriod(
            org.springboot.insurancemanagementsystem.enums.PremiumType premiumType,
            LocalDate today) {

        LocalDate start;
        LocalDate end;

        switch (premiumType) {
            case MONTHLY:
                start = today.withDayOfMonth(1);
                end = YearMonth.from(today).atEndOfMonth();
                break;
            case QUARTERLY:
                int quarterStartMonth = ((today.getMonthValue() - 1) / 3) * 3 + 1;
                start = LocalDate.of(today.getYear(), quarterStartMonth, 1);
                end = start.plusMonths(3).minusDays(1);
                break;
            case HALF_YEARLY:
                int halfStartMonth = today.getMonthValue() <= 6 ? 1 : 7;
                start = LocalDate.of(today.getYear(), halfStartMonth, 1);
                end = start.plusMonths(6).minusDays(1);
                break;
            case ANNUAL:
                start = LocalDate.of(today.getYear(), 1, 1);
                end = LocalDate.of(today.getYear(), 12, 31);
                break;
            default:
                throw new IllegalArgumentException(
                        "Unsupported premium type: " + premiumType);
        }

        return new LocalDateTime[]{
                start.atStartOfDay(),
                end.atTime(23, 59, 59)
        };
    }
}