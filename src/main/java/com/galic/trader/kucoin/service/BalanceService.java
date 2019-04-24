package com.galic.trader.kucoin.service;

import org.knowm.xchange.currency.Currency;

import java.math.BigDecimal;

public interface BalanceService {

    BigDecimal getAvailableBalance(Currency currency);
}
