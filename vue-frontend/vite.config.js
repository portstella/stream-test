import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

// https://vitejs.dev/config/
export default defineConfig({
  plugins: [vue()],
  server: {
    proxy: {
      // 将所有/api开头的请求代理到Spring Boot服务
      '/api': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      },
      // 新增：将/v1开头的请求也代理到Spring Boot服务
      '/v1': {
        target: 'http://localhost:8080',
        changeOrigin: true,
      }
    }
  }
})
