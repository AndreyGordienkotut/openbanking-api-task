package com.banking.openbanking_api.dto.response;

import com.banking.openbanking_api.enums.PaymentStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Payment response after initiation")
public class PaymentResponse {

    @Schema(description = "Internal payment ID", example = "1001")
    private Long id;

    @Schema(description = "Sender IBAN", example = "UA123456789012345678901234567890")
    private String fromIban;

    @Schema(description = "Recipient IBAN", example = "UA111111111111111111111111111111")
    private String toIban;

    @Schema(description = "Payment amount", example = "100.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "EUR")
    private String currency;

    @Schema(description = "Payment status", example = "COMPLETED")
    private PaymentStatus status;

    @Schema(description = "Error message if payment failed", example = "Insufficient funds")
    private String errorMessage;

    @Schema(description = "External bank payment ID", example = "EXT-98765")
    private String externalPaymentId;

    @Schema(description = "Creation timestamp", example = "2026-04-01T00:00:00Z")
    private Instant createdAt;

    @Schema(description = "Last update timestamp", example = "2026-04-01T00:05:00Z")
    private Instant updatedAt;
}