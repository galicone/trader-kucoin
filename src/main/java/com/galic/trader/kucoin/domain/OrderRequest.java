package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.ToString;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;

import java.math.BigDecimal;

@Data
@Builder
@ToString
@JsonIgnoreProperties(ignoreUnknown = true)
public class OrderRequest {

    private CurrencyPair currencyPair;
    private Order.OrderType orderType;
    private BigDecimal quantity;
    private BigDecimal price;
}
