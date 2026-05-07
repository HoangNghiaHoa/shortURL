<template>
  <div class="flex min-h-screen bg-[#F4F7F5] font-sans">
    <Sidebar />

    <main class="flex-1 p-8 max-w-6xl mx-auto w-full">
      <div class="mb-10">
        <h1 class="text-3xl font-black text-gray-900 tracking-tighter flex items-center gap-3">
          Trình tạo mã QR Marketing
          <span class="text-[11px] font-black bg-green-100 text-green-600 px-2 py-1 rounded-md uppercase">Pro Custom</span>
        </h1>
        <p class="text-gray-400 text-sm font-medium mt-1">Tùy chỉnh màu sắc, hình dạng và phong cách cho mã QR của bạn.</p>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-3 gap-8">
        <!-- Cột trái: Cấu hình (Chiếm 2 cột trên màn hình lớn) -->
        <div class="lg:col-span-2 space-y-6">
          <div class="bg-white rounded-[32px] p-8 shadow-sm border border-gray-100">
            <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4">1. Chọn Link rút gọn</label>
            <select 
              v-model="selectedCode" 
              @change="updateQR"
              class="w-full bg-gray-50 border border-gray-200 rounded-2xl px-4 py-3.5 text-sm font-bold text-gray-700 outline-none focus:ring-2 focus:ring-green-500 transition-all appearance-none cursor-pointer"
            >
              <option value="" disabled>-- Chọn một link từ danh sách --</option>
              <option v-for="url in userUrls" :key="url.id" :value="url.shortCode">
                {{ url.shortCode }} ({{ url.originalUrl.substring(0, 30) }}...)
              </option>
            </select>

            <div class="grid grid-cols-1 md:grid-cols-2 gap-6 mt-8">
              <!-- Chỉnh Màu sắc -->
              <div>
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-3">2. Màu sắc</label>
                <div class="flex items-center gap-4 bg-gray-50 p-3 rounded-2xl border border-gray-200">
                  <input type="color" v-model="qrConfig.color" @input="updateQR" class="w-10 h-10 rounded-lg cursor-pointer" />
                  <span class="text-sm font-bold text-gray-600">{{ qrConfig.color }}</span>
                </div>
              </div>
              <!-- Chèn vào dưới phần chọn màu -->
              <div class="mt-6">
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-3">6. Chèn Logo thương hiệu</label>
                <div class="flex items-center gap-4">
                  <input type="file" @change="onLogoChange" accept="image/*" class="hidden" id="logo-upload" />
                  <label for="logo-upload" class="flex-1 cursor-pointer bg-gray-50 border-2 border-dashed border-gray-200 rounded-2xl p-4 text-center hover:border-green-500 transition-colors">
                    <span class="text-sm font-bold text-gray-500">Tải ảnh Logo (.png, .jpg)</span>
                  </label>
                  <button v-if="qrConfig.logo" @click="qrConfig.logo = null; updateQR()" class="p-2 text-red-500 hover:bg-red-50 rounded-full">
                    <XIcon size="20" />
                  </button>
                </div>
              </div>

              <div class="mt-8">
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-4">7. Chọn mẫu nhanh</label>
                <div class="grid grid-cols-2 md:grid-cols-4 gap-3">
                  <button 
                    v-for="tpl in templates" 
                    :key="tpl.name"
                    @click="applyTemplate(tpl)"
                    class="p-3 border rounded-2xl text-[10px] font-black uppercase transition-all hover:shadow-md"
                    :style="{ borderColor: tpl.color, color: tpl.color }"
                  >
                    {{ tpl.name }}
                  </button>
                </div>
              </div>

              <!-- Chỉnh Kích thước -->
              <div>
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-3">3. Kích thước ({{ qrConfig.size }}px)</label>
                <input type="range" min="200" max="500" step="10" v-model="qrConfig.size" @input="updateQR" class="w-full h-2 bg-gray-200 rounded-lg appearance-none cursor-pointer accent-green-500" />
              </div>

              <!-- Chỉnh Hình dạng mắt (Dots) -->
              <div>
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-3">4. Hình dạng điểm ảnh</label>
                <select v-model="qrConfig.dotsType" @change="updateQR" class="w-full bg-gray-50 border border-gray-200 rounded-2xl px-4 py-3 text-sm font-bold outline-none">
                  <option value="square">Mặc định (Vuông)</option>
                  <option value="dots">Chấm tròn</option>
                  <option value="rounded">Bo góc</option>
                  <option value="extra-rounded">Siêu bo góc</option>
                  <option value="classy">Sang trọng</option>
                </select>
              </div>

              <!-- Chỉnh Hình dạng góc (Corners) -->
              <div>
                <label class="block text-xs font-black text-gray-400 uppercase tracking-widest mb-3">5. Kiểu khung góc</label>
                <select v-model="qrConfig.cornersType" @change="updateQR" class="w-full bg-gray-50 border border-gray-200 rounded-2xl px-4 py-3 text-sm font-bold outline-none">
                  <option value="square">Vuông</option>
                  <option value="dot">Tròn</option>
                </select>
              </div>
            </div>
          </div>
        </div>

        <!-- Cột phải: Xem trước (Preview) -->
        <div class="lg:col-span-1">
          <div class="bg-white rounded-[32px] p-8 shadow-sm border border-gray-100 sticky top-8 flex flex-col items-center">
            <h2 class="text-xs font-black text-gray-400 uppercase tracking-widest mb-6">Bản xem trước</h2>
            
            <div class="p-4 bg-white rounded-3xl shadow-2xl border border-gray-50 overflow-hidden" ref="qrRef">
              <!-- Thư viện sẽ vẽ mã QR vào đây -->
            </div>

            <div v-if="selectedCode" class="w-full mt-8 space-y-3">
               <div class="text-center mb-4">
                  <p class="text-sm font-black text-gray-900">nanourl.io/{{ selectedCode }}</p>
               </div>
               <button 
                 @click="downloadQR"
                 class="w-full bg-gray-900 hover:bg-black text-white font-black py-4 rounded-2xl transition-all shadow-lg active:scale-95 flex items-center justify-center gap-2"
               >
                 TẢI MÃ QR (.PNG)
               </button>
            </div>
            
            <div v-else class="mt-10 text-center text-gray-400 font-bold text-sm">
                Vui lòng chọn link để bắt đầu tùy chỉnh
            </div>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, reactive, watch } from 'vue'; // Thêm watch
import QRCodeStyling from 'qr-code-styling'; // Thư viện mới
import Sidebar from '../components/Sidebar.vue';
import api from '@/services/api';

const selectedCode = ref('');
const userUrls = ref([]);
const qrRef = ref(null);
const templates = [
  { name: 'Classic', color: '#000000', dots: 'square', corners: 'square' },
  { name: 'Modern Blue', color: '#0ea5e9', dots: 'dots', corners: 'dot' },
  { name: 'Nature Green', color: '#10b981', dots: 'extra-rounded', corners: 'dot' },
  { name: 'Elegant Purple', color: '#8b5cf6', dots: 'classy', corners: 'square' }
];

const applyTemplate = (tpl) => {
  qrConfig.color = tpl.color;
  qrConfig.dotsType = tpl.dots;
  qrConfig.cornersType = tpl.corners;
  updateQR();
};
// Cấu hình mã QR
const qrConfig = reactive({
  size: 300,
  color: '#111827',
  dotsType: 'square',
  cornersType: 'square',
  logo: null // Thêm biến lưu logo
});

const onLogoChange = (event) => {
  const file = event.target.files[0];
  if (file) {
    const reader = new FileReader();
    reader.onload = (e) => {
      qrConfig.logo = e.target.result; // Lưu chuỗi Base64 của ảnh
      updateQR();
    };
    reader.readAsDataURL(file);
  }
};
const saveQRConfig = async () => {
  if (!selectedCode.value) return;
  
  try {
    // Biến Object qrConfig thành chuỗi JSON để lưu vào cột TEXT
    const configData = JSON.stringify(qrConfig);
    
    await api.put(`/api/urls/${selectedCode.value}/qr-config`, {
      qrConfig: configData
    });
    
    console.log("Đã lưu cấu hình QR vào hệ thống.");
  } catch (e) {
    console.error("Lỗi khi lưu cấu hình QR:", e);
  }
};

// Khởi tạo đối tượng QR
const qrCode = new QRCodeStyling({
  width: qrConfig.size,
  height: qrConfig.size,
  data: "",
  dotsOptions: { color: qrConfig.color, type: qrConfig.dotsType },
  backgroundOptions: { color: "#ffffff" },
  cornersSquareOptions: { type: qrConfig.cornersType },
  imageOptions: { crossOrigin: "anonymous", margin: 5 }
});

const fetchUrls = async () => {
  try {
    const res = await api.get('/api/urls/user');
    userUrls.value = res.data;
  } catch (e) {
    console.error("Lỗi:", e);
  }
};

const fullUrl = computed(() => {
  return selectedCode.value ? `${window.location.origin}/${selectedCode.value}` : "";
});
// Tự động nạp cấu hình khi người dùng chọn mã rút gọn khác nhau
watch(selectedCode, (newCode) => {
  if (newCode) {
    // Tìm object URL tương ứng trong danh sách userUrls đã fetch từ BE
    const currentUrl = userUrls.value.find(u => u.shortCode === newCode);
    
    // Nếu URL này đã có cấu hình QR được lưu từ trước
    if (currentUrl && currentUrl.qrConfig) {
      try {
        const savedConfig = JSON.parse(currentUrl.qrConfig);
        
        // Ghi đè các giá trị hiện tại bằng cấu hình đã lưu
        qrConfig.color = savedConfig.color || '#111827';
        qrConfig.size = savedConfig.size || 300;
        qrConfig.dotsType = savedConfig.dotsType || 'square';
        qrConfig.cornersType = savedConfig.cornersType || 'square';
        qrConfig.logo = savedConfig.logo || null;
        
        // Cập nhật lại hình ảnh QR
        updateQR();
      } catch (e) {
        console.error("Lỗi khi nạp cấu hình QR cũ:", e);
      }
    } else {
      // Nếu chưa có cấu hình (link mới), reset về mặc định để tránh lấy logo của link cũ
      qrConfig.logo = null;
      qrConfig.color = '#111827';
      updateQR();
    }
  }
});

// Hàm cập nhật QR khi có thay đổi
const updateQR = () => {
  if (!selectedCode.value || !qrCode) return;
  
  qrCode.update({
    width: qrConfig.size,
    height: qrConfig.size,
    data: fullUrl.value,
    dotsOptions: { color: qrConfig.color, type: qrConfig.dotsType },
    cornersSquareOptions: { type: qrConfig.cornersType },
    image: qrConfig.logo,
    imageOptions: {
      hideBackgroundDots: true,
      imageSize: 0.4,
      margin: 5
    }
  });
};
onMounted(async () => {
  await fetchUrls();
  if (qrRef.value) {
    qrCode.append(qrRef.value);
  }
});

const downloadQR = async () => {
  // 1. Tải file về máy người dùng
  qrCode.download({ name: `QR-${selectedCode.value}`, extension: "png" });

  // 2. Tự động lưu thiết kế vào Database
  await saveQRConfig();
  
  // 3. (Tùy chọn) Cập nhật lại danh sách local để nếu họ chọn lại link này sẽ thấy QR mới ngay
  const index = userUrls.value.findIndex(u => u.shortCode === selectedCode.value);
  if (index !== -1) {
    userUrls.value[index].qrConfig = JSON.stringify(qrConfig);
  }
};
</script>

<style scoped>
/* Tùy chỉnh thanh kéo cho đẹp */
input[type='range']::-webkit-slider-thumb {
  -webkit-appearance: none;
  height: 20px;
  width: 20px;
  border-radius: 50%;
  background: #10b981;
  cursor: pointer;
  box-shadow: 0 0 10px rgba(16, 185, 129, 0.3);
}
</style>