package com.fintech.liquidity.market;

import com.fintech.liquidity.core.CurrencyPair;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;

public class PriceGenerator {

    private final Random random = new Random();
    private final Map<CurrencyPair, BigDecimal> currentPrices = new ConcurrentHashMap<>();

    public PriceGenerator() {
        currentPrices.put(CurrencyPair.EUR_USD, new BigDecimal("1.0500"));
        currentPrices.put(CurrencyPair.GBP_USD, new BigDecimal("1.2000"));
        currentPrices.put(CurrencyPair.USD_JPY, new BigDecimal("148.00"));
    }

    public BigDecimal generateNewPrice(CurrencyPair pair){
        BigDecimal currentPrice = currentPrices.get(pair);
        BigDecimal pipSize = pair.getPipSize();

        // random.nextInt(3) return 0, 1, 2, subtract 1 to get -1, 0 , 1;
        int move = random.nextInt(3) - 1;
        BigDecimal change = pipSize.multiply(BigDecimal.valueOf(move));
        BigDecimal newPrice = currentPrice.add(change);
        currentPrices.put(pair,newPrice);
        return newPrice;

    }
    public BigDecimal[] generateSpread(BigDecimal midPrice, CurrencyPair pair){
        BigDecimal spread = pair.getPipSize().multiply(new BigDecimal("2"));
        BigDecimal halfSpread = spread.divide(new BigDecimal("2"), RoundingMode.HALF_UP);

        BigDecimal bid = midPrice.subtract(halfSpread);
        BigDecimal ask = midPrice.add(halfSpread);

        return new BigDecimal[]{bid, ask};
    }
}
