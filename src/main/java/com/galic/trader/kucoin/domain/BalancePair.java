package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BalancePair {

    @JsonProperty("currency")
    private String currency;

    @JsonProperty("available")
    private BigDecimal available;

    @JsonProperty("reserved")
    private BigDecimal reserved;
}
