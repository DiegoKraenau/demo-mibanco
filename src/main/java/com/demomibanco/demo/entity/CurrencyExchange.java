package com.demomibanco.demo.entity;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

@Data
@Document(collection = "currency_exchanges")
public class CurrencyExchange {

    @Id
    private String id;
    private String sourceCurrency;
    private String targetCurrency;
    private BigDecimal exchangeRate;
}
