<template>
  <main class="notice-detail-container" aria-label="公告详情">
    <header class="detail-header">
      <el-button 
        type="text" 
        @click="$router.back()"
        class="back-button"
        aria-label="返回"
      >
        <el-icon><ArrowLeft /></el-icon>
        返回
      </el-button>
    </header>
    
    <!-- 公告详情 -->
    <div v-if="notice" class="notice-detail">
      <el-card class="notice-card">
        <div class="card-content">
          <h1 class="notice-title">{{ notice.title }}</h1>
          <div class="notice-meta">
            <span class="notice-publisher">发布人: {{ notice.publisher || '管理员' }}</span>
            <span class="notice-time">{{ formatTime(notice.createdAt) }}</span>
          </div>
          <div class="notice-content">{{ notice.content }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ArrowLeft } from '@element-plus/icons-vue';

const route = useRoute();
const notice = ref(null);
const loading = ref(true);

const loadNoticeDetail = async () => {
  loading.value = true;
  try {
    const noticeId = route.params.id;
    const response = await axios.get(`/api/notice/${noticeId}`);
    notice.value = response.data;
  } catch (error) {
    ElMessage.error('加载公告详情失败');
    console.error('加载公告详情失败:', error);
  } finally {
    loading.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  return date.toLocaleString();
};

onMounted(() => {
  loadNoticeDetail();
});
</script>

<style scoped>
.notice-detail-container {
  max-width: 900px;
  margin: 0 auto;
  padding: 20px;
  padding-bottom: 80px;
}

.detail-header {
  margin-bottom: 20px;
}

.back-button {
  font-size: 14px;
  color: #606266;
  padding: 0;
}

.back-button:hover {
  color: #409EFF;
}

.notice-detail {
  margin-bottom: 30px;
}

.notice-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-content {
  padding: 20px;
}

.notice-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.notice-meta {
  display: flex;
  gap: 20px;
  margin-bottom: 20px;
  font-size: 14px;
  color: #909399;
}

.notice-content {
  font-size: 16px;
  color: #606266;
  line-height: 1.8;
  white-space: pre-wrap;
  word-break: break-word;
}

.loading-state {
  padding: 20px 0;
}

/* 适老模式样式 */
.elder-mode :deep(.notice-card) {
  font-size: 18px;
  padding: 24px;
  border: 1px solid #000000;
  box-shadow: none;
}

.elder-mode :deep(.back-button) {
  font-size: 20px;
  padding: 12px 24px;
}

.elder-mode :deep(.back-button) .el-icon {
  font-size: 20px;
}

.elder-mode :deep(.notice-title) {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 20px 0;
}

.elder-mode :deep(.notice-meta) {
  font-size: 18px;
  color: #000000;
  margin-bottom: 24px;
}

.elder-mode :deep(.notice-content) {
  font-size: 20px;
  color: #000000;
  line-height: 2.0;
  margin-bottom: 24px;
}
</style>
