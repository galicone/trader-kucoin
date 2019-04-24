package com.galic.trader.kucoin.domain;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class Orderbook {

    @JsonProperty("ask")
    private List<Offer> ask;

    @JsonProperty("bid")
    private List<Offer> bid;
}
