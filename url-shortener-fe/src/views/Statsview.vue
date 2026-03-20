<template>
  <div class="min-h-screen bg-gray-50 p-4 md:p-8">
    <div class="max-w-6xl mx-auto">

      <div class="flex flex-col md:flex-row md:items-center justify-between mb-8 gap-4">
        <div >
          <button @click="$router.push('/')" class="text-indigo-600 hover:text-indigo-800 items-center font-medium mb-2 transition group">
            <ArrowLeftIcon class="w-4 h-4 mr-1 group-hover:-translate-x-1 transition-transform"/> Quay lại trang chủ
          </button>
          <h1 class="text-3xl font-bold text-gray-900 tracking-tight"> Thống kê mã: <span class="text-indigo-600
             text-indigo-700 px-3 py-1 rounded-lg ml-2">{{ shortCode }}</span> 
          </h1>
        </div> 
        <div class="bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 flex items-center">
          <span class="text-sm text-gray-500 mr-3">Trạng thái:</span>
            <span class=" flex items-center text-green-600 font-bold text-sm">
              <span class="relative flex h-2 w-2 mr-2">
                <span class="animate-ping absolute inline-flex h-full w-full rounded-full bg-green-400 opacity-75"></span>
                <span class="relative inline-flex rounded-full h-2 w-2 bg-green-500"></span>
              </span>
              Đang hoạt động
            </span> 
        </div>
      </div>

      <div v-if="loading" class="flex flex-col items-center justify-center h-64">
        <Loader2Icon class="animate-spin text-indigo-600 w-12 h-12" />
        <p class="mt-4 text-gray-500 font-medium "> Đang truy xuất dữ liệu.....</p>
      </div>
      
      <div v-else-if="stats" class="space-y-8 animate-in fade-in duration-700">
        <div class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
          <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
            <div class="p-2 bg-blue-50 w-fit rounded-lg mb-4 "><MousePointerClickIcon class="text-blue-600"/></div>
            <p class="text-gray-500 text-sm font-medium">Tổng lượt click</p>
            <p class="text-3xl font-bold text-gray-900">{{ stats.totalClicks }}</p>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <div class="p-2 bg-purple-50 w-fit rounded-lg mb-4"><UsersIcon  class="text-purple-600"/></div>
          <p class="text-gray-500 text-sm font-medium">Người dùng duy nhất</p>
          <p class="text-3xl font-bold text-gray-900">{{ stats.uniqueVisitors }}</p>
        </div>
      </div>

      <div class="grid grid-cols-1 lg:grid-cols-2 gap-8">
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 class="font-bold text-gray-800 mb-6 flex items-center">
            <MonitorIcon class="w-5 h-5 mr-2 text-indigo-500"/> Trình duyệt truy cập
          </h3>
          <div class="h-[300px] relative">
            <Doughnut v-if="hasData(stats.byBrowser)" :data="browserChartData" :options="doughnutOptions" />
            <div v-else class="flex items-center justify-center h-full text-gray-400">Chưa có dữ liệu</div>
          </div>
        </div>
        <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
          <h3 class="font-bold text-gray-800 mb-6 flex items-center">
            <GlobeIcon class="w-5 h-5 mr-2  text-indigo-500"/> Quốc gia hàng đầu
          </h3>
          <div class="h-[300px] relative">
            <Bar v-if="hasData(stats.byCountry)" :data="countryChartData" :options="barOptions" />
            <div v-else class="flex items-center justify-center h-full text-gray-400">Chưa có dữ liệu</div>
          </div>
        </div>

      </div>
      <div class="bg-white p-6 rounded-2xl shadow-sm border border-gray-100">
        <h3 class="font-bold text-gray-800 mb-4"> Phân tích thiết bị</h3>
        <div class="grid grid-cols-1 sm:grid-cols-2 md:grid-cols-3 gap-4">
          <div v-for="d in stats.byDevice" :key="d.name" class="flex items-center p-4 bg-gray-50 rounded-xl">
            <div class="w-2 h-10 bg-indigo-500 rounded-full mr-4"></div>
            <div> 
              <p class="text-sm font-bold text-gray-800">{{ d.name }}</p>
              <p class="text-xs text-gray-500"> {{ d.count }} lượt truy cập</p>
            </div>
          </div>
        </div>
      </div>

    </div>
    <div v-else class="text-center py-20 bg-white rounded-3xl shadow-sm border border-gray-100"> 
      <div class="text-gray-300 mb-4 flex justify-center"><SearchXIcon :size="64" /></div>
      <h2 class="text-2xl font-bold text-gray-800">Không có dữ liệu cho mã này</h2>
      <p class="text-gray-500 mt-2">Có thể chưa có ai nhấn vào liên kết của bạn.</p>
      <button @click="$router.push('/')" class="mt-6 bg-indigo-600 text-white px-6 py-2 rounded-lg">Quay lại</button>
    </div>
  </div>
  </div>
</template>

<script setup>
  import { ref, onMounted, computed } from 'vue'
  import { useRoute } from 'vue-router'
  import { 
    ArrowLeftIcon, Loader2Icon, MousePointerClickIcon, 
    UsersIcon, GlobeIcon, MonitorIcon, SearchXIcon 
  } from 'lucide-vue-next'
  import { getStats } from '../services/urlService'

// Chart.js imports
  import { Doughnut, Bar } from 'vue-chartjs'
  import { 
    Chart as ChartJS, Title, Tooltip, Legend, ArcElement, 
    CategoryScale, LinearScale, BarElement 
  } from 'chart.js'


ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale, BarElement)

const route = useRoute()
const shortCode = route.params.shortCode
const loading = ref(true)
const stats = ref(null)
//MockData để hiển thị dữ liệu 
const mockStats = {
  totalClicks: 1248,
  uniqueVisitors: 856,
  byBrowser: [
    { name: 'Chrome', count: 650 },
    { name: 'Safari', count: 320 },
    { name: 'Firefox', count: 120 },
    { name: 'Edge', count: 158 }
  ],
  byCountry: [
    { name: 'Vietnam', count: 500 },
    { name: 'United States', count: 300 },
    { name: 'Japan', count: 150 },
    { name: 'Singapore', count: 120 },
    { name: 'Germany', count: 80 },
    { name: 'France', count: 98 }
  ],
  byDevice: [
    { name: 'Mobile', count: 850 },
    { name: 'Desktop', count: 350 },
    { name: 'Tablet', count: 48 }
  ]
}
//Thuộc tính cho biểu đồ Trình duyệt
const doughnutOptions ={
  responsive:true,
  maintainAspectRatio:false,
  cutout:'65%',
  plugins:{
    legend:{
      position:'right',
      labels:{
        boxWidth:12,
        padding:16,
        usePointStyle: true
      }
    }
  }
  
}
//Thuộc tính cho biểu đồ COuntry
const barOptions = {
    responsive: true,
    maintainAspectRatio: false,
    plugins: {
      legend: { display: false },
      tooltip: {
        backgroundColor: 'rgba(0, 0, 0, 0.8)',
        padding: 12,
        cornerRadius: 8
      }
  },
  // Hiệu ứng khi di chuột vào cột
  hover: {
    mode: 'index',
    intersec: false
  },
  elements: {
    bar: {
      backgroundColor: '#6366F1', // Màu Indigo chủ đạo
      hoverBackgroundColor: '#4F46E5', // Màu đậm hơn khi hover
      borderWidth: 0,
    }
  },
  scales :{
    y:{
      beginAtZero:true,
      ticks:{precision:0},
      grid:{color:'#F3F4F6'}
    },
    x:{
      grid:{display:false}
    },
  },
  plugins:{
    legend: { display: false }
  }

}

//Hepler kiểm tra dữ liệu
const hasData =(arr) =>arr && arr.length >0
//Computed Dữ liệu cho biểu đồ Browers
const browserChartData = computed(() => ({
  labels: stats.value?.byBrowser.map(i => i.name) || [],
  datasets: [{
    data: stats.value?.byBrowser.map(i => i.count) || [],
    backgroundColor: [
      '#6366F1', //indigo
      '#22C55E',//green
      '#F97316',//orange
      '#EF4444',//red
      '#0EA5E9',//sky(dự phòng)
    ],
    borderWidth:2,
    borderColor:'#fff',
    hoverOffset:10
  }]

}))
//Computed Dữ liệu cho biểu dồ Country
const countryChartData =computed(()=>({
  labels: stats.value?.byCountry.map(i => i.name) || [],
  datasets: [{
    label: 'Lượt click',
    backgroundColor: '#6366F1',
    borderRadius: 6, // Bo góc nhẹ cho hiện đại
    barThickness: 32, // Độ rộng cột cố định để nhìn không bị thô
    maxBarThickness: 40,
    data: stats.value?.byCountry.map(i => i.count) || [],
  }]
}))
// Chạy và hiển thị api stats
onMounted(async () => {
  // BƯỚC 1: Kiểm tra xem có phải là DEMO không trước khi làm bất cứ việc gì khác
  if (shortCode === 'demo') {
    loading.value = true;
    
    // Giả lập trễ 800ms để người dùng thấy cái xoay xoay (cho thật)
    setTimeout(() => {
      stats.value = mockStats; // Gán dữ liệu giả của em vào đây
      loading.value = false;
    }, 800);
    
    return; // QUAN TRỌNG: Kết thúc hàm ở đây, không cho chạy xuống code gọi API bên dưới
  }

  // BƯỚC 2: Chỉ khi KHÔNG PHẢI demo thì mới gọi API thật
  try {
    loading.value = true;
    const res = await getStats(shortCode);
    stats.value = res.data;
  } catch (e) {
    console.error("Lỗi kết nối Backend:", e);
    stats.value = null;
  } finally {
    loading.value = false;
  }
});
</script>

<style scoped>

</style>