<template>
  <div class="dashboard">
    <section class="dashboard-hero">
      <div>
        <h2>运营概览</h2>
        <p>查看场地开放、订单支付、器材库存和用户规模。</p>
      </div>
      <div class="hero-actions">
        <el-button type="primary" @click="$router.push('/admin/venues')">管理场地</el-button>
        <el-button @click="$router.push('/admin/orders')">查看订单</el-button>
      </div>
    </section>

    <section class="stat-grid">
      <article v-for="(item, i) in stats" :key="item.label" class="stat-card">
        <div class="stat-top">
          <span class="stat-icon" :class="'icon-' + i">{{ item.icon }}</span>
          <span class="stat-meta">{{ item.meta }}</span>
        </div>
        <strong>{{ item.value }}</strong>
        <span>{{ item.label }}</span>
      </article>
    </section>

    <section class="dashboard-grid">
      <el-card class="panel trend-panel">
        <template #header>
          <div class="panel-header">
            <span>近 7 日订单</span>
            <small>按预约日期统计</small>
          </div>
        </template>
        <div class="trend-chart">
          <div v-for="d in weekDays" :key="d.date" class="trend-column">
            <div class="bar-wrap">
              <div class="bar" :style="{ height: d.height }"></div>
            </div>
            <strong>{{ d.count }}</strong>
            <span>{{ d.date }}</span>
          </div>
        </div>
      </el-card>

      <el-card class="panel">
        <template #header>
          <div class="panel-header">
            <span>系统状态</span>
            <small>当前运营健康度</small>
          </div>
        </template>
        <div class="sys-list">
          <div class="sys-item" v-for="s in sysItems" :key="s.label">
            <div class="sys-row">
              <span>{{ s.label }}</span>
              <strong>{{ s.value }}</strong>
            </div>
            <el-progress :percentage="s.percent" :color="s.color" :show-text="false" />
          </div>
        </div>
      </el-card>

      <el-card class="panel recent-panel">
        <template #header>
          <div class="panel-header">
            <span>近期订单</span>
            <small>最近创建的预约</small>
          </div>
        </template>
        <el-table :data="recentOrders" class="compact-table" height="286">
          <el-table-column prop="orderNo" label="订单号" min-width="180">
            <template #default="{ row }">
              <span class="mono">{{ row.orderNo }}</span>
            </template>
          </el-table-column>
          <el-table-column prop="bookDate" label="日期" width="120" />
          <el-table-column prop="totalAmount" label="金额" width="100">
            <template #default="{ row }">&yen;{{ Number(row.totalAmount).toFixed(2) }}</template>
          </el-table-column>
          <el-table-column prop="status" label="状态" width="100">
            <template #default="{ row }">
              <span :class="['status-badge', 'status-' + row.status]">
                {{ ['待支付', '已支付', '已取消'][row.status] }}
              </span>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { getAllVenues } from '@/api/venue'
import { getAllOrders } from '@/api/order'
import { getEquipments } from '@/api/equipment'
import { getUsers } from '@/api/user'

const venues = ref([])
const orders = ref([])
const equipments = ref([])
const users = ref([])
const orderTotal = ref(0)
const userTotal = ref(0)

onMounted(async () => {
  const [v, o, e, u] = await Promise.all([
    getAllVenues(),
    getAllOrders({ page: 1, pageSize: 100 }),
    getEquipments({ page: 1, pageSize: 100 }),
    getUsers({ page: 1, pageSize: 100 })
  ])
  venues.value = v.data || []
  orders.value = o.data.records || []
  orderTotal.value = o.data.total || orders.value.length
  equipments.value = e.data.records || []
  users.value = u.data.records || []
  userTotal.value = u.data.total || users.value.length
})

const activeVenueCount = computed(() => venues.value.filter(x => x.status === 1).length)
const paidOrderCount = computed(() => orders.value.filter(x => x.status === 1).length)
const availableEquipmentCount = computed(() => equipments.value.reduce((sum, x) => sum + (x.availableQty || 0), 0))
const totalEquipmentCount = computed(() => equipments.value.reduce((sum, x) => sum + (x.totalQty || 0), 0))

const stats = computed(() => [
  { label: '场地总数', value: venues.value.length, meta: `${activeVenueCount.value} 个开放`, icon: 'V' },
  { label: '订单总数', value: orderTotal.value, meta: `${paidOrderCount.value} 个已支付`, icon: 'O' },
  { label: '器材库存', value: totalEquipmentCount.value, meta: `${availableEquipmentCount.value} 件可借`, icon: 'E' },
  { label: '注册用户', value: userTotal.value, meta: `${users.value.filter(x => x.roleId === 1).length} 位管理员`, icon: 'U' }
])

const weekDays = computed(() => {
  const now = new Date()
  const result = []
  let maxCount = 1
  for (let i = 6; i >= 0; i--) {
    const d = new Date(now)
    d.setDate(d.getDate() - i)
    const ds = d.toISOString().slice(0, 10)
    const count = orders.value.filter(o => o.bookDate === ds).length
    maxCount = Math.max(maxCount, count)
    result.push({ date: `${d.getMonth() + 1}/${d.getDate()}`, count })
  }
  return result.map(r => ({
    ...r,
    height: `${Math.max((r.count / maxCount) * 100, 8)}%`
  }))
})

const sysItems = computed(() => {
  const venueRatio = venues.value.length ? activeVenueCount.value / venues.value.length * 100 : 0
  const paidRatio = orders.value.length ? paidOrderCount.value / orders.value.length * 100 : 0
  const rentalRatio = totalEquipmentCount.value ? availableEquipmentCount.value / totalEquipmentCount.value * 100 : 0
  return [
    { label: '开放场地', value: `${activeVenueCount.value}/${venues.value.length}`, percent: venueRatio, color: '#1a73e8' },
    { label: '支付完成率', value: `${Math.round(paidRatio)}%`, percent: paidRatio, color: '#16a085' },
    { label: '器材可借率', value: `${Math.round(rentalRatio)}%`, percent: rentalRatio, color: '#f59e0b' }
  ]
})

const recentOrders = computed(() => orders.value.slice(0, 8))
</script>

<style scoped>
.dashboard {
  display: flex;
  flex-direction: column;
  gap: 20px;
  animation: fadeIn 0.35s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.dashboard-hero {
  display: flex;
  align-items: center;
  justify-content: space-between;
  gap: 24px;
  min-height: 150px;
  padding: 28px 30px;
  border-radius: 14px;
  background:
    linear-gradient(135deg, rgba(26, 115, 232, 0.95), rgba(22, 160, 133, 0.9)),
    #1a73e8;
  color: #fff;
  box-shadow: 0 18px 38px rgba(26, 115, 232, 0.18);
}

.dashboard-hero h2 {
  margin: 0;
  font-size: 28px;
  font-weight: 850;
}

.dashboard-hero p {
  margin: 10px 0 0;
  color: rgba(255, 255, 255, 0.82);
  font-size: 14px;
}

.hero-actions {
  display: flex;
  gap: 10px;
  flex-shrink: 0;
}

.hero-actions :deep(.el-button) {
  border: none;
  font-weight: 700;
}

.hero-actions :deep(.el-button--primary) {
  background: #fff;
  color: #1a73e8;
}

.stat-grid {
  display: grid;
  grid-template-columns: repeat(4, minmax(0, 1fr));
  gap: 16px;
}

.stat-card {
  min-height: 138px;
  padding: 20px;
  border: 1px solid #e8eef6;
  border-radius: 12px;
  background: #fff;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.06);
}

.stat-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}

.stat-icon {
  width: 38px;
  height: 38px;
  display: grid;
  place-items: center;
  border-radius: 10px;
  color: #fff;
  font-weight: 850;
}

.icon-0 { background: #1a73e8; }
.icon-1 { background: #16a085; }
.icon-2 { background: #f59e0b; }
.icon-3 { background: #667085; }

.stat-meta {
  color: #7b8798;
  font-size: 12px;
  font-weight: 700;
}

.stat-card strong {
  display: block;
  color: #111827;
  font-size: 34px;
  font-weight: 850;
  line-height: 1;
}

.stat-card > span:last-child {
  display: block;
  margin-top: 8px;
  color: #667085;
  font-size: 14px;
  font-weight: 700;
}

.dashboard-grid {
  display: grid;
  grid-template-columns: minmax(0, 1.4fr) minmax(320px, 0.9fr);
  gap: 18px;
}

.panel {
  border: none;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.06);
}

.panel :deep(.el-card__header) {
  padding: 18px 20px;
  border-bottom: 1px solid #eef2f7;
}

.panel-header {
  display: flex;
  align-items: baseline;
  justify-content: space-between;
  gap: 16px;
}

.panel-header span {
  color: #111827;
  font-size: 16px;
  font-weight: 800;
}

.panel-header small {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 700;
}

.trend-panel {
  min-height: 360px;
}

.trend-chart {
  height: 278px;
  display: grid;
  grid-template-columns: repeat(7, minmax(0, 1fr));
  align-items: end;
  gap: 14px;
  padding: 18px 8px 2px;
}

.trend-column {
  display: grid;
  grid-template-rows: 1fr auto auto;
  justify-items: center;
  gap: 8px;
  min-width: 0;
  height: 100%;
}

.bar-wrap {
  width: 100%;
  height: 210px;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  border-radius: 10px;
  background: #f3f6fb;
}

.bar {
  width: 44%;
  min-height: 10px;
  border-radius: 9px 9px 3px 3px;
  background: linear-gradient(180deg, #1a73e8, #16a085);
}

.trend-column strong {
  color: #111827;
  font-size: 13px;
}

.trend-column span {
  color: #98a2b3;
  font-size: 12px;
  font-weight: 700;
}

.sys-list {
  display: flex;
  flex-direction: column;
  gap: 24px;
  padding: 8px 2px;
}

.sys-row {
  display: flex;
  justify-content: space-between;
  margin-bottom: 9px;
}

.sys-row span {
  color: #667085;
  font-size: 14px;
  font-weight: 700;
}

.sys-row strong {
  color: #111827;
  font-size: 14px;
}

.recent-panel {
  grid-column: 1 / -1;
}

.compact-table {
  width: 100%;
}

.mono {
  color: #475467;
  font-family: Consolas, Monaco, monospace;
  font-size: 12px;
}

.status-badge {
  display: inline-block;
  min-width: 56px;
  padding: 3px 10px;
  border-radius: 999px;
  text-align: center;
  font-size: 12px;
  font-weight: 800;
}

.status-0 { background: #fff7e6; color: #b76e00; }
.status-1 { background: #ecfdf3; color: #027a48; }
.status-2 { background: #fff1f0; color: #c4322b; }

@media (max-width: 1100px) {
  .stat-grid,
  .dashboard-grid {
    grid-template-columns: repeat(2, minmax(0, 1fr));
  }
}

@media (max-width: 760px) {
  .dashboard-hero {
    align-items: flex-start;
    flex-direction: column;
  }

  .stat-grid,
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
