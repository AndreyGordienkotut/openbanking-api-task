package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.response.BalanceResponse;
import com.banking.openbanking_api.dto.response.TransactionResponse;
import com.banking.openbanking_api.service.AccountService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;


import com.banking.openbanking_api.exception.ErrorResponse;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;

import org.springframework.web.bind.annotation.*;



@RestController
@RequestMapping("/api/accounts")
@RequiredArgsConstructor
@Tag(name = "Account API", description = "Retrieve account balance and transactions from external bank")
public class AccountController {

    private final AccountService accountService;

    @GetMapping("/{iban}/balance")
    @Operation(
            summary = "Get account balance",
            description = "Retrieve current account balance from external PSD2 banking service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Balance retrieved successfully",
                    content = @Content(schema = @Schema(implementation = BalanceResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External bank service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<BalanceResponse> getBalance(
            @Parameter(description = "Account IBAN", example = "UA123456789012345678901234567890")
            @PathVariable String iban) {
        BalanceResponse balance = accountService.getBalance(iban);
        return ResponseEntity.ok(balance);
    }

    @GetMapping("/{iban}/transactions")
    @Operation(
            summary = "Get recent transactions",
            description = "Retrieve last 10 transactions from external PSD2 banking service"
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "200",
                    description = "Transactions retrieved successfully",
                    content = @Content(schema = @Schema(implementation = TransactionResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External bank service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<List<TransactionResponse>> getTransactions(
            @Parameter(description = "Account IBAN", example = "UA123456789012345678901234567890")
            @PathVariable String iban) {
        List<TransactionResponse> transactions = accountService.getTransactions(iban);
        return ResponseEntity.ok(transactions);
    }
}