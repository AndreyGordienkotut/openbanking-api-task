package com.banking.openbanking_api.dto.response;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Balance response with current account balance")
public class BalanceResponse {

    @Schema(description = "Account IBAN", example = "UA123456789012345678901234567890")
    private String iban;

    @Schema(description = "Current balance", example = "2500.50")
    private BigDecimal balance;

    @Schema(description = "Currency code", example = "EUR")
    private String currency;
}