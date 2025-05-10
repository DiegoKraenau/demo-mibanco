package com.demomibanco.demo.controller;

import com.demomibanco.demo.service.CurrencyExchangeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/currency-exchange")
@RequiredArgsConstructor
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @GetMapping()
    @ResponseStatus(HttpStatus.OK)
    public Mono<Double> convertCurrency(
            @RequestParam("sourceCurrency") String sourceCurrency,
            @RequestParam("targetCurrency") String targetCurrency,
            @RequestParam("amount") double amount) {
        return currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount);
    }
}
