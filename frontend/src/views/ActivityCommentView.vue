<template>
  <div class="activity-comment-container">
    <h2>活动评论管理</h2>
    
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
    
    <!-- 评论列表 -->
    <el-card class="table-card">
      <el-table :data="commentList" v-loading="loading" style="width: 100%">
        <el-table-column prop="id" label="评论ID" width="100" />
        <el-table-column prop="activityId" label="活动ID" width="100" />
        <el-table-column label="活动名称" min-width="150">
          <template #default="scope">
            {{ getActivityTitle(scope.row.activityId) }}
          </template>
        </el-table-column>
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column label="评分" width="120">
          <template #default="scope">
            <el-rate v-model="scope.row.rating" disabled show-score />
          </template>
        </el-table-column>
        <el-table-column prop="content" label="评论内容" min-width="250" />
        <el-table-column label="评论时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
      
      <el-empty v-if="commentList.length === 0" description="暂无评论数据" />
    </el-card>
    
    <!-- 统计信息 -->
    <el-card class="stats-card">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-title">总评论数</div>
          <div class="stat-value">{{ totalComments }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-title">平均评分</div>
          <div class="stat-value">{{ averageRating }}</div>
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
const commentList = ref([]);
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

const loadComments = async () => {
  if (!queryForm.activityId) {
    commentList.value = [];
    return;
  }
  try {
    loading.value = true;
    const response = await axios.get(`/api/activity/comments/${queryForm.activityId}`);
    commentList.value = response.data;
  } catch (error) {
    ElMessage.error('加载评论列表失败');
  } finally {
    loading.value = false;
  }
};

const handleQuery = () => {
  loadComments();
};

const getActivityTitle = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  return activity ? activity.title : `活动${activityId}`;
};

const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

const totalComments = computed(() => commentList.value.length);

const averageRating = computed(() => {
  if (commentList.value.length === 0) return '0.0';
  const sum = commentList.value.reduce((acc, c) => acc + (c.rating || 0), 0);
  return (sum / commentList.value.length).toFixed(1);
});

onMounted(() => {
  loadActivityList();
});
</script>

<style scoped>
.activity-comment-container {
  padding: 20px;
}

.query-card {
  margin-bottom: 20px;
}

.table-card {
  margin-bottom: 20px;
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
</style>
