<template>
  <div class="equipment-page">
    <el-card class="page-card">
      <template #header>
        <div class="page-header">
          <span class="page-title">器材租借</span>
          <el-button text type="primary" @click="fetchData">刷新</el-button>
        </div>
      </template>

      <el-row :gutter="16" v-loading="loading">
        <el-col :xs="24" :sm="12" :md="8" v-for="item in equipments" :key="item.id">
          <el-card class="equipment-card" shadow="hover">
            <div class="equipment-name">{{ item.name }}</div>
            <div class="equipment-meta">
              <span>可借 {{ item.availableQty }}/{{ item.totalQty }}</span>
              <span class="price">&yen;{{ Number(item.pricePerHour).toFixed(2) }}/小时</span>
            </div>
            <el-button
              type="primary"
              class="rent-btn"
              :disabled="item.availableQty <= 0"
              @click="openRent(item)"
            >
              {{ item.availableQty > 0 ? '租借' : '暂无库存' }}
            </el-button>
          </el-card>
        </el-col>
      </el-row>
    </el-card>

    <el-card class="page-card rentals-card">
      <template #header>
        <div class="page-header">
          <span class="page-title">我的租借</span>
          <el-select v-model="rentalStatus" clearable placeholder="全部状态" style="width: 140px" @change="fetchRentals">
            <el-option label="租借中" :value="0" />
            <el-option label="已归还" :value="1" />
          </el-select>
        </div>
      </template>
      <el-table :data="rentals" stripe>
        <el-table-column prop="id" label="记录ID" width="90" />
        <el-table-column prop="equipmentId" label="器材ID" width="100" />
        <el-table-column prop="quantity" label="数量" width="90" />
        <el-table-column prop="createTime" label="租借时间" />
        <el-table-column prop="returnTime" label="归还时间" />
        <el-table-column prop="status" label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 0 ? 'warning' : 'success'">
              {{ row.status === 0 ? '租借中' : '已归还' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <el-dialog v-model="dialogVisible" title="租借器材" width="420px">
      <el-form label-width="80px">
        <el-form-item label="器材">
          <span>{{ currentEquipment?.name }}</span>
        </el-form-item>
        <el-form-item label="数量">
          <el-input-number v-model="quantity" :min="1" :max="currentEquipment?.availableQty || 1" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleRent">确认租借</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getEquipments, getMyRentals, rentEquipment } from '@/api/equipment'
import { ElMessage } from 'element-plus'

const equipments = ref([])
const rentals = ref([])
const loading = ref(false)
const submitting = ref(false)
const dialogVisible = ref(false)
const currentEquipment = ref(null)
const quantity = ref(1)
const rentalStatus = ref(null)

const fetchEquipments = async () => {
  const res = await getEquipments({ page: 1, pageSize: 50 })
  equipments.value = res.data.records
}

const fetchRentals = async () => {
  const res = await getMyRentals({ page: 1, pageSize: 50, status: rentalStatus.value })
  rentals.value = res.data.records
}

const fetchData = async () => {
  loading.value = true
  try {
    await Promise.all([fetchEquipments(), fetchRentals()])
  } finally {
    loading.value = false
  }
}

const openRent = (item) => {
  currentEquipment.value = item
  quantity.value = 1
  dialogVisible.value = true
}

const handleRent = async () => {
  submitting.value = true
  try {
    await rentEquipment({ equipmentId: currentEquipment.value.id, quantity: quantity.value })
    ElMessage.success('租借成功')
    dialogVisible.value = false
    fetchData()
  } finally {
    submitting.value = false
  }
}

onMounted(fetchData)
</script>

<style scoped>
.equipment-page {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.page-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.rentals-card {
  margin-top: 20px;
}

.page-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.page-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.equipment-card {
  margin-bottom: 16px;
  border: 1px solid #eef0f3;
  border-radius: 8px;
}

.equipment-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
}

.equipment-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 12px;
  font-size: 13px;
  color: #86909c;
}

.price {
  color: #e65454;
  font-weight: 700;
}

.rent-btn {
  width: 100%;
  margin-top: 16px;
}
</style>
