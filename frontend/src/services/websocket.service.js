// src/services/websocket.service.js

// global 객체 설정을 최상단에 배치
if (typeof global === 'undefined') {
    window.global = window;
}

import { ref } from 'vue';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
// const SOCKET_URL = 'http://localhost:9832/api/ws';
const SOCKET_URL = 'https://olm.life/api/ws'; // 프록시된 WebSocket URL
const topics = {
    ALL_STOCKS: '/topic/stocks/all',
    SINGLE_STOCK: (code) => `/topic/stocks/${code}`
};

class WebSocketService {
    constructor() {
        this.client = null;
        this.subscriptions = new Map();
        this.isConnected = ref(false);
        this.connectionError = ref(null);
        this.reconnectAttempts = 0;
        this.maxReconnectAttempts = 5;
    }

    async connect() {
        if (this.client?.connected) {
            return Promise.resolve();
        }

        return new Promise((resolve, reject) => {
            try {
                this.client = new Client({
                    webSocketFactory: () => new SockJS(SOCKET_URL, null, {
                        transports: ['websocket'],  // WebSocket 전송만 사용
                        secure: true  // SSL/TLS 사용
                    }),
                    debug: (str) => {
                        console.debug(str);
                    },
                    reconnectDelay: 5000,
                    heartbeatIncoming: 4000,
                    heartbeatOutgoing: 4000,
                    onConnect: () => {
                        console.log('Connected to WebSocket');
                        this.isConnected.value = true;
                        this.connectionError.value = null;
                        this.reconnectAttempts = 0;
                        resolve();
                    },
                    onDisconnect: () => {
                        console.log('Disconnected from WebSocket');
                        this.isConnected.value = false;
                    },
                    onStompError: (frame) => {
                        console.error('STOMP Error:', frame);
                        this.handleError(new Error(frame.headers.message || 'WebSocket Error'));
                        reject(frame);
                    }
                });

                this.client.activate();
            } catch (error) {
                this.handleError(error);
                reject(error);
            }
        });
    }

    handleError(error) {
        console.error('WebSocket Error:', error);
        this.connectionError.value = error;
        this.isConnected.value = false;

        if (this.reconnectAttempts < this.maxReconnectAttempts) {
            this.reconnectAttempts++;
            console.log(`Attempting to reconnect (${this.reconnectAttempts}/${this.maxReconnectAttempts})...`);
            setTimeout(() => this.connect(), 5000 * Math.pow(2, this.reconnectAttempts - 1));
        }
    }

    subscribeToAllStocks(callback) {
        const doSubscribe = async () => {
            if (!this.client?.connected) {
                await this.connect();
            }

            if (this.subscriptions.has('all')) {
                console.log('Already subscribed to /topic/stocks/all');
                return;
            }

            console.log('Subscribing to /topic/stocks/all');

            const subscription = this.client.subscribe(topics.ALL_STOCKS, (message) => {
                console.log('Received message:', message); // 메시지 전체 로그 출력
                try {
                    const data = JSON.parse(message.body);
                    console.log('Parsed data:', data); // 파싱된 데이터 로그 출력
                    callback(data);
                } catch (error) {
                    console.error('Error processing message:', error);
                }
            });

            this.subscriptions.set('all', subscription);
            return subscription;
        };

        return doSubscribe().catch(error => {
            console.error('Subscription error:', error);
            this.handleError(error);
        });
    }




    subscribeToStock(stockCode, callback) {
        const doSubscribe = async () => {
            if (!this.client?.connected) {
                await this.connect();
            }

            if (this.subscriptions.has(stockCode)) {
                return;
            }

            const subscription = this.client.subscribe(topics.SINGLE_STOCK(stockCode), (message) => {
                try {
                    const data = JSON.parse(message.body);
                    callback(data);
                } catch (error) {
                    console.error('Error processing message:', error);
                }
            });

            this.subscriptions.set(stockCode, subscription);
            return subscription;
        };

        return doSubscribe().catch(error => {
            console.error('Subscription error:', error);
            this.handleError(error);
        });
    }

    unsubscribeFromStock(stockCode) {
        const subscription = this.subscriptions.get(stockCode);
        if (subscription) {
            subscription.unsubscribe();
            this.subscriptions.delete(stockCode);
        }
    }

    disconnect() {
        if (this.client) {
            this.subscriptions.forEach(subscription => subscription.unsubscribe());
            this.subscriptions.clear();

            try {
                this.client.deactivate();
                console.log('WebSocket disconnected successfully');
            } catch (error) {
                console.error('Error during disconnect:', error);
            }

            this.client = null;
            this.isConnected.value = false;
        }
    }
}

export default new WebSocketService();