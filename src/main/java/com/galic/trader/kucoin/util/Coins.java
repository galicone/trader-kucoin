package com.galic.trader.kucoin.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knowm.xchange.currency.Currency;

@AllArgsConstructor
@Getter
public enum Coins {

    BTC("BTC", new Currency("BTC")),
    CPC("CPC", new Currency("CPC")),
    EBTC("EBTC", new Currency("EBTC")),
    BCD("BCD", new Currency("BCD")),
    CXO("CXO", new Currency("CXO"));

    private String pair;
    private Currency name;


}
