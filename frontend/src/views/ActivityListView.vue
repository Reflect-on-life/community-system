<template>
  <main class="activity-list-container" aria-label="社区活动列表">
    <header class="page-header">
      <h1>社区活动</h1>
      <el-button 
        type="info" 
        @click="goToMySignups"
        aria-label="查看我的报名"
      >
        我的报名
      </el-button>
    </header>
    
    <!-- 活动列表 -->
    <ul class="activity-list" aria-label="活动列表">
      <li 
        v-for="activity in activityList" 
        :key="activity.id"
        class="activity-item"
        tabindex="0"
        @keyup.enter="handleCardClick(activity)"
        @keyup.space="handleCardClick(activity)"
      >
        <el-card class="activity-card">
          <template #header>
            <div class="card-header">
              <h3 class="activity-title">{{ activity.title }}</h3>
              <el-tag 
                :type="getActivityStatusType(activity)"
                aria-label="活动状态"
              >
                {{ getActivityStatusText(activity) }}
              </el-tag>
            </div>
          </template>
          
          <div class="activity-content">
            <!-- 活动海报 -->
            <div class="activity-poster">
              <img 
                :src="activity.posterUrl || 'https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=community%20activity%20poster%20colorful%20friendly&image_size=square'" 
                :alt="`${activity.title} 的活动海报`"
                class="poster-image"
                loading="lazy"
              />
            </div>
            
            <!-- 活动信息 -->
            <div class="activity-info">
              <p 
                class="activity-description"
                :title="activity.description"
              >{{ activity.description }}</p>
              <div class="activity-meta">
                <div class="meta-item">
                  <el-icon aria-hidden="true"><Calendar /></el-icon>
                  <span>{{ formatDateTime(activity.startTime) }} - {{ formatDateTime(activity.endTime) }}</span>
                </div>
                <div class="meta-item">
                  <el-icon aria-hidden="true"><Location /></el-icon>
                  <span>{{ activity.location }}</span>
                </div>
                <div class="meta-item">
                  <el-icon aria-hidden="true"><User /></el-icon>
                  <span>剩余名额: {{ activity.quota - activity.enrolledCount }}</span>
                </div>
              </div>
              
              <!-- 报名按钮 -->
              <div class="activity-actions">
                <el-button 
                  :type="getSignupButtonType(activity)" 
                  @click="handleSignup(activity.id)"
                  :disabled="!canSignup(activity)"
                  :aria-label="`${getSignupButtonText(activity)}: ${activity.title}`"
                  class="signup-button"
                >
                  {{ getSignupButtonText(activity) }}
                </el-button>
              </div>
            </div>
          </div>
        </el-card>
      </li>
    </ul>
    
    <!-- 空状态 -->
    <div v-if="activityList.length === 0" class="empty-state">
      <el-empty description="暂无活动" />
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>
  </main>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElIcon, ElEmpty, ElSkeleton } from 'element-plus';
import { Calendar, Location, User } from '@element-plus/icons-vue';
import router from '../router';

const user = JSON.parse(localStorage.getItem('user') || '{}');
const activityList = ref([]);
const userSignups = ref([]);
const isCertified = ref(false);
const loading = ref(true);

// 加载活动列表
const loadActivityList = async () => {
  loading.value = true;
  try {
    console.log('开始加载活动列表');
    const response = await axios.get('/api/activity/list');
    activityList.value = response.data;
    console.log('加载活动列表成功，响应数据:', activityList.value);
  } catch (error) {
    ElMessage.error('加载活动列表失败');
    console.error('加载活动列表失败，错误信息:', error);
  } finally {
    loading.value = false;
  }
};

// 加载用户活动报名列表
const loadUserSignups = async () => {
  if (!user.userId) return;
  try {
    console.log('开始加载用户活动报名列表，userId:', user.userId);
    const response = await axios.get(`/api/activity/user-signups/${user.userId}`);
    userSignups.value = response.data;
    console.log('加载用户活动报名列表成功，响应数据:', userSignups.value);
  } catch (error) {
    ElMessage.error('加载用户活动报名列表失败');
    console.error('加载用户活动报名列表失败，错误信息:', error);
  }
};

// 检查用户是否已认证房产
const checkUserCertified = async () => {
  if (!user.userId) return;
  try {
    const response = await axios.get('/api/resident-house/certified', {
      params: {
        userId: user.userId
      }
    });
    isCertified.value = response.data;
  } catch (error) {
    console.error('检查用户认证状态失败:', error);
  }
};

// 处理报名
const handleSignup = async (activityId) => {
  if (!user.userId) {
    ElMessage.error('请先登录');
    return;
  }
  
  try {
    await axios.post('/api/activity/signup', {}, {
      params: {
        activityId,
        userId: user.userId
      }
    });
    ElMessage.success('报名成功');
    // 重新加载活动列表和用户报名记录
    await loadActivityList();
    await loadUserSignups();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '报名失败');
    console.error(error);
  }
};

// 获取活动状态类型（根据时间动态判断）
const getActivityStatusType = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return 'info'; // 未开始
  } else if (now <= endTime) {
    return 'success'; // 进行中
  } else {
    return 'danger'; // 已结束
  }
};

// 获取活动状态文本（根据时间动态判断）
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

// 检查是否可以报名（根据时间动态判断）
const canSignup = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  // 活动未开始，不能报名
  if (now < startTime) {
    return false;
  }
  
  // 活动已结束，不能报名
  if (now > endTime) {
    return false;
  }
  
  // 活动名额已满，不能报名
  if (activity.quota - activity.enrolledCount <= 0) {
    return false;
  }
  
  // 用户已报名，不能重复报名
  if (userSignups.value.some(signup => signup.activityId === activity.id)) {
    return false;
  }
  
  // 用户未认证房产，不能报名
  if (!isCertified.value) {
    return false;
  }
  
  return true;
};

// 获取报名按钮文本（根据时间动态判断）
const getSignupButtonText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  // 活动未开始
  if (now < startTime) {
    return '报名未开始';
  }
  
  // 活动已结束
  if (now > endTime) {
    return '活动已结束';
  }
  
  // 活动名额已满
  if (activity.quota - activity.enrolledCount <= 0) {
    return '名额已满';
  }
  
  // 用户已报名
  if (userSignups.value.some(signup => signup.activityId === activity.id)) {
    return '已报名';
  }
  
  // 用户未认证房产
  if (!isCertified.value) {
    return '请先认证房产';
  }
  
  // 可以报名
  return '立即报名';
};

// 获取报名按钮类型
const getSignupButtonType = (activity) => {
  return canSignup(activity) ? 'primary' : 'info';
};

// 跳转到我的报名页面
const goToMySignups = () => {
  router.push('/my-signups');
};

// 处理卡片点击
const handleCardClick = (activity) => {
  // 这里可以添加跳转到活动详情页的逻辑
  console.log('点击活动:', activity.title);
};

// 初始化数据
onMounted(async () => {
  await loadActivityList();
  await loadUserSignups();
  await checkUserCertified();
});
</script>

<style scoped>
.activity-list-container {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.page-header h1 {
  font-size: 24px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.activity-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(350px, 1fr));
  gap: 24px;
  margin-top: 20px;
  list-style: none;
  padding: 0;
}

.activity-item {
  transition: transform 0.2s ease;
  outline: none;
}

.activity-item:focus {
  outline: 2px solid #409eff;
  border-radius: 4px;
  box-shadow: 0 0 0 2px rgba(64, 158, 255, 0.2);
}

.activity-item:hover {
  transform: translateY(-2px);
}

.activity-card {
  transition: all 0.3s ease;
  height: 100%;
  display: flex;
  flex-direction: column;
}

.activity-card:hover {
  box-shadow: 0 6px 16px rgba(0, 0, 0, 0.12);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 8px;
}

.activity-title {
  font-size: 18px;
  font-weight: bold;
  margin: 0;
  color: #333;
  flex: 1;
  margin-right: 12px;
  line-height: 1.4;
}

.activity-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
  flex: 1;
}

.activity-poster {
  width: 100%;
  height: 200px;
  overflow: hidden;
  border-radius: 4px;
  background-color: #f5f7fa;
}

.poster-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.activity-card:hover .poster-image {
  transform: scale(1.03);
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  flex: 1;
}

.activity-description {
  font-size: 14px;
  line-height: 1.5;
  color: #666;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 3;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
  margin-top: auto;
}

.meta-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #999;
}

.activity-actions {
  margin-top: 12px;
  display: flex;
  justify-content: flex-end;
}

.signup-button {
  min-width: 120px;
  height: 36px;
  font-size: 14px;
}

.empty-state {
  text-align: center;
  padding: 60px 20px;
  color: #909399;
}

.loading-state {
  margin-top: 20px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-list-container {
    padding: 16px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 12px;
  }
  
  .page-header h1 {
    font-size: 20px;
  }
  
  .activity-list {
    grid-template-columns: 1fr;
    gap: 16px;
  }
  
  .activity-poster {
    height: 180px;
  }
  
  .activity-title {
    font-size: 16px;
  }
  
  .activity-description {
    font-size: 13px;
    -webkit-line-clamp: 2;
  }
  
  .signup-button {
    min-width: 100px;
    height: 32px;
    font-size: 13px;
  }
}

@media (min-width: 769px) and (max-width: 1199px) {
  .activity-list {
    grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  }
}

/* 触摸目标优化 */
@media (hover: none) and (pointer: coarse) {
  .signup-button {
    min-width: 130px;
    height: 40px;
  }
  
  .activity-item {
    padding: 4px;
  }
}
</style>