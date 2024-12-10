<!-- src/views/StockViews.vue -->
<template>
  <div class="min-h-screen bg-gray-100 flex">
    <!-- 우측 사이드바 탭 네비게이션 -->
    <div class="w-1/5 bg-white shadow-md p-4">
      <h2 class="text-lg font-bold mb-4">종목 선택</h2>
      <div class="flex flex-col space-y-2">
        <button
            class="px-4 py-2 text-sm font-medium rounded hover:bg-gray-200"
            :class="activeTab === 'all' ? 'bg-blue-500 text-white' : 'text-gray-700'"
            @click="activeTab = 'all'"
        >
          전체 종목
        </button>
        <button
            v-for="stock in stocks"
            :key="stock.stockCode"
            class="px-4 py-2 text-sm font-medium rounded hover:bg-gray-200"
            :class="activeTab === stock.stockCode ? 'bg-blue-500 text-white' : 'text-gray-700'"
            @click="activeTab = stock.stockCode"
        >
          {{ stock.companyName }}
        </button>
      </div>
    </div>

    <!-- 메인 콘텐츠 영역 -->
    <div class="w-4/5 p-4">
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

      <!-- 전체 종목 뷰 -->
      <div v-if="activeTab === 'all'" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
        <div
            v-for="stock in stocks"
            :key="stock.stockCode"
            class="bg-white rounded-lg shadow p-4 cursor-pointer hover:shadow-lg transition-shadow"
            @click="activeTab = stock.stockCode"
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
      <div v-else-if="selectedStock" class="bg-white rounded-lg shadow p-4">
        <div class="space-y-4">
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
        </div>
      </div>
    </div>
  </div>
</template>


<script>
import { ref, computed, onMounted, onUnmounted } from 'vue';
import { Line as LineChart } from 'vue-chartjs';
import webSocketService from '@/services/websocket.service';

export default {
  name: 'StockViews',
  components: { LineChart },

  setup() {
    const stocks = ref([]);
    const activeTab = ref('all');
    const isConnected = ref(false);
    const error = ref(null);
    const tradeHistory = ref(new Map());
    const priceHistory = ref(new Map());

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

    const handleStockUpdate = (data) => {
      const index = stocks.value.findIndex(s => s.stockCode === data.stockCode);
      if (index >= 0) {
        stocks.value[index] = data;
      } else {
        stocks.value.push(data);
      }

      updateHistory(data);
    };

    const updateHistory = (data) => {
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

    const getTradeHistory = (stockCode) => {
      return tradeHistory.value.get(stockCode) || [];
    };

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
      chartData,
      chartOptions,
      getTradeHistory,
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

button {
  transition: background-color 0.3s, color 0.3s;
}
</style>
