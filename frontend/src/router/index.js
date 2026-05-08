import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/user/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('@/views/user/Register.vue')
  },
  {
    path: '/admin/login',
    name: 'AdminLogin',
    component: () => import('@/views/admin/AdminLogin.vue')
  },
  {
    path: '/',
    redirect: '/login'
  },
  {
    path: '/',
    component: () => import('@/views/user/UserLayout.vue'),
    meta: { requireAuth: true },
    children: [
      { path: 'venues', name: 'Venues', component: () => import('@/views/user/VenueList.vue') },
      { path: 'book/:id', name: 'Booking', component: () => import('@/views/user/Booking.vue') },
      { path: 'equipments', name: 'Equipments', component: () => import('@/views/user/EquipmentRent.vue') },
      { path: 'profile', name: 'Profile', component: () => import('@/views/user/Profile.vue') },
      { path: 'my-orders', name: 'MyOrders', component: () => import('@/views/user/MyOrders.vue') }
    ]
  },
  {
    path: '/admin',
    component: () => import('@/views/admin/AdminLayout.vue'),
    meta: { requireAuth: true, requireAdmin: true },
    children: [
      { path: '', redirect: '/admin/dashboard' },
      { path: 'dashboard', name: 'Dashboard', component: () => import('@/views/admin/Dashboard.vue') },
      { path: 'venues', name: 'AdminVenues', component: () => import('@/views/admin/VenueManage.vue') },
      { path: 'orders', name: 'AdminOrders', component: () => import('@/views/admin/OrderManage.vue') },
      { path: 'equipments', name: 'AdminEquipments', component: () => import('@/views/admin/EquipmentManage.vue') },
      { path: 'users', name: 'AdminUsers', component: () => import('@/views/admin/UserManage.vue') }
    ]
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token')
  const role = localStorage.getItem('role')
  if (to.meta.requireAuth && !token) {
    if (to.meta.requireAdmin) {
      next('/admin/login')
    } else {
      next('/login')
    }
    return
  }
  if (to.meta.requireAdmin && role !== 'admin') {
    next('/venues')
    return
  }
  next()
})

export default router
