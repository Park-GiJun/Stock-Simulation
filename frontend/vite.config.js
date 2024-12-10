import { fileURLToPath, URL } from 'node:url'

import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'
import vueDevTools from 'vite-plugin-vue-devtools'

export default defineConfig({
  plugins: [
    vue(),
    vueDevTools(),
  ],
  server: {
    port: 8080,  // 프론트엔드 서버 포트를 8080으로 설정
    proxy: {
      '/ws': {
        target: 'http://localhost:9832', // 백엔드 API 프록시 설정
        ws: true,
        changeOrigin: true
      },
    },
  },
  resolve: {
    alias: {
      '@': fileURLToPath(new URL('./src', import.meta.url))
    },
  },
  define: {
    global: 'window',
  },
})
