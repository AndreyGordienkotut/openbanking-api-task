package com.banking.openbanking_api.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPaymentRequest {
    private String fromIban;
    private String toIban;
    private BigDecimal amount;
    private String currency;
}