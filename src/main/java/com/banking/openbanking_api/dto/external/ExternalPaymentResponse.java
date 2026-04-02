package com.banking.openbanking_api.dto.external;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ExternalPaymentResponse {
    private String paymentId;
    private String status;
    private String message;
    private Instant processedAt;
}