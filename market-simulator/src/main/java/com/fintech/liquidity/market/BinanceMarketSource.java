//package com.fintech.liquidity.market;
//
//import com.fasterxml.jackson.databind.JsonNode;
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.fintech.liquidity.core.CurrencyPair;
//import com.fintech.liquidity.core.Tick;
//import jakarta.annotation.PostConstruct;
//import org.springframework.kafka.core.KafkaTemplate;
//import org.springframework.stereotype.Service;
//import org.springframework.web.socket.TextMessage;
//import org.springframework.web.socket.WebSocketSession;
//import org.springframework.web.socket.client.standard.StandardWebSocketClient;
//import org.springframework.web.socket.handler.TextWebSocketHandler;
//
//import java.math.BigDecimal;
//import java.time.Instant;
//
//@Service
//public class BinanceMarketSource extends TextWebSocketHandler {
//    private static final String BINANCE_URL = "wss://stream.binance.com:9443/stream?streams=btcusdt@bookTicker/ethusdt@bookTicker";    private final KafkaTemplate<String, Tick> kafkaTemplate;
//    private final ObjectMapper jsonParser = new ObjectMapper();
//
//    public BinanceMarketSource(KafkaTemplate<String, Tick> kafkaTemplate) {
//        this.kafkaTemplate = kafkaTemplate;
//    }
//
//    @PostConstruct
//    public void connect(){
//        System.out.println("Connecting to Binance Real-Time Feed....");
//        StandardWebSocketClient client = new StandardWebSocketClient();
//        client.doHandshake(this, BINANCE_URL);
//    }
//
//    @Override
//    public void handleTextMessage(WebSocketSession session, TextMessage message){
//        try {
//            JsonNode root = jsonParser.readTree(message.getPayload());
//            JsonNode node = root.has("data")? root.get("data") : root;
//            if (node.has("s") && node.has("b") && node.has("a")){
//                String symbol = node.get("s").asText();
//                String bestBid = node.get("b").asText();
//                String bestAsk = node.get("a").asText();
//
//                String normalizedString= formatSymbol(symbol);
//                try {
//                    CurrencyPair pairEnum = CurrencyPair.valueOf(normalizedString);
//                    Tick tick = new Tick(
//                            pairEnum,
//                            new BigDecimal(bestBid),
//                            new BigDecimal(bestAsk),
//                            Instant.now(),
//                            "BINANCE"
//
//                    );
//                    kafkaTemplate.send("market.data.raw",normalizedString,tick);
//                    System.out.println("⚡ [BINANCE] " + normalizedString + ": " + bestBid);
//                }catch (IllegalArgumentException e){
//
//                }
//
//            }
//        }catch (Exception e){
//            System.err.println("Error parsing Binance message: " + e.getMessage());
//        }
//    }
//    private String formatSymbol(String binanceSymbol){
//        if (binanceSymbol.equalsIgnoreCase("BTCUSDT")) return "BTC_USDT";
//        if (binanceSymbol.equalsIgnoreCase("ETHUSDT")) return "ETH_USDE";
//        return binanceSymbol;
//    }
//}
