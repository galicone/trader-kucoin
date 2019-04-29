package com.galic.trader.kucoin.util;

import com.galic.trader.kucoin.domain.Offer;
import com.galic.trader.kucoin.domain.OrderResponse;
import com.galic.trader.kucoin.service.OrderService;
import com.galic.trader.kucoin.service.PriceService;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;

@Component
public class AmountCalculator {

    private static final BigDecimal MINIMAL_AMOUNT = BigDecimal.valueOf(0.00000001);

    @Autowired
    private PriceService priceService;

    @Autowired
    private OrderService orderService;

    public BigDecimal calculateAmountToBuy(Coins coin, BigDecimal btcAmount, String buyOrderId, String sellOrderId) throws IOException {
        String currencyPair = coin.getPair();
        int pricePrecision = coin.getPricePrecision();

        BigDecimal bestBuyPrice = priceService.getBestBuyPrice(currencyPair, null, pricePrecision);
        BigDecimal buyPrice = bestBuyPrice.add(MINIMAL_AMOUNT);

        BigDecimal amountFromBuyOrder = BigDecimal.ZERO;
        if (buyOrderId != null) {
            LimitOrder buyOrder = orderService.getOrder(buyOrderId);
            if (buyOrder != null) {
                amountFromBuyOrder = buyOrder.getOriginalAmount();
            }
        }

        BigDecimal amountFromSellOrder = BigDecimal.ZERO;
        if (sellOrderId != null) {
            LimitOrder sellOrder = orderService.getOrder(sellOrderId);
            if (sellOrder != null) {
                amountFromSellOrder = sellOrder.getOriginalAmount();
            }
        }

        return btcAmount.divide(buyPrice, 3, RoundingMode.DOWN).subtract(amountFromBuyOrder).subtract(amountFromSellOrder);
    }
}
