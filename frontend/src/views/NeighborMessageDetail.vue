<template>
  <main class="neighbor-message-detail-container" aria-label="帖子详情">
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
    
    <!-- 帖子详情 -->
    <div v-if="message" class="message-detail">
      <el-card class="message-card">
        <div class="card-content">
          <div class="user-info">
            <el-avatar :size="50" :src="message.avatar || ''" aria-hidden="true">
              {{ message.authorName || message.userName ? (message.authorName || message.userName).charAt(0) : 'U' }}
            </el-avatar>
            <div class="user-details">
              <span class="user-name">{{ message.authorName || message.userName }}</span>
              <span class="publish-time">{{ formatTime(message.createTime || message.createdAt) }}</span>
            </div>
          </div>
          
          <h1 class="message-title">{{ message.title }}</h1>
          <div class="message-content">{{ message.content }}</div>
          
          <div class="message-stats">
            <span class="stat-item">
              <el-icon aria-hidden="true"><ChatDotRound /></el-icon>
              {{ message.replyCount || 0 }} 评论
            </span>
            <span class="stat-item" @click="handleLike" style="cursor: pointer;">
              <el-icon :class="{ 'liked': isLiked }" aria-hidden="true"><Star /></el-icon>
              {{ message.likeCount || 0 }} 点赞
            </span>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="5" animated />
    </div>
    
    <!-- 评论列表 -->
    <div v-if="!loading && message" class="comments-section">
      <h2 class="section-title">评论 ({{ replyList.length }})</h2>
      
      <div v-if="replyList.length === 0" class="empty-comments">
        <el-empty description="暂无评论，快来抢沙发吧！" />
      </div>
      
      <ul v-else class="comment-list" aria-label="评论列表">
        <li 
          v-for="reply in replyList" 
          :key="reply.id"
          class="comment-item"
        >
          <div class="comment-content">
            <el-avatar :size="36" :src="reply.avatar || ''" aria-hidden="true">
              {{ reply.authorName || reply.userName ? (reply.authorName || reply.userName).charAt(0) : 'U' }}
            </el-avatar>
            <div class="comment-details">
              <div class="comment-header">
                <span class="comment-user">{{ reply.authorName || reply.userName }}</span>
                <span class="comment-time">{{ formatTime(reply.createTime || reply.createdAt) }}</span>
              </div>
              <p class="comment-text">{{ reply.content }}</p>
            </div>
          </div>
        </li>
      </ul>
    </div>
    
    <!-- 底部评论输入框 -->
    <div v-if="!loading && message" class="comment-input-container">
      <div class="comment-input-wrapper">
        <el-input
          v-model="replyForm.content"
          type="textarea"
          :rows="3"
          placeholder="发表你的评论..."
          maxlength="200"
          show-word-limit
          @keyup.ctrl.enter="handleReply"
          class="comment-input"
        />
        <el-button 
          type="primary" 
          @click="handleReply"
          :loading="replying"
          class="send-button"
          aria-label="发送评论"
        >
          发送
        </el-button>
      </div>
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRoute, useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ArrowLeft, ChatDotRound, Star } from '@element-plus/icons-vue';

const route = useRoute();
const router = useRouter();
const user = JSON.parse(localStorage.getItem('user') || '{}');

const message = ref(null);
const replyList = ref([]);
const loading = ref(true);
const replying = ref(false);
const isLiked = ref(false);
const liking = ref(false);

const replyForm = ref({
  content: ''
});

const loadMessageDetail = async () => {
  loading.value = true;
  try {
    const messageId = route.params.id;
    const response = await axios.get(`/api/neighbor-messages/${messageId}`);
    message.value = response.data;
    // 检查用户是否已经点赞
    if (user.userId) {
      try {
        const likeResponse = await axios.get(`/api/neighbor-messages/${messageId}/is-liked`, {
          params: { userId: user.userId }
        });
        isLiked.value = likeResponse.data.liked;
      } catch (error) {
        console.error('检查点赞状态失败:', error);
      }
    }
  } catch (error) {
    ElMessage.error('加载帖子详情失败');
    console.error('加载帖子详情失败:', error);
  } finally {
    loading.value = false;
  }
};

const loadReplyList = async () => {
  try {
    const messageId = route.params.id;
    const response = await axios.get(`/api/neighbor-messages/${messageId}/replies`);
    replyList.value = response.data || [];
  } catch (error) {
    ElMessage.error('加载评论列表失败');
    console.error('加载评论列表失败:', error);
  }
};

const handleReply = async () => {
  if (!user.userId) {
    ElMessage.error('请先登录');
    return;
  }
  
  if (!replyForm.value.content.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  
  replying.value = true;
  try {
    const messageId = route.params.id;
    await axios.post(`/api/neighbor-messages/${messageId}/reply`, {
      userId: user.userId,
      content: replyForm.value.content
    });
    ElMessage.success('评论成功');
    replyForm.value.content = '';
    await loadReplyList();
    await loadMessageDetail();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评论失败');
    console.error('评论失败:', error);
  } finally {
    replying.value = false;
  }
};

const handleLike = async () => {
  if (!user.userId) {
    ElMessage.error('请先登录');
    return;
  }
  
  liking.value = true;
  try {
    const messageId = route.params.id;
    await axios.post(`/api/neighbor-messages/${messageId}/like`, {
      userId: user.userId
    });
    ElMessage.success(isLiked.value ? '取消点赞成功' : '点赞成功');
    isLiked.value = !isLiked.value;
    await loadMessageDetail();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '操作失败');
    console.error('点赞操作失败:', error);
  } finally {
    liking.value = false;
  }
};



const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  
  const minute = 60 * 1000;
  const hour = 60 * minute;
  const day = 24 * hour;
  
  const isToday = date.toDateString() === now.toDateString();
  
  if (diff < minute) {
    return '刚刚';
  } else if (diff < hour) {
    return `${Math.floor(diff / minute)}分钟前`;
  } else if (isToday) {
    return `今天 ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } else if (diff < 7 * day) {
    const weekdays = ['周日', '周一', '周二', '周三', '周四', '周五', '周六'];
    return `${weekdays[date.getDay()]} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } else {
    return `${date.getFullYear()}-${(date.getMonth() + 1).toString().padStart(2, '0')}-${date.getDate().toString().padStart(2, '0')} ${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  }
};

onMounted(() => {
  loadMessageDetail();
  loadReplyList();
});
</script>

<style scoped>
.neighbor-message-detail-container {
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

.message-detail {
  margin-bottom: 30px;
}

.message-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.card-content {
  padding: 20px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 16px;
}

.user-details {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
}

.user-name {
  font-size: 16px;
  font-weight: 500;
  color: #303133;
}

.publish-time {
  font-size: 13px;
  color: #909399;
  margin-top: 4px;
}

.message-title {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 16px 0;
  line-height: 1.4;
}

.message-content {
  font-size: 15px;
  color: #606266;
  line-height: 1.8;
  margin-bottom: 20px;
  white-space: pre-wrap;
  word-break: break-word;
}

.message-stats {
  display: flex;
  gap: 24px;
  padding-top: 16px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 14px;
  color: #909399;
  gap: 4px;
}

.stat-item:hover {
  color: #409EFF;
}

.liked {
  color: #F56C6C !important;
}

.loading-state {
  padding: 20px 0;
}

.comments-section {
  margin-top: 30px;
}

.section-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 20px 0;
}

.empty-comments {
  padding: 40px 0;
}

.comment-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.comment-item {
  padding: 16px 0;
  border-bottom: 1px solid #f0f0f0;
}

.comment-item:last-child {
  border-bottom: none;
}

.comment-content {
  display: flex;
  gap: 12px;
}

.comment-details {
  flex: 1;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 8px;
}

.comment-user {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.comment-text {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin: 0;
  white-space: pre-wrap;
  word-break: break-word;
}

.comment-input-container {
  position: sticky;
  bottom: 0;
  background: #fff;
  padding: 16px 20px;
  box-shadow: 0 -2px 10px rgba(0, 0, 0, 0.1);
  z-index: 100;
  box-sizing: border-box;
  width: 100%;
  margin-top: 30px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .comment-input-container {
    padding: 12px 16px;
  }
}

.comment-input-wrapper {
  max-width: 900px;
  margin: 0 auto;
  display: flex;
  gap: 12px;
  align-items: flex-end;
}

.comment-input {
  flex: 1;
}

.send-button {
  flex-shrink: 0;
}

/* 适老模式样式 */
.elder-mode :deep(.message-card) {
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

.elder-mode :deep(.user-name) {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.elder-mode :deep(.publish-time) {
  font-size: 16px;
  color: #000000;
  margin-top: 8px;
}

.elder-mode :deep(.message-title) {
  font-size: 28px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 20px 0;
}

.elder-mode :deep(.message-content) {
  font-size: 20px;
  color: #000000;
  line-height: 2.0;
  margin-bottom: 24px;
}

.elder-mode :deep(.message-stats) {
  gap: 30px;
  padding-top: 20px;
  border-top: 1px solid #000000;
}

.elder-mode :deep(.stat-item) {
  font-size: 18px;
  color: #000000;
  gap: 8px;
}

.elder-mode :deep(.stat-item) .el-icon {
  font-size: 20px;
}

.elder-mode :deep(.section-title) {
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 24px 0;
}

.elder-mode :deep(.comment-item) {
  padding: 24px 0;
  border-bottom: 1px solid #000000;
}

.elder-mode :deep(.comment-user) {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.elder-mode :deep(.comment-time) {
  font-size: 16px;
  color: #000000;
}

.elder-mode :deep(.comment-text) {
  font-size: 18px;
  color: #000000;
  line-height: 1.8;
}

.elder-mode :deep(.comment-input-container) {
  padding: 20px;
  box-shadow: 0 -4px 20px rgba(0, 0, 0, 0.2);
}

.elder-mode :deep(.comment-input-wrapper) {
  gap: 16px;
}

.elder-mode :deep(.el-textarea__inner) {
  font-size: 20px;
  min-height: 120px;
  border: 1px solid #000000;
}

.elder-mode :deep(.el-button) {
  font-size: 20px;
  padding: 14px 24px;
}

.elder-mode :deep(.el-empty__description) {
  font-size: 18px;
  color: #000000;
}

.elder-mode :deep(.el-empty__button) .el-button {
  font-size: 20px;
  padding: 14px 24px;
}
</style>
