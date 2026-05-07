<template>
  <div class="flex min-h-screen bg-[#F8FAFC] font-sans selection:bg-green-100">
    <Sidebar />

    <main class="flex-1 p-8 lg:p-12">
      <!-- Header Section -->
      <div class="max-w-6xl mx-auto mb-12 flex flex-col md:flex-row md:items-center justify-between gap-8">
        <div class="space-y-2">
          <div class="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-green-100 text-green-700 text-[10px] font-black uppercase tracking-wider">
            Premium Feature
          </div>
          <h1 class="text-4xl lg:text-5xl font-black text-slate-900 tracking-tight">Bí danh cá nhân</h1>
          <p class="text-slate-500 font-medium max-w-md text-sm lg:text-base">
            Tùy chỉnh đường dẫn giúp tăng tỷ lệ nhấn vào link lên đến 40%.
          </p>
        </div>

        <div class="relative group">
          <SearchIcon class="absolute left-4 top-1/2 -translate-y-1/2 text-slate-400 group-focus-within:text-green-500 transition-colors" :size="20" />
          <input 
            v-model="searchQuery" 
            type="text" 
            placeholder="Tìm kiếm bí danh..." 
            class="pl-12 pr-6 py-4 bg-white border border-slate-200 rounded-2xl focus:ring-4 focus:ring-green-500/10 focus:border-green-500 outline-none w-full md:w-80 shadow-sm transition-all font-medium"
          />
        </div>
      </div>

      <!-- Loading State -->
      <div v-if="isLoading" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8 max-w-6xl mx-auto">
        <div v-for="i in 6" :key="i" class="h-56 bg-white border border-slate-100 rounded-2xl animate-pulse p-6">
          <div class="flex justify-between mb-4"><div class="w-12 h-12 bg-slate-100 rounded-xl"></div><div class="w-10 h-6 bg-slate-100 rounded-md"></div></div>
          <div class="h-6 w-3/4 bg-slate-100 rounded-md mb-2"></div>
          <div class="h-4 w-full bg-slate-100 rounded-md mb-6"></div>
          <div class="flex gap-2"><div class="flex-1 h-10 bg-slate-100 rounded-xl"></div><div class="w-12 h-10 bg-slate-100 rounded-xl"></div></div>
        </div>
      </div>

      <!-- Main Content -->
      <div v-else class="max-w-6xl mx-auto">
        <div v-if="filteredAliases.length > 0" class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <AliasCard 
            v-for="url in filteredAliases" 
            :key="url.id" 
            :url="url"
            @copy="handleCopy"
            @edit="openEditModal"
          />
        </div>

        <!-- Empty State -->
        <div v-else class="flex flex-col items-center justify-center py-24 bg-white rounded-[40px] border-2 border-dashed border-slate-200">
          <div class="w-24 h-24 bg-slate-50 rounded-full flex items-center justify-center mb-6">
            <HashIcon :size="48" class="text-slate-200" />
          </div>
          <h3 class="text-2xl font-black text-slate-900 mb-2">Chưa tìm thấy bí danh</h3>
          <p class="text-slate-400 font-medium mb-8 text-center max-w-xs px-4">
            Bạn chưa có bí danh nào hoặc từ khóa tìm kiếm không khớp.
          </p>
          <button @click="$router.push('/dashboard')" class="bg-slate-900 text-white px-8 py-4 rounded-2xl font-black text-sm hover:bg-green-600 transition-all shadow-lg active:scale-95">
            TẠO ALIAS NGAY
          </button>
        </div>
      </div>
    </main>

    <!-- Modal Cập Nhật -->
    <div v-if="showEditModal" class="fixed inset-0 z-[100] flex items-center justify-center p-6">
      <div class="absolute inset-0 bg-slate-900/60 backdrop-blur-md" @click="showEditModal = false"></div>
      <div class="relative bg-white rounded-[32px] shadow-2xl w-full max-w-md p-10 animate-in zoom-in duration-300">
        <h3 class="text-3xl font-black text-slate-900 mb-2">Đổi bí danh</h3>
        <p class="text-slate-400 text-sm mb-8 font-medium">Bí danh giúp người dùng tin tưởng hơn khi nhấn vào.</p>
        
        <div class="space-y-6">
          <div>
            <label class="text-[11px] font-black uppercase tracking-widest text-slate-400 ml-1 mb-2 block">Bí danh mong muốn</label>
            <div class="flex items-center bg-slate-50 border-2 rounded-2xl px-5 py-4 transition-all" :class="aliasStatusClass">
              <span class="text-slate-400 font-bold text-sm">nanourl.io/</span>
              <input v-model="newAliasInput" type="text" @input="debouncedCheck"
                class="flex-1 bg-transparent border-none outline-none ml-1 text-sm font-black text-slate-800" />
              
              <Loader2Icon v-if="isChecking" class="animate-spin text-blue-500" :size="20" />
              <CheckCircleIcon v-else-if="isAvailable && newAliasInput" class="text-green-500" :size="20" />
              <XCircleIcon v-else-if="!isAvailable && newAliasInput" class="text-red-500" :size="20" />
            </div>
            <p v-if="checkMessage" class="mt-3 text-xs font-bold px-1" :class="isAvailable ? 'text-green-600' : 'text-red-500'">
              {{ checkMessage }}
            </p>
          </div>
        </div>

        <div class="flex gap-4 mt-12">
          <button @click="showEditModal = false" class="flex-1 px-4 py-4 rounded-2xl font-black text-xs text-slate-400 hover:bg-slate-50 transition-all uppercase tracking-widest">Hủy</button>
          <button @click="handleUpdate" :disabled="!isAvailable || isChecking"
            class="flex-[2] px-4 py-4 rounded-2xl bg-green-600 font-black text-xs text-white hover:bg-green-700 disabled:bg-slate-200 disabled:text-slate-400 shadow-xl shadow-green-200 transition-all active:scale-95 uppercase tracking-widest">
            Cập nhật ngay
          </button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import AliasCard from '../components/AliasCard.vue'  
import api from '@/services/api'
import Swal from 'sweetalert2'
import { SearchIcon, HashIcon, Loader2Icon, CheckCircleIcon, XCircleIcon } from 'lucide-vue-next'
import _ from 'lodash' // Nhớ npm install lodash nhé

const urls = ref([])
const searchQuery = ref('')
const isLoading = ref(false)
const showEditModal = ref(false)
const editingUrl = ref(null)

// Logic check trùng
const newAliasInput = ref('')
const isChecking = ref(false)
const isAvailable = ref(false)
const checkMessage = ref('')

const fetchAliases = async () => {
  isLoading.value = true
  try {
    const res = await api.get('/api/urls/user')
    // Tạm thời bỏ filter để kiểm tra hiển thị Card
    urls.value = res.data || [] 
  } catch (e) { 
    console.error(e) 
  } finally { 
    isLoading.value = false 
  }
}

const filteredAliases = computed(() => {
  return urls.value.filter(u => u.shortCode.toLowerCase().includes(searchQuery.value.toLowerCase()))
})

const openEditModal = (url) => {
  editingUrl.value = url
  newAliasInput.value = url.shortCode
  isAvailable.value = true
  checkMessage.value = ''
  showEditModal.value = true
}

// Debounce check trùng để tránh gọi API liên tục
const debouncedCheck = _.debounce(async () => {
  if (!newAliasInput.value || newAliasInput.value === editingUrl.value.shortCode) {
    isAvailable.value = true
    checkMessage.value = ''
    return
  }
  
  isChecking.value = true
  try {
    const res = await api.get(`/api/urls/check-alias?alias=${newAliasInput.value}`)
    isAvailable.value = res.data.available
    checkMessage.value = res.data.message
  } catch (e) {
    isAvailable.value = false
    checkMessage.value = "Lỗi kiểm tra bí danh"
  } finally {
    isChecking.value = false
  }
}, 500)

const handleUpdate = async () => {
  try {
    // 1. Lấy userId từ localStorage
    const userId = localStorage.getItem('userId');

    // 2. Kiểm tra nếu không có userId thì báo lỗi luôn, không gửi request
    if (!userId) {
      Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không tìm thấy thông tin người dùng!' });
      return;
    }

    await api.put('/api/urls/update-alias', {
      oldShortCode: editingUrl.value.shortCode,
      newAlias: newAliasInput.value,
      userId: Number(userId) // Ép kiểu về số để khớp với kiểu Long ở Backend
    })

    showEditModal.value = false
    Swal.fire({ 
      icon: 'success', 
      title: 'Đã cập nhật!', 
      timer: 1500, 
      showConfirmButton: false 
    })
    fetchAliases()
  } catch (e) {
    console.error(e)
    Swal.fire({ 
      icon: 'error', 
      title: 'Thất bại', 
      text: e.response?.data?.message || 'Có lỗi xảy ra khi đổi bí danh' 
    })
  }
}

const handleCopy = (code) => {
  navigator.clipboard.writeText(`http://localhost:8080/${code}`)
  Swal.fire({ toast: true, position: 'bottom-end', icon: 'success', title: 'Đã copy link!', showConfirmButton: false, timer: 1000 })
}

const aliasStatusClass = computed(() => {
  if (!newAliasInput.value) return 'border-slate-200'
  if (isChecking.value) return 'border-blue-200'
  return isAvailable.value ? 'border-green-200 bg-green-50/30' : 'border-red-200 bg-red-50/30'
})

onMounted(fetchAliases)
</script>