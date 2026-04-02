package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.service.MockBankService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/mock-bank")
@RequiredArgsConstructor
@Tag(name = "Mock External Bank", description = "Simulated PSD2 external banking service for testing")
public class MockBankController {

    private final MockBankService mockBankService;

    @GetMapping("/accounts/{iban}/balance")
    @Operation(
            summary = "Get balance (Mock)",
            description = "Mock endpoint simulating external bank balance retrieval"
    )
    public ResponseEntity<ExternalBalanceResponse> getBalance(
            @Parameter(description = "Account IBAN", example = "UA123456789012345678901234567890")
            @PathVariable String iban) {
        ExternalBalanceResponse response = mockBankService.getBalance(iban);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/accounts/{iban}/transactions")
    @Operation(
            summary = "Get transactions (Mock)",
            description = "Mock endpoint simulating external bank transaction retrieval"
    )
    public ResponseEntity<List<ExternalTransactionResponse>> getTransactions(
            @Parameter(description = "Account IBAN", example = "UA123456789012345678901234567890")
            @PathVariable String iban) {
        List<ExternalTransactionResponse> transactions = mockBankService.getTransactions(iban);
        return ResponseEntity.ok(transactions);
    }

    @PostMapping("/payments")
    @Operation(
            summary = "Process payment (Mock)",
            description = "Mock endpoint simulating external bank payment processing"
    )
    public ResponseEntity<ExternalPaymentResponse> processPayment(
            @RequestBody ExternalPaymentRequest request) {
        ExternalPaymentResponse response = mockBankService.processPayment(request);
        return ResponseEntity.ok(response);
    }
}