package com.galic.trader.kucoin.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.knowm.xchange.currency.Currency;

@AllArgsConstructor
@Getter
public enum Coins {

    BTC("BTC", new Currency("BTC"), 8),
    CPC("CPC", new Currency("CPC"), 8),
    EBTC("EBTC", new Currency("EBTC"), 8),
    BCD("BCD", new Currency("BCD"), 8),
    CXO("CXO", new Currency("CXO"), 8),
    DCC("DCC", new Currency("DCC"), 10),
    MVP("MVP", new Currency("MVP"), 10);

    private String pair;
    private Currency name;
    private int pricePrecision;


}
