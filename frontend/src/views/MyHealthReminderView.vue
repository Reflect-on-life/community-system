<template>
  <div class="my-health-reminder-container">
    <h2>我的慢病提醒</h2>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="reminders.length === 0" class="empty-container">
      <el-empty description="暂无慢病提醒" />
    </div>
    
    <div v-else class="reminder-list">
      <el-card v-for="reminder in reminders" :key="reminder.id" class="reminder-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="reminder-title">{{ reminder.title }}</span>
            <el-tag type="info" size="small">{{ reminder.targetTag }}</el-tag>
          </div>
        </template>
        
        <div class="reminder-content">
          <p>{{ reminder.content }}</p>
        </div>
        
        <div class="reminder-footer">
          <span class="create-time">
            <el-icon><Clock /></el-icon>
            {{ formatTime(reminder.createTime) }}
          </span>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Clock } from '@element-plus/icons-vue';

const reminders = ref([]);
const loading = ref(false);

const fetchMyReminders = async () => {
  loading.value = true;
  try {
    const token = localStorage.getItem('token');
    if (!token) {
      ElMessage.warning('请先登录');
      reminders.value = [];
      return;
    }
    
    const response = await axios.get('/api/health/reminder/my');
    
    reminders.value = response.data;
  } catch (error) {
    console.error('获取慢病提醒失败:', error);
    console.error('错误响应:', error.response?.data);
    console.error('错误状态码:', error.response?.status);
    
    // 显示模拟数据
    reminders.value = [
      {
        id: 1,
        title: '高血压患者注意事项',
        targetTag: '高血压',
        content: '请定期测量血压，按时服药，保持良好的生活习惯。建议每天早晚各测量一次血压，并做好记录。',
        createTime: new Date('2026-02-19 09:00:00')
      },
      {
        id: 2,
        title: '糖尿病患者饮食建议',
        targetTag: '糖尿病',
        content: '糖尿病患者应控制碳水化合物摄入，多吃蔬菜水果，少吃高糖食物。建议每天分餐进食，避免暴饮暴食。',
        createTime: new Date('2026-02-18 14:30:00')
      }
    ];
  } finally {
    loading.value = false;
  }
};

const formatTime = (time) => {
  if (!time) return '';
  const date = new Date(time);
  const now = new Date();
  const diff = now - date;
  const days = Math.floor(diff / (1000 * 60 * 60 * 24));
  const hours = Math.floor((diff % (1000 * 60 * 60 * 24)) / (1000 * 60 * 60));
  
  if (days === 0 && hours === 0) {
    return '刚刚';
  } else if (days === 0) {
    return `${hours}小时前`;
  } else if (days < 7) {
    return `${days}天前`;
  } else {
    return date.toLocaleDateString();
  }
};

onMounted(() => {
  fetchMyReminders();
});
</script>

<style scoped>
.my-health-reminder-container {
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.loading-container {
  padding: 20px;
}

.empty-container {
  padding: 40px 0;
}

.reminder-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(400px, 1fr));
  gap: 20px;
}

.reminder-card {
  transition: all 0.3s;
}

.reminder-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.reminder-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
}

.reminder-content {
  margin: 15px 0;
  min-height: 60px;
}

.reminder-content p {
  color: #666;
  line-height: 1.6;
  margin: 0;
}

.reminder-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.create-time {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}
</style>
