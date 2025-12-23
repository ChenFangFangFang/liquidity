package com.fintech.liquidity.gateway;

import com.fintech.liquidity.core.Tick;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class MarketDataBridge {

    private final SimpMessagingTemplate webSocket;
    // cache, latest price for REST
    private final Map<String, Tick> latestPrices = new ConcurrentHashMap<>();
    // monitor, last time saw an update
    private final Map<String, Instant> lastHeartbeat = new ConcurrentHashMap<>();
    public MarketDataBridge(SimpMessagingTemplate webSocket) {
        this.webSocket = webSocket;
    }

    @KafkaListener(topics = "market.data.clean", groupId = "client-gateway-group")
    public void consume(Tick tick){
        String pair = tick.pair().name();
        // 1. update cache for REST
        latestPrices.put(pair,tick);
        lastHeartbeat.put(pair, Instant.now());
        // 2. push to websocket for streaming
        webSocket.convertAndSend("/topic/prices", tick);
    }

    public Tick getLatestPrice(String pair){
        return latestPrices.get(pair);
    }
    //run every 1000ms
    @Scheduled(fixedRate = 1000)
    public void checkStaleData(){
        System.out.println("⏰ Tick-Tock (Checking " + lastHeartbeat.size() + " pairs)");
        Instant now = Instant.now();
        lastHeartbeat.forEach((pair, lastTime) -> {
            long secondsElapsed = Duration.between(lastTime,now).getSeconds();
            if (secondsElapsed > 5){
                System.out.println("⚠️ [ALERT] Data Stale for " + pair + " (" + secondsElapsed + "s)");
                MarketStatus warning = new MarketStatus(
                        pair,
                        "STALE",
                        "Heartbeat missing for " + secondsElapsed + "s"
                );
                webSocket.convertAndSend("/topic/status", warning);
            }


        });
    }

}
