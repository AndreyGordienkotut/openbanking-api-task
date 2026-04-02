package com.banking.openbanking_api.service;

import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.exception.ExternalBankException;
import com.banking.openbanking_api.exception.InsufficientFundsException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MockBankServiceTest {

    @InjectMocks
    private MockBankService mockBankService;

    @Test
    @DisplayName("Should return balance for known IBAN")
    void shouldReturnBalanceForKnownIban() {
        String iban = "UA123456789012345678901234567890";

        ExternalBalanceResponse result = mockBankService.getBalance(iban);

        assertThat(result).isNotNull();
        assertThat(result.getIban()).isEqualTo(iban);
        assertThat(result.getAvailableBalance()).isEqualByComparingTo(new BigDecimal("5000.00"));
        assertThat(result.getCurrency()).isEqualTo("EUR");
    }

    @Test
    @DisplayName("Should return zero balance for unknown IBAN")
    void shouldReturnZeroBalanceForUnknownIban() {
        String iban = "UA999999999999999999999999999999";

        ExternalBalanceResponse result = mockBankService.getBalance(iban);

        assertThat(result).isNotNull();
        assertThat(result.getAvailableBalance()).isEqualByComparingTo(BigDecimal.ZERO);
    }

    @Test
    @DisplayName("Should return list of transactions")
    void shouldReturnListOfTransactions() {
        String iban = "UA123456789012345678901234567890";

        List<ExternalTransactionResponse> result = mockBankService.getTransactions(iban);

        assertThat(result).isNotNull();
        assertThat(result).hasSize(3);
        assertThat(result.get(0).getType()).isEqualTo("DEBIT");
        assertThat(result.get(1).getType()).isEqualTo("CREDIT");
    }

    @Test
    @DisplayName("Should process payment successfully when sufficient balance")
    void shouldProcessPaymentSuccessfullyWhenSufficientBalance() {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        ExternalPaymentResponse result = mockBankService.processPayment(request);

        assertThat(result).isNotNull();
        assertThat(result.getStatus()).isEqualTo("COMPLETED");
        assertThat(result.getPaymentId()).isNotNull();
    }

    @Test
    @DisplayName("Should throw exception when insufficient funds")
    void shouldThrowExceptionWhenInsufficientFunds() {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("UA987654321098765432109876543210")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("200.00"))
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> mockBankService.processPayment(request))
                .isInstanceOf(InsufficientFundsException.class)
                .hasMessageContaining("Not enough funds");
    }

    @Test
    @DisplayName("Should throw exception for simulated bank error")
    void shouldThrowExceptionForSimulatedBankError() {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("UA222222222222222222222222222222")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("30.00"))
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> mockBankService.processPayment(request))
                .isInstanceOf(ExternalBankException.class)
                .hasMessageContaining("External bank service temporarily unavailable");
    }

    @Test
    @DisplayName("Should throw exception when amount is negative")
    void shouldThrowExceptionWhenAmountIsNegative() {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("UA123456789012345678901234567890")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("-50.00"))
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> mockBankService.processPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Payment amount must be positive");
    }

    @Test
    @DisplayName("Should throw exception when from IBAN is blank")
    void shouldThrowExceptionWhenFromIbanIsBlank() {
        ExternalPaymentRequest request = ExternalPaymentRequest.builder()
                .fromIban("")
                .toIban("UA111111111111111111111111111111")
                .amount(new BigDecimal("100.00"))
                .currency("EUR")
                .build();

        assertThatThrownBy(() -> mockBankService.processPayment(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("From IBAN is required");
    }
}