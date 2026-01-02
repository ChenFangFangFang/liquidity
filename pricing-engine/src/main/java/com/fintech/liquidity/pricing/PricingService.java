package com.fintech.liquidity.pricing;

import com.fintech.liquidity.core.CurrencyPair;
import com.fintech.liquidity.core.LiquidityAggregator;
import com.fintech.liquidity.core.MarginService;
import com.fintech.liquidity.core.Tick;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
public class PricingService {
    private static final String INPUT_TOPIC = "market.data.raw";
    private static final String OUTPUT_TOPIC = "market.data.clean";
    private final LiquidityAggregator aggregator;
    private final MarginService marginService;
    private final KafkaTemplate<String, Tick> kafkaTemplate;
    private final PriceRepository priceRepository;
    private final AtomicInteger tickCounter = new AtomicInteger(0);
    private final Map<CurrencyPair, Map<String, Tick>> marketState = new ConcurrentHashMap<>();
    private final PricingPersistenceService pricingPersistenceService;

    public PricingService(LiquidityAggregator aggregator,
                          MarginService marginService,
                          KafkaTemplate<String, Tick> kafkaTemplate,
                          PriceRepository priceRepository, PricingPersistenceService pricingPersistenceService) {
        this.aggregator = aggregator;
        this.marginService = marginService;
        this.kafkaTemplate = kafkaTemplate;
        this.priceRepository = priceRepository;
        this.pricingPersistenceService = pricingPersistenceService;
    }

@KafkaListener(topics = "market.data.raw", groupId = "pricing-engine-group")
public void onMessage(Tick rawTick) {
    // 👇 ADD THIS PRINT AT THE VERY TOP
    System.out.println("📥 [PRICING HIT] Raw Tick received: " + rawTick);

    try {
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
        int currentCount = tickCounter.incrementAndGet();
        if (currentCount % 10 == 0){
            pricingPersistenceService.saveTick(cleanTick,currentCount);
        }

    } catch (Exception e) {
        System.err.println("❌ Logic Error: " + e.getMessage());
    }
}
}
