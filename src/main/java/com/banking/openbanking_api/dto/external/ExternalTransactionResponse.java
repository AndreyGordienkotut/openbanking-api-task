package com.banking.openbanking_api.dto.external;

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
public class ExternalTransactionResponse {
    private String transactionId;
    private String type;
    private BigDecimal amount;
    private String currency;
    private String counterpartyIban;
    private String description;
    private Instant timestamp;
}