package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.response.BalanceResponse;
import com.banking.openbanking_api.dto.response.TransactionResponse;
import com.banking.openbanking_api.service.AccountService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AccountController.class)
@AutoConfigureMockMvc(addFilters = false)
class AccountControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private AccountService accountService;

    @Test
    @DisplayName("Should return balance when IBAN is valid")
    void shouldReturnBalanceWhenIbanIsValid() throws Exception {
        String iban = "UA123456789012345678901234567890";
        BalanceResponse response = BalanceResponse.builder()
                .iban(iban)
                .balance(new BigDecimal("5000.00"))
                .currency("EUR")
                .build();

        when(accountService.getBalance(iban)).thenReturn(response);

        mockMvc.perform(get("/api/accounts/{iban}/balance", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.balance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(accountService, times(1)).getBalance(iban);
    }

    @Test
    @DisplayName("Should return transactions when IBAN is valid")
    void shouldReturnTransactionsWhenIbanIsValid() throws Exception {
        String iban = "UA123456789012345678901234567890";
        List<TransactionResponse> transactions = List.of(
                TransactionResponse.builder()
                        .transactionId("tx-1")
                        .type("DEBIT")
                        .amount(new BigDecimal("100.00"))
                        .currency("EUR")
                        .counterpartyIban("DE89370400440532013000")
                        .description("Purchase")
                        .timestamp(Instant.now())
                        .build(),
                TransactionResponse.builder()
                        .transactionId("tx-2")
                        .type("CREDIT")
                        .amount(new BigDecimal("500.00"))
                        .currency("EUR")
                        .counterpartyIban("FR1420041010050500013M02606")
                        .description("Salary")
                        .timestamp(Instant.now())
                        .build()
        );

        when(accountService.getTransactions(iban)).thenReturn(transactions);

        mockMvc.perform(get("/api/accounts/{iban}/transactions", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[0].transactionId").value("tx-1"))
                .andExpect(jsonPath("$[0].type").value("DEBIT"))
                .andExpect(jsonPath("$[1].transactionId").value("tx-2"))
                .andExpect(jsonPath("$[1].type").value("CREDIT"));

        verify(accountService, times(1)).getTransactions(iban);
    }
}