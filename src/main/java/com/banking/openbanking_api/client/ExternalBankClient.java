package com.banking.openbanking_api.client;


import com.banking.openbanking_api.dto.external.ExternalBalanceResponse;
import com.banking.openbanking_api.dto.external.ExternalPaymentRequest;
import com.banking.openbanking_api.dto.external.ExternalPaymentResponse;
import com.banking.openbanking_api.dto.external.ExternalTransactionResponse;
import com.banking.openbanking_api.exception.ExternalBankException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
public class ExternalBankClient {

    private final RestTemplate restTemplate;
    private final String baseUrl;

    public ExternalBankClient(
            RestTemplate restTemplate,
            @Value("${external.bank.base-url}") String baseUrl) {
        this.restTemplate = restTemplate;
        this.baseUrl = baseUrl;
    }

    public ExternalBalanceResponse getBalance(String iban) {
        String url = baseUrl + "/accounts/" + iban + "/balance";
        log.debug("Calling external bank API: GET {}", url);

        try {
            ResponseEntity<ExternalBalanceResponse> response = restTemplate.getForEntity(
                    url,
                    ExternalBalanceResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("External bank API error: {}", e.getMessage());
            throw new ExternalBankException("Failed to fetch balance from external bank", e);
        } catch (Exception e) {
            log.error("Unexpected error calling external bank: {}", e.getMessage());
            throw new ExternalBankException("Unexpected error communicating with external bank", e);
        }
    }

    public List<ExternalTransactionResponse> getTransactions(String iban) {
        String url = baseUrl + "/accounts/" + iban + "/transactions";
        log.debug("Calling external bank API: GET {}", url);

        try {
            ResponseEntity<List<ExternalTransactionResponse>> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    null,
                    new ParameterizedTypeReference<>() {}
            );
            return response.getBody();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            log.error("External bank API error: {}", e.getMessage());
            throw new ExternalBankException("Failed to fetch transactions from external bank", e);
        } catch (Exception e) {
            log.error("Unexpected error calling external bank: {}", e.getMessage());
            throw new ExternalBankException("Unexpected error communicating with external bank", e);
        }
    }

    public ExternalPaymentResponse initiatePayment(ExternalPaymentRequest request) {
        String url = baseUrl + "/payments";
        log.debug("Calling external bank API: POST {}", url);

        try {
            ResponseEntity<ExternalPaymentResponse> response = restTemplate.postForEntity(
                    url,
                    request,
                    ExternalPaymentResponse.class
            );
            return response.getBody();
        } catch (HttpClientErrorException e) {
            log.error("External bank client error: status={}, body={}",
                    e.getStatusCode(), e.getResponseBodyAsString());
            throw new ExternalBankException("External bank rejected payment: " + e.getResponseBodyAsString(), e);
        } catch (HttpServerErrorException e) {
            log.error("External bank server error: status={}", e.getStatusCode());
            throw new ExternalBankException("External bank service unavailable", e);
        } catch (Exception e) {
            log.error("Unexpected error calling external bank: {}", e.getMessage());
            throw new ExternalBankException("Unexpected error communicating with external bank", e);
        }
    }
}