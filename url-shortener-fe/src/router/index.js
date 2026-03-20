import { createRouter, createWebHistory } from 'vue-router'

import Homeview from '../views/Homeview.vue'
import Statsview from '../views/Statsview.vue'


const routes =[
  {path:'/', name:'home', component:Homeview},
  {path:'/stats/:shortCode',name:'stats',component:Statsview}

]
const router = createRouter({
  history:  createWebHistory(),
  routes
})
export default router