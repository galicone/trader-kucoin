package com.galic.trader.kucoin.service;

import com.galic.trader.kucoin.domain.OrderRequest;
import com.galic.trader.kucoin.domain.OrderResponse;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.dto.trade.OpenOrders;
import org.knowm.xchange.exceptions.ExchangeException;
import org.knowm.xchange.kucoin.service.KucoinApiException;
import org.knowm.xchange.service.trade.TradeService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.NoSuchElementException;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {

    @Autowired
    private TradeService tradeService;

    @Override
    public String placeOrder(OrderRequest orderRequest) {
        try {
            LimitOrder limitOrder =
                    new LimitOrder.Builder(orderRequest.getOrderType(), orderRequest.getCurrencyPair())
                            .limitPrice(orderRequest.getPrice())
                            // Multiplied with 0.99 to prevent insufitient funds
                            .originalAmount(orderRequest.getQuantity().multiply(BigDecimal.valueOf(0.99)).setScale(3, RoundingMode.DOWN))
                            .build();

            String orderId = tradeService.placeLimitOrder(limitOrder);

            return orderId;
        } catch (Exception e) {
            log.error("Error occured on placing an order {}. Order request {}", e, orderRequest);
            // TODO Check this
            // throw  e;
        }

        return null;
    }

    @Override
    public void cancelOrder(String orderId, String currencyPair) {
        try {
            if (getOrder(orderId) == null) {
                log.info("-----------> ORDER IS MOST PROBABLY REALISED FOR {} <-----------", currencyPair);
            }
            tradeService.cancelOrder(orderId);
        } catch (ExchangeException e) {
            if ((e.getCause() instanceof  KucoinApiException) && ((KucoinApiException) e.getCause()).getCode() == "400100") {
                log.info("-----------> ORDER IS MOST PROBABLY REALISED FOR {} <-----------", currencyPair);
            }
        } catch (Exception e) {
            log.error("Order does not exists. Error {}", e);
        }
    }

    @Override
    public LimitOrder getOrder(String orderId) {
        ResponseEntity<OrderResponse> response = null;
        try {
            OpenOrders orders = tradeService.getOpenOrders();
            LimitOrder order = (LimitOrder) orders.getAllOpenOrders().stream()
                    .filter(o -> o.getId().equals(orderId))
                    .findFirst()
                    .orElseThrow(NoSuchElementException::new);

            return order;
        } catch (Exception e) {
            log.info("Order {} not found", orderId);
            return null;
        }
    }
}
