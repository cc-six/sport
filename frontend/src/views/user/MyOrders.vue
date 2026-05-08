<template>
  <el-card class="orders-card">
    <template #header>
      <div class="orders-header">
        <span>我的订单</span>
        <el-button text type="primary" :loading="loading" @click="fetchOrders">刷新</el-button>
      </div>
    </template>
    <el-empty v-if="!loading && orders.length === 0" description="暂无订单" />
    <el-table
      :data="orders"
      stripe
      v-loading="loading"
      element-loading-text="加载中..."
      :row-class-name="rowClassName"
    >
      <el-table-column prop="orderNo" label="订单号" />
      <el-table-column prop="bookDate" label="日期" />
      <el-table-column label="时段">
        <template #default="{ row }">{{ formatTime(row) }}</template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额">
        <template #default="{ row }">¥{{ Number(row.totalAmount).toFixed(2) }}</template>
      </el-table-column>
      <el-table-column prop="status" label="状态">
        <template #default="{ row }">
          <el-tag :type="row.status === 1 ? 'success' : row.status === 0 ? 'warning' : 'danger'">
            {{ ['待支付', '已支付', '已取消'][row.status] }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="primary" @click="handlePay(row)">支付</el-button>
          <el-button v-if="row.status === 0" size="small" type="danger" @click="handleCancel(row)">取消</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { getMyOrders, payOrder, cancelOrder } from '@/api/order'
import { ElMessage, ElMessageBox } from 'element-plus'

const route = useRoute()
const orders = ref([])
const loading = ref(false)

const formatTime = (row) => `${formatClock(row.startTime)} - ${formatClock(row.endTime)}`
const formatClock = (value) => value ? String(value).substring(0, 5) : ''
const rowClassName = ({ row }) => {
  return String(row.id) === String(route.query.newOrderId || '') ? 'new-order-row' : ''
}

const fetchOrders = async () => {
  loading.value = true
  try {
    const res = await getMyOrders({ page: 1, pageSize: 20 })
    orders.value = res.data.records
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}

const handlePay = async (row) => {
  try {
    await ElMessageBox.confirm('确认支付该订单？', '支付确认', {
      confirmButtonText: '确认支付',
      cancelButtonText: '取消'
    })
    await payOrder(row.id)
    ElMessage.success('支付成功')
    fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      // Error already shown by interceptor
    }
  }
}

const handleCancel = async (row) => {
  try {
    await ElMessageBox.confirm('确认取消该订单？', '取消确认', {
      confirmButtonText: '确认取消',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await cancelOrder(row.id)
    ElMessage.success('订单已取消')
    fetchOrders()
  } catch (e) {
    if (e !== 'cancel') {
      // Error already shown by interceptor
    }
  }
}

onMounted(fetchOrders)

watch(() => route.query.t, () => {
  fetchOrders()
})
</script>

<style scoped>
.orders-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.orders-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 18px;
  font-weight: 600;
}

:deep(.new-order-row) {
  --el-table-tr-bg-color: #eef7ff;
}
</style>
