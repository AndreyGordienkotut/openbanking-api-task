package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.request.PaymentInitiateRequest;
import com.banking.openbanking_api.dto.response.PaymentResponse;
import com.banking.openbanking_api.enums.PaymentStatus;
import com.banking.openbanking_api.exception.InsufficientFundsException;
import com.banking.openbanking_api.service.PaymentService;
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

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(PaymentController.class)
@AutoConfigureMockMvc(addFilters = false)
class PaymentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PaymentService paymentService;

    @Test
    @DisplayName("Should return 201 when payment is successful")
    void shouldReturn201WhenPaymentIsSuccessful() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        PaymentResponse response = PaymentResponse.builder()
                .id(1L)
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.COMPLETED)
                .externalPaymentId("ext-123")
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(paymentService.initiatePayment(any(PaymentInitiateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value("COMPLETED"))
                .andExpect(jsonPath("$.fromIban").value(request.getFromIban()))
                .andExpect(jsonPath("$.amount").value(100.00))
                .andExpect(jsonPath("$.externalPaymentId").value("ext-123"));

        verify(paymentService, times(1)).initiatePayment(any(PaymentInitiateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when validation fails")
    void shouldReturn400WhenValidationFails() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(paymentService, never()).initiatePayment(any());
    }

    @Test
    @DisplayName("Should return 400 when insufficient funds")
    void shouldReturn400WhenInsufficientFunds() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA987654321098765432109876543210")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .build();

        when(paymentService.initiatePayment(any(PaymentInitiateRequest.class)))
                .thenThrow(new InsufficientFundsException("Insufficient funds. Available: 150.00, Required: 200.00"));

        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("INSUFFICIENT_FUNDS"))
                .andExpect(jsonPath("$.message").value(containsString("Insufficient funds")));

        verify(paymentService, times(1)).initiatePayment(any(PaymentInitiateRequest.class));
    }

    @Test
    @DisplayName("Should return 400 when amount is negative")
    void shouldReturn400WhenAmountIsNegative() throws Exception {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("-50.00"))
                .currency("EUR")
                .build();

        mockMvc.perform(post("/api/payments/initiate")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.error").value("VALIDATION_ERROR"));

        verify(paymentService, never()).initiatePayment(any());
    }
}