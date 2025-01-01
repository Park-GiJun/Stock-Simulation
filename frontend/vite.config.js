import { fileURLToPath, URL } from 'node:url';

import { defineConfig } from 'vite';
import vue from '@vitejs/plugin-vue';
import vueDevTools from 'vite-plugin-vue-devtools';

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    port: 8080, // 프론트엔드 개발 서버 포트
    proxy: {
      '/api': {
        target: 'http://15.164.142.172:9832',  // 백엔드 서버 주소
        changeOrigin: true, // Origin 헤더 변경 허용
        secure: false,      // HTTPS가 아닌 경우 설정
        rewrite: (path) => path.replace(/^\/api/, '/api'), // URL 경로 유지
      },
      '/ws': {
        target: 'http://15.164.142.172:9832',  // 백엔드 서버 주소
        ws: true,         // WebSocket 지원 활성화
        changeOrigin: true,
        secure: false,
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url)),
    },
  },
  define: {
    global: 'window', // `global` 사용 문제 해결
  },
});
