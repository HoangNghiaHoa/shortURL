<template>
    <!-- ================== APP WRAPPER ================== -->
  <!-- Container bao toàn bộ landing page -->
  <div class="min-h-screen bg-white text-gray-900 font-sans selection:bg-indigo-100">
     <!-- ================== NAVBAR / HEADER ================== -->
    <!-- Thanh điều hướng trên cùng -->
    <nav class="flex items-center justify-between px-6 py-4 max-w-7xl mx-auto">
          <!-- Logo + Tên app -->
      <div class="flex items-center space-x-2">
        <div class="bg-indigo-600 p-2 rounded-lg text-white">
          <Link2Icon :size="24" />
        </div>
        <span class="text-xl font-bold tracking-tight">NanoURL</span>
      </div>
      
      <!-- Menu (ẩn trên mobile) -->
      <div class="hidden md:flex space-x-8 text-sm font-medium text-gray-600">
        <a href="#features" class="hover:text-indigo-600 transition">Tính năng</a>
        <a href="#analytics" class="hover:text-indigo-600 transition">Phân tích</a>
        <a href="#" class="hover:text-indigo-600 transition">Bảng giá</a>
      </div>
       <!-- Nút đăng nhập -->
      <button class="bg-gray-900 text-white px-5 py-2 rounded-full text-sm font-semibold hover:bg-gray-800 transition">
        Đăng nhập
      </button>
    </nav>

    <!-- ================== HERO SECTION ================== -->
    <!-- Phần giới thiệu + nhập link rút gọn -->
    <section class="relative pt-20 pb-32 overflow-hidden">
      <div class="max-w-7xl mx-auto px-6 text-center">
          <!-- Tiêu đề chính -->
        <h1 class="text-5xl md:text-7xl font-extrabold text-gray-900 mb-6 tracking-tight">
          Rút gọn link. <span class="text-indigo-600">Đo lường kết quả.</span>
        </h1>
           <!-- Mô tả ngắn -->
        <p class="text-lg text-gray-500 max-w-2xl mx-auto mb-10">
          Công cụ mạnh mẽ giúp bạn tạo các liên kết ngắn gọn, thương hiệu hóa và theo dõi dữ liệu người dùng thời gian thực trên toàn cầu.
        </p>
        <!-- Ô nhập link + nút rút gọn -->
        <div class="max-w-3xl mx-auto bg-white p-2 rounded-2xl shadow-[0_20px_50px_rgba(8,_112,_184,_0.1)] border border-gray-100">
          <div class="flex flex-col md:flex-row gap-2">
              <!-- Input: nhập link gốc -->
            <input 
              v-model="originalUrl" 
              type="text"
              placeholder="Dán link dài cần rút gọn tại đây..." 
              class="flex-1 px-6 py-4 rounded-xl bg-gray-50 focus:bg-white outline-none border-2 border-transparent 
              focus:border-indigo-500 transition-all"
            />
               <!-- Button: gọi API rút gọn -->
            <button 
              @click="handleShorten"
              :disabled="isLoading"
              class="bg-indigo-600 hover:bg-indigo-700 text-white px-8 py-4 rounded-xl font-bold transition-all
               flex items-center justify-center disabled:opacity-50"
            >
              <Loader2Icon v-if="isLoading" class="animate-spin mr-2" />
              {{ isLoading ? 'Đang tạo...' : 'Rút gọn ngay' }}

            </button>
          </div>
        </div>
      </div>
<!--Demo xem thử thống kê với link rút gọn-->
        <div class="mt-8 flex flex-col items-center animate-fade-in">
          <p class="text-gray-400 text-sm mb-3 italic">Chưa có link? Hãy thử khám phá dữ liệu mẫu</p>
          <router-link 
            to="/stats/demo" 
            class="group flex items-center gap-3 bg-white hover:bg-indigo-50 text-indigo-600 border-2 border-indigo-100 
            px-6 py-3 rounded-full font-bold transition-all shadow-sm hover:shadow-md hover:border-indigo-300"
          >
            <div class="p-1.5 bg-indigo-100 text-indigo-600 rounded-full group-hover:bg-indigo-600 group-hover:text-white transition-colors">
              <BarChart3Icon :size="16" />
            </div>
            Xem thống kê mẫu (Real-time Demo)
          </router-link>
        </div>
        <!--Hiển thị phần người dùng, lượt truy cập mock-data-->
        <div class="mt-12 flex justify-center space-x-16">
          <div class="text-center"> 
            <p class="text-2xl font-bold text-gray-900">10M+</p>
            <p class="text-xs text-gray-400 uppercase tracking-widest"> Link rút gọn</p>
          </div>
          <div class="border-l border-gray-100"></div>
          <div class="text-center">
            <p class="text-2xl font-bold text-gray-900">50+</p>
            <p class="text-xs text-gray-400 uppercase tracking-widest">Lượt click / tháng</p>
          </div>
          <div class="border-l border-gray-100"></div>
          <div class="text-center">
            <p class="text-2xl font-bold text-gray-900">99.9%</p>
            <p class="text-xs text-gray-400 uppercase tracking-widest">Thời gian hoạt động</p>
          </div>
        </div>
<!--Dải băng chạy dọc tạo tính động cho website-->
<div class="mt-28 mb-16">
  <div class="mb-10 text-center px-4">
    <div class="inline-block">
      <p class="text-[12px] md:text-sm font-black tracking-[0.3em] text-gray-700 uppercase mb-3">
        Giải pháp tối ưu cho các nhà sáng tạo và doanh nghiệp toàn cầu
      </p>
      <div class="h-1.5 w-16 bg-indigo-600 mx-auto rounded-full"></div>
    </div>
  </div>

  <div class="bg-gray-50/80 py-12 border-y border-gray-100 overflow-hidden relative group">
    <div class="absolute inset-y-0 left-0 w-12 bg-gradient-to-r from-white to-transparent z-10"></div>
    <div class="absolute inset-y-0 right-0 w-12 bg-gradient-to-l from-white to-transparent z-10"></div>

    <div class="flex animate-marquee whitespace-nowrap">
      <div class="flex items-center space-x-16 px-8">
        <div class="flex items-center space-x-3 text-gray-800 font-extrabold">
          <ShoppingCartIcon :size="22" class="text-indigo-600" />
          <span class="tracking-tight">E-COMMERCE</span>
        </div>
        <div class="flex items-center space-x-3 text-gray-800 font-extrabold">
          <MegaphoneIcon :size="22" class="text-indigo-600" />
          <span class="tracking-tight">DIGITAL MARKETING</span>
        </div>
        <div class="flex items-center space-x-3 text-gray-800 font-extrabold">
          <StoreIcon :size="22" class="text-indigo-600" />
          <span class="tracking-tight">RETAIL BẢN LẺ</span>
        </div>
        <div class="flex items-center space-x-3 text-gray-800 font-extrabold">
          <Building2Icon :size="22" class="text-indigo-600" />
          <span class="tracking-tight">BẤT ĐỘNG SẢN</span>
        </div>
        <div class="flex items-center space-x-3 text-gray-800 font-extrabold">
          <GraduationCapIcon :size="22" class="text-indigo-600" />
          <span class="tracking-tight">GIÁO DỤC 4.0</span>
        </div>
      </div>
      
      <div class="flex items-center space-x-16 px-8 text-gray-800 font-extrabold">
         <div class="flex items-center space-x-3"> <ShoppingCartIcon :size="22" class="text-indigo-600" /> <span>E-COMMERCE</span> </div>
         <div class="flex items-center space-x-3"> <MegaphoneIcon :size="22" class="text-indigo-600" /> <span>DIGITAL MARKETING</span> </div>
         <div class="flex items-center space-x-3"> <StoreIcon :size="22" class="text-indigo-600" /> <span>RETAIL BẢN LẺ</span> </div>
         <div class="flex items-center space-x-3"> <Building2Icon :size="22" class="text-indigo-600" /> <span>BẤT ĐỘNG SẢN</span> </div>
         <div class="flex items-center space-x-3"> <GraduationCapIcon :size="22" class="text-indigo-600" /> <span>GIÁO DỤC 4.0</span> </div>
      </div>
    </div>
      </div>
        <!-- ================== SHORT URL RESULT ================== -->
        <!-- Hiển thị kết quả sau khi rút gọn -->
        <transition name="slide-up">
          <div v-if="shortUrl" class="max-w-3xl mx-auto mt-6 p-4 bg-indigo-50 rounded-xl border
          border-indigo-100 flex items-center justify-between">
          
            <!-- Link rút gọn + copy -->
            <div class="flex items-center space-x-3">
              <span class="text-indigo-600 font-semibold">{{ shortUrl }}</span>
              <button @click="copyToClipboard" class="p-2 hover:bg-indigo-200 rounded-lg transition">
                <CopyIcon :size="18" class="text-indigo-600" />
              </button>
            </div>
               <!-- Link xem thống kê -->
            <router-link :to="`/stats/${shortCode}`" class="text-sm font-bold text-indigo-700 
            flex items-center hover:underline">
              <BarChart3Icon class="w-4 h-4 mr-1" /> Xem thống kê
            </router-link>
          </div>
        </transition>
      </div>
    </section>

    <!-- ================== FEATURES SECTION ================== -->
    <!-- Phần giới thiệu tính năng -->
    <section id="features" class="py-24 bg-gray-50">
      <div class="max-w-7xl mx-auto px-6">
        
        <!-- Tiêu đề -->
        <div class="text-center mb-16">
          <h2 class="text-3xl font-bold mb-4">Tại sao chọn NanoURL?</h2>
          <div class="w-20 h-1 bg-indigo-600 mx-auto"></div>
        </div>
        
        <!-- Danh sách tính năng -->
        <div class="grid md:grid-cols-3 gap-12">
          <!-- Feature 1 -->
          <div class="bg-white p-8 rounded-2xl shadow-sm border
           border-gray-100 hover:-translate-y-2 hover:shadow-xl transition-all duration-300 group">
            <div class="w-12 h-12 bg-blue-100 text-blue-600 rounded-lg flex items-center justify-center mb-6
            group-hover:scale-110 group-hover:bg-blue-600 group-hover:text-white transition-all">
              <MousePointerClickIcon />
            </div>
            <h3 class="text-xl font-bold mb-3 group-hover:text-indigo-600 transition-colors">Theo dõi lượt Click</h3>
            <p class="text-gray-500 leading-relaxed">
              Biết chính xác có bao nhiêu người đã nhấn vào link của bạn theo thời gian thực.
            </p>
          </div>
          <!-- Feature 2-->
          <div class="bg-white p-8 rounded-2xl shadow-sm 
          border border-gray-100 hover:-translate-y-2 hover:shadow-xl transition-all duration-300 group">
            <div class="w-12 h-12 bg-blue-100 text-blue-600 rounded-lg flex items-center justify-center mb-6
            group-hover:scale-110 group-hover:bg-blue-600 group-hover:text-white transition-all">
              <GlobeIcon />
            </div>
            <h3 class="text-xl font-bold mb-3 group-hover:text-indigo-600 transition-colors">Vị trí địa lý</h3>
            <p class="text-gray-500 leading-relaxed">
              Phân tích người dùng đến từ quốc gia nào để tối ưu hóa chiến dịch marketing.
            </p>
          </div>
          <!-- Feature 3 -->
          <div class="bg-white p-8 rounded-2xl shadow-sm 
          border border-gray-100 hover:-translate-y-2 hover:shadow-xl transition-all duration-300 group">
            <div class="w-12 h-12 bg-purple-100 text-purple-600 rounded-lg flex items-center justify-center mb-6
            group-hover:scale-110 group-hover:bg-blue-600 group-hover:text-white transition-all">
              <MonitorIcon/>
            </div>
            <h3 class="text-xl font-bold mb-3 group-hover:text-indigo-600 transition-colors">Thiết bị truy cập</h3>
            <p class="text-gray-500 leading-relaxed">
              Phân loại dữ liệu dựa trên Desktop, Mobile hoặc Tablet của người dùng.
            </p>
          </div>
        </div>
      </div>
    </section>
  <!-- ================== FOOTER ================== -->
  <footer class="py-8 border-t border-gray-50">
    <div class="max-w-7xl mx-auto px-6 flex flex-col md:flex-row justify-between items-center text-gray-400 text-sm gap-4">
      <p>© 2026 NanoURL. Build with ❤️ and Vue 3.</p>
      
      <div class="flex items-center space-x-6">
        <a href="#" class="hover:text-indigo-600 transition">Github</a>
        <a href="#" class="hover:text-indigo-600 transition">Twitter</a>
        <span class="flex items-center text-green-500/80">
          <span class="w-1.5 h-1.5 bg-green-500 rounded-full mr-2"></span>
          System Online
        </span>
      </div>
    </div>
  </footer>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { 
  Link2Icon, CopyIcon, Loader2Icon, BarChart3Icon, 
  MousePointerClickIcon, GlobeIcon, MonitorIcon ,ShoppingCartIcon, MegaphoneIcon, StoreIcon, 
  Building2Icon, GraduationCapIcon, CameraIcon
} from 'lucide-vue-next'
import Swal from 'sweetalert2'
import { createShortUrl } from '../services/urlService'

const originalUrl = ref('')
const shortUrl = ref('')
const shortCode = ref('')
const isLoading = ref(false)
const isError = ref(false)

//Logic chính
 async function  handleShorten(){
//1. Kiểm tra định dạng URL đơn giản
  if(!originalUrl.value.startsWith('http')){
    isError.value = true
    setTimeout(() => isError.value = false, 500) // Rung trong 0.5s
    Swal.fire({
      icon:'error',
      title:'URL không hợp lệ!',
      text:'Vui lòng bắt đầu bằng http:// hoặc https://',
      confirmButtonColor: '#4f46e5'
    })
    return
  }
  isLoading.value=true
  try{
    const res = await createShortUrl(originalUrl.value)
    // Gán kết quả vào backend
    shortCode.value=res.data.shortCode
    //Gắn vào link để hiển thị link shortUrl
    shortUrl.value=`http://localhost:8080/${res.data.shortCode}`
    //Thông báo thành công kiểu Toast
    Swal.fire({
      toast:true,
      position:'top-end',
      icon:'success',
      title:'Đã tạo link rút gọn!',
      showConfirmButton:false,
      timer:2000
    })
  }catch(error){
    Swal.fire({
      icon:'error',
      title:'Thất bại',
      text:'Backend đang gặp lỗi'
    })

  }finally{
    isLoading.value=false
  }
}
//Hàm copy vào bộ nhớ
  function copyToClipboard(){
    navigator.clipboard.writeText(shortUrl.value)
    Swal.fire({
      toast:true,
      icon:'success',
      position:'bottom',
      title:'Đã copy vào bộ nhớ',
      showConfirmButton:false,
      timer:1500,

    })

  }
</script>
<style scoped>
/* Animation chạy ngang vô tận */
@keyframes marquee {
  0% { transform: translateX(0); }
  100% { transform: translateX(-50%); }
}

.animate-marquee {
  animation: marquee 30s linear infinite;
  display: flex;
  width: max-content;
}

/* Tạm dừng khi người dùng di chuột vào để họ kịp đọc */
.animate-marquee:hover {
  animation-play-state: paused;
}
.slide-up-enter-active, .slide-up-leave-active {
  transition: all 0.4s ease-out;
}
.slide-up-enter-from {
  opacity: 0;
  transform: translateY(30px);
}
/* Thêm vào phần <style scoped> */
.max-w-3xl:focus-within {
  box-shadow: 0 0 40px rgba(79, 70, 229, 0.15);
  transform: translateY(-2px);
  transition: all 0.3s ease;
}
.shake {
  animation: shake 0.5s cubic-bezier(.36,.07,.19,.97) both;
}
@keyframes shake {
  10%, 90% { transform: translate3d(-1px, 0, 0); }
  20%, 80% { transform: translate3d(2px, 0, 0); }
  30%, 50%, 70% { transform: translate3d(-4px, 0, 0); }
  40%, 60% { transform: translate3d(4px, 0, 0); }
}
</style>
