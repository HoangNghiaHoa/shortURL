<script setup>
import { 
  LayoutDashboardIcon, BarChart3Icon, GlobeIcon, 
  SettingsIcon, PlusCircleIcon, ZapIcon,HashIcon,QrCodeIcon
} from 'lucide-vue-next'
</script>

<template>
  <aside class="w-64 bg-white border-r border-gray-200 flex flex-col h-screen sticky top-0">
    <div class="p-6">
      <div class="flex items-center gap-2 mb-8 px-2">
        <div class="bg-green-600 p-1.5 rounded-lg text-white">
          <ZapIcon :size="20" fill="currentColor" />
        </div>
        <span class="text-xl font-black tracking-tight text-gray-800">NanoURL</span>
      </div>

      <div class="flex items-center gap-3 mb-8 p-2 bg-gray-50 rounded-xl border border-gray-100">
        <div class="bg-green-100 text-green-700 h-9 w-9 rounded-full flex items-center justify-center font-bold text-sm">H</div>
        <div class="overflow-hidden">
          <p class="text-xs font-bold truncate text-gray-800">Hòa Hoàng Ng...</p>
          <p class="text-[10px] text-green-600 font-semibold uppercase">Free Plan</p>
        </div>
      </div>

      <nav class="space-y-1">
        <p class="text-[10px] font-bold text-gray-400 uppercase px-3 mb-2 tracking-widest">Dashboard</p>
        
        <router-link to="/dashboard" 
          class="flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:bg-gray-50 text-gray-600"
          active-class="bg-green-50 text-green-700 font-bold">
          <LayoutDashboardIcon :size="18" /> Branded Links
        </router-link>
        
        <!-- MỤC MỚI: CUSTOM ALIASES -->
        <router-link to="/custom-aliases" 
          class="flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:bg-gray-50 text-gray-600"
          active-class="bg-green-50 text-green-700 font-bold">
          <HashIcon :size="18" /> Custom Aliases
        </router-link>

        <router-link to="/statistics" 
          class="flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:bg-gray-50 text-gray-600"
          active-class="bg-green-50 text-green-700 font-bold">
          <BarChart3Icon :size="18" /> Statistics
        </router-link>
        
        <!-- Trong phần nav -->
        <router-link to="/qr-generator" 
          class="flex items-center gap-3 px-3 py-2 text-sm font-medium rounded-lg transition-colors hover:bg-gray-50 text-gray-600"
          active-class="bg-green-50 text-green-700 font-bold">
          <QrCodeIcon :size="18" /> QR Generator
        </router-link>

        <a href="#" class="flex items-center gap-3 px-3 py-2 text-sm font-medium text-gray-600 hover:bg-gray-50 rounded-lg transition-colors">
          <GlobeIcon :size="18" /> Domain Settings
        </a>
      </nav>
    </div>

    <div class="mt-auto p-6 border-t border-gray-100">
      <button class="flex items-center gap-2 text-sm font-semibold text-gray-500 hover:text-green-600 transition-colors">
        <PlusCircleIcon :size="18" /> Add Domain
      </button>
    </div>
  </aside>
</template>