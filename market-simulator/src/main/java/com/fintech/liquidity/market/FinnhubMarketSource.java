package com.fintech.liquidity.market;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fintech.liquidity.core.CurrencyPair;
import com.fintech.liquidity.core.Tick;
import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Profile;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
@Profile("prod")
public class FinnhubMarketSource extends TextWebSocketHandler {
    private static final String API_KEY = "d59pijpr01qgqlm1ovdgd59pijpr01qgqlm1ove0";
    private static final String FINNHUB_URL = "wss://ws.finnhub.io?token=" + API_KEY;
    private final KafkaTemplate<String, Tick> kafkaTemplate;
    private final ObjectMapper jsonParser = new ObjectMapper();
    private final Map<String, CurrencyPair> symbolMap = new ConcurrentHashMap<>();

    public FinnhubMarketSource(KafkaTemplate<String, Tick> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
        symbolMap.put("OANDA:EUR_USD", CurrencyPair.EUR_USD);
        symbolMap.put("OANDA:GBP_USD", CurrencyPair.GBP_USD);
        symbolMap.put("OANDA:USD_JPY", CurrencyPair.USD_JPY);
        symbolMap.put("BINANCE:BTCUSDT", CurrencyPair.BTC_USDT);
        symbolMap.put("BINANCE:ETHUSDT", CurrencyPair.ETH_USDT);
    }

    @PostConstruct
    public void connect(){
        System.out.println("🚀 Connecting to Finnhub FX Feed...");
        StandardWebSocketClient client = new StandardWebSocketClient();
        try{
            client.doHandshake(this,FINNHUB_URL);
        } catch (Exception e) {
            System.err.println("❌ Failed to connect to Finnhub: " + e.getMessage());
        }
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception{
        System.out.println("✅ Connected to Finnhub! Subscribing to pairs...");
        for (String finnhubSymbol: symbolMap.keySet()){
            String msg = "{\"type\":\"subscribe\",\"symbol\":\"" + finnhubSymbol + "\"}";
            session.sendMessage(new TextMessage(msg));
            System.out.println("📡 Subscribed to: " + finnhubSymbol);
        }
    }
    @Override
    public void handleTextMessage(WebSocketSession session, TextMessage message){
        String payload = message.getPayload();
        System.out.println("📩 [RAW]: " + payload);
        try{
            JsonNode root = jsonParser.readTree(message.getPayload());
            if (root.has("type") && root.get("type").asText().equals("trade")){
                JsonNode dataArray = root.get("data");
                for(JsonNode item: dataArray){
                    String symbol = item.get("s").asText();
                    double price = item.get("p").asDouble();
                    if(symbolMap.containsKey((symbol))){
                        publishTick(symbolMap.get(symbol),price);
                    }
                }
            }
        } catch (Exception e) {
            System.err.println("⚠️ [PARSE ERROR] " + e.getMessage());
        }
    }
    private void publishTick(CurrencyPair pair, double midPrice){
        BigDecimal mid = BigDecimal.valueOf(midPrice);
        BigDecimal halfSpread = pair.getPipSize();
        BigDecimal bid = mid.subtract(halfSpread);
        BigDecimal ask = mid.add(halfSpread);
        Tick tick = new Tick(
                pair,bid, ask, Instant.now(),"FINNHUB_FX"
        );
        kafkaTemplate.send("market.data.raw", pair.name(),tick);
    }
}
