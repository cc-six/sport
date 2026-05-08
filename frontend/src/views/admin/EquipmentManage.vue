<template>
  <el-card class="page-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">
          <span class="title-icon">&#127942;</span>
          器材管理
        </span>
        <el-button type="primary" @click="dialogVisible = true; editingId = null">
          <span style="margin-right: 4px;">&#43;</span> 新增器材
        </el-button>
      </div>
    </template>
    <el-table :data="equipments" stripe class="styled-table" v-loading="loading">
      <el-empty v-if="!loading && equipments.length === 0" slot="empty" description="暂无器材" />
      <el-table-column prop="name" label="器材名称" min-width="220">
        <template #default="{ row }">
          <span class="eq-name">
            <span class="eq-icon">&#128295;</span>
            {{ row.name }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="totalQty" label="总数量" min-width="140">
        <template #default="{ row }">
          <el-tag size="small" type="info" effect="plain">{{ row.totalQty }}</el-tag>
        </template>
      </el-table-column>
      <el-table-column prop="availableQty" label="可借数量" min-width="160">
        <template #default="{ row }">
          <span :class="['qty-badge', row.availableQty === 0 ? 'qty-empty' : '']">
            <span class="qty-dot" :style="{ background: row.availableQty > 0 ? '#52c41a' : '#ff4d4f' }"></span>
            {{ row.availableQty }}
          </span>
        </template>
      </el-table-column>
      <el-table-column prop="pricePerHour" label="租借单价/小时" min-width="180">
        <template #default="{ row }">
          <span class="price">&yen;{{ row.pricePerHour.toFixed(2) }}</span>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="140" fixed="right">
        <template #default="{ row }">
          <el-button size="small" type="primary" @click="editEquipment(row)">编辑</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div v-if="equipments.length" class="table-footer">
      <span class="total-text">共 {{ equipments.length }} 种器材</span>
    </div>

    <el-dialog v-model="dialogVisible" :title="editingId ? '编辑器材' : '新增器材'" @close="resetForm">
      <el-form :model="form" :rules="formRules" ref="formRef" label-width="100px">
        <el-form-item label="名称" prop="name"><el-input v-model="form.name" placeholder="请输入器材名称" /></el-form-item>
        <el-form-item label="总数量" prop="totalQty"><el-input-number v-model="form.totalQty" :min="1" /></el-form-item>
        <el-form-item label="租借单价" prop="pricePerHour"><el-input-number v-model="form.pricePerHour" :min="0" :precision="2" /></el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSave">保存</el-button>
      </template>
    </el-dialog>
  </el-card>

  <el-card class="page-card rental-card">
    <template #header>
      <div class="page-header">
        <span class="page-title">
          <span class="title-icon">&#128221;</span>
          租借记录
        </span>
        <el-select v-model="rentalStatus" placeholder="状态筛选" clearable @change="fetchRentals" style="width: 140px;">
          <el-option label="租借中" :value="0" />
          <el-option label="已归还" :value="1" />
        </el-select>
      </div>
    </template>
    <el-table :data="rentals" stripe class="styled-table" v-loading="rentalLoading">
      <el-table-column prop="id" label="记录ID" min-width="100" />
      <el-table-column prop="userId" label="用户ID" min-width="110" />
      <el-table-column prop="equipmentId" label="器材ID" min-width="110" />
      <el-table-column prop="quantity" label="数量" min-width="90" />
      <el-table-column prop="createTime" label="租借时间" min-width="180" />
      <el-table-column prop="returnTime" label="归还时间" min-width="180" />
      <el-table-column prop="status" label="状态" min-width="110">
        <template #default="{ row }">
          <el-tag :type="row.status === 0 ? 'warning' : 'success'">
            {{ row.status === 0 ? '租借中' : '已归还' }}
          </el-tag>
        </template>
      </el-table-column>
      <el-table-column label="操作" width="110" fixed="right">
        <template #default="{ row }">
          <el-button v-if="row.status === 0" size="small" type="success" @click="handleReturn(row)">归还</el-button>
        </template>
      </el-table-column>
    </el-table>
  </el-card>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getEquipments, addEquipment, updateEquipment, getAllRentals, returnEquipment } from '@/api/equipment'
import { ElMessage, ElMessageBox } from 'element-plus'

const equipments = ref([])
const dialogVisible = ref(false)
const editingId = ref(null)
const formRef = ref()
const loading = ref(false)
const rentalLoading = ref(false)
const rentals = ref([])
const rentalStatus = ref(null)
const form = reactive({ name: '', totalQty: 10, pricePerHour: 0 })
const formRules = {
  name: [{ required: true, message: '请输入器材名称', trigger: 'blur' }],
  totalQty: [{ required: true, message: '请输入数量', trigger: 'blur' }],
  pricePerHour: [{ required: true, message: '请输入价格', trigger: 'blur' }]
}

const fetchEquipments = async () => {
  loading.value = true
  try {
    const res = await getEquipments({ page: 1, pageSize: 50 })
    equipments.value = res.data.records
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}

const fetchRentals = async () => {
  rentalLoading.value = true
  try {
    const res = await getAllRentals({ page: 1, pageSize: 50, status: rentalStatus.value })
    rentals.value = res.data.records
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    rentalLoading.value = false
  }
}

const editEquipment = (row) => {
  editingId.value = row.id
  Object.assign(form, { name: row.name, totalQty: row.totalQty, pricePerHour: row.pricePerHour })
  dialogVisible.value = true
}

const handleSave = async () => {
  await formRef.value.validate()
  if (editingId.value) {
    await updateEquipment(editingId.value, form)
  } else {
    form.availableQty = form.totalQty
    await addEquipment(form)
  }
  ElMessage.success('保存成功')
  dialogVisible.value = false
  fetchEquipments()
}

const handleReturn = async (row) => {
  try {
    await ElMessageBox.confirm('确认该器材已归还？', '归还确认', {
      confirmButtonText: '确认归还',
      cancelButtonText: '取消',
      type: 'warning'
    })
    await returnEquipment(row.id)
    ElMessage.success('归还成功')
    fetchEquipments()
    fetchRentals()
  } catch (e) {
    if (e !== 'cancel') {
      // Error already shown by interceptor
    }
  }
}

const resetForm = () => {
  Object.assign(form, { name: '', totalQty: 10, pricePerHour: 0 })
  editingId.value = null
}

onMounted(() => {
  fetchEquipments()
  fetchRentals()
})
</script>

<style scoped>
.page-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 10px 28px rgba(16, 24, 40, 0.06);
  animation: fadeIn 0.4s ease;
}

.rental-card {
  margin-top: 20px;
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

.eq-name {
  display: flex;
  align-items: center;
  gap: 6px;
  font-weight: 500;
}

.eq-icon {
  font-size: 16px;
}

.qty-badge {
  display: inline-flex;
  align-items: center;
  gap: 6px;
  padding: 2px 10px;
  border-radius: 10px;
  font-size: 13px;
  font-weight: 600;
  background: #f6ffed;
  color: #389e0d;
}

.qty-empty {
  background: #fff1f0;
  color: #cf1322;
}

.qty-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
}

.price {
  font-weight: 700;
  color: #409EFF;
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
