<template>
  <div class="flex min-h-screen bg-[#F4F7F5] font-sans selection:bg-green-100">
    <Sidebar />
    
    <main class="flex-1 flex flex-col">
      <header class="bg-white/80 backdrop-blur-md border-b border-gray-100 p-4 sticky top-0 z-10">
        <div class="max-w-4xl mx-auto">
          <div class="relative flex items-center bg-gray-50 border border-gray-200 rounded-2xl p-1.5 focus-within:ring-2 focus-within:ring-green-500 focus-within:bg-white transition-all shadow-sm">
            <div class="pl-4 text-gray-400">
              <LinkIcon :size="18" />
            </div>
            <input 
              v-model="originalUrl"
              @keyup.enter="handleShorten"
              type="text" 
              placeholder="Dán link dài vào đây để rút gọn ngay..." 
              class="flex-1 bg-transparent border-none outline-none px-4 py-2.5 text-sm font-bold text-gray-700 placeholder:font-medium placeholder:text-gray-300"
            />
            <button 
              @click="handleShorten" 
              :disabled="!originalUrl"
              class="bg-green-600 hover:bg-green-700 disabled:bg-gray-300 text-white px-6 py-2.5 rounded-xl transition-all shadow-md active:scale-95 font-bold text-sm flex items-center gap-2"
            >
              <PlusIcon :size="18" /> Rút gọn
            </button>
          </div>
        </div>
      </header>

      <div class="p-8 max-w-7xl mx-auto w-full">
        <div class="flex flex-col md:flex-row md:items-center justify-between mb-10 gap-6">
          <div>
          <h1 class="text-3xl font-black text-gray-900 tracking-tighter flex items-center gap-3">
            Danh sách liên kết
            <span class="text-[11px] font-black text-green-600 ...">
              {{ filteredUrls.length }} Links
            </span>
          </h1>
            <p class="text-gray-400 text-sm font-medium mt-1">Quản lý và theo dõi hiệu suất các đường dẫn của bạn.</p>
          </div>
          
          <div class="flex items-center gap-3">
            <div class="relative group">
              <SearchIcon :size="16" class="absolute left-3 top-1/2 -translate-y-1/2 text-gray-300 group-focus-within:text-green-500 transition-colors" />
              <input 
                v-model="searchQuery"
                type="text" 
                placeholder="Tìm kiếm link..." 
                class="pl-10 pr-4 py-3 border border-gray-200 rounded-xl text-sm font-bold focus:ring-2 focus:ring-green-500 outline-none w-72 bg-white shadow-sm transition-all" 
              />
            </div>
            <button class="flex items-center gap-2 px-5 py-3 bg-white border border-gray-100 rounded-xl text-sm font-black text-gray-600 hover:bg-gray-50 shadow-sm transition-all uppercase tracking-tighter">
              <FilterIcon :size="16" /> Lọc
            </button>
          </div>
        </div>

        <div class="bg-white rounded-[32px] shadow-[0_20px_50px_rgba(0,0,0,0.03)] border border-gray-100 overflow-hidden">
          <table class="w-full text-left border-collapse">
            <thead class="bg-gray-50/50 border-b border-gray-100 text-gray-400 text-[10px] uppercase tracking-[0.2em] font-black">
              <tr>
                <th class="px-8 py-5">Ngày tạo</th>
                <th class="px-8 py-5">Link rút gọn</th>
                <th class="px-8 py-5">Link gốc</th>
                <th class="px-8 py-5 text-center">Lượt click</th>
                <th class="px-8 py-5 text-right">Hành động</th>
              </tr>
            </thead>
            <tbody class="divide-y divide-gray-50">
              <template v-if="isLoading">
                <SkeletonRow v-for="i in 5" :key="i" />
              </template>
              <tr v-if="filteredUrls.length === 0">
                <td colspan="5" class="py-24 text-center">
                  <div class="flex flex-col items-center">
                    <div class="w-20 h-20 bg-gray-50 rounded-full flex items-center justify-center mb-4">
                       <SearchIcon :size="40" class="text-gray-200" />
                    </div>
                    <p class="text-gray-900 font-black text-lg">Không tìm thấy kết quả</p>
                    <p class="text-sm text-gray-400 font-medium">Thử thay đổi từ khóa tìm kiếm của bạn xem sao.</p>
                  </div>
                </td>
              </tr>

              <template v-else>
               <tr v-for="url in filteredUrls" :key="url.id" class="hover:bg-green-50/30 transition-all group">
                <td class="px-8 py-6">
                  <div class="flex items-center gap-3">
                    <div class="w-8 h-8 bg-gray-100 rounded-xl flex items-center justify-center text-[10px] font-black text-gray-500 group-hover:bg-green-100 group-hover:text-green-600 transition-colors">
                      {{ url.shortCode?.charAt(0)?.toUpperCase() || '?' }}
                    </div>
                    <span class="text-xs text-gray-400 font-bold uppercase">
                      {{ new Date(url.createdAt).toLocaleDateString('vi-VN') }}
                    </span>
                  </div>
                </td>
                <td class="px-8 py-6">
                  <div class="flex items-center gap-2">
                    <span class="font-black text-green-600 text-sm tracking-tight">nanourl.io/{{ url.shortCode }}</span>
                    <button @click="handleCopy(url.shortCode)" class="p-1.5 hover:bg-white rounded-lg border border-transparent hover:border-gray-200 transition-all text-gray-300 hover:text-green-600">
                      <CopyIcon :size="14" />
                    </button>
                  </div>
                </td>
                <td class="px-8 py-6">
                  <div class="max-w-[280px]">
                    <p class="text-sm font-black text-gray-800 truncate mb-0.5">Tiêu đề liên kết...</p>
                    <a :href="url.originalUrl" target="_blank" class="text-xs text-gray-400 truncate flex items-center hover:text-green-500 font-medium">
                      {{ url.originalUrl }} <ExternalLinkIcon :size="10" class="ml-1" />
                    </a>
                  </div>
                </td>
               <td class="px-8 py-6 text-center">
                <span class="inline-block text-xs font-black bg-gray-900 text-white px-3 py-1 rounded-lg shadow-sm">
                  {{ url.clickCount || 0 }} </span>
              </td>
                <td class="px-8 py-6 text-right">
                  <div class="flex justify-end gap-1">
                    <button @click="openEditModal(url)" class="p-2.5 text-gray-400 hover:text-green-600 ...">
                    <Edit3Icon :size="16"/>
                    </button>
                    <button 
                      @click="$router.push(`/stats/${url.shortCode}`)" 
                      class="p-2.5 text-gray-400 hover:text-blue-500 ..."
                    >
                      <BarChart3Icon :size="16"/>
                    </button>
                    <button class="p-2.5 text-gray-400 hover:text-red-500 hover:bg-white rounded-xl transition-all border border-transparent hover:border-gray-100 shadow-sm hover:shadow-md">
                      <Trash2Icon :size="16"/>
                    </button>
                  </div>
                </td>
              </tr>
            </template>
            </tbody>
          </table>
          
          <div class="px-8 py-5 border-t border-gray-50 bg-gray-50/20 flex justify-between items-center text-[11px] font-black text-gray-400 uppercase tracking-widest">
              <span>Hiển thị {{ filteredUrls.length }} kết quả</span>
              <div class="flex items-center gap-4">
                <div class="flex items-center gap-2">
                  <span>Mỗi trang:</span>
                  <select class="bg-transparent font-black text-gray-600 outline-none cursor-pointer">
                    <option>20</option>
                    <option>50</option>
                  </select>
                </div>
                <div class="flex gap-2">
                  <button class="p-1 hover:text-green-600 transition disabled:opacity-30" disabled><ChevronLeftIcon :size="18"/></button>
                  <button class="p-1 hover:text-green-600 transition"><ChevronRightIcon :size="18"/></button>
                </div>
              </div>
          </div>
        </div>
      </div>
      <!-- MODAL (Thêm vào cuối file, trước thẻ đóng </main>) -->
      <div v-if="isEditingAlias" class="fixed inset-0 z-[100] flex items-center justify-center p-4">
        <div class="absolute inset-0 bg-gray-900/40 backdrop-blur-sm" @click="isEditingAlias = false"></div>
        <div class="relative bg-white rounded-[24px] shadow-2xl w-full max-w-md p-8 animate-in fade-in zoom-in duration-200">
          <h3 class="text-xl font-black text-gray-900 mb-2">Đổi bí danh (Alias)</h3>
          <p class="text-sm text-gray-400 font-medium mb-6">Gợi ý: dùng các từ khóa liên quan đến nội dung link.</p>
          
          <div class="bg-gray-50 border border-gray-200 rounded-xl px-4 py-3 flex items-center focus-within:ring-2 focus-within:ring-green-500">
            <span class="text-gray-400 font-bold text-sm">nanourl.io/</span>
            <input v-model="newAliasInput" type="text" class="flex-1 bg-transparent border-none outline-none ml-1 text-sm font-bold text-gray-800" />
          </div>

          <div class="flex gap-3 mt-8">
            <button @click="isEditingAlias = false" class="flex-1 px-6 py-3 rounded-xl border font-black text-sm text-gray-500 hover:bg-gray-50">HỦY</button>
            <button @click="handleUpdateAlias" class="flex-1 px-6 py-3 rounded-xl bg-green-600 font-black text-sm text-white hover:bg-green-700 shadow-lg active:scale-95 transition-all">LƯU LẠI</button>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue' // Thêm watch
import { useRoute, useRouter } from 'vue-router' // Thêm useRoute và useRouter
import SkeletonRow from '../components/SkeletonRow.vue' // Import Skeleton
import Sidebar from '../components/Sidebar.vue'
import api from '@/services/api'
import { 
  CopyIcon, BarChart3Icon, Edit3Icon, Trash2Icon, 
  PlusIcon, SearchIcon, FilterIcon, ExternalLinkIcon,
  LinkIcon, ChevronLeftIcon, ChevronRightIcon
} from 'lucide-vue-next'
import Swal from 'sweetalert2'

// DATA MẪU
const route = useRoute()
const router = useRouter()
const urls = ref([])
const originalUrl = ref('')
const searchQuery = ref('')
const isLoading = ref(false)
// Logic cho Custom Alias Modal
const isEditingAlias = ref(false)
const editingItem = ref(null)
const newAliasInput = ref('')



// LOGIC LỌC DỮ LIỆU ĐA NĂNG
const filteredUrls = computed(() => {
  let list = urls.value

  // Lọc theo thanh tìm kiếm (giữ nguyên logic của em)
  return list.filter(item => 
    item.shortCode.toLowerCase().includes(searchQuery.value.toLowerCase()) ||
    item.originalUrl.toLowerCase().includes(searchQuery.value.toLowerCase())
  )
})


// 1. Hàm lấy dữ liệu từ Backend
const fetchLinks = async () => {
  try {
    isLoading.value = true

    const response = await api.get('/api/urls/user') // ✅ đúng
    urls.value = response.data || []

  } catch (error) {
    console.error("Fetch error:", error)
  } finally {
    isLoading.value = false
  }
}
// 2. Hàm rút gọn link (Call API thật)
const handleShorten = async () => {
  if (!originalUrl.value) return

  try {
    const userId = localStorage.getItem('userId')

    if (!userId) {
      console.error("No userId found")
      return
    }

    const response = await api.post('/api/shorten', {
      url: originalUrl.value,
      userId: userId
    })
    
    // Sau khi tạo xong, tải lại danh sách
    await fetchLinks()
    originalUrl.value = ''
    
    Swal.fire({
      toast: true,
      position: 'top-end',
      icon: 'success',
      title: 'Rút gọn thành công!',
      showConfirmButton: false,
      timer: 2000
    })
  } catch (error) {
    Swal.fire({ icon: 'error', title: 'Lỗi', text: 'Không thể tạo link' })
  }
}

// 3. Hàm Copy
const handleCopy = (code) => {
  // Thay thế bằng URL Backend của em
  const backendBaseUrl = 'http://localhost:8080' 
  const fullUrl = `${backendBaseUrl}/${code}` 
  
  navigator.clipboard.writeText(fullUrl)
  
  Swal.fire({ 
    toast: true, 
    position: 'bottom-end', 
    icon: 'success', 
    title: 'Đã copy link rút gọn!', 
    showConfirmButton: false, 
    timer: 1500 
  })
}

onMounted(fetchLinks)

// Kiểm tra xem user đang ở trang nào dựa vào Router index.js em đã gửi
const isAliasPage = computed(() => route.meta.isAliasPage === true)

// Hàm mở Modal chỉnh sửa
const openEditModal = (url) => {
  editingItem.value = url
  newAliasInput.value = url.shortCode
  isEditingAlias.value = true
}

// Hàm gọi API update Alias
const handleUpdateAlias = async () => {
  const userId = localStorage.getItem('userId'); // Lấy ID người dùng

  try {
    isLoading.value = true;
    await api.put('/api/urls/update-alias', {
      oldShortCode: editingItem.value.shortCode,
      newAlias: newAliasInput.value,
      userId: userId // ✅ Gửi thêm userId lên Backend
    });
    
    isEditingAlias.value = false;
    await fetchLinks(); // Load lại danh sách để cập nhật UI
    if (route.path.includes('/stats/')) {
       router.push(`/stats/${newAliasInput.value}`);
    }
    
    Swal.fire({ 
      icon: 'success', 
      title: 'Thành công', 
      text: 'Đã đổi bí danh và bảo toàn dữ liệu!', // Nếu BE em đã xử lý update click
      timer: 1500, 
      showConfirmButton: false 
    });
  } catch (error) {
    console.error("Update Alias Error:", error);
    Swal.fire({ 
      icon: 'error', 
      title: 'Lỗi cập nhật', 
      text: error.response?.data?.message || error.response?.data || 'Bí danh đã tồn tại hoặc lỗi hệ thống' 
    });
  } finally {
    isLoading.value = false;
  }
}

// Theo dõi khi chuyển giữa các menu trên Sidebar
watch(() => route.path, () => {
  searchQuery.value = '' // Reset tìm kiếm khi chuyển trang
})
</script>

<style scoped>
/* Hiệu ứng mượt cho các dòng trong bảng */
tr {
  transition: all 0.2s ease;
}
</style>