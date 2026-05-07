<template>
  <div class="flex items-center justify-center min-h-screen">
    <p class="animate-pulse">Đang chuyển hướng...</p>
  </div>
</template>
<script setup>
import { onMounted } from 'vue'
import { useRoute } from 'vue-router'
import api from '../services/api'

const route = useRoute()

onMounted(async () => {
  try {
    const code = route.params.shortCode
    // Gọi API lấy URL gốc và tăng click đồng thời
    const res = await api.get(`/api/url/${code}`) 
    window.location.href = res.data.originalUrl
  } catch (err) {
    // Nếu không thấy mã, đẩy về trang 404 hoặc Home
    window.location.href = '/'
  }
})
</script>
