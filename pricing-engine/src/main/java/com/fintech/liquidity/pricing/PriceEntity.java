package com.fintech.liquidity.pricing;

import com.fintech.liquidity.core.Tick;
import jakarta.persistence.*;
import lombok.Getter;

import java.math.BigDecimal;
import java.time.Instant;

@Entity
@Table(name = "price_history")
public class PriceEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;
    @Getter
    private String currencyPair;
    @Getter
    private BigDecimal bid;
    @Getter
    private BigDecimal ask;
    private Instant timestamp;
    private String source;

    public PriceEntity(){}
    public PriceEntity(Tick tick){
        this.currencyPair = tick.pair().name();
        this.bid = tick.bid();
        this.ask = tick.ask();
        this.timestamp = tick.timestamp();
        this.source = tick.source();
    }
    public Long getId(){
        return id;
    }

}
