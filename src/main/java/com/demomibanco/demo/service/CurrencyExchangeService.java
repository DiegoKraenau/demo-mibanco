package com.demomibanco.demo.service;

import com.demomibanco.demo.entity.CurrencyExchange;
import com.demomibanco.demo.repository.CurrencyExchangeRepository;

import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.math.BigDecimal;
import java.util.concurrent.TimeUnit;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;

import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
@Slf4j
public class CurrencyExchangeService {

    private final CurrencyExchangeRepository currencyExchangeRepository;

    private Cache<String, CurrencyExchange> exchangeCache;

    @PostConstruct
    public void initCache() {
        exchangeCache = Caffeine.newBuilder()
                .expireAfterWrite(5, TimeUnit.MINUTES)
                .maximumSize(500)
                .build();
    }

    private String cacheKey(String source, String target) {
        return source + "->" + target;
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getExchangeRateFallback")
    public Mono<CurrencyExchange> getExchangeRate(String sourceCurrency, String targetCurrency) {
        String key = cacheKey(sourceCurrency, targetCurrency);
        CurrencyExchange cached = exchangeCache.getIfPresent(key);

        if (cached != null) {
            log.info("Tipo de cambio obtenido desde caché: {}", key);
            return Mono.just(cached);
        }

        log.info("Buscando tasa de cambio para {} -> {}", sourceCurrency, targetCurrency);

        return currencyExchangeRepository.findBySourceCurrencyAndTargetCurrency(sourceCurrency, targetCurrency)
                .doOnNext(rate -> {
                    log.info("Guardando en caché el tipo de cambio: {}", key);
                    exchangeCache.put(key, rate);
                })
                .doOnError(error -> log.error("Error al buscar tipo de cambio para {} -> {}: {}", sourceCurrency,
                        targetCurrency, error.getMessage()))
                .switchIfEmpty(
                        Mono.defer(() -> {
                            log.warn("Tipo de cambio no encontrado para {} -> {}", sourceCurrency, targetCurrency);
                            return Mono.error(
                                    new ResponseStatusException(HttpStatus.NOT_FOUND, "Tipo de cambio no encontrado"));
                        }));
    }

    @CircuitBreaker(name = "default", fallbackMethod = "getExchangeRateFallback")
    public Mono<Double> convertCurrency(String sourceCurrency, String targetCurrency, double amount) {
        log.info("Convirtiendo {} {} a {}", amount, sourceCurrency, targetCurrency);

        return getExchangeRate(sourceCurrency, targetCurrency)
                .map(exchangeRate -> {
                    double convertedAmount = amount * exchangeRate.getExchangeRate().doubleValue();
                    log.info("Monto convertido: {} -> {} = {}", amount, targetCurrency, convertedAmount);
                    return convertedAmount;
                })
                .doOnError(error -> log.error("Error al convertir moneda: {}", error.getMessage()));
    }

    public Mono<CurrencyExchange> getExchangeRateFallback(Throwable t) {
        log.error("Error al obtener tasa de cambio, ejecutando fallback: {}", t.getMessage());

        CurrencyExchange defaultExchange = new CurrencyExchange();
        defaultExchange.setSourceCurrency("USD");
        defaultExchange.setTargetCurrency("EUR");
        defaultExchange.setExchangeRate(BigDecimal.valueOf(1.0));

        return Mono.just(defaultExchange);
    }
}
