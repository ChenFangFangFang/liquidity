package com.fintech.liquidity.core;

import java.math.BigDecimal;
import java.time.Instant;

public record Tick(
        CurrencyPair pair,
        BigDecimal bid,
        BigDecimal ask,
        Instant timestamp,
        String source
) {
    public Tick{
        if (pair == null || bid == null || ask == null || timestamp == null|| source == null){
            throw new IllegalArgumentException("Tick fields cannot be null");        }
        if (bid.compareTo(BigDecimal.ZERO) <= 0 || ask.compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Prices must be positive");
        }
        if (bid.compareTo(ask) > 0) {
            throw new IllegalArgumentException("Invalid Spread: Bid (%s) > Ask (%s)".formatted(bid, ask));
        }
    }
}