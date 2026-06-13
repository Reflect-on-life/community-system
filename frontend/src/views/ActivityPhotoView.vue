<template>
  <div class="activity-photo-container">
    <h2>活动照片管理</h2>
    
    <!-- 活动筛选 -->
    <el-card class="query-card">
      <el-form :inline="true" :model="queryForm">
        <el-form-item label="选择活动">
          <el-select v-model="queryForm.activityId" placeholder="请选择活动" style="width: 350px;" clearable @change="handleQuery">
            <el-option
              v-for="activity in activityList"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            >
              <div class="option-content">
                <span class="option-title">{{ activity.title }}</span>
                <span class="option-date">{{ formatDate(activity.startTime) }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 照片展示 -->
    <el-card class="photo-card">
      <div class="photo-grid" v-if="photoList.length > 0">
        <div v-for="photo in photoList" :key="photo.id" class="photo-item">
          <el-image
            :src="photo.url"
            fit="cover"
            class="photo-image"
            :preview-src-list="photoList.map(p => p.url)"
            :initial-index="photoList.indexOf(photo)"
          />
          <div class="photo-info">
            <span class="photo-user">上传者ID: {{ photo.userId }}</span>
            <span class="photo-time">{{ formatDate(photo.uploadedAt) }}</span>
          </div>
        </div>
      </div>
      
      <el-empty v-else description="暂无照片" />
    </el-card>
    
    <!-- 统计信息 -->
    <el-card class="stats-card">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-title">照片总数</div>
          <div class="stat-value">{{ photoList.length }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-title">上传人数</div>
          <div class="stat-value">{{ uniqueUploaders }}</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const queryForm = reactive({
  activityId: ''
});

const activityList = ref([]);
const photoList = ref([]);
const loading = ref(false);
const activityMap = ref(new Map());

const loadActivityList = async () => {
  try {
    const response = await axios.get('/api/activity/list');
    activityList.value = response.data;
    activityList.value.forEach(a => activityMap.value.set(Number(a.id), a));
  } catch (error) {
    ElMessage.error('加载活动列表失败');
  }
};

const loadPhotos = async () => {
  if (!queryForm.activityId) {
    photoList.value = [];
    return;
  }
  try {
    loading.value = true;
    const response = await axios.get(`/api/activity/photos/${queryForm.activityId}`);
    photoList.value = response.data;
  } catch (error) {
    ElMessage.error('加载照片列表失败');
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  loadPhotos();
};

const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

const uniqueUploaders = computed(() => {
  if (photoList.value.length === 0) return 0;
  const userIds = new Set(photoList.value.map(p => p.userId));
  return userIds.size;
});

onMounted(() => {
  loadActivityList();
});
</script>

<style scoped>
.activity-photo-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.photo-card {
  margin-bottom: 20px;
}

.photo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(250px, 1fr));
  gap: 20px;
}

.photo-item {
  border: 1px solid #ebeef5;
  border-radius: 8px;
  overflow: hidden;
  transition: all 0.3s ease;
}

.photo-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.photo-image {
  width: 100%;
  height: 200px;
  display: block;
}

.photo-info {
  padding: 12px;
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.photo-user {
  font-size: 13px;
  color: #666;
}

.photo-time {
  font-size: 12px;
  color: #999;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-container {
  display: flex;
  gap: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.option-content {
  display: flex;
  flex-direction: column;
}

.option-title {
  font-weight: 500;
}

.option-date {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .photo-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
    gap: 12px;
  }
}
</style>
