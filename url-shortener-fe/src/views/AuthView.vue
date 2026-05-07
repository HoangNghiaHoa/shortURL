<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { ZapIcon, MailIcon, LockIcon, UserIcon, ArrowRightIcon, Loader2Icon } from 'lucide-vue-next'
import Swal from 'sweetalert2'
import api from '@/services/api'
import { jwtDecode } from "jwt-decode"
const router = useRouter()
const isLogin = ref(true)
const loading = ref(false)

// 1. Gắn đúng data binding
const form = ref({
  email: '',
  password: '',
  fullName: ''
})

// 2. Định nghĩa các hàm Social Login (tránh lỗi undefined)
const loginWithGoogle = () => {
  Swal.fire({
    title: 'Google Login',
    text: 'Tính năng này yêu cầu cấu hình Firebase/Google Cloud ở Backend.',
    icon: 'info',
    confirmButtonColor: '#16A34A'
  })
}

const loginWithFacebook = () => {
  Swal.fire({
    title: 'Facebook Login',
    text: 'Đang kết nối tới API Facebook...',
    icon: 'info',
    confirmButtonColor: '#16A34A'
  })
}

const handleAuth = async () => {
  if (!form.value.email || !form.value.password) {
    Swal.fire('Lỗi', 'Vui lòng điền đầy đủ thông tin', 'warning')
    return
  }

  loading.value = true

  try {
    let res

    if (isLogin.value) {
      // LOGIN
      res = await api.post('/api/auth/login', {
        email: form.value.email,
        password: form.value.password
      })

      const token = res.data.token

        localStorage.setItem('token', token)

        // decode token
        const decoded = jwtDecode(token)

        localStorage.setItem('userId', decoded.userId)

      Swal.fire({
        icon: 'success',
        title: 'Đăng nhập thành công!',
        timer: 1200,
        showConfirmButton: false
      }).then(() => {
        router.push('/dashboard')   // 👉 CHUYỂN TRANG Ở ĐÂY
      })

    } else {
      // REGISTER
      res = await api.post('/api/auth/register', {
        email: form.value.email,
        password: form.value.password,
        fullName: form.value.fullName
      })

      Swal.fire({
        icon: 'success',
        title: 'Đăng ký thành công!',
        text: 'Bây giờ hãy đăng nhập'
      })

      isLogin.value = true
    }

  } catch (err) {
    Swal.fire('Lỗi', err.response?.data || 'Có lỗi xảy ra', 'error')
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <div class="min-h-screen bg-[#F4F7F5] flex items-center justify-center p-6 font-sans">
    <div class="max-w-md w-full">
      <div class="text-center mb-8">
        <div class="inline-flex p-3 bg-green-600 rounded-2xl shadow-xl shadow-green-100 mb-4 cursor-pointer" @click="$router.push('/')">
          <ZapIcon :size="32" class="text-white" fill="currentColor" />
        </div>
        <h1 class="text-3xl font-black text-gray-900 tracking-tight">
          {{ isLogin ? 'Mừng bạn trở lại' : 'Tạo tài khoản mới' }}
        </h1>
      </div>

      <div class="bg-white p-8 rounded-[32px] shadow-sm border border-gray-100">
        
        <div class="grid grid-cols-2 gap-4 mb-8">
          <button @click="loginWithGoogle" type="button"
            class="flex items-center justify-center gap-2 py-3 px-4 border border-gray-200 rounded-2xl hover:bg-gray-50 transition-all font-bold text-sm">
            <img src="https://www.svgrepo.com/show/355037/google.svg" class="w-5 h-5" />
            Google
          </button>

          <button @click="loginWithFacebook" type="button"
            class="flex items-center justify-center gap-2 py-3 px-4 border border-gray-200 rounded-2xl hover:bg-gray-50 transition-all font-bold text-sm">
            <img src="https://www.svgrepo.com/show/521654/facebook.svg" class="w-5 h-5" />
            Facebook
          </button>
        </div>

        <div class="relative mb-8">
          <div class="absolute inset-0 flex items-center">
            <div class="w-full border-t border-gray-100"></div>
          </div>
          <div class="relative flex justify-center text-[10px] uppercase font-black tracking-widest text-gray-400">
            <span class="bg-white px-4">Hoặc dùng Email</span>
          </div>
        </div>

        <form @submit.prevent="handleAuth" class="space-y-4">
          
          <div v-if="!isLogin">
             <div class="relative group">
              <UserIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" :size="18" />
              <input v-model="form.fullName" type="text" placeholder="Họ và tên" required
                class="w-full pl-11 pr-4 py-3.5 bg-gray-50 border border-transparent focus:border-green-500 focus:bg-white rounded-2xl outline-none text-sm transition-all" />
            </div>
          </div>

          <div class="relative group">
            <MailIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" :size="18" />
            <input v-model="form.email" type="email" placeholder="Email của bạn" required
              class="w-full pl-11 pr-4 py-3.5 bg-gray-50 border border-transparent focus:border-green-500 focus:bg-white rounded-2xl outline-none text-sm transition-all" />
          </div>

          <div class="relative group">
            <LockIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-gray-400 group-focus-within:text-green-600 transition-colors" :size="18" />
            <input v-model="form.password" type="password" placeholder="Mật khẩu" required
              class="w-full pl-11 pr-4 py-3.5 bg-gray-50 border border-transparent focus:border-green-500 focus:bg-white rounded-2xl outline-none text-sm transition-all" />
          </div>

          <button :disabled="loading" type="submit"
            class="w-full py-4 bg-gray-900 text-white rounded-2xl font-black text-sm flex items-center justify-center gap-2 hover:bg-black transition-all active:scale-[0.98] disabled:opacity-70">
            <Loader2Icon v-if="loading" class="animate-spin" :size="20" />
            <span v-else>{{ isLogin ? 'Đăng nhập' : 'Tạo tài khoản' }}</span>
            <ArrowRightIcon v-if="!loading" :size="18" />
          </button>
        </form>

        <div class="mt-8 pt-6 border-t border-gray-50 text-center">
          <p class="text-sm text-gray-500 font-medium">
            {{ isLogin ? 'Chưa có tài khoản?' : 'Đã có tài khoản?' }}
            <button @click="isLogin = !isLogin" type="button" class="text-green-600 font-bold hover:underline ml-1">
              {{ isLogin ? 'Đăng ký ngay' : 'Đăng nhập' }}
            </button>
          </p>
        </div>
      </div>
    </div>
  </div>
</template>