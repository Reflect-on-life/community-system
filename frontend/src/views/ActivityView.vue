<template>
  <div class="activity-view-container">
    <h2>社区活动</h2>
    
    <el-row :gutter="20">
      <el-col :xs="24" :sm="12" :md="8" v-for="activity in activities" :key="activity.id">
        <el-card class="activity-card" shadow="hover" @click="openDetailDialog(activity)">
          <template #header>
            <div class="card-header">
              <span class="activity-title">{{ activity.title }}</span>
              <el-tag :type="getActivityStatusType(activity)" size="small">
                {{ getActivityStatusText(activity) }}
              </el-tag>
            </div>
          </template>
          
          <!-- 活动海报 -->
          <div class="poster-container">
            <img 
              :src="activity.posterUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=community%20activity%20poster%20colorful%20friendly&image_size=square'" 
              :alt="activity.title"
              class="poster-image"
            />
          </div>
          
          <!-- 活动信息 -->
          <div class="activity-info">
            <p class="description">{{ activity.description }}</p>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ activity.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>剩余名额：{{ activity.quota - activity.enrolledCount }}</span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="card-actions">
            <el-button type="primary" @click.stop="openDetailDialog(activity)" size="small">
              查看详情
            </el-button>
          </div>
        </el-card>
      </el-col>
    </el-row>
    
    <el-empty v-if="activities.length === 0" description="暂无活动" />
    
    <!-- 活动详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentActivity?.title || '活动详情'"
      width="1000px"
      top="5vh"
      class="activity-detail-dialog"
    >
      <div v-if="currentActivity" class="dialog-content">
        <!-- 活动基本信息 -->
        <div class="activity-basic-info">
          <img 
            :src="currentActivity.posterUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=community%20activity%20poster%20colorful%20friendly&image_size=square'" 
            :alt="currentActivity.title"
            class="dialog-poster"
          />
          <div class="dialog-info">
            <h3>{{ currentActivity.title }}</h3>
            <el-tag :type="getActivityStatusType(currentActivity)">{{ getActivityStatusText(currentActivity) }}</el-tag>
            <p class="dialog-description">{{ currentActivity.description }}</p>
            <div class="info-item">
              <el-icon><Calendar /></el-icon>
              <span>{{ formatDateTime(currentActivity.startTime) }} - {{ formatDateTime(currentActivity.endTime) }}</span>
            </div>
            <div class="info-item">
              <el-icon><Location /></el-icon>
              <span>{{ currentActivity.location }}</span>
            </div>
            <div class="info-item">
              <el-icon><User /></el-icon>
              <span>剩余名额：{{ currentActivity.quota - currentActivity.enrolledCount }}</span>
            </div>
          </div>
        </div>

        <el-divider />

        <!-- 按用户分组显示评论和图片，方便管理员一起查看和删除 -->
        <div class="user-content-section">
          <h3>用户参与情况</h3>
          
          <el-table 
            :data="userActivities" 
            style="width: 100%"
            :header-cell-style="{ background: '#f5f7fa', fontWeight: 'bold' }"
          >
            <el-table-column label="用户" width="100" align="center">
              <template #default="scope">
                <span class="user-badge">用户 {{ scope.row.userId }}</span>
              </template>
            </el-table-column>
            
            <el-table-column label="评论内容" min-width="200">
              <template #default="scope">
                <div v-if="scope.row.comment" class="comment-cell">
                  <el-rate v-model="scope.row.comment.rating" disabled :max="5" size="small" />
                  <p class="comment-text">{{ scope.row.comment.content }}</p>
                  <span class="comment-time">{{ formatDateTime(scope.row.comment.createdAt) }}</span>
                </div>
                <span v-else class="no-content">暂无评论</span>
              </template>
            </el-table-column>
            
            <el-table-column label="照片" min-width="200">
              <template #default="scope">
                <div v-if="scope.row.photos.length > 0" class="photos-cell">
                  <el-image
                    v-for="photo in scope.row.photos"
                    :key="photo.id"
                    :src="photo.url"
                    fit="cover"
                    class="user-photo"
                    :preview-src-list="scope.row.photos.map(p => p.url)"
                  >
                    <template #error>
                      <div class="photo-error">图片加载失败</div>
                    </template>
                  </el-image>
                  <span class="photo-time">{{ formatDateTime(scope.row.photos[0].uploadedAt) }}</span>
                </div>
                <span v-else class="no-content">暂无照片</span>
              </template>
            </el-table-column>
            
            <el-table-column label="操作" width="120" align="center" v-if="isPropertyOrAdmin">
              <template #default="scope">
                <el-button 
                  type="danger" 
                  size="small"
                  @click="handleDeleteUserContent(scope.row)"
                  :disabled="!scope.row.comment && scope.row.photos.length === 0"
                >
                  删除
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Calendar, Location, User } from '@element-plus/icons-vue';

const activities = ref([]);
const detailDialogVisible = ref(false);
const currentActivity = ref(null);
const userActivities = ref([]);

const user = JSON.parse(localStorage.getItem('user') || '{}');
const isPropertyOrAdmin = computed(() => {
  return user.type === 'property' || user.type === 'admin';
});

const loadActivities = async () => {
  try {
    const response = await axios.get('/api/activity/list');
    activities.value = response.data;
  } catch (error) {
    ElMessage.error('加载活动列表失败');
  }
};

const openDetailDialog = async (activity) => {
  currentActivity.value = activity;
  detailDialogVisible.value = true;
  
  // 加载评论和照片并合并到按用户分组的列表中
  await loadUserActivities(activity.id);
};

const loadUserActivities = async (activityId) => {
  try {
    const [commentsRes, photosRes] = await Promise.all([
      axios.get(`/api/activity/comments/${activityId}`),
      axios.get(`/api/activity/photos/${activityId}`)
    ]);
    
    const comments = commentsRes.data || [];
    const photos = photosRes.data || [];
    
    // 获取所有用户 ID
    const userIds = new Set();
    comments.forEach(c => userIds.add(c.userId));
    photos.forEach(p => userIds.add(p.userId));
    
    // 按用户分组
    userActivities.value = Array.from(userIds).map(userId => {
      const userComments = comments.filter(c => c.userId === userId);
      const userPhotos = photos.filter(p => p.userId === userId);
      
      return {
        userId,
        comment: userComments.length > 0 ? userComments[0] : null,
        photos: userPhotos
      };
    });
  } catch (error) {
    ElMessage.error('加载用户活动失败');
  }
};

const handleDeleteUserContent = async (userActivity) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除用户 ${userActivity.userId} 的评论内容和图片吗？`, 
      '删除确认', 
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    );
    
    // 删除评论
    if (userActivity.comment) {
      await axios.delete(`/api/activity/comment/${userActivity.comment.id}`);
    }
    
    // 删除所有照片
    for (const photo of userActivity.photos) {
      await axios.delete(`/api/activity/photo/${photo.id}`);
    }
    
    ElMessage.success('删除成功');
    
    // 刷新用户活动列表
    await loadUserActivities(currentActivity.value.id);
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
    }
  }
};

const getActivityStatusType = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) return 'info';
  if (now <= endTime) return 'success';
  return 'danger';
};

const getActivityStatusText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) return '未开始';
  if (now <= endTime) return '进行中';
  return '已结束';
};

const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  return new Date(dateTime).toLocaleString();
};

onMounted(() => {
  loadActivities();
});
</script>

<style scoped>
.activity-view-container {
  padding: 20px;
}

.activity-card {
  margin-bottom: 20px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.activity-card:hover {
  transform: translateY(-4px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-title {
  font-weight: bold;
  font-size: 16px;
}

.poster-container {
  width: 100%;
  height: 200px;
  overflow: hidden;
  margin-bottom: 16px;
  border-radius: 4px;
}

.poster-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info {
  margin-bottom: 16px;
}

.description {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #909399;
  margin-bottom: 8px;
}

.card-actions {
  display: flex;
  justify-content: center;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.dialog-content {
  max-height: 70vh;
  overflow-y: auto;
}

.activity-basic-info {
  display: flex;
  gap: 20px;
}

.dialog-poster {
  width: 300px;
  height: 200px;
  object-fit: cover;
  border-radius: 8px;
  flex-shrink: 0;
}

.dialog-info h3 {
  margin: 0 0 8px 0;
  font-size: 20px;
}

.dialog-description {
  font-size: 14px;
  color: #606266;
  margin: 8px 0;
  line-height: 1.6;
}

.dialog-info .info-item {
  margin-bottom: 8px;
}

.user-content-section h3 {
  margin: 0 0 16px 0;
  font-size: 18px;
  color: #333;
}

.user-badge {
  display: inline-block;
  padding: 4px 12px;
  background-color: #e6f7ff;
  color: #1890ff;
  border-radius: 16px;
  font-weight: bold;
  font-size: 13px;
}

.comment-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.comment-text {
  margin: 0;
  color: #606266;
  font-size: 14px;
  line-height: 1.5;
}

.comment-time {
  font-size: 12px;
  color: #909399;
}

.photos-cell {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.user-photo {
  width: 60px;
  height: 60px;
  border-radius: 4px;
  cursor: pointer;
}

.photo-time {
  font-size: 12px;
  color: #909399;
}

.no-content {
  color: #c0c4cc;
  font-size: 13px;
}
</style>
