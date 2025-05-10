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

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CurrencyExchangeServiceTest {

    @Mock
    private CurrencyExchangeRepository currencyExchangeRepository;

    @InjectMocks
    private CurrencyExchangeService currencyExchangeService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        currencyExchangeService.initCache();
    }

    @Test
    void testConvertCurrency_WhenExchangeRateExists() {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        double amount = 100.0;

        CurrencyExchange exchangeRate = new CurrencyExchange();
        exchangeRate.setSourceCurrency(sourceCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setExchangeRate(BigDecimal.valueOf(0.85));

        when(currencyExchangeRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency))
                .thenReturn(Mono.just(exchangeRate));

        Double result = currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount).block();

        assertEquals(85.0, result);
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

        assertThrows(ResponseStatusException.class, () -> {
            currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount).block();
        });

        verify(currencyExchangeRepository, times(1))
                .findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency);
    }

    @Test
    void testConvertCurrency_UsesCacheAfterFirstCall() {
        String sourceCurrency = "USD";
        String targetCurrency = "EUR";
        double amount = 200.0;

        CurrencyExchange exchangeRate = new CurrencyExchange();
        exchangeRate.setSourceCurrency(sourceCurrency);
        exchangeRate.setTargetCurrency(targetCurrency);
        exchangeRate.setExchangeRate(BigDecimal.valueOf(0.85));

        when(currencyExchangeRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency))
                .thenReturn(Mono.just(exchangeRate));

        Double result1 = currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount).block();
        assertEquals(170.0, result1);

        Double result2 = currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount).block();
        assertEquals(170.0, result2);

        verify(currencyExchangeRepository, times(1))
                .findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency);
    }
}
