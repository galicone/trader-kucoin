package com.galic.trader.kucoin.service;

import com.galic.trader.kucoin.domain.OrderRequest;
import com.galic.trader.kucoin.domain.OrderResponse;
import org.knowm.xchange.dto.trade.LimitOrder;

public interface OrderService {

    String placeOrder(OrderRequest orderRequest);

    void cancelOrder(String orderId, String currencyPair);

    LimitOrder getOrder(String orderId);
}
