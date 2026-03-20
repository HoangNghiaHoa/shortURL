import { defineConfig } from 'vite'
import vue from '@vitejs/plugin-vue'

export default defineConfig({
  plugins: [vue()],
  resolve: {
    alias: {
      '@': '/src',  // Nếu dùng import '@/components/...' thì thêm cái này
    },
  },
})