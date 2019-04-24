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

    @Autowired
    private MarketDataService marketDataService;

    @Autowired
    private AmountCalculator amountCalculator;

    @Override
    public BigDecimal getBestSellPrice(String currencyPair, LimitOrder order) throws IOException {
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());
        OrderBook orderBook = marketDataService.getOrderBook(currencyPairFormatted);

        LimitOrder currentBestOffer = orderBook.getAsks().stream()
                .filter(offer -> amountCalculator.doesOrderHasEnoughVolume(offer))
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .min(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        LimitOrder offerBeforeBestOffer = orderBook.getAsks().stream()
                .filter(offer -> amountCalculator.doesOrderHasEnoughVolume(offer))
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .filter(offer -> offer.getLimitPrice() != currentBestOffer.getLimitPrice())
                .min(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        // Used to prevent sellers which wants just to cheat me
        if (offerBeforeBestOffer.getLimitPrice().divide(currentBestOffer.getLimitPrice(), 7, RoundingMode.DOWN)
                .subtract(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.20)) == -1) {
            return currentBestOffer.getLimitPrice();
        }

        return offerBeforeBestOffer.getLimitPrice();
    }

    @Override
    public BigDecimal getBestBuyPrice(String currencyPair, LimitOrder order) throws IOException {
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());
        OrderBook orderBook = marketDataService.getOrderBook(currencyPairFormatted);

        Stream<LimitOrder> baseOffer = orderBook.getBids().stream()
                .filter(offer -> amountCalculator.doesOrderHasEnoughVolume(offer))
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true);

        LimitOrder currentBestOffer = orderBook.getBids().stream()
                .filter(offer -> amountCalculator.doesOrderHasEnoughVolume(offer))
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .max(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        LimitOrder offerBeforeBestOffer = orderBook.getBids().stream()
                .filter(offer -> amountCalculator.doesOrderHasEnoughVolume(offer))
                .filter(order != null ? offer -> offer.getLimitPrice().compareTo(order.getLimitPrice()) != 0 : offer -> true)
                .filter(offer -> offer.getLimitPrice() != currentBestOffer.getLimitPrice())
                .max(Comparator.comparing(LimitOrder::getLimitPrice))
                .orElseThrow(NoSuchElementException::new);

        // Used to prevent buyers which wants just to cheat me
        if (currentBestOffer.getLimitPrice().divide(offerBeforeBestOffer.getLimitPrice(), 7, RoundingMode.DOWN)
                .subtract(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.20)) == -1) {
            return currentBestOffer.getLimitPrice();
        }

        return offerBeforeBestOffer.getLimitPrice();
    }
}
