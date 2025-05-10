package com.demomibanco.demo.repository;

import com.demomibanco.demo.entity.CurrencyExchange;
import org.springframework.data.mongodb.repository.ReactiveMongoRepository;
import reactor.core.publisher.Mono;

public interface CurrencyExchangeRepository extends ReactiveMongoRepository<CurrencyExchange, String> {
    Mono<CurrencyExchange> findBySourceCurrencyAndTargetCurrency(String sourceCurrency, String targetCurrency);
}
