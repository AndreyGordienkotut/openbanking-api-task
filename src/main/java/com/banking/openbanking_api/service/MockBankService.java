package com.banking.openbanking_api.service;

import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.exception.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;


import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Slf4j
@Service
public class MockBankService {

    private static final Map<String, BigDecimal> ACCOUNT_BALANCES = Map.of(
            "UA123456789012345678901234567890", new BigDecimal("5000.00"),
            "UA987654321098765432109876543210", new BigDecimal("150.00"),
            "UA111111111111111111111111111111", new BigDecimal("10000.00"),
            "UA222222222222222222222222222222", new BigDecimal("50.00")
    );

    public ExternalBalanceResponse getBalance(String iban) {
        log.debug("Fetching balance for IBAN: {}", iban);

        BigDecimal balance = ACCOUNT_BALANCES.getOrDefault(iban, BigDecimal.ZERO);

        return ExternalBalanceResponse.builder()
                .iban(iban)
                .availableBalance(balance)
                .currency("EUR")
                .build();
    }

    public List<ExternalTransactionResponse> getTransactions(String iban) {
        log.debug("Fetching transactions for IBAN: {}", iban);
        return List.of(
                ExternalTransactionResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .type("DEBIT")
                        .amount(new BigDecimal("100.00"))
                        .currency("EUR")
                        .counterpartyIban("DE89370400440532013000")
                        .description("Online purchase")
                        .timestamp(Instant.now().minusSeconds(86400))
                        .build(),
                ExternalTransactionResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .type("CREDIT")
                        .amount(new BigDecimal("500.00"))
                        .currency("EUR")
                        .counterpartyIban("FR1420041010050500013M02606")
                        .description("Salary")
                        .timestamp(Instant.now().minusSeconds(172800))
                        .build(),
                ExternalTransactionResponse.builder()
                        .transactionId(UUID.randomUUID().toString())
                        .type("DEBIT")
                        .amount(new BigDecimal("50.00"))
                        .currency("EUR")
                        .counterpartyIban("GB29NWBK60161331926819")
                        .description("Subscription fee")
                        .timestamp(Instant.now().minusSeconds(259200))
                        .build()
        );
    }

    public ExternalPaymentResponse processPayment(ExternalPaymentRequest request) {
        log.info("Processing payment: from={}, to={}, amount={}",
                request.getFromIban(), request.getToIban(), request.getAmount());

        validatePaymentRequest(request);

        BigDecimal balance = ACCOUNT_BALANCES.getOrDefault(request.getFromIban(), BigDecimal.ZERO);

        if (balance.compareTo(request.getAmount()) < 0) {
            log.warn("Insufficient funds for IBAN: {}. Balance: {}, Required: {}",
                    request.getFromIban(), balance, request.getAmount());
            throw new InsufficientFundsException(
                    String.format("Not enough funds. Available: %s, Required: %s", balance, request.getAmount())
            );
        }

        if ("UA222222222222222222222222222222".equals(request.getFromIban())) {
            log.error("Simulating bank error for IBAN: {}", request.getFromIban());
            throw new ExternalBankException("External bank service temporarily unavailable");
        }

        String paymentId = UUID.randomUUID().toString();
        log.info("Payment processed successfully. PaymentId: {}", paymentId);

        return ExternalPaymentResponse.builder()
                .paymentId(paymentId)
                .status("COMPLETED")
                .message("Payment processed successfully")
                .processedAt(Instant.now())
                .build();
    }

    private void validatePaymentRequest(ExternalPaymentRequest request) {
        if (request.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Payment amount must be positive");
        }
        if (request.getFromIban() == null || request.getFromIban().isBlank()) {
            throw new IllegalArgumentException("From IBAN is required");
        }
        if (request.getToIban() == null || request.getToIban().isBlank()) {
            throw new IllegalArgumentException("To IBAN is required");
        }
    }
}