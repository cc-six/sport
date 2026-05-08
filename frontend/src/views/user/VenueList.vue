<template>
  <div class="venue-page">
    <el-card class="search-card">
      <div class="search-bar">
        <span class="search-title">场地查询</span>
        <el-select v-model="type" placeholder="场地类型" clearable style="width: 200px;" @change="fetchVenues">
          <el-option label="全部" value="" />
          <el-option label="羽毛球" value="羽毛球" />
          <el-option label="篮球" value="篮球" />
          <el-option label="乒乓球" value="乒乓球" />
          <el-option label="网球" value="网球" />
          <el-option label="足球" value="足球" />
        </el-select>
      </div>
    </el-card>
    <el-row :gutter="20" style="margin-top: 20px;">
      <el-col :span="8" v-for="venue in venues" :key="venue.id">
        <el-card shadow="hover" class="venue-card">
          <div class="venue-header">
            <span class="venue-icon">{{ venueIcon(venue.type) }}</span>
            <div class="venue-info">
              <div class="venue-name">{{ venue.name }}</div>
              <el-tag size="small" :type="typeColor(venue.type)" effect="plain">{{ typeLabel(venue.type) }}</el-tag>
            </div>
            <div class="venue-status">
              <span class="status-dot" :style="{ background: venue.status === 1 ? '#52c41a' : '#ff4d4f' }"></span>
            </div>
          </div>
          <div class="venue-details">
            <div class="detail-item">
              <span class="detail-label">时段</span>
              <span class="detail-value">{{ venue.openTime }} — {{ venue.closeTime }}</span>
            </div>
            <div class="detail-item">
              <span class="detail-label">价格</span>
              <span class="detail-value price">¥{{ venue.pricePerHour }}/小时</span>
            </div>
          </div>
          <el-button type="primary" class="book-btn" @click="$router.push(`/book/${venue.id}`)" :disabled="venue.status === 0">
            {{ venue.status === 1 ? '立即预约' : '暂不可用' }}
          </el-button>
        </el-card>
      </el-col>
    </el-row>
    <div v-if="!venues.length" class="empty-state">暂无可用场地</div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { getVenues } from '@/api/venue'

const venues = ref([])
const type = ref('')

const typeLabel = (t) => t

const typeColor = (t) => ({
  '羽毛球': '', '篮球': 'success', '乒乓球': 'warning',
  '网球': 'primary', '足球': 'danger'
}[t] || '')

const venueIcon = (t) => ({
  '羽毛球': '\u{1F3F8}', '篮球': '\u{1F3C0}', '乒乓球': '\u{1F3D3}',
  '网球': '\u{1F3BE}', '足球': '\u{26BD}'
}[t] || '\u{1F3DB}')

const fetchVenues = async () => {
  const res = await getVenues({ type: type.value || null, page: 1, pageSize: 20 })
  venues.value = res.data.records
}
onMounted(fetchVenues)
</script>

<style scoped>
.venue-page {
  animation: fadeIn 0.4s ease;
}

@keyframes fadeIn {
  from { opacity: 0; transform: translateY(12px); }
  to { opacity: 1; transform: translateY(0); }
}

.search-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
}

.search-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-title {
  font-size: 18px;
  font-weight: 600;
  color: #1d1d1f;
}

.venue-card {
  border: none;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.04);
  transition: transform 0.2s, box-shadow 0.2s;
  margin-bottom: 20px;
}

.venue-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
}

.venue-header {
  display: flex;
  align-items: center;
  gap: 12px;
}

.venue-icon {
  font-size: 36px;
  filter: drop-shadow(0 2px 4px rgba(0,0,0,0.1));
}

.venue-info {
  flex: 1;
}

.venue-name {
  font-size: 16px;
  font-weight: 600;
  color: #1d1d1f;
  margin-bottom: 4px;
}

.venue-details {
  margin-top: 16px;
  padding-top: 12px;
  border-top: 1px solid #f0f0f0;
}

.detail-item {
  display: flex;
  justify-content: space-between;
  margin-bottom: 8px;
}

.detail-label {
  font-size: 13px;
  color: #86909c;
}

.detail-value {
  font-size: 13px;
  color: #4a4a4a;
  font-weight: 500;
}

.detail-value.price {
  color: #e65454;
  font-weight: 700;
  font-size: 16px;
}

.book-btn {
  width: 100%;
  margin-top: 16px;
  height: 42px;
  font-size: 15px;
  border-radius: 8px;
}

.empty-state {
  text-align: center;
  padding: 60px 0;
  color: #86909c;
  font-size: 16px;
}
</style>
