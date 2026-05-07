<template>
  <div class="min-h-screen bg-[#F4F7F5] text-[#1A202C] font-sans selection:bg-green-100 overflow-x-hidden">
    
    <nav class="fixed top-0 w-full z-[100] bg-white/80 backdrop-blur-md border-b border-gray-100">
      <div class="max-w-7xl mx-auto px-6 py-4 flex items-center justify-between">
        <div class="flex items-center space-x-2 cursor-pointer" @click="$router.push('/')">
          <div class="bg-[#16A34A] p-2 rounded-lg text-white">
            <Link2Icon :size="22" />
          </div>
          <span class="text-xl font-black tracking-tighter">NanoURL</span>
        </div>
        <div class="hidden lg:flex space-x-8 text-[14px] font-bold text-gray-500">
          <a href="#features" class="hover:text-green-600 transition">Tính năng</a>
          <a href="#pricing" class="hover:text-green-600 transition">Giá cả</a>
          <a href="#" class="hover:text-green-600 transition">Doanh nghiệp</a>
        </div>
        <button @click="$router.push('/dashboard')" class="bg-[#16A34A] text-white px-5 py-2.5 rounded-lg text-sm font-bold hover:bg-green-700 transition-all shadow-md">
          Mở Dashboard
        </button>
      </div>
    </nav>

    <section class="pt-40 pb-20 px-6 max-w-7xl mx-auto flex flex-col lg:flex-row items-center gap-12">
      <div class="flex-1 text-left">
        <p class="text-green-600 font-bold text-sm mb-4">Có gói miễn phí • Không cần thẻ tín dụng</p>
        
        <h1 class="text-4xl md:text-6xl font-black text-[#1A202C] leading-tight mb-6 whitespace-nowrap">
          Rút gọn URL với <br/>
          <span class="text-green-600 inline-block min-w-[300px]">
            {{ typeValue }}<span class="animate-pulse">|</span>
          </span>
        </h1>

        <p class="text-gray-500 text-lg max-w-lg mb-8 leading-relaxed font-medium">
          Tạo link rút gọn thương hiệu với tên miền tùy chỉnh. Theo dõi từng click, tạo mã QR và tối ưu hóa chiến dịch.
        </p>
        <div class="flex gap-4">
          <button @click="$router.push('/auth')" class="bg-[#16A34A] text-white px-8 py-4 rounded-xl font-bold text-lg hover:bg-green-700 shadow-lg shadow-green-100 transition-all flex items-center gap-2">
            Tạo tài khoản miễn phí <ArrowRightIcon :size="20"/>
          </button>
        </div>
      </div>

      <div class="flex-1 w-full max-w-xl">
        <div class="bg-white p-8 rounded-[32px] shadow-[0_20px_60px_rgba(0,0,0,0.05)] border border-gray-100">
          <div class="flex items-center gap-2 text-green-600 font-bold mb-6">
            <LinkIcon :size="20"/> Rút gọn link của bạn
          </div>
          <div class="flex gap-2 mb-6">
            <input v-model="originalUrl" type="text" placeholder="Dán URL dài của bạn vào đây..." 
              class="flex-1 bg-gray-50 border-none px-5 py-4 rounded-xl focus:ring-2 focus:ring-green-500 outline-none font-bold text-gray-700"/>
            <button @click="handleShorten" class="bg-[#16A34A] text-white px-6 py-4 rounded-xl font-bold hover:bg-green-700 transition-all">
              Rút gọn
            </button>
          </div>
          <div v-if="shortUrl" class="bg-green-50 p-5 rounded-2xl border border-dashed border-green-200 animate-fade-in">
             <div class="flex justify-between items-center">
                <span class="text-green-700 font-bold">{{ shortUrl }}</span>
                <button @click="copyToClipboard" class="text-green-600 hover:scale-110 transition"><CopyIcon :size="18"/></button>
             </div>
          </div>
        </div>
      </div>
    </section>

    <div class="bg-white py-14 border-y border-gray-100 overflow-hidden relative">
      <p class="text-center text-gray-400 font-black text-[10px] uppercase tracking-[0.4em] mb-10">
        ĐƯỢC TIN DÙNG BỞI CÁC STARTUP TRÊN TOÀN THẾ GIỚI
      </p>
      
      <div class="flex animate-marquee whitespace-nowrap items-center">
        <div class="flex items-center gap-16 px-8">
          <div v-for="ind in industries" :key="ind.name" class="flex items-center gap-3 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-500 cursor-default">
             <component :is="ind.icon" :size="30" class="text-gray-700"/>
             <span class="font-black text-gray-700 text-lg tracking-tighter italic">{{ ind.name }}</span>
          </div>
        </div>
        <div class="flex items-center gap-16 px-8">
          <div v-for="ind in industries" :key="ind.name + '2'" class="flex items-center gap-3 grayscale opacity-50 hover:grayscale-0 hover:opacity-100 transition-all duration-500 cursor-default">
             <component :is="ind.icon" :size="30" class="text-gray-700"/>
             <span class="font-black text-gray-700 text-lg tracking-tighter italic">{{ ind.name }}</span>
          </div>
        </div>
      </div>
    </div>

    <section id="features" class="py-24 max-w-7xl mx-auto px-6">
      <div class="text-center mb-16">
        <h2 class="text-4xl font-black text-[#1A202C] mb-4 tracking-tight">Mọi thứ bạn cần để quản lý link</h2>
        <p class="text-gray-500 font-medium text-lg">Hệ thống phân tích và tối ưu hóa chuyên sâu</p>
      </div>
      <div class="grid md:grid-cols-3 gap-8 text-center md:text-left">
        <div v-for="feat in mainFeatures" :key="feat.title" class="bg-white p-10 rounded-[24px] border border-gray-100 hover:shadow-2xl hover:-translate-y-2 transition-all duration-300 group">
          <div class="w-14 h-14 bg-green-50 rounded-2xl flex items-center justify-center mb-6 text-green-600 group-hover:bg-green-600 group-hover:text-white transition-all">
            <component :is="feat.icon" :size="28"/>
          </div>
          <h3 class="text-xl font-black mb-3">{{ feat.title }}</h3>
          <p class="text-gray-500 text-sm leading-relaxed mb-6 font-medium">{{ feat.desc }}</p>
          <button class="text-green-600 font-bold text-sm flex items-center gap-1 hover:gap-2 transition-all mx-auto md:mx-0">
            Tìm hiểu thêm <ArrowRightIcon :size="16"/>
          </button>
        </div>
      </div>
    </section>

    <footer class="py-12 text-center text-gray-400 text-sm border-t border-gray-100">
      <p class="font-bold">© 2026 NanoURL System. Built with ❤️ for Students.</p>
    </footer>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { 
  Link2Icon, LinkIcon, ArrowRightIcon, CopyIcon, BarChart3Icon, 
  QrCodeIcon, UsersIcon, Code2Icon, FolderIcon, CpuIcon, 
  UtensilsIcon, GavelIcon, FilmIcon, LandmarkIcon
} from 'lucide-vue-next'
import Swal from 'sweetalert2'

const industries = [
  { name: "CÔNG NGHỆ & AI", icon: CpuIcon },
  { name: "THỰC PHẨM & DỊCH VỤ", icon: UtensilsIcon },
  { name: "CHÍNH PHỦ & CÔNG", icon: GavelIcon },
  { name: "TRUYỀN THÔNG", icon: FilmIcon },
  { name: "TÀI CHÍNH", icon: LandmarkIcon }
]

const mainFeatures = [
  { title: "Phân tích nâng cao", icon: BarChart3Icon, desc: "Theo dõi click, chuyển đổi và hành vi người dùng thời gian thực." },
  { title: "Mã QR động", icon: QrCodeIcon, desc: "Tạo mã QR có thể chỉnh sửa bất cứ lúc nào mà không cần in lại." },
  { title: "Quyền truy cập API", icon: Code2Icon, desc: "Tích hợp NanoURL vào ứng dụng của bạn với REST API mạnh mẽ." }
]

// Typing Effect - FIX: Tránh nhảy dòng
const typeValue = ref('')
const typeArray = ['tên miền tùy chỉnh', 'thương hiệu riêng', 'quản lý thông minh']
let typeArrayIndex = 0
let charIndex = 0

const typeText = () => {
  if (charIndex < typeArray[typeArrayIndex].length) {
    typeValue.value += typeArray[typeArrayIndex].charAt(charIndex);
    charIndex++;
    setTimeout(typeText, 100);
  } else {
    setTimeout(eraseText, 2500);
  }
}

const eraseText = () => {
  if (charIndex > 0) {
    typeValue.value = typeArray[typeArrayIndex].substring(0, charIndex - 1);
    charIndex--;
    setTimeout(eraseText, 50);
  } else {
    typeArrayIndex = (typeArrayIndex + 1) % typeArray.length;
    setTimeout(typeText, 1000);
  }
}

const originalUrl = ref('')
const shortUrl = ref('')
const handleShorten = () => {
  if(!originalUrl.value) return;
  shortUrl.value = 'nanourl.io/brand-link-2026';
  Swal.fire({ toast: true, position: 'top-end', icon: 'success', title: 'Đã tạo xong!', showConfirmButton: false, timer: 1500 });
}

onMounted(typeText);
</script>

<style>
/* Font Inter từ Google Fonts giúp web trông cực "xịn" */
@import url('https://fonts.googleapis.com/css2?family=Inter:wght@400;500;700;900&display=swap');

body {
  font-family: 'Inter', sans-serif;
}

/* Animation chạy ngang vô tận cho dải icon */
@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}
.animate-marquee {
  animation: marquee 30s linear infinite;
  display: flex;
  width: max-content;
}
.animate-marquee:hover {
  animation-play-state: paused;
}

.animate-fade-in {
  animation: fadeIn 0.5s ease-out;
}
@keyframes fadeIn {
  from { opacity: 0; transform: translateY(10px); }
  to { opacity: 1; transform: translateY(0); }
}
</style>