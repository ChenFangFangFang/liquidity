package com.fintech.liquidity.market;

import com.fintech.liquidity.core.Tick;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class MarketDataProducer {
    private static final String TOPIC = "market.data.raw";
    private final KafkaTemplate<String, Tick> kafkaTemplate;

    public MarketDataProducer(KafkaTemplate<String, Tick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }
    public void publish(Tick tick){
        String key = tick.pair().name();
        kafkaTemplate.send(TOPIC, key,tick);
        System.out.println("KAFKA sent: " + tick);

    }
}
