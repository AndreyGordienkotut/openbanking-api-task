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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private ExternalBankClient externalBankClient;

    @InjectMocks
    private PaymentService paymentService;

    @Test
    @DisplayName("Should complete payment successfully when sufficient balance")
    void shouldCompletePaymentSuccessfullyWhenSufficientBalance() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .createdAt(Instant.now())
                .build();

        ExternalBalanceResponse balanceResponse = ExternalBalanceResponse.builder()
                .iban(request.getFromIban())
                .availableBalance(new BigDecimal("5000.00"))
                .currency("EUR")
                .build();

        ExternalPaymentResponse externalResponse = ExternalPaymentResponse.builder()
                .paymentId("ext-payment-123")
                .status("COMPLETED")
                .message("Success")
                .processedAt(Instant.now())
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(externalBankClient.getBalance(request.getFromIban())).thenReturn(balanceResponse);
        when(externalBankClient.initiatePayment(any(ExternalPaymentRequest.class)))
                .thenReturn(externalResponse);

        PaymentResponse result = paymentService.initiatePayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo(PaymentStatus.COMPLETED);
        assertThat(result.getExternalPaymentId()).isEqualTo("ext-payment-123");

        verify(paymentRepository, times(2)).save(any(Payment.class));
        verify(externalBankClient, times(1)).getBalance(request.getFromIban());
        verify(externalBankClient, times(1)).initiatePayment(any(ExternalPaymentRequest.class));
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA987654321098765432109876543210")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();

        ExternalBalanceResponse balanceResponse = ExternalBalanceResponse.builder()
                .iban(request.getFromIban())
                .availableBalance(new BigDecimal("150.00"))
                .currency("EUR")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(externalBankClient.getBalance(request.getFromIban())).thenReturn(balanceResponse);

        assertThatThrownBy(() -> paymentService.initiatePayment(request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Insufficient funds");

        verify(paymentRepository, times(1)).save(any(Payment.class));
        verify(externalBankClient, times(1)).getBalance(request.getFromIban());
        verify(externalBankClient, never()).initiatePayment(any(ExternalPaymentRequest.class));
    }

    @Test
    @DisplayName("Should mark payment as failed when external bank throws exception")
    void shouldMarkPaymentAsFailedWhenExternalBankThrowsException() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA222222222222222222222222222222")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("30.00"))
                .currency("EUR")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();

        ExternalBalanceResponse balanceResponse = ExternalBalanceResponse.builder()
                .iban(request.getFromIban())
                .availableBalance(new BigDecimal("50.00"))
                .currency("EUR")
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(externalBankClient.getBalance(request.getFromIban())).thenReturn(balanceResponse);
        when(externalBankClient.initiatePayment(any(ExternalPaymentRequest.class)))
                .thenThrow(new ExternalBankException("External bank service temporarily unavailable"));

        assertThatThrownBy(() -> paymentService.initiatePayment(request))
                .isInstanceOf(ExternalBankException.class)
                .hasMessageContaining("External bank service temporarily unavailable");

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, times(2)).save(paymentCaptor.capture());

        Payment failedPayment = paymentCaptor.getValue();
        assertThat(failedPayment.getStatus()).isEqualTo(PaymentStatus.FAILED);
        assertThat(failedPayment.getErrorMessage()).contains("External bank service temporarily unavailable");
    }

    @Test
    @DisplayName("Should create pending payment before external call")
    void shouldCreatePendingPaymentBeforeExternalCall() {
        PaymentInitiateRequest request = PaymentInitiateRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        Payment savedPayment = Payment.builder()
                .id(1L)
                .fromIban(request.getFromIban())
                .toIban(request.getToIban())
                .amount(request.getAmount())
                .currency(request.getCurrency())
                .status(PaymentStatus.PENDING)
                .build();

        when(paymentRepository.save(any(Payment.class))).thenReturn(savedPayment);
        when(externalBankClient.getBalance(request.getFromIban()))
                .thenReturn(ExternalBalanceResponse.builder()
                        .iban(request.getFromIban())
                        .availableBalance(new BigDecimal("5000.00"))
                        .currency("EUR")
                        .build());
        when(externalBankClient.initiatePayment(any(ExternalPaymentRequest.class)))
                .thenReturn(ExternalPaymentResponse.builder()
                        .paymentId("ext-123")
                        .status("COMPLETED")
                        .build());

        paymentService.initiatePayment(request);

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository, atLeastOnce()).save(paymentCaptor.capture());

        Payment firstSave = paymentCaptor.getAllValues().get(0);
        assertThat(firstSave.getStatus()).isEqualTo(PaymentStatus.PENDING);
    }
}