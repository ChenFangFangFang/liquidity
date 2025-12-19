package com.fintech.liquidity.pricing;

import com.fintech.liquidity.core.LiquidityAggregator;
import com.fintech.liquidity.core.MarginService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfig {
    @Bean
    public LiquidityAggregator liquidityAggregator(){
        return new LiquidityAggregator();
    }

    @Bean
    public MarginService marginService(){
        return new MarginService();
    }
}
