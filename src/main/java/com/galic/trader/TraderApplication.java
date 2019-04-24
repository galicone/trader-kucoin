package com.galic.trader;

import org.knowm.xchange.Exchange;
import org.knowm.xchange.ExchangeFactory;
import org.knowm.xchange.ExchangeSpecification;
import org.knowm.xchange.kucoin.KucoinExchange;
import org.knowm.xchange.service.account.AccountService;
import org.knowm.xchange.service.marketdata.MarketDataService;
import org.knowm.xchange.service.trade.TradeService;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TraderApplication {

    public static void main(String[] args) {
        SpringApplication.run(TraderApplication.class, args);
    }

    @Bean
    Exchange getExchange() {
        ExchangeSpecification exSpec = new ExchangeSpecification(KucoinExchange.class);
        exSpec.setApiKey("x");
        exSpec.setSecretKey("x");
        exSpec.setExchangeSpecificParametersItem("passphrase", "x");

        return ExchangeFactory.INSTANCE.createExchange(exSpec);
    }

    @Bean
    AccountService getAccountService() {
        return getExchange().getAccountService();
    }

    @Bean
    TradeService getTradeService() {
        return getExchange().getTradeService();
    }

    @Bean
    MarketDataService getMarketDataService() {
        return getExchange().getMarketDataService();
    }
}

