<template>
  <div class="bg-white rounded-2xl border border-gray-100 p-6 shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 group relative overflow-hidden">
    <!-- Header Card -->
    <div class="flex justify-between items-start mb-5">
      <div 
        @click="downloadQR"
        class="p-3 bg-green-50 rounded-xl text-green-600 cursor-pointer hover:bg-green-600 hover:text-white transition-colors tooltip"
        title="Tải mã QR"
      >
        <QrCodeIcon :size="22" />
      </div>
      <div class="flex flex-col items-end">
        <span class="text-[10px] font-black text-slate-300 uppercase tracking-[0.15em]">Clicks</span>
        <span class="text-2xl font-black text-slate-900 leading-none">{{ url.clickCount || 0 }}</span>
      </div>
    </div>

    <!-- Content -->
    <div class="relative z-10">
      <h3 class="text-xl font-black text-slate-800 truncate mb-1 flex items-center gap-1">
        <span class="text-green-500">/</span>{{ url.shortCode }}
      </h3>
      <p class="text-xs text-slate-400 font-medium truncate mb-6 w-full italic">
        {{ url.originalUrl }}
      </p>
    </div>

    <!-- Action Buttons -->
    <div class="flex gap-2 relative z-10">
      <button 
        @click="$emit('copy', url.shortCode)" 
        class="flex-[2] py-2.5 bg-slate-900 text-white rounded-xl text-xs font-bold hover:bg-green-600 transition-all flex items-center justify-center gap-2 active:scale-95"
      >
        <CopyIcon :size="14" /> Sao chép
      </button>
      <button 
        @click="$emit('edit', url)" 
        class="flex-1 py-2.5 bg-slate-100 text-slate-600 rounded-xl hover:bg-blue-500 hover:text-white transition-all flex items-center justify-center active:scale-95"
      >
        <Edit3Icon :size="16" />
      </button>
    </div>
    
    <!-- QR Code Background Decor -->
    <div class="absolute -right-6 -bottom-6 opacity-[0.03] group-hover:opacity-10 transition-opacity duration-500 rotate-12">
      <qrcode-vue 
        ref="qrRef"
        :value="'http://localhost:8080/' + url.shortCode" 
        :size="120" 
        level="H" 
        render-as="canvas" 
      />
    </div>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import QrcodeVue from 'qrcode.vue'
import { QrCodeIcon, CopyIcon, Edit3Icon } from 'lucide-vue-next'

const props = defineProps(['url'])
defineEmits(['copy', 'edit'])

const qrRef = ref(null)

const downloadQR = () => {
  try {
    // 1. Lấy ra element thực tế
    // Nếu qrRef.value là Component, ta lấy $el. Nếu không thì lấy chính nó.
    const element = qrRef.value?.$el || qrRef.value;

    if (!element) {
      console.error("Không tìm thấy QR element");
      return;
    }

    // 2. Tìm canvas
    let canvas = null;
    
    // Kiểm tra xem chính nó có phải là canvas không
    if (element.tagName === 'CANVAS') {
      canvas = element;
    } else if (element.querySelector) {
      // Nếu nó là một cái hộp (div) và có hàm querySelector thì tìm bên trong
      canvas = element.querySelector('canvas');
    }

    // 3. Xử lý tải file
    if (canvas) {
      const dataUrl = canvas.toDataURL("image/png");
      const link = document.createElement('a');
      link.download = `qr-${props.url.shortCode}.png`;
      link.href = dataUrl;
      link.click();
    } else {
      // Trường hợp cuối cùng: nếu vẫn không thấy, thử tìm qua DOM (cách thô sơ nhưng hiệu quả)
      const fallbackCanvas = document.querySelector(`.group qrcode-vue canvas`) || 
                             document.querySelector(`canvas`);
      if (fallbackCanvas) {
         // Thực hiện tải như trên...
         const dataUrl = fallbackCanvas.toDataURL("image/png");
         const link = document.createElement('a');
         link.download = `qr-fixed-${props.url.shortCode}.png`;
         link.href = dataUrl;
         link.click();
      }
    }
  } catch (error) {
    console.error("Lỗi khi tải QR:", error);
  }
};
</script>