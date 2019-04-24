package com.galic.trader.kucoin.service;

import com.galic.trader.kucoin.domain.OrderRequest;
import com.galic.trader.kucoin.domain.OrderResponse;
import com.galic.trader.kucoin.domain.OrderType;
import com.galic.trader.kucoin.util.AmountCalculator;
import com.galic.trader.kucoin.util.BuyOrderInfo;
import com.galic.trader.kucoin.util.Coins;
import com.galic.trader.kucoin.util.Properties;
import lombok.extern.slf4j.Slf4j;
import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.currency.CurrencyPair;
import org.knowm.xchange.dto.Order;
import org.knowm.xchange.dto.trade.LimitOrder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class TradeServiceImpl implements TradeService {

    @Autowired
    private OrderService orderService;
    @Autowired
    private BalanceService balanceService;
    @Autowired
    private PriceService priceService;
    @Autowired
    private AmountCalculator amountCalculator;

    private static final BigDecimal MINIMAL_AMOUNT = BigDecimal.valueOf(0.00000001).setScale(8);
    private static final BigDecimal MINIMAL_BUY_AMOUNT = BigDecimal.valueOf(0.1);
    private static final BigDecimal MINIMAL_SELL_AMOUNT = BigDecimal.valueOf(0.0032);
    private static final BigDecimal MAX_BTC_AMOUNT_PER_ORDER = BigDecimal.valueOf(0.0013);

    private Map<String, BigDecimal> stateHolder = new HashMap<String, BigDecimal>();

    private Map<String, BuyOrderInfo> buyOrderIds = new HashMap<String, BuyOrderInfo>();
    private Map<String, String> sellOrderIds = new HashMap<String, String>();

    @Scheduled(fixedRate = 40000)
    public void scheduledRound() {
        executeRound(Coins.BCD, MAX_BTC_AMOUNT_PER_ORDER);
        executeRound(Coins.CPC, MAX_BTC_AMOUNT_PER_ORDER);
        executeRound(Coins.EBTC, MAX_BTC_AMOUNT_PER_ORDER);
    }

    private void executeRound(Coins coin, BigDecimal maxBTCAmount) {
        try {
            String currencyPair = coin.getPair();
            Currency currency = coin.getName();

            // check if I have available btc or another currency
            BigDecimal currencyAvailableBalance = balanceService.getAvailableBalance(currency);
            BigDecimal btcAvailableBalance = balanceService.getAvailableBalance(Currency.BTC);
            BigDecimal btcAmountToUse = btcAvailableBalance.compareTo(maxBTCAmount) == -1 ? btcAvailableBalance : maxBTCAmount;

            BigDecimal amountToBuy = amountCalculator.calculateAmountToBuy(currencyPair, btcAmountToUse,
                    getbuyOrderId(buyOrderIds.get(currencyPair)), sellOrderIds.get(currencyPair));

            if (currencyAvailableBalance.compareTo(MINIMAL_SELL_AMOUNT) > 0) {
                placeSellOrder(currencyPair, currency);
                stateHolder.remove(currencyPair);
            }

            if (amountToBuy.compareTo(MINIMAL_BUY_AMOUNT) > 0 && (stateHolder.get(currencyPair) == null
                    || stateHolder.get(currencyPair).compareTo(btcAmountToUse) == -1)) {
                placeBuyOrder(currencyPair, amountToBuy);
                stateHolder.put(currencyPair, btcAmountToUse);
            }

            adjustBuyOrders(currencyPair);
            adjustSellOrders(currencyPair, currency);
        } catch (Exception e) {
            log.error("Exception occured: {}", e);
        }
    }

    private void placeSellOrder(String currencyPair, Currency currency) throws IOException {
        LimitOrder order = null;
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());

        // Case when order is already bought but we are not yet aware of that
        if (sellOrderIds.containsKey(currencyPair)) {
            order = orderService.getOrder(sellOrderIds.get(currencyPair));

            if (order != null) {
                orderService.cancelOrder(sellOrderIds.get(currencyPair), currencyPair);
            }
            sellOrderIds.remove(currencyPair);
        }

        BigDecimal currencyAvailableBalance = balanceService.getAvailableBalance(currency);
        BigDecimal bestSellPrice = priceService.getBestSellPrice(currencyPair, order);
        bestSellPrice = bestSellPrice.subtract(MINIMAL_AMOUNT);

        if (buyOrderIds.get(currencyPair) != null) {
            BigDecimal buyingPrice = buyOrderIds.get(currencyPair).getBuyOrderPrice();
            if (bestSellPrice.divide(buyingPrice, 7, RoundingMode.DOWN).subtract(BigDecimal.ONE).compareTo(BigDecimal.valueOf(0.03)) == -1) {
                bestSellPrice = buyingPrice.multiply(BigDecimal.valueOf(1.03));
            }
        }

        if (currencyAvailableBalance.compareTo(MINIMAL_SELL_AMOUNT) > 0) {
            OrderRequest orderRequest = OrderRequest.builder()
                    .price(bestSellPrice)
                    .quantity(currencyAvailableBalance)
                    .orderType(Order.OrderType.ASK)
                    .currencyPair(currencyPairFormatted)
                    .build();

            String orderId = orderService.placeOrder(orderRequest);
            sellOrderIds.put(currencyPair, orderId);

            log.info("Sell order placed for {}", currencyPair);
        } else {
            log.error("Tried to sell with wrong amount {}", currencyAvailableBalance);
        }

    }

    private void placeBuyOrder(String currencyPair, BigDecimal amount) throws IOException {
        LimitOrder order = null;
        CurrencyPair currencyPairFormatted = new CurrencyPair(currencyPair, Coins.BTC.getPair());

        // Case when order is already bought but we are not yet aware of that
        if (buyOrderIds.containsKey(currencyPair)) {
            order = orderService.getOrder(getbuyOrderId(buyOrderIds.get(currencyPair)));

            if (order != null) {
                orderService.cancelOrder(getbuyOrderId(buyOrderIds.get(currencyPair)), currencyPair);
            }
            buyOrderIds.remove(currencyPair);
        }
        if (amount.compareTo(MINIMAL_SELL_AMOUNT) > 0) {
            BigDecimal bestBuyPrice = priceService.getBestBuyPrice(currencyPair, order);
            OrderRequest orderRequest = OrderRequest.builder()
                    .price(bestBuyPrice.add(MINIMAL_AMOUNT))
                    .quantity(amount)
                    .orderType(Order.OrderType.BID)
                    .currencyPair(currencyPairFormatted)
                    .build();

            String orderId = orderService.placeOrder(orderRequest);
            buyOrderIds.put(currencyPair, new BuyOrderInfo(orderId, bestBuyPrice.add(MINIMAL_AMOUNT)));

            log.info("Buy order placed for {}", currencyPair);
        } else {
            log.error("Tried to buy with wrong amount {}", amount);
        }
    }

    private void adjustBuyOrders(String currencyPair) throws IOException {
        if (buyOrderIds.containsKey(currencyPair)) {
            LimitOrder order = orderService.getOrder(getbuyOrderId(buyOrderIds.get(currencyPair)));

            BigDecimal bestBuyPrice = priceService.getBestBuyPrice(currencyPair, order);

            if (bestBuyPrice.add(MINIMAL_AMOUNT).compareTo(order.getLimitPrice()) != 0) {
                orderService.cancelOrder(getbuyOrderId(buyOrderIds.get(currencyPair)), currencyPair);
                buyOrderIds.remove(currencyPair);

                BigDecimal btcAvailableBalance = balanceService.getAvailableBalance(Currency.BTC);
                BigDecimal BTCAmountToUse = btcAvailableBalance.compareTo(MAX_BTC_AMOUNT_PER_ORDER) == -1 ? btcAvailableBalance : MAX_BTC_AMOUNT_PER_ORDER;

                placeBuyOrder(currencyPair, amountCalculator.calculateAmountToBuy(currencyPair, BTCAmountToUse,
                        getbuyOrderId(buyOrderIds.get(currencyPair)), sellOrderIds.get(currencyPair)));
                log.info("Buy order adjusted for {}", currencyPair);
            }
        }
    }

    private void adjustSellOrders(String currencyPair, Currency currency) throws IOException {
        if (sellOrderIds.containsKey(currencyPair)) {
            LimitOrder order = orderService.getOrder(sellOrderIds.get(currencyPair));

            BigDecimal bestSellPrice = priceService.getBestSellPrice(currencyPair, order);

            if (order.getLimitPrice().add(MINIMAL_AMOUNT).compareTo(bestSellPrice) != 0) {
                orderService.cancelOrder(sellOrderIds.get(currencyPair), currencyPair);
                sellOrderIds.remove(currencyPair);

                placeSellOrder(currencyPair, currency);
                log.info("Sell order adjusted for {}", currencyPair);
            }
        }
    }

    private String getbuyOrderId(BuyOrderInfo buyOrderInfo) {
        if (buyOrderInfo != null) {
            return buyOrderInfo.getClientOrderId();
        } else {
            return null;
        }
    }

}
