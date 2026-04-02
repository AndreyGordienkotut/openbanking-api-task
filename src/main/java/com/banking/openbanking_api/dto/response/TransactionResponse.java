package com.banking.openbanking_api.dto.response;

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
@Schema(description = "Transaction response with details of a single transaction")
public class TransactionResponse {

    @Schema(description = "Transaction ID", example = "TX-123456")
    private String transactionId;

    @Schema(description = "Transaction type (debit/credit)", example = "DEBIT")
    private String type;

    @Schema(description = "Transaction amount", example = "50.00")
    private BigDecimal amount;

    @Schema(description = "Currency code", example = "EUR")
    private String currency;

    @Schema(description = "Counterparty IBAN", example = "UA222222222222222222222222222222")
    private String counterpartyIban;

    @Schema(description = "Transaction description", example = "Payment for invoice #123")
    private String description;

    @Schema(description = "Transaction timestamp", example = "2026-04-01T00:10:00Z")
    private Instant timestamp;
}