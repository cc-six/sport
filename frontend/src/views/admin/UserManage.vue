<template>
  <el-card class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">
          <span class="title-icon">&#128100;</span>
          用户管理
        </span>
      </div>
    </template>
    <el-table :data="users" stripe class="styled-table" v-loading="loading">
      <el-table-column label="用户名" min-width="260">
        <template #default="{ row }">
          <div class="user-cell">
            <div class="user-avatar">{{ row.username.charAt(0) }}</div>
            <span class="user-name">{{ row.username }}</span>
          </div>
        </template>
      </el-table-column>
      <el-table-column prop="phone" label="手机号" min-width="180" />
      <el-table-column prop="roleId" label="角色" min-width="130">
        <template #default="{ row }">
          <el-tag size="small" :type="row.roleId === 1 ? 'danger' : ''" effect="plain">
            {{ row.roleId === 1 ? '管理员' : '用户' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="createTime" label="注册时间" min-width="220" />
    </el-table>
    <div v-if="users.length" class="table-footer">
      <span class="total-text">共 {{ users.length }} 位用户</span>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getUsers } from '@/api/user'

const users = ref([])
const loading = ref(false)

const fetchUsers = async () => {
  loading.value = true
  try {
    const res = await getUsers({ page: 1, pageSize: 50 })
    users.value = res.data.records
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}
onMounted(fetchUsers)
</script>

<style scoped>
.page-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.06);
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.title-icon {
  margin-right: 8px;
}

.styled-table {
  width: 100%;
  --el-table-header-bg-color: #f8fafc;
}

.styled-table :deep(.el-table__header th) {
  color: #667085;
  font-size: 13px;
  font-weight: 800;
}

.user-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}

.user-avatar {
  width: 36px;
  height: 36px;
  border-radius: 50%;
  background: linear-gradient(135deg, #1a73e8, #0288d1);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
  font-weight: 600;
  flex-shrink: 0;
}

.user-name {
  font-weight: 500;
}

.table-footer {
  padding: 12px 0;
  text-align: right;
}

.total-text {
  font-size: 13px;
  color: #86909c;
}
</style>
