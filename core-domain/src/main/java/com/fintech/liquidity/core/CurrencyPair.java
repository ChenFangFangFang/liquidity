package com.fintech.liquidity.core;

public enum CurrencyPair {
    EUR_USD,
    GBP_USD,
    USD_JPY;

    @Override
    public String toString(){
        return name().replace("_","/");
    }
}