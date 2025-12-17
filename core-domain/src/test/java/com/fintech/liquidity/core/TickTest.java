package com.fintech.liquidity.core;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
class TickTest {

    @Test
    void shouldCreateValidTick() {
        // Given
        CurrencyPair pair = CurrencyPair.EUR_USD;
        BigDecimal bid = new BigDecimal("1.0500");
        BigDecimal ask = new BigDecimal("1.0502");
        Instant now = Instant.now();

        // When
        Tick tick = new Tick(pair, bid, ask, now, "TestLP");

        // Then
        assertThat(tick.pair()).isEqualTo(CurrencyPair.EUR_USD);
        assertThat(tick.bid()).isEqualByComparingTo("1.0500");
        assertThat(tick.source()).isEqualTo("TestLP");
    }

    @Test
    void shouldThrowExceptionWhenBidIsHigherThanAsk() {
        // Given (Crossed Market)
        BigDecimal highBid = new BigDecimal("1.0600");
        BigDecimal lowAsk = new BigDecimal("1.0500");

        // When/Then
        assertThatThrownBy(() -> new Tick(
                CurrencyPair.EUR_USD,
                highBid,
                lowAsk,
                Instant.now(),
                "TestLP"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bid (1.0600) > Ask (1.0500)");
    }

    @Test
    void shouldThrowExceptionForNegativePrice() {
        assertThatThrownBy(() -> new Tick(
                CurrencyPair.EUR_USD,
                new BigDecimal("-1.00"),
                new BigDecimal("1.05"),
                Instant.now(),
                "TestLP"
        ))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Prices must be positive");
    }
}