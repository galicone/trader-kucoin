package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
public class OrderResult {

    @JsonProperty("id")
    private String id;

    @JsonProperty("side")
    private String side;

    @JsonProperty("status")
    private String status;

    @JsonProperty("type")
    private String type;

    @JsonProperty("timeInForce")
    private String timeInForce;

    @JsonProperty("quantity")
    private Double quantity;

    @JsonProperty("price")
    private Double price;

    @JsonProperty("cumQuantity")
    private String cumQuantity;

    @JsonProperty("createdAt")
    private String createdAt;

    @JsonProperty("updatedAt")
    private String updatedAt;

    @JsonProperty("stopPrice")
    private Double stopPrice;

    @JsonProperty("expireTime")
    private String expireTime;
}
