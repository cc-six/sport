<template>
  <el-container class="admin-container">
    <el-aside class="admin-aside" width="236px">
      <div class="admin-brand">
        <div class="brand-mark">SH</div>
        <div>
          <h3>管理后台</h3>
<!--          <p>SportHall Admin</p>-->
        </div>
      </div>
      <el-menu router class="admin-menu" :default-active="$route.path">
        <el-menu-item index="/admin/dashboard">仪表盘</el-menu-item>
        <el-menu-item index="/admin/venues">场地管理</el-menu-item>
        <el-menu-item index="/admin/orders">订单管理</el-menu-item>
        <el-menu-item index="/admin/equipments">器材管理</el-menu-item>
        <el-menu-item index="/admin/users">用户管理</el-menu-item>
      </el-menu>
    </el-aside>
    <el-container>
      <el-header class="admin-header">
        <div class="header-title">
          <span class="page-kicker">体育馆管理系统</span>
          <strong>{{ currentTitle }}</strong>
        </div>
        <div class="header-actions">
          <span class="admin-user">{{ username }}</span>
          <button class="logout-btn" type="button" @click="handleLogout">退出</button>
        </div>
      </el-header>
      <el-main class="admin-main">
        <router-view />
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username') || '管理员'
const titleMap = {
  '/admin/dashboard': '运营仪表盘',
  '/admin/venues': '场地管理',
  '/admin/orders': '订单管理',
  '/admin/equipments': '器材管理',
  '/admin/users': '用户管理'
}
const currentTitle = computed(() => titleMap[route.path] || '管理后台')
const handleLogout = () => {
  localStorage.clear()
  router.replace('/admin/login')
}
</script>

<style scoped>
.admin-container {
  min-height: 100vh;
  background: #f5f7fb;
}

.admin-aside {
  background: #101828;
  box-shadow: 8px 0 28px rgba(16, 24, 40, 0.16);
}

.admin-brand {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 24px 20px 20px;
}

.brand-mark {
  width: 42px;
  height: 42px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: #fff;
  font-size: 13px;
  font-weight: 800;
  background: linear-gradient(135deg, #1a73e8, #16a085);
  box-shadow: 0 12px 24px rgba(26, 115, 232, 0.26);
}

.admin-brand h3 {
  margin: 0;
  color: #fff;
  font-size: 18px;
  font-weight: 800;
}

.admin-brand p {
  margin: 5px 0 0;
  color: rgba(255, 255, 255, 0.5);
  font-size: 12px;
  font-weight: 600;
}

.admin-menu {
  background: transparent;
  border-right: none;
}

.admin-menu :deep(.el-menu-item) {
  color: rgba(255, 255, 255, 0.66);
  height: 44px;
  line-height: 44px;
  margin: 6px 14px;
  border-radius: 10px;
  font-size: 14px;
  font-weight: 600;
  transition: all 0.2s;
}

.admin-menu :deep(.el-menu-item:hover),
.admin-menu :deep(.el-menu-item.is-active) {
  background: rgba(255, 255, 255, 0.12);
  color: #fff;
}

.admin-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  height: 72px;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.92);
  border-bottom: 1px solid #e7ecf3;
  box-shadow: 0 8px 24px rgba(16, 24, 40, 0.04);
  backdrop-filter: blur(12px);
}

.header-title {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.page-kicker {
  color: #8a95a6;
  font-size: 12px;
  font-weight: 700;
}

.header-title strong {
  color: #111827;
  font-size: 20px;
  font-weight: 800;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}

.admin-user {
  color: #344054;
  font-size: 14px;
  font-weight: 700;
}

.logout-btn {
  height: 34px;
  padding: 0 14px;
  border: 1px solid #d9e1ec;
  border-radius: 9px;
  background: #fff;
  color: #667085;
  font-size: 13px;
  font-weight: 700;
  cursor: pointer;
}

.logout-btn:hover {
  border-color: #fecaca;
  background: #fff5f5;
  color: #f56c6c;
}

.admin-main {
  min-width: 0;
  background: #f5f7fb;
  padding: 24px 28px 32px;
}
</style>
