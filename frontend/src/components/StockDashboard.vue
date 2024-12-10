<!-- StockDashboard.vue -->
<template>
  <div class="p-4">
    <!-- 연결 상태 표시 -->
    <div class="mb-4 flex items-center gap-2">
      <span class="icon">
        <font-awesome-icon
            :icon="isConnected ? 'circle-check' : 'circle-exclamation'"
            :class="isConnected ? 'text-green-500' : 'text-red-500'"
        />
      </span>
      <span :class="isConnected ? 'text-green-500' : 'text-red-500'">
        {{ isConnected ? '실시간 연결됨' : '연결 끊김' }}
      </span>
      <span v-if="error" class="text-red-500 text-sm">{{ error }}</span>
    </div>

    <!-- 종목 탭 네비게이션 -->
    <div class="mb-4 overflow-x-auto">
      <div class="inline-flex space-x-2 border-b border-gray-200 w-full">
        <!-- 전체 종목 탭 -->
        <button
            class="px-4 py-2 text-sm font-medium"
            :class="activeTab === 'all' ? 'border-b-2 border-blue-500 text-blue-600' : 'text-gray-500'"
            @click="activeTab = 'all'"
        >
          전체 종목
        </button>
        <!-- 개별 종목 탭 -->
        <button
            v-for="stock in stocks"
            :key="stock.stockCode"
            class="px-4 py-2 text-sm font-medium whitespace-nowrap"
            :class="activeTab === stock.stockCode ? 'border-b-2 border-blue-500 text-blue-600' : 'text-gray-500'"
            @click="activeTab = stock.stockCode"
        >
          {{ stock.companyName }}
        </button>
      </div>
    </div>

    <!-- 전체 종목 뷰 -->
    <div v-if="activeTab === 'all'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
      <div
          v-for="stock in stocks"
          :key="stock.stockCode"
          class="bg-white rounded-lg shadow p-4"
      >
        <div class="flex justify-between items-center">
          <div>
            <h3 class="font-bold">{{ stock.companyName }}</h3>
            <p class="text-sm text-gray-500">{{ stock.stockCode }}</p>
          </div>
          <div :class="getPriceColorClass(stock)" class="text-right">
            <div class="text-xl font-bold">
              ￦{{ formatPrice(stock.currentPrice) }}
            </div>
            <div class="text-sm">
              {{ stock.currentPrice > stock.previousPrice ? '+' : '' }}
              {{ calculateChangePercent(stock) }}%
            </div>
          </div>
        </div>
        <div class="mt-2 text-sm text-gray-500">
          거래량: {{ formatVolume(stock.volume) }}
        </div>
      </div>
    </div>

    <!-- 개별 종목 상세 뷰 -->
    <div v-else>
      <div class="bg-white rounded-lg shadow p-4">
        <div v-if="selectedStock" class="space-y-4">
          <!-- 주식 기본 정보 -->
          <div class="flex justify-between items-start">
            <div>
              <h2 class="text-xl font-bold">{{ selectedStock.companyName }}</h2>
              <p class="text-gray-500">{{ selectedStock.stockCode }}</p>
            </div>
            <div :class="getPriceColorClass(selectedStock)" class="text-right">
              <div class="text-3xl font-bold">
                ￦{{ formatPrice(selectedStock.currentPrice) }}
              </div>
              <div class="text-lg">
                {{ selectedStock.currentPrice > selectedStock.previousPrice ? '+' : '' }}
                {{ calculateChangePercent(selectedStock) }}%
                <font-awesome-icon
                    :icon="selectedStock.currentPrice > selectedStock.previousPrice ? 'trending-up' : 'trending-down'"
                />
              </div>
            </div>
          </div>

          <!-- 거래 정보 -->
          <div class="grid grid-cols-2 gap-4 p-4 bg-gray-50 rounded-lg">
            <div>
              <p class="text-sm text-gray-500">거래량</p>
              <p class="text-lg font-bold">{{ formatVolume(selectedStock.volume) }}</p>
            </div>
            <div>
              <p class="text-sm text-gray-500">기준가</p>
              <p class="text-lg font-bold">￦{{ formatPrice(selectedStock.basePrice) }}</p>
            </div>
          </div>

          <!-- 가격 차트 -->
          <div class="h-80">
            <LineChart
                v-if="chartData"
                :chart-data="chartData"
                :options="chartOptions"
            />
          </div>

          <!-- 실시간 거래 기록 -->
          <div class="mt-4">
            <h3 class="text-lg font-bold mb-2">실시간 거래 기록</h3>
            <div class="space-y-2">
              <div
                  v-for="(trade, index) in tradeHistory"
                  :key="index"
                  class="flex justify-between items-center p-2 bg-gray-50 rounded"
              >
                <span class="text-sm">{{ formatTime(trade.timestamp) }}</span>
                <span :class="getPriceColorClass(trade)">
                  ￦{{ formatPrice(trade.currentPrice) }}
                </span>
                <span class="text-sm">{{ formatVolume(trade.volume) }}주</span>
              </div>
            </div>
          </div>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref, computed, onMounted, onUnmounted, watch } from 'vue';
import { Line as LineChart } from 'vue-chartjs';
import webSocketService from '@/services/websocket.service.js';

export default {
  name: 'StockDashboard',
  components: { LineChart },

  setup() {
    const stocks = ref([]);
    const activeTab = ref('all');
    const isConnected = ref(false);
    const error = ref(null);
    const tradeHistory = ref(new Map()); // 종목별 거래 기록
    const priceHistory = ref(new Map()); // 종목별 가격 기록

    const selectedStock = computed(() =>
        stocks.value.find(stock => stock.stockCode === activeTab.value)
    );

    const chartData = computed(() => {
      if (!selectedStock.value) return null;

      const history = priceHistory.value.get(selectedStock.value.stockCode) || [];
      return {
        labels: history.map(item => formatTime(item.timestamp)),
        datasets: [{
          label: '주가',
          data: history.map(item => item.currentPrice),
          borderColor: '#2563eb',
          tension: 0.1,
          fill: false
        }]
      };
    });

    const chartOptions = {
      responsive: true,
      maintainAspectRatio: false,
      scales: {
        y: {
          beginAtZero: false,
          ticks: {
            callback: value => `￦${formatPrice(value)}`
          }
        }
      },
      plugins: {
        legend: {
          display: false
        }
      }
    };

    // WebSocket 이벤트 핸들러
    const handleStockUpdate = (data) => {
      const index = stocks.value.findIndex(s => s.stockCode === data.stockCode);
      if (index >= 0) {
        stocks.value[index] = data;
      } else {
        stocks.value.push(data);
      }

      // 거래 기록 업데이트
      if (!tradeHistory.value.has(data.stockCode)) {
        tradeHistory.value.set(data.stockCode, []);
      }
      const history = tradeHistory.value.get(data.stockCode);
      history.unshift(data);
      if (history.length > 20) history.pop();

      // 가격 기록 업데이트
      if (!priceHistory.value.has(data.stockCode)) {
        priceHistory.value.set(data.stockCode, []);
      }
      const prices = priceHistory.value.get(data.stockCode);
      prices.push(data);
      if (prices.length > 100) prices.shift();
    };

    // 유틸리티 함수들
    const formatPrice = (price) => {
      return new Intl.NumberFormat('ko-KR').format(price);
    };

    const formatVolume = (volume) => {
      return new Intl.NumberFormat('ko-KR').format(volume);
    };

    const formatTime = (timestamp) => {
      return new Date(timestamp).toLocaleTimeString();
    };

    const calculateChangePercent = (stock) => {
      if (!stock.previousPrice) return '0.00';
      return ((stock.currentPrice - stock.previousPrice) / stock.previousPrice * 100).toFixed(2);
    };

    const getPriceColorClass = (stock) => {
      if (stock.currentPrice > stock.previousPrice) return 'text-green-500';
      if (stock.currentPrice < stock.previousPrice) return 'text-red-500';
      return 'text-gray-500';
    };

    // 라이프사이클 훅
    onMounted(async () => {
      try {
        await webSocketService.connect();
        isConnected.value = true;
        webSocketService.subscribeToAllStocks(handleStockUpdate);
      } catch (err) {
        error.value = '연결 실패: ' + err.message;
      }
    });

    onUnmounted(() => {
      webSocketService.disconnect();
    });

    return {
      stocks,
      activeTab,
      isConnected,
      error,
      selectedStock,
      tradeHistory,
      chartData,
      chartOptions,
      formatPrice,
      formatVolume,
      formatTime,
      calculateChangePercent,
      getPriceColorClass
    };
  }
};
</script>

<style scoped>
.icon {
  width: 1rem;
  height: 1rem;
}
</style>