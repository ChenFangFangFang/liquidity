package com.fintech.liquidity.gateway;

import com.fintech.liquidity.core.Tick;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/prices")
public class MarketDataController {
    private final MarketDataBridge bridge;

    public MarketDataController(MarketDataBridge bridge) {
        this.bridge = bridge;
    }
    @GetMapping("/{pair}")
    public ResponseEntity<Tick> getPrice(@PathVariable("pair") String pair){
        Tick tick = bridge.getLatestPrice(pair);
        if(tick == null){
            return ResponseEntity.notFound().build();

        }
        return ResponseEntity.ok(tick);
    }
}
