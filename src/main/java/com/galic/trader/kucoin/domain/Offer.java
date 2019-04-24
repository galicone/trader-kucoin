package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class Offer {

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("amount")
    private BigDecimal amount;
}
