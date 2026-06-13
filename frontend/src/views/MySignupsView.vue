<template>
  <div class="my-signups-container">
    <h2>我的活动报名</h2>
    
    <!-- 报名列表 -->
    <div class="signup-list">
      <el-card 
        v-for="signup in userSignups" 
        :key="signup.id"
        class="signup-card"
      >
        <template #header>
          <div class="card-header">
            <span class="activity-title">{{ getActivityTitle(signup.activityId) }}</span>
            <el-tag :type="getSignupStatusType(signup.status)">
              {{ getSignupStatusText(signup.status) }}
            </el-tag>
          </div>
        </template>
        
        <div class="signup-content">
          <!-- 活动信息 -->
          <div class="activity-info">
            <div class="info-item">
              <span class="label">活动时间：</span>
              <span class="value">{{ formatDateTime(getActivityTime(signup.activityId)) }}</span>
            </div>
            <div class="info-item">
              <span class="label">活动地点：</span>
              <span class="value">{{ getActivityLocation(signup.activityId) }}</span>
            </div>
            <div class="info-item">
              <span class="label">报名时间：</span>
              <span class="value">{{ formatDateTime(signup.signupTime) }}</span>
            </div>
          </div>
          
          <!-- 操作按钮 -->
          <div class="signup-actions">
            <el-button 
              type="primary" 
              @click="viewActivityDetail(signup.activityId)"
              size="small"
            >
              查看详情
            </el-button>
            <el-button 
              type="success" 
              @click="uploadPhoto(signup.activityId)"
              size="small"
              v-if="canUploadPhoto(signup.activityId)"
            >
              上传照片
            </el-button>
            <el-button 
              type="info" 
              @click="addComment(signup.activityId)"
              size="small"
              v-if="canAddComment(signup.activityId)"
            >
              评价活动
            </el-button>
          </div>
        </div>
      </el-card>
    </div>
    
    <!-- 空状态 -->
    <div v-if="userSignups.length === 0" class="empty-state">
      <el-empty description="您还没有报名任何活动" />
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElEmpty } from 'element-plus';
import router from '../router';

const user = JSON.parse(localStorage.getItem('user') || '{}');
const userSignups = ref([]);
const activityMap = ref(new Map());

// 加载用户活动报名列表
const loadUserSignups = async () => {
  if (!user.userId) {
    ElMessage.error('请先登录');
    return;
  }
  
  try {
    console.log('开始加载用户活动报名列表，userId:', user.userId);
    const response = await axios.get(`/api/activity/user-signups/${user.userId}`);
    userSignups.value = response.data;
    console.log('加载用户活动报名列表成功，响应数据:', userSignups.value);
    
    // 加载活动详情
    await loadActivityDetails();
  } catch (error) {
    ElMessage.error('加载用户活动报名列表失败');
    console.error('加载用户活动报名列表失败，错误信息:', error);
  }
};

// 加载活动详情
const loadActivityDetails = async () => {
  if (userSignups.value.length === 0) return;
  
  try {
    // 获取所有活动
    const response = await axios.get('/api/activity/list');
    const activities = response.data;
    
    // 构建活动映射，确保ID为Number类型
    activities.forEach(activity => {
      activityMap.value.set(Number(activity.id), activity);
    });
    
    console.log('活动映射已构建:', activityMap.value);
  } catch (error) {
    console.error('加载活动详情失败:', error);
  }
};

// 获取活动标题
const getActivityTitle = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  return activity ? activity.title : '活动信息加载中...';
};

// 获取活动时间
const getActivityTime = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  return activity ? activity.startTime : '';
};

// 获取活动地点
const getActivityLocation = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  return activity ? activity.location : '';
};

// 获取报名状态类型
const getSignupStatusType = (status) => {
  switch (status) {
    case 'confirmed':
      return 'success';
    case 'pending':
      return 'warning';
    case 'cancelled':
      return 'danger';
    default:
      return 'default';
  }
};

// 获取报名状态文本
const getSignupStatusText = (status) => {
  switch (status) {
    case 'confirmed':
      return '已确认';
    case 'pending':
      return '待确认';
    case 'cancelled':
      return '已取消';
    default:
      return '未知状态';
  }
};

// 格式化日期时间
const formatDateTime = (dateTime) => {
  if (!dateTime) return '';
  const date = new Date(dateTime);
  return date.toLocaleString();
};

// 查看活动详情
const viewActivityDetail = (activityId) => {
  router.push(`/activity/detail/${activityId}`);
};

// 上传照片
const uploadPhoto = (activityId) => {
  // 这里可以打开照片上传对话框
  ElMessage.info('照片上传功能开发中');
};

// 评价活动
const addComment = (activityId) => {
  // 这里可以打开评价对话框
  ElMessage.info('活动评价功能开发中');
};

// 检查是否可以上传照片
const canUploadPhoto = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  if (!activity) return false;
  // 活动已结束才能上传照片
  return activity.status === 2;
};

// 检查是否可以评价活动
const canAddComment = (activityId) => {
  const activity = activityMap.value.get(Number(activityId));
  if (!activity) return false;
  // 活动已结束才能评价
  return activity.status === 2;
};

// 初始化数据
onMounted(async () => {
  await loadUserSignups();
});
</script>

<style scoped>
.my-signups-container {
  padding: 20px;
}

.signup-list {
  margin-top: 20px;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.signup-card {
  transition: all 0.3s ease;
}

.signup-card:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.activity-title {
  font-size: 16px;
  font-weight: bold;
}

.signup-content {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-info {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.info-item {
  display: flex;
  align-items: center;
}

.label {
  font-weight: bold;
  margin-right: 8px;
  min-width: 80px;
}

.value {
  color: #666;
}

.signup-actions {
  display: flex;
  gap: 8px;
  justify-content: flex-end;
}

.empty-state {
  margin-top: 40px;
  text-align: center;
}

@media (max-width: 768px) {
  .info-item {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .label {
    min-width: auto;
  }
  
  .signup-actions {
    flex-direction: column;
  }
}
</style>
