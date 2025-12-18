package com.fintech.liquidity.core;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

public class MarginServiceTest {

    private final MarginService marginService = new MarginService();

    @Test
    void shouldWidenSpreadForStandardPair(){
        Tick rawTick = new Tick(
                CurrencyPair.EUR_USD,
                new BigDecimal("1.0500"),
                new BigDecimal("1.0510"),
                Instant.now(),
                "RawMarket"
        );

        Tick clientTick = marginService.applyMargin(rawTick, new BigDecimal("2.0"));
        assertThat(clientTick.bid()).isEqualByComparingTo("1.0498");
        assertThat(clientTick.ask()).isEqualByComparingTo("1.0512");
        assertThat(clientTick.source()).isEqualTo("NORD_MARGIN");
    }

    @Test
    void shouldHandleJPYPipsCorrectly(){
        Tick rawTick = new Tick(
                CurrencyPair.USD_JPY,
                new BigDecimal("145.00"),
                new BigDecimal("145.10"),
                Instant.now(),
                "RawMarket"
        );

        Tick clientTick = marginService.applyMargin(rawTick,new BigDecimal("1.0"));
        assertThat(clientTick.bid()).isEqualByComparingTo("144.99");
        assertThat(clientTick.ask()).isEqualByComparingTo("145.11");

    }
}
