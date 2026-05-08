<template>
  <el-card class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">
          <span class="title-icon">&#128203;</span>
          订单管理
        </span>
        <el-select v-model="status" placeholder="状态筛选" clearable @change="fetchOrders" class="filter-select">
          <el-option label="待支付" :value="0" />
          <el-option label="已支付" :value="1" />
          <el-option label="已取消" :value="2" />
        </el-select>
      </div>
    </template>
    <el-table :data="orders" stripe class="styled-table">
      <el-table-column prop="orderNo" label="订单号" min-width="230">
        <template #default="{ row }">
          <span class="order-no">{{ row.orderNo }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="bookDate" label="日期" min-width="130" />
      <el-table-column label="时段" min-width="190">
        <template #default="{ row }">
          <span class="time-range">
            <span class="time-dot"></span>
            {{ row.startTime }} — {{ row.endTime }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="totalAmount" label="金额" min-width="120">
        <template #default="{ row }">
          <span class="amount">&yen;{{ row.totalAmount.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="120">
        <template #default="{ row }">
          <span :class="['status-badge', 'status-' + row.status]">
            {{ ['待支付', '已支付', '已取消'][row.status] }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="userId" label="用户ID" min-width="110" />
      <el-table-column prop="venueId" label="场地ID" min-width="110" />
      <el-table-column prop="createTime" label="创建时间" min-width="180" />
    </el-table>
    <div v-if="orders.length" class="table-footer">
      <span class="total-text">共 {{ orders.length }} 条记录</span>
    </div>
  </el-card>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getAllOrders } from '@/api/order'

const orders = ref([])
const status = ref(null)

const fetchOrders = async () => {
  const res = await getAllOrders({ page: 1, pageSize: 50, status: status.value })
  orders.value = res.data.records
}
onMounted(fetchOrders)
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

.filter-select {
  width: 150px;
}

.styled-table {
  width: 100%;
  --el-table-header-bg-color: #fafbfc;
}

.styled-table :deep(.el-table__header th) {
  color: #667085;
  font-size: 13px;
  font-weight: 800;
}

.order-no {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 13px;
  color: #4a4a4a;
}

.time-range {
  display: flex;
  align-items: center;
  gap: 6px;
}

.time-dot {
  width: 6px;
  height: 6px;
  border-radius: 50%;
  background: #409EFF;
}

.amount {
  font-weight: 700;
  color: #e65454;
}

.status-badge {
  display: inline-block;
  padding: 2px 12px;
  border-radius: 12px;
  font-size: 12px;
  font-weight: 500;
}

.status-0 { background: #fff7e6; color: #d48806; }
.status-1 { background: #f6ffed; color: #389e0d; }
.status-2 { background: #fff1f0; color: #cf1322; }

.table-footer {
  padding: 12px 0;
  text-align: right;
}

.total-text {
  font-size: 13px;
  color: #86909c;
}
</style>
