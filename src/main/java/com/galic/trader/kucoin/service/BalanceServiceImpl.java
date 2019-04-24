package com.galic.trader.kucoin.service;

import org.knowm.xchange.currency.Currency;
import org.knowm.xchange.dto.account.AccountInfo;
import org.knowm.xchange.service.account.AccountService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.math.BigDecimal;

@Service
public class BalanceServiceImpl implements BalanceService {

    private static final String WALLET_NAME = "trade";

    @Autowired
    private AccountService accountService;

    @Override
    public BigDecimal getAvailableBalance(Currency currency) {
        try {
            AccountInfo accountInfo = accountService.getAccountInfo();
            BigDecimal balance = accountInfo.getWallet(WALLET_NAME).getBalance(currency).getAvailable();

            return balance;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return BigDecimal.ZERO;
    }
}
