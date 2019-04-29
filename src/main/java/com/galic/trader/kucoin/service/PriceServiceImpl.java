package com.galic.trader.kucoin.service;

import com.galic.trader.kucoin.util.AmountCalculator;
import com.galic.trader.kucoin.util.Coins;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.marketdata.OrderBook;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Comparator;
import java.util.NoSuchElementException;
import java.util.stream.Stream;

@Service
public class PriceServiceImpl implements PriceService {

    private static final BigDecimal MINIMUM_PERCENTAGE_PROFIT = BigDecimal.valueOf(1.03);

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private AmountCalculator amountCalculator;

    @Override
    public BigDecimal getBestSellPrice(String currencyPair, LimitOrder order, int pricePrecision) throws IOException {
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());
        OrderBook orderBook = marketDataService.getOrderBook(currencyPairFormatted);
        BigDecimal sellPrice;

        LimitOrder currentBestSellOrder = orderBook.getAsks().stream()
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .min(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        LimitOrder currentBestBuyOrder = orderBook.getBids().stream()
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .max(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        BigDecimal currentBestSellPrice = currentBestSellOrder.getLimitPrice();
        BigDecimal currentBestBuyPrice = currentBestBuyOrder.getLimitPrice();

        if (currentBestSellPrice.divide(currentBestBuyPrice, pricePrecision, RoundingMode.DOWN).compareTo(MINIMUM_PERCENTAGE_PROFIT) == 1) {
            sellPrice = currentBestSellPrice;
        } else {
            sellPrice = currentBestBuyPrice.multiply(MINIMUM_PERCENTAGE_PROFIT);
        }

        return sellPrice.setScale(pricePrecision, RoundingMode.DOWN);
    }

    @Override
    public BigDecimal getBestBuyPrice(String currencyPair, LimitOrder order, int pricePrecision) throws IOException {
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());
        OrderBook orderBook = marketDataService.getOrderBook(currencyPairFormatted);
        BigDecimal buyPrice;

        LimitOrder currentBestSellOrder = orderBook.getAsks().stream()
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .min(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        LimitOrder currentBestBuyOrder = orderBook.getBids().stream()
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .max(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        BigDecimal currentBestSellPrice = currentBestSellOrder.getLimitPrice();
        BigDecimal currentBestBuyPrice = currentBestBuyOrder.getLimitPrice();

        if (currentBestSellPrice.divide(currentBestBuyPrice, pricePrecision, RoundingMode.DOWN).compareTo(MINIMUM_PERCENTAGE_PROFIT) == 1) {
            buyPrice = currentBestBuyPrice;
        } else {
            buyPrice = currentBestSellPrice.multiply(BigDecimal.valueOf(2).subtract(MINIMUM_PERCENTAGE_PROFIT).abs());
        }

        return buyPrice.setScale(pricePrecision, RoundingMode.DOWN);
    }
}
