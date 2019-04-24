package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class OrderResponse {

    @JsonProperty("clientOrderId")
    private String clientOrderId;

    @JsonProperty("symbol")
    private String symbol;

    @JsonProperty("side")
    private String side;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("timeInForce")
    private String timeInForce;

    @JsonProperty("quantity")
    private BigDecimal quantity;

    @JsonProperty("price")
    private BigDecimal price;

    @JsonProperty("cumQuantity")
    private BigDecimal cumQuantity;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("stopPrice")
    private BigDecimal stopPrice;

    @JsonProperty("expireTime")
    private String expireTime;
}
