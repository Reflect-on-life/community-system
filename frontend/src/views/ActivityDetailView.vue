<template>
  <main class="activity-detail-container" aria-label="活动详情">
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="10" animated />
    </div>
    
    <!-- 活动详情内容 -->
    <div v-else-if="activity" class="activity-detail">
      <!-- 返回按钮 -->
      <el-button 
        type="info" 
        @click="goBack"
        class="back-button"
        aria-label="返回活动列表"
      >
        返回活动列表
      </el-button>
      
      <!-- 活动标题 -->
      <h1 class="activity-title">{{ activity.title }}</h1>
      
      <!-- 活动状态标签 -->
      <el-tag 
        :type="getActivityStatusType(activity)"
        class="status-tag"
      >
        {{ getActivityStatusText(activity) }}
      </el-tag>
      
      <!-- 活动海报 -->
      <div class="activity-poster">
        <img 
          :src="activity.posterUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=community%20activity%20poster%20colorful%20friendly&image_size=square'" 
          :alt="`${activity.title} 的活动海报`"
          class="poster-image"
        />
      </div>
      
      <!-- 活动信息 -->
      <div class="activity-info-section">
        <h2>活动信息</h2>
        <div class="info-grid">
          <div class="info-item">
            <span class="label">活动时间：</span>
            <span class="value">{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
          </div>
          <div class="info-item">
            <span class="label">活动地点：</span>
            <span class="value">{{ activity.location }}</span>
          </div>
          <div class="info-item">
            <span class="label">活动名额：</span>
            <span class="value">{{ activity.quota }} 人</span>
          </div>
          <div class="info-item">
            <span class="label">已报名人数：</span>
            <span class="value">{{ activity.enrolledCount }} 人</span>
          </div>
          <div class="info-item">
            <span class="label">剩余名额：</span>
            <span class="value">{{ activity.quota - activity.enrolledCount }} 人</span>
          </div>
        </div>
      </div>
      
      <!-- 活动描述 -->
      <div class="activity-description-section">
        <h2>活动详情</h2>
        <p class="description">{{ activity.description || '暂无描述' }}</p>
      </div>
      
      <!-- 报名按钮 -->
      <div v-if="userType === 'resident'" class="action-section">
        <el-button 
          :type="getSignupButtonType(activity)" 
          @click="handleSignup"
          :disabled="!canSignup(activity)"
          size="large"
          class="signup-button"
        >
          {{ getSignupButtonText(activity) }}
        </el-button>
      </div>

      <!-- 参与后的功能区域（已报名用户或管理用户可见） -->
      <div v-if="hasSignedUp || userType !== 'resident'" class="participation-section">
        <!-- 照片上传 -->
        <div class="photo-upload-section">
          <h3>上传活动照片</h3>
          <el-upload
            v-model:file-list="photoFileList"
            :http-request="customPhotoUpload"
            list-type="picture-card"
            accept="image/*"
            :limit="9"
          >
            <el-icon><Plus /></el-icon>
          </el-upload>
        </div>

        <!-- 发表评论 -->
        <div class="comment-section">
          <h3>发表评价</h3>
          <el-form :model="commentForm" label-width="80px">
            <el-form-item label="评分">
              <el-rate v-model="commentForm.rating" :max="5" />
            </el-form-item>
            <el-form-item label="评论内容">
              <el-input
                v-model="commentForm.content"
                type="textarea"
                :rows="4"
                placeholder="分享您的活动体验..."
              />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitComment" :loading="commentLoading">
                提交评价
              </el-button>
            </el-form-item>
          </el-form>
        </div>

        <!-- 已有评论展示 -->
        <div class="comments-display-section">
          <h3>用户评价 ({{ comments.length }})</h3>
          <div v-if="comments.length > 0" class="comment-list">
            <div v-for="comment in comments" :key="comment.id" class="comment-item">
              <div class="comment-header">
                <span class="comment-user">用户 {{ comment.userId }}</span>
                <el-rate v-model="comment.rating" disabled :max="5" size="small" />
                <span class="comment-time">{{ formatDateTime(comment.createdAt) }}</span>
              </div>
              <p class="comment-content">{{ comment.content }}</p>
            </div>
          </div>
          <el-empty v-else description="暂无评价" />
        </div>

        <!-- 已有照片展示 -->
        <div class="photos-display-section">
          <h3>活动照片 ({{ photos.length }})</h3>
          <div v-if="photos.length > 0" class="photo-gallery">
            <div v-for="photo in photos" :key="photo.id" class="photo-item">
              <el-image
                :src="photo.url"
                fit="cover"
                class="gallery-photo"
                :preview-src-list="photos.map(p => p.url)"
                :initial-index="photos.indexOf(photo)"
              />
              <div class="photo-meta">
                <span>上传者: {{ photo.userId }}</span>
                <span>{{ formatDateTime(photo.uploadedAt) }}</span>
              </div>
            </div>
          </div>
          <el-empty v-else description="暂无照片" />
        </div>
      </div>
    </div>
    
    <!-- 空状态 -->
    <div v-else class="empty-state">
      <el-empty description="活动不存在" />
      <el-button type="primary" @click="goBack">返回活动列表</el-button>
    </div>
  </main>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import { useRoute } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';
import router from '../router';

const route = useRoute();
const activity = ref(null);
const userSignups = ref([]);
const isCertified = ref(false);
const loading = ref(true);
const userType = ref('resident');
const photoFileList = ref([]);
const comments = ref([]);
const photos = ref([]);
const commentLoading = ref(false);
const commentForm = reactive({
  rating: 5,
  content: ''
});

const user = JSON.parse(localStorage.getItem('user') || '{}');

// 检查用户类型
const loadUserType = () => {
  if (user.type) {
    userType.value = user.type;
  }
};

// 是否已报名
const hasSignedUp = computed(() => {
  if (!user.userId || !activity.value) return false;
  return userSignups.value.some(s => s.activityId === activity.value.id);
});

// 加载活动详情
const loadActivityDetail = async () => {
  const activityId = route.params.id;
  if (!activityId) {
    ElMessage.error('活动ID不存在');
    loading.value = false;
    return;
  }
  
  try {
    console.log('开始加载活动详情，activityId:', activityId);
    const response = await axios.get(`/api/activity/detail/${activityId}`);
    activity.value = response.data;
    console.log('加载活动详情成功，响应数据:', activity.value);
    
    // 加载评论和照片
    if (hasSignedUp.value || userType.value !== 'resident') {
      await loadComments();
      await loadPhotos();
    }
  } catch (error) {
    ElMessage.error('加载活动详情失败');
    console.error('加载活动详情失败，错误信息:', error);
  } finally {
    loading.value = false;
  }
};

// 加载用户活动报名列表
const loadUserSignups = async () => {
  if (!user.userId) return;
  try {
    const response = await axios.get(`/api/activity/user-signups/${user.userId}`);
    userSignups.value = response.data;
  } catch (error) {
    console.error('加载用户活动报名列表失败:', error);
  }
};

// 检查用户是否已认证房产
const checkUserCertified = async () => {
  if (!user.userId) return;
  try {
    const response = await axios.get('/api/resident-house/certified', {
      params: { userId: user.userId }
    });
    isCertified.value = response.data;
  } catch (error) {
    console.error('检查用户认证状态失败:', error);
  }
};

// 处理报名
const handleSignup = async () => {
  if (!user.userId) {
    ElMessage.error('请先登录');
    return;
  }
  
  try {
    await axios.post('/api/activity/signup', {}, {
      params: {
        activityId: activity.value.id,
        userId: user.userId
      }
    });
    ElMessage.success('报名成功');
    // 重新加载活动详情
    await loadActivityDetail();
    await loadUserSignups();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '报名失败');
    console.error(error);
  }
};

// 加载评论
const loadComments = async () => {
  if (!activity.value?.id) return;
  try {
    const response = await axios.get(`/api/activity/comments/${activity.value.id}`);
    comments.value = response.data;
  } catch (error) {
    console.error('加载评论失败:', error);
  }
};

// 加载照片
const loadPhotos = async () => {
  if (!activity.value?.id) return;
  try {
    const response = await axios.get(`/api/activity/photos/${activity.value.id}`);
    photos.value = response.data;
  } catch (error) {
    console.error('加载照片失败:', error);
  }
};

// 上传照片前检查
const beforePhotoUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  if (!isImage) {
    ElMessage.error('只能上传图片文件!');
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
  }
  return isImage && isLt5M;
};

// 照片上传成功
const handlePhotoUploadSuccess = () => {
  ElMessage.success('照片上传成功');
  photoFileList.value = [];
  loadPhotos();
};

// 照片上传失败
const handlePhotoUploadError = () => {
  ElMessage.error('照片上传失败');
};

// 自定义照片上传请求（包含 JWT 认证）
const customPhotoUpload = async (options) => {
  const token = localStorage.getItem('token');
  const formData = new FormData();
  formData.append('file', options.file);
  try {
    const response = await axios.post('/api/file/upload', formData, {
      headers: { 
        Authorization: token ? `Bearer ${token}` : '',
        'Content-Type': 'multipart/form-data'
      }
    });
    
    // 后端返回的是 { code: 200, data: "/upload/...", message: "成功" } 格式
    const photoUrl = response.data.data;
    
    // 调用后端保存照片记录
    await axios.post('/api/activity/upload-photo', null, {
      params: { 
        activityId: activity.value?.id, 
        userId: user.userId,
        photoUrl: photoUrl
      },
      headers: { Authorization: token ? `Bearer ${token}` : '' }
    });
    
    ElMessage.success('照片上传成功');
    options.onSuccess();
    await loadPhotos();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '照片上传失败');
    options.onError(error);
  }
};

// 提交评论
const submitComment = async () => {
  if (!commentForm.content.trim()) {
    ElMessage.warning('请输入评论内容');
    return;
  }
  
  try {
    commentLoading.value = true;
    await axios.post('/api/activity/add-comment', null, {
      params: {
        activityId: activity.value.id,
        userId: user.userId,
        rating: commentForm.rating,
        content: commentForm.content
      }
    });
    ElMessage.success('评价提交成功');
    commentForm.content = '';
    commentForm.rating = 5;
    await loadComments();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '评价提交失败');
    console.error(error);
  } finally {
    commentLoading.value = false;
  }
};

// 获取活动状态类型
const getActivityStatusType = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return 'info';
  } else if (now <= endTime) {
    return 'success';
  } else {
    return 'danger';
  }
};

// 获取活动状态文本
const getActivityStatusText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString();
};

// 检查是否可以报名
const canSignup = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime || now > endTime) {
    return false;
  }
  
  if (activity.quota - activity.enrolledCount <= 0) {
    return false;
  }
  
  if (userSignups.value.some(signup => signup.activityId === activity.id)) {
    return false;
  }
  
  if (!isCertified.value) {
    return false;
  }
  
  return true;
};

// 获取报名按钮文本
const getSignupButtonText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return '报名未开始';
  }
  
  if (now > endTime) {
    return '活动已结束';
  }
  
  if (activity.quota - activity.enrolledCount <= 0) {
    return '名额已满';
  }
  
  if (userSignups.value.some(signup => signup.activityId === activity.id)) {
    return '已报名';
  }
  
  if (!isCertified.value) {
    return '请先认证房产';
  }
  
  return '立即报名';
};

// 获取报名按钮类型
const getSignupButtonType = (activity) => {
  return canSignup(activity) ? 'primary' : 'info';
};

// 返回活动列表
const goBack = () => {
  router.push('/activity');
};

// 初始化数据
onMounted(async () => {
  loadUserType();
  await loadActivityDetail();
  await loadUserSignups();
  await checkUserCertified();
});
</script>

<style scoped>
.activity-detail-container {
  padding: 20px;
  max-width: 900px;
  margin: 0 auto;
}

.loading-state {
  padding: 20px;
}

.back-button {
  margin-bottom: 20px;
}

.activity-title {
  font-size: 28px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: #333;
}

.status-tag {
  margin-bottom: 24px;
}

.activity-poster {
  width: 100%;
  height: 400px;
  overflow: hidden;
  border-radius: 8px;
  background-color: #f5f7fa;
  margin-bottom: 32px;
}

.poster-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.activity-info-section,
.activity-description-section {
  margin-bottom: 32px;
}

.activity-info-section h2,
.activity-description-section h2 {
  font-size: 20px;
  font-weight: bold;
  margin: 0 0 16px 0;
  color: #333;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.info-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 16px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  color: #606266;
  min-width: 100px;
}

.value {
  color: #909399;
}

.description {
  font-size: 15px;
  line-height: 1.8;
  color: #606266;
  margin: 0;
  white-space: pre-wrap;
}

.action-section {
  margin-top: 32px;
  display: flex;
  justify-content: center;
}

.signup-button {
  min-width: 200px;
  height: 48px;
  font-size: 16px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
}

.participation-section {
  margin-top: 40px;
}

.participation-section h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 32px 0 16px 0;
  color: #333;
  border-left: 4px solid #409eff;
  padding-left: 12px;
}

.photo-upload-section {
  margin-bottom: 32px;
}

.comment-section {
  margin-bottom: 32px;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.comments-display-section {
  margin-bottom: 32px;
}

.comment-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.comment-item {
  padding: 16px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.comment-header {
  display: flex;
  align-items: center;
  gap: 16px;
  margin-bottom: 8px;
}

.comment-user {
  font-weight: bold;
  color: #333;
}

.comment-time {
  font-size: 12px;
  color: #999;
  margin-left: auto;
}

.comment-content {
  margin: 0;
  color: #606266;
  line-height: 1.6;
}

.photos-display-section {
  margin-bottom: 32px;
}

.photo-gallery {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 16px;
}

.gallery-photo {
  width: 100%;
  height: 200px;
  cursor: pointer;
}

.photo-meta {
  display: flex;
  flex-direction: column;
  gap: 4px;
  padding: 8px 0;
  font-size: 12px;
  color: #909399;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-detail-container {
    padding: 16px;
  }
  
  .activity-title {
    font-size: 22px;
  }
  
  .activity-poster {
    height: 250px;
  }
  
  .info-grid {
    grid-template-columns: 1fr;
  }
  
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .label {
    min-width: auto;
  }
  
  .photo-gallery {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
}
</style>
