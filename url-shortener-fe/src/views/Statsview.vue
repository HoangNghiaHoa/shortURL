<template>
  <div class="flex min-h-screen bg-[#F4F7F6]">
    <Sidebar />
    <main class="flex-1 p-8 overflow-y-auto">
      
      <!-- HEADER: Hiển thị đường dẫn và tên mã rút gọn -->
      <div class="max-w-7xl mx-auto mb-8 flex justify-between items-end">
        <div>
          <nav class="flex items-center gap-2 text-xs text-gray-400 mb-1">
            <router-link to="/dashboard" class="hover:text-green-600 font-bold">Links</router-link>
            <span>/</span>
            <span class="text-gray-600 font-black tracking-tight">Analytics</span>
          </nav>
          <!-- Hiển thị shortCode từ URL hoặc tiêu đề mặc định -->
          <h1 class="text-3xl font-black text-gray-800 tracking-tight">{{ shortCodeDisplay }}</h1>
        </div>
        <!-- Trạng thái thời gian thực -->
        <div class="bg-white px-4 py-2 rounded-xl shadow-sm border border-gray-100 text-sm font-bold text-gray-500 flex items-center gap-2">
          <CalendarIcon :size="16" /> Real-time Analytics
        </div>
      </div>

      <!-- LOADING STATE: Hiển thị khi đang chờ API phản hồi -->
      <div v-if="loading" class="py-20 flex justify-center">
        <Loader2Icon class="animate-spin text-green-500 w-10 h-10"/>
      </div>

      <!-- MAIN CONTENT: Chỉ hiển thị khi đã có dữ liệu (stats) -->
      <div v-else-if="stats" class="max-w-7xl mx-auto space-y-6 pb-10">
        
        <!-- ROW 1: QUICK STATS (Các thẻ số liệu nhanh) -->
        <div class="grid grid-cols-1 md:grid-cols-4 gap-4">
          <!-- Tổng lượt click -->
          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-50">
            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Total Clicks</p>
            <p class="text-3xl font-black text-gray-800">{{ processedStats.totalClicks || 0 }}</p>
          </div>
          <!-- Lượt khách truy cập duy nhất (Unique) -->
          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-50">
            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Unique Visitors</p>
            <p class="text-3xl font-black text-green-600">{{ processedStats.uniqueVisitors || 0 }}</p>
          </div>
          <!-- Thiết bị truy cập nhiều nhất -->
          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-50">
            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Top Device</p>
            <p class="text-xl font-black text-gray-800 truncate">{{ processedStats.byDevice?.[0]?.name || 'N/A' }}</p>
          </div>
          <!-- Nguồn truy cập hàng đầu (QR hoặc Direct) -->
          <div class="bg-white p-5 rounded-3xl shadow-sm border border-gray-50">
            <p class="text-[10px] font-black text-gray-400 uppercase tracking-widest">Top Source</p>
            <p class="text-xl font-black text-blue-600 uppercase">{{ processedStats.bySource?.[0]?.name || 'Direct' }}</p>
          </div>
        </div>

        <!-- ROW 2: CHART & DONUT (Biểu đồ đường và biểu đồ tròn) -->
        <div class="grid grid-cols-1 lg:grid-cols-3 gap-6">
          <!-- Biểu đồ xu hướng lượt click theo giờ -->
          <div class="lg:col-span-2 bg-white p-6 rounded-[32px] shadow-sm border border-gray-50">
            <h3 class="font-black text-gray-400 uppercase text-[10px] tracking-widest mb-6">Clicks Trend (Last 24h)</h3>
            <div class="h-[280px]"><Line :data="trendChartData" :options="lineOptions" /></div>
          </div>

          <!-- Biểu đồ tỷ lệ trình duyệt (Doughnut) -->
          <div class="bg-white p-6 rounded-[32px] shadow-sm border border-gray-50 flex flex-col items-center">
            <h3 class="font-black text-gray-400 uppercase text-[10px] tracking-widest mb-4">Browsers</h3>
            <div class="h-[220px] w-full relative">
              <Doughnut :data="browserChartData" :options="doughnutOptions" />
              <!-- Số liệu tổng giữa vòng tròn -->
              <div class="absolute inset-0 flex flex-col items-center justify-center pointer-events-none mt-2">
                <span class="text-4xl font-black text-gray-800">{{ processedStats.totalClicks }}</span>
                <span class="text-[10px] font-bold text-gray-400">CLICKS</span>
              </div>
            </div>
            <!-- Danh sách chi tiết tỷ lệ % của từng trình duyệt -->
            <div class="w-full mt-4 space-y-2">
              <div v-for="browser in processedStats.byBrowser" :key="browser.name" class="flex items-center gap-4">
                <!-- Thêm Logo/Icon trình duyệt -->
                <div class="w-10 h-10 rounded-xl bg-gray-50 flex items-center justify-center p-2 border border-gray-100">
                  <img :src="getBrowserIcon(browser.name)" :alt="browser.name" class="w-6 h-6 object-contain" />
                </div>
                
                <div class="flex-1">
                  <div class="flex justify-between text-sm font-bold mb-1">
                    <span class="text-gray-700">{{ browser.name }}</span>
                    <!-- Tính % dựa trên tổng các browser hiện có -->
                    <span class="text-blue-600">{{ calculatePercentage(browser.count, processedStats.byBrowser) }}%</span>
                  </div>
                  <div class="w-full bg-gray-100 h-2 rounded-full overflow-hidden">
                    <div class="bg-blue-500 h-full rounded-full transition-all duration-1000" 
                        :style="{ width: calculatePercentage(browser.count, processedStats.byBrowser) + '%' }"></div>
                  </div>
                </div>
              </div>
            </div>
          </div>
        </div>

    <!-- ROW 3: MAP & COUNTRIES (Đã fix để danh sách nằm trên cùng) -->
    <div class="bg-white p-8 rounded-[32px] shadow-sm border border-gray-50">
      <h3 class="text-xl font-black text-gray-800 mb-8">Audience Geography</h3>
      
      <!-- Thay items-center bằng items-start để danh sách nhảy lên trên -->
      <div class="grid grid-cols-1 lg:grid-cols-12 gap-12 items-start">
        
        <!-- Cột quốc gia: Giờ đây sẽ bắt đầu từ trên cùng -->
        <div class="lg:col-span-5 space-y-6 mt-4"> 
          <div v-for="country in [...(processedStats.byCountry || [])].sort((a, b) => b.count - a.count).slice(0, 6)" 
              :key="country.name" 
              class="group">
            <div class="flex justify-between text-sm font-bold mb-2">
              <span class="flex items-center gap-3">
                <span class="text-2xl drop-shadow-sm">{{ getFlagEmoji(country.name) }}</span>
                <span class="text-gray-700 font-black">{{ formatCountryName(country.name) }}</span>
              </span>
              <span class="text-green-600 font-black">
                {{ country.count }} <span class="text-[10px] text-gray-400">CLICKS</span>
              </span>
            </div>
            <div class="w-full bg-gray-100 h-2.5 rounded-full overflow-hidden">
              <div class="bg-gradient-to-r from-green-400 to-green-600 h-full rounded-full transition-all duration-1000" 
                  :style="{ width: (country.count / processedStats.totalClicks * 100) + '%' }"></div>
            </div>
          </div>
        </div>

        <!-- Bản đồ thế giới (Giữ nguyên vị trí) -->
        <div class="lg:col-span-7">
          <div class="w-full bg-slate-50 rounded-[40px] p-8 border border-gray-100 shadow-inner">
            <div id="map" style="width: 100%; height: 420px;"></div>
          </div>
        </div>
      </div>
    </div>

        <!-- ROW 4: RECENT ACTIVITY LOGS (Nhật ký truy cập chi tiết) -->
        <div class="bg-white p-8 rounded-[32px] shadow-sm border border-gray-50">
          <h3 class="text-lg font-black text-gray-800 mb-6">Recent Activity Logs</h3>
          <div class="overflow-x-auto">
            <table class="w-full text-left">
              <thead>
                <tr class="text-[10px] font-black text-gray-400 uppercase tracking-widest border-b border-gray-100">
                  <th class="pb-4">Timestamp</th>
                  <th class="pb-4">IP Address</th>
                  <th class="pb-4">Location</th>
                  <th class="pb-4">Device/Browser</th>
                  <th class="pb-4">Source</th>
                </tr>
              </thead>
              <tbody class="divide-y divide-gray-50">
                <!-- Duyệt qua mảng recentClicks từ Backend trả về -->
                <tr v-for="click in processedStats.recentClicks" :key="click.id" class="hover:bg-gray-50 transition-colors">
                  <td class="py-4 text-xs font-bold text-gray-500">{{ formatTime(click.timestamp) }}</td>
                  <td class="py-4 font-mono text-xs font-black text-green-600">{{ click.ipAddress }}</td>
                  <td class="py-4 text-sm font-medium">{{ click.city || 'Local' }}, {{ click.country }}</td>
                  <td class="py-4">
                    <div class="flex items-center gap-2">
                      <span class="text-[10px] font-black bg-gray-100 px-2 py-0.5 rounded text-gray-600">{{ click.operatingSystem || click.deviceType }}</span>
                      <span class="text-xs text-gray-400">{{ click.browser }}</span>
                    </div>
                  </td>
                  <td class="py-4">
                    <span :class="click.accessSource === 'QR_CODE' ? 'text-purple-600 bg-purple-50' : 'text-blue-600 bg-blue-50'" 
                          class="text-[9px] font-black px-2 py-0.5 rounded uppercase">
                      {{ click.accessSource || 'DIRECT' }}
                    </span>
                  </td>
                </tr>
                <!-- Hiển thị khi không có dữ liệu -->
                <tr v-if="!processedStats.recentClicks || processedStats.recentClicks.length === 0">
                  <td colspan="5" class="py-10 text-center text-gray-400 italic">No activity recorded yet.</td>
                </tr>
              </tbody>
            </table>
          </div>
        </div>
      </div>
    </main>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch, nextTick } from 'vue'
import Sidebar from '../components/Sidebar.vue'
import { useRoute } from 'vue-router'
import api from '@/services/api'
import { CalendarIcon, Loader2Icon } from 'lucide-vue-next'
import { Doughnut, Line } from 'vue-chartjs'
import jsVectorMap from 'jsvectormap'
import 'jsvectormap/dist/maps/world.js'
import 'jsvectormap/dist/jsvectormap.css'

// Đăng ký các thành phần cần thiết cho Chart.js
import { Chart as ChartJS, Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale, PointElement, LineElement, Filler } from 'chart.js'
ChartJS.register(Title, Tooltip, Legend, ArcElement, CategoryScale, LinearScale, PointElement, LineElement, Filler)

const route = useRoute()
const stats = ref(null) 
const loading = ref(true) 
const shortCodeDisplay = ref('') 
let mapInstance = null 

const countryCodeMap = { 
  'Vietnam': 'VN', 'VN': 'VN',
  'United States': 'US', 'US': 'US',
  'Germany': 'DE', 'DE': 'DE',
  'Japan': 'JP', 'JP': 'JP',
  'France': 'FR', 'Korea': 'KR' 
}

/**
 * 1. LOGIC GỘP DỮ LIỆU (AGGREGATION)
 * Giúp gộp VN (150 click) và Vietnam (2 click) thành một dòng duy nhất
 */
const processedStats = computed(() => {
  if (!stats.value) return null;

  const raw = stats.value;
  const isOverview = !route.params.shortCode;

  // Gộp dữ liệu Quốc gia
  const countryMap = {};
  raw.byCountry?.forEach(c => {
    const name = formatCountryName(c.name);
    countryMap[name] = (countryMap[name] || 0) + parseInt(c.count);
  });
  const aggregatedCountry = Object.keys(countryMap).map(name => ({
    name,
    count: countryMap[name]
  })).sort((a, b) => b.count - a.count);

  // Trả về object đã chuẩn hóa
  return {
    ...raw,
    byCountry: aggregatedCountry,
    // Nếu là Overview, ưu tiên hiển thị thông tin tổng quát
    displayTitle: isOverview ? 'Global Overview' : `/${route.params.shortCode}`,
    topDeviceName: raw.byDevice?.[0]?.name || 'N/A'
  };
});

const initMap = () => {
  const mapContainer = document.getElementById('map')
  if (!mapContainer || !processedStats.value) return
  
  if (mapInstance) { mapInstance.destroy(); mapContainer.innerHTML = ''; }

  const markers = {}
  processedStats.value.byCountry?.forEach(c => {
    const code = countryCodeMap[c.name] || c.name 
    markers[code] = c.count
  })

  mapInstance = new jsVectorMap({
    selector: '#map',
    map: 'world',
    zoomOnScroll: false,
    regionStyle: {
      initial: { fill: '#F1F5F9', stroke: '#CBD5E1', strokeWidth: 1 },
      hover: { fill: '#059669' }
    },
    series: {
      regions: [{
        attribute: 'fill',
        scale: ['#DCFCE7', '#10B981', '#064E3B'], 
        values: markers
      }]
    }
  })
}

const fetchStats = async () => {
  const code = route.params.shortCode
  const userId = localStorage.getItem('userId') || 1
  try {
    loading.value = true
    // Logic gọi API: Nếu không có code, gọi API tổng quan của User
    const url = code ? `/api/stats/${code}` : `/api/stats/user/${userId}`
    const res = await api.get(url)
    stats.value = res.data
    shortCodeDisplay.value = code ? `/${code}` : 'Global Overview'
    
    await nextTick()
    setTimeout(initMap, 200) 
  } catch (e) {
    stats.value = null
  } finally {
    loading.value = false
  }
}

onMounted(fetchStats)
watch(() => route.params.shortCode, fetchStats)

const getFlagEmoji = (name) => {
  const n = name.toUpperCase();
  if (n === 'VIETNAM' || n === 'VN') return '🇻🇳';
  if (n === 'UNITED STATES' || n === 'US') return '🇺🇸';
  if (n === 'JAPAN' || n === 'JP') return '🇯🇵';
  if (n === 'GERMANY' || n === 'DE') return '🇩🇪';
  return '🏳️';
};

const trendChartData = computed(() => {
  const hourly = stats.value?.hourlyTrend || {}
  const sortedKeys = Object.keys(hourly).sort() 
  return {
    labels: sortedKeys.map(k => k.split('-').pop() + ':00'),
    datasets: [{
      label: 'Clicks',
      data: sortedKeys.map(k => hourly[k]),
      borderColor: route.params.shortCode ? '#10B981' : '#3B82F6', // Đổi màu xanh dương nếu là Overview
      backgroundColor: route.params.shortCode ? 'rgba(16, 185, 129, 0.1)' : 'rgba(59, 130, 246, 0.1)',
      fill: true,
      tension: 0.4
    }]
  }
})

const browserChartData = computed(() => ({
  labels: stats.value?.byBrowser?.map(i => i.name) || [],
  datasets: [{
    data: stats.value?.byBrowser?.map(i => i.count) || [],
    backgroundColor: ['#10B981', '#3B82F6', '#F59E0B', '#8B5CF6', '#EF4444'], 
    borderWidth: 0, 
    borderRadius: 10
  }]
}))

const lineOptions = { responsive: true, maintainAspectRatio: false, plugins: { legend: { display: false } } }
const doughnutOptions = { responsive: true, maintainAspectRatio: false, cutout: '85%', plugins: { legend: { display: false } } }

const formatTime = (ts) => {
  if (!ts) return 'N/A';
  return new Date(ts).toLocaleString('vi-VN', { hour: '2-digit', minute: '2-digit', day: '2-digit', month: '2-digit' });
}

const getBrowserIcon = (browserName) => {
  const name = browserName.toLowerCase();
  if (name.includes('chrome')) return 'https://cdnjs.cloudflare.com/ajax/libs/browser-logos/74.1.0/chrome/chrome.svg';
  if (name.includes('safari')) return 'https://cdnjs.cloudflare.com/ajax/libs/browser-logos/74.1.0/safari/safari.svg';
  return 'https://cdn-icons-png.flaticon.com/512/657/657502.png';
};

const calculatePercentage = (count, items) => {
  if (!items) return 0;
  const total = items.reduce((sum, item) => sum + parseInt(item.count), 0);
  return total > 0 ? Math.round((count / total) * 100) : 0;
};

const formatCountryName = (name) => {
  const mapping = { 'VN': 'Vietnam', 'US': 'United States', 'JP': 'Japan', 'DE': 'Germany' };
  const upper = name ? name.toUpperCase() : '';
  return mapping[upper] || name;
};
</script>

<style scoped>
/* Xóa nền mặc định của thư viện bản đồ để khớp với UI */
:deep(.jvm-container) { background-color: transparent !important; }
/* Chỉnh sửa tooltip của bản đồ cho đẹp hơn */
:deep(.jvm-tooltip) {
  background: #1f2937 !important;
  border-radius: 8px !important;
  font-weight: bold !important;
  padding: 4px 10px !important;
}
</style>