package com.banking.openbanking_api.controller;

import com.banking.openbanking_api.dto.request.PaymentInitiateRequest;
import com.banking.openbanking_api.dto.response.PaymentResponse;
import com.banking.openbanking_api.exception.ErrorResponse;
import com.banking.openbanking_api.service.PaymentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/payments")
@RequiredArgsConstructor
@Tag(name = "Payment API", description = "Initiate and manage IBAN-to-IBAN payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/initiate")
    @Operation(
            summary = "Initiate payment",
            description = "Create and process a new IBAN-to-IBAN payment. Checks balance before execution."
    )
    @ApiResponses(value = {
            @ApiResponse(
                    responseCode = "201",
                    description = "Payment initiated and completed successfully",
                    content = @Content(schema = @Schema(implementation = PaymentResponse.class))
            ),
            @ApiResponse(
                    responseCode = "400",
                    description = "Validation error or insufficient funds",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            ),
            @ApiResponse(
                    responseCode = "503",
                    description = "External bank service unavailable",
                    content = @Content(schema = @Schema(implementation = ErrorResponse.class))
            )
    })
    public ResponseEntity<PaymentResponse> initiatePayment(
            @io.swagger.v3.oas.annotations.parameters.RequestBody(
                    description = "Payment details",
                    required = true,
                    content = @Content(schema = @Schema(implementation = PaymentInitiateRequest.class))
            )
            @Valid @RequestBody PaymentInitiateRequest request) {

        PaymentResponse response = paymentService.initiatePayment(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }
}