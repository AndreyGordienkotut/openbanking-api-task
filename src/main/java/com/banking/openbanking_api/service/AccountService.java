package com.banking.openbanking_api.service;

import com.banking.openbanking_api.client.ExternalBankClient;
import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.dto.response.BalanceResponse;
import com.banking.openbanking_api.dto.response.TransactionResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AccountService {

    private final ExternalBankClient externalBankClient;

    public BalanceResponse getBalance(String iban) {
        log.info("Fetching balance for IBAN: {}", iban);

        ExternalBalanceResponse externalBalance = externalBankClient.getBalance(iban);

        return BalanceResponse.builder()
                .iban(externalBalance.getIban())
                .balance(externalBalance.getAvailableBalance())
                .currency(externalBalance.getCurrency())
                .build();
    }

    public List<TransactionResponse> getTransactions(String iban) {
        log.info("Fetching transactions for IBAN: {}", iban);

        List<ExternalTransactionResponse> externalTransactions =
                externalBankClient.getTransactions(iban);

        return externalTransactions.stream()
                .map(this::mapToTransactionResponse)
                .collect(Collectors.toList());
    }

    private TransactionResponse mapToTransactionResponse(ExternalTransactionResponse external) {
        return TransactionResponse.builder()
                .transactionId(external.getTransactionId())
                .type(external.getType())
                .amount(external.getAmount())
                .currency(external.getCurrency())
                .counterpartyIban(external.getCounterpartyIban())
                .description(external.getDescription())
                .timestamp(external.getTimestamp())
                .build();
    }
}