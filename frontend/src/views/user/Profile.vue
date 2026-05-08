<template>
  <el-card class="profile-card">
    <div class="profile-header">
      <div class="avatar">{{ username.charAt(0) }}</div>
      <div class="profile-info">
        <h2>{{ username }}</h2>
        <el-tag :type="role === 'admin' ? 'danger' : ''" effect="plain">
          {{ role === 'admin' ? '管理员' : '普通用户' }}
        </el-tag>
      </div>
    </div>
    <div class="profile-stats">
      <div class="stat-item" @click="$router.push('/my-orders')">
        <div class="stat-num">{{ orderCount }}</div>
        <div class="stat-label">我的订单</div>
      </div>
      <div class="stat-item">
        <div class="stat-num">-</div>
        <div class="stat-label">器材租借</div>
      </div>
      <div class="stat-item">
        <div class="stat-num">-</div>
        <div class="stat-label">场馆收藏</div>
      </div>
    </div>
    <div class="profile-actions">
      <el-button type="primary" @click="$router.push('/my-orders')">查看我的订单</el-button>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getMyOrders } from '@/api/order'

const username = localStorage.getItem('username') || '用户'
const role = localStorage.getItem('role')
const orderCount = ref(0)

onMounted(async () => {
  try {
    const res = await getMyOrders({ page: 1, pageSize: 1 })
    orderCount.value = res.data.total || 0
  } catch (e) {
    // Non-critical
  }
})
</script>

<style scoped>
.profile-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  max-width: 600px;
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.profile-header {
  display: flex;
  align-items: center;
  gap: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #f0f0f0;
}

.avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8, #0288d1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 28px;
  font-weight: 600;
  flex-shrink: 0;
}

.profile-info h2 {
  margin: 0 0 8px 0;
  font-size: 20px;
  color: #1d1d1f;
}

.profile-stats {
  display: flex;
  gap: 0;
  padding: 20px 0;
  border-bottom: 1px solid #f0f0f0;
}

.stat-item {
  flex: 1;
  text-align: center;
  cursor: pointer;
}

.stat-num {
  font-size: 24px;
  font-weight: 700;
  color: #409EFF;
}

.stat-label {
  font-size: 13px;
  color: #86909c;
  margin-top: 4px;
}

.profile-actions {
  padding-top: 20px;
  text-align: center;
}

.profile-actions :deep(.el-button) {
  width: 200px;
  height: 42px;
  font-size: 15px;
  border-radius: 8px;
}
</style>
