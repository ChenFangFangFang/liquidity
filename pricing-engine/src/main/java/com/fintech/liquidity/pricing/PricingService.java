package com.fintech.liquidity.pricing;

import com.fintech.liquidity.core.CurrencyPair;
import com.fintech.liquidity.core.LiquidityAggregator;
import com.fintech.liquidity.core.MarginService;
import com.fintech.liquidity.core.Tick;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class PricingService {
    private static final String INPUT_TOPIC = "market.data.raw";
    private static final String OUTPUT_TOPIC = "market.data.clean";

    private final LiquidityAggregator aggregator;
    private final MarginService marginService;
    private final KafkaTemplate<String, Tick> kafkaTemplate;
    private final Map<CurrencyPair, Map<String, Tick>> marketState = new ConcurrentHashMap<>();


    public PricingService(LiquidityAggregator aggregator, MarginService marginService, KafkaTemplate<String, Tick> kafkaTemplate) {
        this.aggregator = aggregator;
        this.marginService = marginService;
        this.kafkaTemplate = kafkaTemplate;
    }

//    @KafkaListener(topics = INPUT_TOPIC, groupId = "pricing-engine-group")
//    public void onMarketTick(Tick incomingTick){
//        // update internal memory
//        marketState.computeIfAbsent(incomingTick.pair(),k -> new ConcurrentHashMap<>())
//                .put(incomingTick.source(),incomingTick);
//
//        var currentTicks = new ArrayList<>(marketState.get(incomingTick.pair()).values());
//
//        if (currentTicks.isEmpty()) return;
//        try{
//            Tick bestPrice = aggregator.aggregate(currentTicks);
//            Tick clientPrice = marginService.applyMargin(bestPrice, new BigDecimal("1.5"));
//            kafkaTemplate.send(OUTPUT_TOPIC, clientPrice.pair().name(), clientPrice);
//            System.out.println("✅ [Clean Price] " + clientPrice.pair() + " | Bid: " + clientPrice.bid() + " | Ask: " + clientPrice.ask());
//        }catch (Exception e){
//            System.err.println("⚠️ Pricing Logic Skipped: " + e.getMessage());
//        }
//    }
@KafkaListener(topics = "market.data.raw", groupId = "pricing-engine-group")
public void onMessage(Tick rawTick) {
    // 👇 ADD THIS PRINT AT THE VERY TOP
    System.out.println("📥 [PRICING HIT] Raw Tick received: " + rawTick);

    try {
        // Your logic...
        BigDecimal bid = rawTick.bid().multiply(new BigDecimal("0.9995"));
        BigDecimal ask = rawTick.ask().multiply(new BigDecimal("1.0005"));

        Tick cleanTick = new Tick(
                rawTick.pair(),
                bid,
                ask,
                Instant.now(),
                "LIQUIDITY_HUB"
        );

        kafkaTemplate.send("market.data.clean", cleanTick);
        System.out.println("✅ [CLEAN] Published: " + cleanTick.pair());

    } catch (Exception e) {
        System.err.println("❌ Logic Error: " + e.getMessage());
    }
}
}
