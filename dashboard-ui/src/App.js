import React, { useState, useEffect, useRef } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs'; // Use the Client class directly
import './App.css';

function App() {
    const [prices, setPrices] = useState({});
    const [connectionStatus, setConnectionStatus] = useState('🔴 Disconnected');

    // Ref to keep the client instance stable
    const clientRef = useRef(null);

    useEffect(() => {
        // 1. Configure the Client (The Modern Way)
        const client = new Client({
            // We use webSocketFactory for SockJS compatibility
            webSocketFactory: () => new SockJS('http://localhost:8082/ws-market'),

            // Reconnect automatically if connection drops (every 5s)
            reconnectDelay: 5000,

            // Lifecycle Callbacks
            onConnect: () => {
                setConnectionStatus('🟢 Connected');
                console.log("✅ WebSocket Connected!");

                // 2. Subscribe to Prices
                client.subscribe('/topic/prices', (message) => {
                    // DEBUG: Log the raw message to the browser console
                    console.log("📩 Raw Price Update:", message.body);

                    try {
                        const tick = JSON.parse(message.body);
                        updatePrice(tick);
                    } catch (e) {
                        console.error("❌ JSON Parse Error:", e);
                    }
                });

                // 3. Subscribe to Alerts
                client.subscribe('/topic/status', (message) => {
                    console.warn("⚠️ Alert Received:", message.body);
                    const alert = JSON.parse(message.body);
                    handleAlert(alert);
                });
            },

            onStompError: (frame) => {
                console.error('❌ Broker reported error: ' + frame.headers['message']);
                console.error('Additional details: ' + frame.body);
            },

            onWebSocketClose: () => {
                setConnectionStatus('🔴 Disconnected');
                console.log("🔌 Socket Closed");
            }
        });

        // Start the connection
        client.activate();
        clientRef.current = client;

        // Cleanup on unmount
        return () => {
            client.deactivate();
        };
    }, []);

    const updatePrice = (tick) => {
        // Safety check: Ensure the object has a pair
        if (!tick || !tick.pair) {
            console.warn("⚠️ Received invalid tick:", tick);
            return;
        }

        // Handle "tick.pair" being an object (e.g. {name: "EUR_USD"}) or a string
        const pairName = typeof tick.pair === 'object' ? tick.pair.name : tick.pair;

        setPrices((prev) => ({
            ...prev,
            [pairName]: { ...tick, pair: pairName, status: 'LIVE' }
        }));
    };

    const handleAlert = (alert) => {
        setPrices((prev) => {
            if (!prev[alert.pair]) return prev;
            return {
                ...prev,
                [alert.pair]: {
                    ...prev[alert.pair],
                    status: 'STALE',
                    message: alert.message
                }
            };
        });
    };

    return (
        <div className="dashboard-container">
            <header>
                <h1>⚡️ Liquidity Hub</h1>
                <div className="status-badge">{connectionStatus}</div>
            </header>

            <table className="market-table">
                <thead>
                <tr>
                    <th>Pair</th>
                    <th>Bid</th>
                    <th>Ask</th>
                    <th>Status</th>
                </tr>
                </thead>
                <tbody>
                {Object.values(prices).map((tick) => (
                    <tr key={tick.pair} className={tick.status === 'STALE' ? 'row-stale' : 'row-live'}>
                        <td className="pair-text">{tick.pair}</td>
                        <td className="price-text">{tick.bid?.toFixed(5) || "--"}</td>
                        <td className="price-text">{tick.ask?.toFixed(5) || "--"}</td>
                        <td>
                            {tick.status === 'LIVE' ? (
                                <span className="badge-live">⚡️ LIVE</span>
                            ) : (
                                <span className="badge-stale">⚠️ STALE</span>
                            )}
                        </td>
                    </tr>
                ))}
                {Object.keys(prices).length === 0 && (
                    <tr><td colSpan="4" style={{textAlign: "center", padding: "20px", color: "#666"}}>
                        Waiting for Market Data...<br/>
                        <small>(Check Browser Console F12 if this persists)</small>
                    </td></tr>
                )}
                </tbody>
            </table>
        </div>
    );
}

export default App;