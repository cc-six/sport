<template>
  <div class="user-layout">
    <header class="user-nav">
      <div class="nav-left">
        <button class="brand-button" type="button" @click="goHome">
          <span class="brand-mark">场</span>
          <span class="brand-text">
            <strong>体育馆预约</strong>
            <small>SportHall</small>
          </span>
        </button>
        <nav class="nav-tabs">
          <button
            v-for="item in navItems"
            :key="item.path"
            type="button"
            :class="['nav-tab', { active: activeMenu === item.path }]"
            @click="router.push(item.path)"
          >
            {{ item.label }}
          </button>
        </nav>
      </div>
      <div class="nav-right">
        <span class="nav-user">{{ username }}</span>
        <button class="logout-button" type="button" @click.stop.prevent="handleLogout">退出登录</button>
      </div>
    </header>
    <div class="user-content">
      <router-view />
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'

const route = useRoute()
const router = useRouter()
const username = localStorage.getItem('username') || '用户'
const activeMenu = computed(() => route.path)
const navItems = [
  { path: '/venues', label: '场地查询' },
  { path: '/equipments', label: '器材租借' },
  { path: '/my-orders', label: '我的订单' },
  { path: '/profile', label: '个人中心' }
]

const goHome = () => {
  router.push('/venues')
}

const handleLogout = () => {
  localStorage.clear()
  router.replace('/login')
}
</script>

<style scoped>
.user-layout {
  min-height: 100vh;
  background:
    radial-gradient(circle at top left, rgba(26, 115, 232, 0.08), transparent 32%),
    var(--content-bg);
}

.user-nav {
  display: flex;
  align-items: center;
  justify-content: space-between;
  position: sticky;
  top: 0;
  z-index: 20;
  min-height: 68px;
  padding: 0 28px;
  background: rgba(255, 255, 255, 0.94);
  border-bottom: 1px solid rgba(26, 115, 232, 0.12);
  box-shadow: 0 10px 28px rgba(17, 32, 61, 0.08);
  backdrop-filter: blur(14px);
}

.nav-left {
  display: flex;
  align-items: center;
  gap: 28px;
  min-width: 0;
  flex: 1;
}

.brand-button {
  display: inline-flex;
  align-items: center;
  gap: 10px;
  border: none;
  background: transparent;
  padding: 0;
  cursor: pointer;
  flex-shrink: 0;
}

.brand-mark {
  width: 38px;
  height: 38px;
  border-radius: 10px;
  display: grid;
  place-items: center;
  color: #fff;
  font-size: 15px;
  font-weight: 800;
  background: linear-gradient(135deg, #1a73e8, #16a085);
  box-shadow: 0 8px 18px rgba(26, 115, 232, 0.24);
}

.brand-text {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
  line-height: 1.05;
}

.brand-text strong {
  color: #15213a;
  font-size: 17px;
  font-weight: 800;
}

.brand-text small {
  color: #7b8798;
  font-size: 11px;
  font-weight: 600;
  margin-top: 4px;
}

.nav-tabs {
  display: flex;
  align-items: center;
  gap: 6px;
  min-width: 0;
}

.nav-tab {
  height: 38px;
  border: none;
  border-radius: 10px;
  padding: 0 16px;
  background: transparent;
  color: #536174;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.18s ease, color 0.18s ease, box-shadow 0.18s ease;
}

.nav-tab:hover {
  background: #eef5ff;
  color: #1a73e8;
}

.nav-tab.active {
  background: #1a73e8;
  color: #fff;
  box-shadow: 0 8px 18px rgba(26, 115, 232, 0.22);
}

.nav-right {
  display: flex;
  align-items: center;
  gap: 14px;
  position: relative;
  z-index: 2;
  flex-shrink: 0;
}

.nav-user {
  color: #334155;
  font-size: 14px;
  font-weight: 600;
  max-width: 120px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.logout-button {
  height: 36px;
  border: 1px solid #d7e1ef;
  border-radius: 10px;
  padding: 0 14px;
  background: #fff;
  color: #536174;
  font-size: 14px;
  font-weight: 600;
  cursor: pointer;
  transition: border-color 0.18s ease, color 0.18s ease, background 0.18s ease;
}

.logout-button:hover {
  background: #fff5f5;
  border-color: #ffcaca;
  color: #d93025;
}

.user-content {
  padding: 24px;
  max-width: 1200px;
  margin: 0 auto;
}

@media (max-width: 820px) {
  .user-nav {
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
    padding: 14px 18px;
  }

  .nav-left {
    width: 100%;
    align-items: flex-start;
    flex-direction: column;
    gap: 14px;
  }

  .nav-tabs {
    width: 100%;
    overflow-x: auto;
    padding-bottom: 2px;
  }

  .nav-tab {
    flex: 0 0 auto;
  }

  .nav-right {
    width: 100%;
    justify-content: space-between;
  }
}
</style>
