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
public class ExternalBalanceResponse {
    private String iban;
    private BigDecimal availableBalance;
    private String currency;
}