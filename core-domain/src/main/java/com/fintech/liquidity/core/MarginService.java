package com.fintech.liquidity.core;

import java.math.BigDecimal;

public class MarginService {

    public Tick applyMargin(Tick rawTick, BigDecimal marginInPips){
        if (marginInPips.signum() < 0){
            throw new IllegalArgumentException("Margin cannot be negative");
        }

        BigDecimal pipSize = rawTick.pair().getPipSize();
        BigDecimal marginAmount = pipSize.multiply(marginInPips);

        BigDecimal clientBid = rawTick.bid().subtract(marginAmount);
        BigDecimal clientAsk = rawTick.ask().add(marginAmount);

        return new Tick(
            rawTick.pair(),
                clientBid,
                clientAsk,
                rawTick.timestamp(),
                "NORD_MARGIN"
        );
    }

}
