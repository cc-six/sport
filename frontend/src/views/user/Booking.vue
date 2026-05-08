<template>
  <el-card style="max-width: 800px; margin: 20px auto;">
    <template #header>预约 - {{ venue?.name }}</template>
    <el-form :model="form" label-width="80px">
      <el-form-item label="预约日期">
        <el-date-picker v-model="form.date" type="date" value-format="YYYY-MM-DD"
          @change="fetchSchedule" :disabled-date="disabledDate" placeholder="请选择日期" />
      </el-form-item>
      <el-form-item label="选择时段" v-if="availableSlots.length > 0">
        <div class="time-range">
          <span>从</span>
          <el-select v-model="form.startHour" placeholder="开始时间" style="width: 120px;" @change="onStartChange">
            <el-option v-for="s in availableSlots" :key="s" :label="s" :value="s" />
          </el-select>
          <span>到</span>
          <el-select v-model="form.endHour" placeholder="结束时间" style="width: 120px;" :disabled="!form.startHour">
            <el-option v-for="e in endOptions" :key="e" :label="e" :value="e" />
          </el-select>
        </div>
      </el-form-item>
      <el-form-item label="时段概况" v-if="occupiedSlots.length > 0 && form.date">
        <div class="occupied-info">
          <el-tag v-for="t in occupiedSlots" :key="t" type="danger" size="small" style="margin: 3px;">
            {{ t }} 已约
          </el-tag>
        </div>
      </el-form-item>
      <el-form-item label="金额" v-if="form.startHour && form.endHour">
        <span class="price-text">¥{{ amount }}</span>
      </el-form-item>
      <el-form-item>
        <el-button type="primary" :loading="loading" :disabled="!form.startHour || !form.endHour" @click="handleBook">提交预约</el-button>
      </el-form-item>
    </el-form>
  </el-card>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getVenue, getSchedule } from '@/api/venue'
import { createOrder } from '@/api/order'
import { ElMessage } from 'element-plus'

const route = useRoute()
const router = useRouter()
const venue = ref(null)
const schedule = ref({})
const loading = ref(false)
const form = reactive({ date: null, startHour: '', endHour: '' })

const disabledDate = (time) => {
  const today = new Date()
  today.setHours(0, 0, 0, 0)
  return time.getTime() < today.getTime()
}

const venueSlots = computed(() => {
  if (!venue.value) return []
  const start = venue.value.openTime?.substring(0, 5) || '08:00'
  const end = venue.value.closeTime?.substring(0, 5) || '22:00'
  const startH = parseInt(start.split(':')[0])
  const endH = parseInt(end.split(':')[0])
  const slots = []
  for (let h = startH; h < endH; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
  }
  return slots
})

const timeBoundaries = computed(() => {
  if (!venue.value) return []
  const start = venue.value.openTime?.substring(0, 5) || '08:00'
  const end = venue.value.closeTime?.substring(0, 5) || '22:00'
  const startH = parseInt(start.split(':')[0])
  const endH = parseInt(end.split(':')[0])
  const slots = []
  for (let h = startH; h <= endH; h++) {
    slots.push(`${String(h).padStart(2, '0')}:00`)
  }
  return slots
})

const occupiedSlots = computed(() => {
  return venueSlots.value.filter(slot => schedule.value[slot])
})

const availableSlots = computed(() => {
  return venueSlots.value.filter(slot => !schedule.value[slot])
})

const endOptions = computed(() => {
  if (!form.startHour) return []
  const startIdx = timeBoundaries.value.indexOf(form.startHour)
  if (startIdx < 0) return []
  const options = []
  for (let i = startIdx + 1; i < timeBoundaries.value.length; i++) {
    const previousSlot = timeBoundaries.value[i - 1]
    if (schedule.value[previousSlot]) break
    options.push(timeBoundaries.value[i])
  }
  return options
})

const amount = computed(() => {
  if (!venue.value || !form.startHour || !form.endHour) return 0
  const startH = parseInt(form.startHour.split(':')[0])
  const endH = parseInt(form.endHour.split(':')[0])
  return venue.value.pricePerHour * (endH - startH)
})

const fetchSchedule = async () => {
  if (!form.date) return
  const res = await getSchedule(route.params.id, form.date)
  schedule.value = res.data
  form.startHour = ''
  form.endHour = ''
}

const onStartChange = () => {
  form.endHour = ''
}

const handleBook = async () => {
  if (!form.date) return ElMessage.warning('请选择日期')
  if (!form.startHour || !form.endHour) return ElMessage.warning('请选择时段')
  loading.value = true
  try {
    const res = await createOrder({
      venueId: route.params.id,
      bookDate: form.date,
      startTime: `${form.startHour}:00`,
      endTime: `${form.endHour}:00`
    })
    ElMessage.success('预约成功，请尽快支付')
    router.push({ path: '/my-orders', query: { newOrderId: res.data.id, t: Date.now() } })
  } catch (e) {
    // Error already shown by interceptor
  } finally {
    loading.value = false
  }
}

onMounted(async () => {
  const res = await getVenue(route.params.id)
  venue.value = res.data
})
</script>

<style scoped>
.time-range {
  display: flex;
  align-items: center;
  gap: 8px;
}
.time-range span {
  color: #86909c;
  font-size: 14px;
}
.occupied-info {
  display: flex;
  flex-wrap: wrap;
}
.price-text {
  font-size: 20px;
  font-weight: 700;
  color: #e65454;
}
</style>
