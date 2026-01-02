package com.fintech.liquidity.pricing;

import com.fintech.liquidity.core.Tick;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class PricingPersistenceService {
    private final PriceRepository priceRepository;

    @CircuitBreaker(name = "pricingDb", fallbackMethod = "fallbackSave")
    public void saveTick(Tick cleanTick, int count){
        PriceEntity entity = new PriceEntity(cleanTick);
        priceRepository.save(entity);
        System.out.println("💾 [DB SAVED] " + cleanTick.pair() + " (Tick #" + count + ")");
    }
    public void fallbackSave(Tick cleanTick, int count, Throwable t){
        System.err.println("⚠️ DB DOWN! Skipping save for tick #" + count + ". Error: " + t.getMessage());
    }
}
