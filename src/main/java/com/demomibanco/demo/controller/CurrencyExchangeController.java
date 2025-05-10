package com.demomibanco.demo.controller;

import com.demomibanco.demo.service.CurrencyExchangeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;

@RestController
@RequestMapping("/api/currency-exchange")
@RequiredArgsConstructor
@Tag(name = "Currency Exchange", description = "Operaciones de conversión de divisas")
public class CurrencyExchangeController {

    private final CurrencyExchangeService currencyExchangeService;

    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    @Operation(summary = "Convierte un monto de una moneda a otra", description = "Este endpoint convierte una cantidad de una moneda fuente a una moneda objetivo utilizando una tasa de cambio actual.")
    public Mono<Double> convertCurrency(
            @Parameter(description = "Moneda de origen (ej: USD)", required = true) @RequestParam("sourceCurrency") String sourceCurrency,

            @Parameter(description = "Moneda de destino (ej: PEN)", required = true) @RequestParam("targetCurrency") String targetCurrency,

            @Parameter(description = "Cantidad a convertir", required = true) @RequestParam("amount") double amount) {
        return currencyExchangeService.convertCurrency(sourceCurrency, targetCurrency, amount);
    }
}
