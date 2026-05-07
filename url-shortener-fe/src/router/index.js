import { createRouter, createWebHistory } from 'vue-router'

import Homeview from '../views/Homeview.vue'
import DashboardView from '../views/DashboardView.vue'
import AuthView from '../views/AuthView.vue'
import CustomAliasView from '../views/CustomAliasView.vue'
import QRCodeGeneratorView from '../views/QRCodeGeneratorView.vue' // Import vào đây
const routes =[
  {path:'/', name:'home', component:Homeview},
  {
    path: '/qr-generator', // Khai báo đường dẫn này
    name: 'QRCodeGenerator',
    component: QRCodeGeneratorView,
    meta: { requiresAuth: true }
  },
 {
  path: '/stats/:shortCode', // Chi tiết từng link
  name: 'LinkStats',
  component: () => import('../views/Statsview.vue')
  },
  {
  path: '/statistics', // Tổng quan
  name: 'GeneralStats',
  component: () => import('../views/Statsview.vue')
  },
  {
  path: '/dashboard',
  name: 'dashboard',
  component: DashboardView,
  meta: { requiresAuth: true }
  },
   { path: '/auth', component: AuthView },
{
    path: '/custom-aliases',
    name: 'CustomAliases',
    component: CustomAliasView, // Dùng trực tiếp thay vì dùng arrow function import
    meta: { requiresAuth: true 
      ,isAliasPage: true
    } 
  },
  {
    path: '/:shortCode', // Luôn để cái này ở cuối cùng để tránh tranh chấp với các route /auth, /dashboard
    name: 'redirect',
    component: () => import('../views/RedirectView.vue')
  }

]
const router = createRouter({
  history:  createWebHistory(),
  routes
})
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')

  if (to.meta.requiresAuth && !token) {
    next('/auth')
  } else {
    next()
  }
})
export default router