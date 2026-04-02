package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.service.MockBankService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(MockBankController.class)
@AutoConfigureMockMvc(addFilters = false)
class MockBankControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MockBankService mockBankService;

    @Test
    @DisplayName("Should return balance from mock bank")
    void shouldReturnBalanceFromMockBank() throws Exception {
        String iban = "UA123456789012345678901234567890";
        ExternalBalanceResponse response = ExternalBalanceResponse.builder()
                .iban(iban)
                .availableBalance(new BigDecimal("5000.00"))
                .currency("EUR")
                .build();

        when(mockBankService.getBalance(iban)).thenReturn(response);

        mockMvc.perform(get("/mock-bank/accounts/{iban}/balance", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.iban").value(iban))
                .andExpect(jsonPath("$.availableBalance").value(5000.00))
                .andExpect(jsonPath("$.currency").value("EUR"));

        verify(mockBankService, times(1)).getBalance(iban);
    }

    @Test
    @DisplayName("Should return transactions from mock bank")
    void shouldReturnTransactionsFromMockBank() throws Exception {
        String iban = "UA123456789012345678901234567890";
        List<ExternalTransactionResponse> transactions = List.of(
                ExternalTransactionResponse.builder()
                        .transactionId("tx-1")
                        .type("DEBIT")
                        .amount(new BigDecimal("100.00"))
                        .currency("EUR")
                        .counterpartyIban("DE89370400440532013000")
                        .description("Purchase")
                        .timestamp(Instant.now())
                        .build()
        );

        when(mockBankService.getTransactions(iban)).thenReturn(transactions);

        mockMvc.perform(get("/mock-bank/accounts/{iban}/transactions", iban))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].transactionId").value("tx-1"))
                .andExpect(jsonPath("$[0].type").value("DEBIT"));

        verify(mockBankService, times(1)).getTransactions(iban);
    }

    @Test
    @DisplayName("Should process payment in mock bank")
    void shouldProcessPaymentInMockBank() throws Exception {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        ExternalPaymentResponse response = ExternalPaymentResponse.builder()
                .paymentId("ext-123")
                .status("COMPLETED")
                .message("Payment processed successfully")
                .processedAt(Instant.now())
                .build();

        when(mockBankService.processPayment(any(ExternalPaymentRequest.class))).thenReturn(response);

        mockMvc.perform(post("/mock-bank/payments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.paymentId").value("ext-123"))
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.message").value("Payment processed successfully"));

        verify(mockBankService, times(1)).processPayment(any(ExternalPaymentRequest.class));
    }
}