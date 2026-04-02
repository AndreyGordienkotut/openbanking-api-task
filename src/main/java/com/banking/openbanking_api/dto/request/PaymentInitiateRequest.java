package com.banking.openbanking_api.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment initiation request")
public class PaymentInitiateRequest {

    @NotBlank(message = "From IBAN is required")
    @Schema(description = "Sender IBAN", example = "UA123456789012345678901234567890")
    private String fromIban;

    @NotBlank(message = "To IBAN is required")
    @Schema(description = "Recipient IBAN", example = "UA111111111111111111111111111111")
    private String toIban;

    @NotNull(message = "Amount is required")
    @Positive(message = "Amount must be positive")
    @Schema(description = "Payment amount", example = "100.00")
    private BigDecimal amount;

    @NotBlank(message = "Currency is required")
    @Schema(description = "Currency code", example = "EUR")
    private String currency;
}