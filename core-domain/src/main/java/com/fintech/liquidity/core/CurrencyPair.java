package com.fintech.liquidity.core;

import java.math.BigDecimal;

public enum CurrencyPair {
    EUR_USD(new BigDecimal("0.0001")),
    GBP_USD(new BigDecimal("0.0001")),
    USD_JPY(new BigDecimal("0.01")),
    BTC_USDT(new BigDecimal("0.01")),
    ETH_USDT(new BigDecimal("0.01"));

    private final BigDecimal pipSize;
    CurrencyPair(BigDecimal pipSize){
        this.pipSize  = pipSize;
    }

    public BigDecimal getPipSize() {
        return pipSize;
    }

    @Override
    public String toString(){
        return name().replace("_","/");
    }
}