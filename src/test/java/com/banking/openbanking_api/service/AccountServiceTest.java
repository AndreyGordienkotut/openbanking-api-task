package com.banking.openbanking_api.service;

import com.banking.openbanking_api.client.ExternalBankClient;
import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.dto.response.BalanceResponse;
import com.banking.openbanking_api.dto.response.TransactionResponse;
import com.banking.openbanking_api.exception.ExternalBankException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AccountServiceTest {

    @Mock
    private ExternalBankClient externalBankClient;

    @InjectMocks
    private AccountService accountService;

    @Test
    @DisplayName("Should return balance when external bank responds successfully")
    void shouldReturnBalanceWhenExternalBankRespondsSuccessfully() {
        String iban = "UA123456789012345678901234567890";
        ExternalBalanceResponse externalResponse = ExternalBalanceResponse.builder()
                .iban(iban)
                .availableBalance(new BigDecimal("5000.00"))
                .currency("EUR")
                .build();

        when(externalBankClient.getBalance(iban)).thenReturn(externalResponse);

        BalanceResponse result = accountService.getBalance(iban);

        assertThat(result).isNotNull();
        assertThat(result.getIban()).isEqualTo(iban);
        assertThat(result.getBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getCurrency()).isEqualTo("EUR");

        verify(externalBankClient, times(1)).getBalance(iban);
    }

    @Test
    @DisplayName("Should throw exception when external bank fails to fetch balance")
    void shouldThrowExceptionWhenExternalBankFailsToFetchBalance() {
        String iban = "UA123456789012345678901234567890";

        when(externalBankClient.getBalance(iban))
                .thenThrow(new ExternalBankException("External bank service unavailable"));

        assertThatThrownBy(() -> accountService.getBalance(iban))
                .isInstanceOf(ExternalBankException.class)
                .hasMessageContaining("External bank service unavailable");

        verify(externalBankClient, times(1)).getBalance(iban);
    }

    @Test
    @DisplayName("Should return list of transactions when external bank responds successfully")
    void shouldReturnListOfTransactionsWhenExternalBankRespondsSuccessfully() {
        String iban = "UA123456789012345678901234567890";
        List<ExternalTransactionResponse> externalTransactions = List.of(
                ExternalTransactionResponse.builder()
                        .transactionId("tx-1")
                        .type("DEBIT")
                        .amount(new BigDecimal("100.00"))
                        .currency("EUR")
                        .counterpartyIban("DE89370400440532013000")
                        .description("Purchase")
                        .timestamp(Instant.now())
                        .build(),
                ExternalTransactionResponse.builder()
                        .transactionId("tx-2")
                        .type("CREDIT")
                        .amount(new BigDecimal("500.00"))
                        .currency("EUR")
                        .counterpartyIban("FR1420041010050500013M02606")
                        .description("Salary")
                        .timestamp(Instant.now())
                        .build()
        );

        when(externalBankClient.getTransactions(iban)).thenReturn(externalTransactions);

        List<TransactionResponse> result = accountService.getTransactions(iban);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getTransactionId()).isEqualTo("tx-1");
        assertThat(result.get(0).getType()).isEqualTo("DEBIT");
        assertThat(result.get(1).getTransactionId()).isEqualTo("tx-2");
        assertThat(result.get(1).getType()).isEqualTo("CREDIT");

        verify(externalBankClient, times(1)).getTransactions(iban);
    }

    @Test
    @DisplayName("Should throw exception when external bank fails to fetch transactions")
    void shouldThrowExceptionWhenExternalBankFailsToFetchTransactions() {
        String iban = "UA123456789012345678901234567890";

        when(externalBankClient.getTransactions(iban))
                .thenThrow(new ExternalBankException("External bank service unavailable"));

        assertThatThrownBy(() -> accountService.getTransactions(iban))
                .isInstanceOf(ExternalBankException.class)
                .hasMessageContaining("External bank service unavailable");

        verify(externalBankClient, times(1)).getTransactions(iban);
    }
}