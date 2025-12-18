package com.fintech.liquidity.core;

import java.util.Comparator;
import java.util.List;

public class LiquidityAggregator {

 public Tick aggregate(List<Tick> marketTicks){
     if(marketTicks == null || marketTicks.isEmpty()){
         throw new IllegalArgumentException("Cannot aggregate empty market data");
     }
     // Consistency Check
     CurrencyPair distinctPair = marketTicks.get(0).pair();
     boolean allSamePair = marketTicks.stream().allMatch(
             t->t.pair() == distinctPair
     );
     if (!allSamePair){
         throw new IllegalArgumentException("Cannot aggregate mixed currency");
     }

     // Find Best Bid
     Tick bestBidTick = marketTicks.stream()
             .max(Comparator.comparing(Tick::bid))
             .orElseThrow();

     // Find Best Ask
     Tick bestAskTick = marketTicks.stream()
             .min(Comparator.comparing(Tick::ask))
             .orElseThrow();

     // Return composite "Best Price"
     return new Tick(
             distinctPair,
             bestBidTick.bid(),
             bestAskTick.ask(),
             java.time.Instant.now(),
             "AGGREGATED_BBO" //best bid and offer
     );

 }
}
