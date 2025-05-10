package com.demomibanco.demo.service;

import com.demomibanco.demo.entity.CurrencyExchange;
import com.demomibanco.demo.repository.CurrencyExchangeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import org.springframework.web.server.ResponseStatusException;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class CurrencyExchangeServiceTest {

    @Mock
    private CurrencyExchangeRepository currencyExchangeRepository;

    @InjectMocks
    private CurrencyExchangeService currencyExchangeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testConvertCurrency_WhenExchangeRateExists() {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        double amount = 100.0;

        CurrencyExchange exchangeRate = new CurrencyExchange();
        exchangeRate.setSourceCurrency("USD");
        exchangeRate.setTargetCurrency("EUR");
        exchangeRate.setExchangeRate(BigDecimal.valueOf(3.85));

        when(currencyExchangeRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency))
                .thenReturn(Mono.just(exchangeRate));

        Mono<Double> convertedAmountMono = currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency,
                amount);

        convertedAmountMono.subscribe(convertedAmount -> {
            assertEquals(85.0, convertedAmount);
        });

        verify(currencyExchangeRepository, times(1))
                .findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency);
    }

    @Test
    void testConvertCurrency_WhenExchangeRateNotFound() {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        double amount = 100.0;

        when(currencyExchangeRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency))
                .thenReturn(Mono.empty());

        Mono<Double> convertedAmountMono = currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency,
                amount);

        assertThrows(ResponseStatusException.class, () -> {
            convertedAmountMono.block();
        });

        verify(currencyExchangeRepository, times(1))
                .findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency);
    }
}
