<template>
  <el-card class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">
          <span class="title-icon">&#127958;</span>
          场地管理
        </span>
        <el-button type="primary" @click="dialogVisible = true; editingId = null">
          <span style="margin-right: 4px;">&#43;</span> 新增场地
        </el-button>
      </div>
    </template>
    <el-table :data="venues" stripe class="styled-table" v-loading="tableLoading">
      <el-table-column prop="name" label="场地名称" min-width="220">
        <template #default="{ row }">
          <span class="venue-name">
            <span class="venue-dot" :style="{ background: row.status === 1 ? '#52c41a' : '#ff4d4f' }"></span>
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="type" label="类型" min-width="120">
        <template #default="{ row }">
          <el-tag size="small" :type="typeColor(row.type)" effect="plain">{{ typeLabel(row.type) }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column label="开放时间" min-width="190">
        <template #default="{ row }">
          <span class="time-range">{{ row.openTime }} — {{ row.closeTime }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="pricePerHour" label="价格/小时" min-width="140">
        <template #default="{ row }">
          <span class="price">&yen;{{ row.pricePerHour.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column prop="status" label="状态" min-width="110">
        <template #default="{ row }">
          <span :class="['status-badge', row.status === 1 ? 'status-on' : 'status-off']">
            {{ row.status === 1 ? '上架' : '下架' }}
          </span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="190" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="editVenue(row)">编辑</el-button>
          <el-button size="small" :type="row.status === 1 ? 'danger' : 'success'" plain
                     @click="toggleStatus(row)">
            {{ row.status === 1 ? '下架' : '上架' }}
          </el-button>
        </template>
      </el-table-column>
    </el-table>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑场地' : '新增场地'" @close="resetForm" width="500px">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="请输入场地名称" /></el-form-item>
        <el-form-item label="类型" prop="type">
          <el-select v-model="form.type" style="width: 100%;">
            <el-option label="羽毛球" value="羽毛球" />
            <el-option label="篮球" value="篮球" />
            <el-option label="乒乓球" value="乒乓球" />
            <el-option label="网球" value="网球" />
            <el-option label="足球" value="足球" />
          </el-select>
        </el-form-item>
        <el-form-item label="开放开始">
          <el-time-picker v-model="form.openTime" format="HH:mm" value-format="HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="开放结束">
          <el-time-picker v-model="form.closeTime" format="HH:mm" value-format="HH:mm:ss" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="价格/小时"><el-input-number v-model="form.pricePerHour" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAllVenues, addVenue, updateVenue, updateVenueStatus } from '@/api/venue'
import { ElMessage, ElMessageBox } from 'element-plus'

const venues = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const tableLoading = ref(false)
const form = reactive({ name: '', type: '', openTime: '', closeTime: '', pricePerHour: 0 })
const formRules = {
  name: [{ required: true, message: '请输入场地名称', trigger: 'blur' }],
  type: [{ required: true, message: '请选择场地类型', trigger: 'change' }],
  openTime: [{ required: true, message: '请选择开放时间', trigger: 'change' }],
  closeTime: [{ required: true, message: '请选择关闭时间', trigger: 'change' }],
  pricePerHour: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const typeColor = (type) => {
  const map = { '羽毛球': '', '篮球': 'success', '乒乓球': 'warning', '网球': 'primary', '足球': 'danger' }
  return map[type] || ''
}

const typeLabel = (type) => {
  return type
}

const fetchVenues = async () => {
  tableLoading.value = true
  try {
    const res = await getAllVenues()
    venues.value = res.data
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    tableLoading.value = false
  }
}

const editVenue = (row) => {
  editingId.value = row.id
  Object.assign(form, { name: row.name, type: row.type, openTime: row.openTime, closeTime: row.closeTime, pricePerHour: row.pricePerHour })
  dialogVisible.value = true
}

const toggleStatus = async (row) => {
  const newStatus = row.status === 1 ? 0 : 1
  try {
    await ElMessageBox.confirm(
      `确认${newStatus === 1 ? '上架' : '下架'}该场地？`,
      '状态变更',
      { type: 'warning' }
    )
    await updateVenueStatus(row.id, { status: newStatus })
    ElMessage.success('状态已更新')
    fetchVenues()
  } catch (e) {
    if (e !== 'cancel') {
      // Error already shown by interceptor
    }
  }
}

const handleSave = async () => {
  await formRef.value.validate()
  if (editingId.value) {
    await updateVenue(editingId.value, form)
  } else {
    await addVenue(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchVenues()
}

const resetForm = () => {
  Object.assign(form, { name: '', type: '', openTime: '', closeTime: '', pricePerHour: 0 })
  editingId.value = null
}

onMounted(fetchVenues)
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

.venue-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.venue-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  flex-shrink: 0;
}

.time-range {
  font-family: 'SF Mono', 'Fira Code', monospace;
  font-size: 13px;
  color: #4a4a4a;
}

.price {
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

.status-on { background: #f6ffed; color: #389e0d; }
.status-off { background: #fff1f0; color: #cf1322; }
</style>
