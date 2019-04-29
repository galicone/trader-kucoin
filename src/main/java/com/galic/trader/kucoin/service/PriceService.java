package com.galic.trader.kucoin.service;

import com.galic.trader.kucoin.domain.OrderResponse;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;

import java.io.IOException;
import java.math.BigDecimal;

public interface PriceService {

    BigDecimal getBestSellPrice(String currencyPair, LimitOrder order, int pricePrecision) throws IOException;

    BigDecimal getBestBuyPrice(String currencyPair, LimitOrder order, int pricePrecision) throws IOException;
}
