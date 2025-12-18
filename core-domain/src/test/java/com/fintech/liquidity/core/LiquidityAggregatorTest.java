package com.fintech.liquidity.core;

import org.junit.jupiter.api.Test;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

class LiquidityAggregatorTest {

    private final LiquidityAggregator aggregator = new LiquidityAggregator();

    @Test
    void shouldPickBestBidAndLowestAsk() {
        // Given: 3 Banks sending prices
        Tick bankA = createTick("1.0500", "1.0520"); // Wide spread
        Tick bankB = createTick("1.0505", "1.0525"); // Better Bid
        Tick bankC = createTick("1.0490", "1.0515"); // Better Ask

        // When
        Tick result = aggregator.aggregate(List.of(bankA, bankB, bankC));

        // Then
        // Best Bid should be 1.0505 (from Bank B)
        // Best Ask should be 1.0515 (from Bank C)
        assertThat(result.bid()).isEqualByComparingTo("1.0505");
        assertThat(result.ask()).isEqualByComparingTo("1.0515");
        assertThat(result.source()).isEqualTo("AGGREGATED_BBO");
    }

    @Test
    void shouldThrowExceptionWhenMarketIsCrossed() {
        // Given: A crossed market scenario
        // Bank A is buying high (1.0530)
        // Bank B is selling low (1.0520)
        // This means Bid (1.0530) > Ask (1.0520) -> Arbitrage opportunity/Error
        Tick bankA = createTick("1.0530", "1.0540");
        Tick bankB = createTick("1.0510", "1.0520");

        // When/Then
        // We expect the Aggregator (or the Tick constructor inside it) to scream "Invalid Spread"
        assertThatThrownBy(() -> aggregator.aggregate(List.of(bankA, bankB)))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Bid (1.0530) > Ask (1.0520)");
    }
    // Helper to keep tests clean
    private Tick createTick(String bid, String ask) {
        return new Tick(
                CurrencyPair.EUR_USD,
                new BigDecimal(bid),
                new BigDecimal(ask),
                Instant.now(),
                "TestBank"
        );
    }
}