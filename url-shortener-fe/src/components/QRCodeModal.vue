<template>
  <div v-if="isOpen" class="modal-overlay" @click.self="$emit('close')">
    <div class="modal-content">
      <h3>Mã QR cho: {{ shortCode }}</h3>
      
      <div class="qr-container" ref="qrParent">
        <!-- Component vẽ mã QR -->
        <qrcode-vue
          :value="fullUrl"
          :size="200"
          level="H"
          render-as="canvas"
          id="qr-canvas"
        />
      </div>

      <div class="modal-footer">
        <button @click="downloadQR" class="btn-download">Tải về máy</button>
        <button @click="$emit('close')" class="btn-close">Đóng</button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue';
import QrcodeVue from 'qrcode.vue';

const props = defineProps({
  isOpen: Boolean,
  shortCode: String
});

const emit = defineEmits(['close']);

// Tạo link kèm tham số src=qr để Backend của em bắt được
const fullUrl = computed(() => {
  return `${window.location.origin}/${props.shortCode}?src=qr`;
});

const downloadQR = () => {
  const canvas = document.getElementById('qr-canvas');
  if (canvas) {
    const url = canvas.toDataURL('image/png');
    const link = document.createElement('a');
    link.href = url;
    link.download = `qr-${props.shortCode}.png`;
    link.click();
  }
};
</script>

<style scoped>
.modal-overlay {
  position: fixed;
  top: 0; left: 0; width: 100%; height: 100%;
  background: rgba(0, 0, 0, 0.7);
  display: flex; justify-content: center; align-items: center;
  z-index: 9999;
}
.modal-content {
  background: white; padding: 2rem; border-radius: 12px; text-align: center;
}
.qr-container {
  background: white; padding: 15px; display: inline-block; margin: 15px 0;
}
.btn-download { background: #42b983; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; margin-right: 8px; }
.btn-close { background: #ff4d4f; color: white; border: none; padding: 8px 16px; border-radius: 4px; cursor: pointer; }
</style>