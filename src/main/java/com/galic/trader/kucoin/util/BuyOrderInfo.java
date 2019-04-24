package com.galic.trader.kucoin.util;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class BuyOrderInfo {

    private String clientOrderId;
    private BigDecimal buyOrderPrice;
}
