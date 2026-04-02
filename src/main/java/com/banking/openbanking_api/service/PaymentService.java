package com.banking.openbanking_api.service;

import com.banking.openbanking_api.client.ExternalBankClient;
import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.request.PaymentInitiateRequest;
import com.banking.openbanking_api.dto.response.PaymentResponse;
import com.banking.openbanking_api.entity.Payment;
import com.banking.openbanking_api.enums.PaymentStatus;
import com.banking.openbanking_api.exception.ExternalBankException;
import com.banking.openbanking_api.exception.InsufficientFundsException;
import com.banking.openbanking_api.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final ExternalBankClient externalBankClient;

    public PaymentResponse initiatePayment(PaymentInitiateRequest request) {
        log.info("Initiating payment: from={}, to={}, amount={}",
                request.getFromIban(), request.getToIban(), request.getAmount());

        Payment payment = createPendingPayment(request);

        ExternalBalanceResponse balance = externalBankClient.getBalance(request.getFromIban());
        validateBalance(balance.getAvailableBalance(), request.getAmount());

        try {
            ExternalPaymentRequest externalRequest = buildExternalPaymentRequest(request);
            ExternalPaymentResponse externalResponse = externalBankClient.initiatePayment(externalRequest);

            completePayment(payment, externalResponse);
            log.info("Payment completed successfully: paymentId={}", payment.getId());

        } catch (ExternalBankException e) {
            failPayment(payment, e.getMessage());
            log.error("Payment failed due to external bank error: paymentId={}", payment.getId());
            throw e;
        } catch (Exception e) {
            failPayment(payment, "Unexpected error: " + e.getMessage());
            log.error("Payment failed due to unexpected error: paymentId={}", payment.getId(), e);
            throw new ExternalBankException("Payment processing failed", e);
        }

        return mapToPaymentResponse(payment);
    }

    @Transactional
    protected Payment createPendingPayment(PaymentInitiateRequest request) {
        Payment payment = Payment.builder()
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();

        payment = paymentRepository.save(payment);
        log.debug("Payment saved with PENDING status: id={}", payment.getId());

        return payment;
    }

    private void validateBalance(BigDecimal availableBalance, BigDecimal requestedAmount) {
        if (availableBalance.compareTo(requestedAmount) < 0) {
            String message = String.format("Insufficient funds. Available: %s, Required: %s",
                    availableBalance, requestedAmount);
            log.warn(message);
            throw new InsufficientFundsException(message);
        }
    }

    @Transactional
    protected void completePayment(Payment payment, ExternalPaymentResponse externalResponse) {
        payment.setStatus(PaymentStatus.COMPLETED);
        payment.setExternalPaymentId(externalResponse.getPaymentId());
        paymentRepository.save(payment);
    }

    @Transactional
    protected void failPayment(Payment payment, String errorMessage) {
        payment.setStatus(PaymentStatus.FAILED);
        payment.setErrorMessage(errorMessage);
        paymentRepository.save(payment);
    }

    private ExternalPaymentRequest buildExternalPaymentRequest(PaymentInitiateRequest request) {
        return ExternalPaymentRequest.builder()
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .build();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .fromIban(payment.getFromIban())
                .toIban(payment.getToIban())
                .amount(payment.getAmount())
                .currency(payment.getCurrency())
                .status(payment.getStatus())
                .errorMessage(payment.getErrorMessage())
                .externalPaymentId(payment.getExternalPaymentId())
                .createdAt(payment.getCreatedAt())
                .updatedAt(payment.getUpdatedAt())
                .build();
    }
}