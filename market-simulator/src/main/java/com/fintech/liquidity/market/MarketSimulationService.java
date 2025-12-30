package com.fintech.liquidity.market;


import com.fintech.liquidity.core.CurrencyPair;
import com.fintech.liquidity.core.Tick;
import org.springframework.context.annotation.Profile;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@Profile("dev")
public class MarketSimulationService {
    private final PriceGenerator priceGenerator = new PriceGenerator();
    private final MarketDataProducer producer;

    public MarketSimulationService(MarketDataProducer producer) {
        this.producer = producer;
    }

    // Runs every 500ms
    @Scheduled(fixedRate = 500)
    public void simulateFastProvider() {
        generateAndLogTick(CurrencyPair.EUR_USD, "LP_FAST");
        generateAndLogTick(CurrencyPair.USD_JPY, "LP_FAST");
    }

    // Runs every 3 seconds
    @Scheduled(fixedRate = 3000)
    public void simulateSlowProvide() {
        generateAndLogTick(CurrencyPair.EUR_USD, "LP_SLOW");
        generateAndLogTick(CurrencyPair.GBP_USD, "LP_SLOW");
    }

    private void generateAndLogTick(CurrencyPair pair, String source) {
        var midPrice = priceGenerator.generateNewPrice(pair);
        var prices = priceGenerator.generateSpread(midPrice, pair);
        var bid = prices[0];
        var ask = prices[1];

        Tick tick = new Tick(pair, bid, ask, Instant.now(), source);
        producer.publish(tick);
    }
}