import { defineConfig } from 'vite'
import react from '@vitejs/plugin-react'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [react()],
  server: {
    proxy: {
      // shared/api/index.ts가 "/api/..." 상대경로로 fetch하므로,
      // 백엔드(Spring Boot, 8080)로 프록시해야 CORS 설정 없이 바로 붙는다.
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
    },
  },
})
