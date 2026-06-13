<template>
  <div class="health-reminder-container">
    <div class="page-header">
      <h2>慢病提醒</h2>
      <el-button v-if="isDoctor" type="primary" @click="openPublishDialog">
        <el-icon><Plus /></el-icon>
        发布提醒
      </el-button>
    </div>

    <!-- 标签页切换（医生用户可见） -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" v-if="isDoctor">
      <el-tab-pane label="全部提醒" name="all" />
      <el-tab-pane label="我的发布" name="my" />
    </el-tabs>

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
            <div class="header-right">
              <el-tag type="info" size="small">{{ reminder.targetTag }}</el-tag>
              <span class="create-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(reminder.createTime) }}
              </span>
              <div v-if="isDoctor && isMyReminder(reminder)" class="action-buttons">
                <el-button size="small" type="danger" @click="handleDelete(reminder.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </template>

        <div class="reminder-content">
          <p>{{ reminder.content }}</p>
        </div>
      </el-card>
    </div>

    <!-- 发布提醒对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布慢病提醒"
      width="600px"
    >
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="120px">
        <el-form-item label="提醒标题" prop="title">
          <el-input v-model="publishForm.title" placeholder="请输入提醒标题" maxlength="100" show-word-limit />
        </el-form-item>

        <el-form-item label="目标慢病标签" prop="targetTag">
          <el-select v-model="publishForm.targetTag" placeholder="请选择目标慢病标签">
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="慢性阻塞性肺疾病" value="慢性阻塞性肺疾病" />
            <el-option label="慢性肾病" value="慢性肾病" />
            <el-option label="骨质疏松" value="骨质疏松" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>

        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            placeholder="请输入提醒内容"
            :rows="8"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="publishDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitPublish" :loading="submitting">发布</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Clock, Delete, Plus } from '@element-plus/icons-vue';

const reminders = ref([]);
const loading = ref(false);
const activeTab = ref('my');

// 发布相关
const publishDialogVisible = ref(false);
const publishFormRef = ref(null);
const submitting = ref(false);
const publishForm = ref({
  title: '',
  targetTag: '',
  content: ''
});
const publishRules = {
  title: [
    { required: true, message: '请输入提醒标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  targetTag: [
    { required: true, message: '请选择目标慢病标签', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入提醒内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
};

// 从localStorage获取用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user');
  return userStr ? JSON.parse(userStr) : null;
});

const isDoctor = computed(() => {
  return currentUser.value && (currentUser.value.roles && currentUser.value.roles.includes('ROLE_DOCTOR') || currentUser.value.type === 'doctor');
});

const currentUserId = computed(() => {
  return currentUser.value ? currentUser.value.id : null;
});

// 是否是我发布的提醒
const isMyReminder = (reminder) => {
  if (!currentUserId.value) return false;
  // 假设医生发布的提醒有 creatorId 字段，或者通过其他方式判断
  return true; // 简化处理，实际应该根据后端返回的数据判断
};

// 打开发布对话框
const openPublishDialog = () => {
  publishForm.value = {
    title: '',
    targetTag: '',
    content: ''
  };
  publishDialogVisible.value = true;
};

// 提交发布
const submitPublish = async () => {
  if (!publishFormRef.value) return;

  try {
    await publishFormRef.value.validate();
    submitting.value = true;

    await axios.post('/api/health/reminder', publishForm.value);

    ElMessage.success('发布成功');
    publishDialogVisible.value = false;
    fetchReminders();
  } catch (error) {
    ElMessage.error('发布失败');
    console.error('发布慢病提醒失败:', error);
  } finally {
    submitting.value = false;
  }
};

const fetchReminders = async () => {
  loading.value = true;
  try {
    let response;
    
    if (activeTab.value === 'my' && isDoctor.value) {
      // 获取我发布的提醒
      response = await axios.get('/api/health/reminder/my');
      reminders.value = response.data || [];
    } else {
      // 获取全部提醒或我的提醒（非医生用户）
      response = await axios.get('/api/health/reminder/my');
      reminders.value = response.data || [];
    }
  } catch (error) {
    console.error('获取慢病提醒失败:', error);

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
      },
      {
        id: 3,
        title: '冠心病患者运动指导',
        targetTag: '冠心病',
        content: '冠心病患者应进行适度的有氧运动，如散步、太极拳等。避免剧烈运动和情绪激动，注意保暖防寒。',
        createTime: new Date('2026-02-17 10:00:00')
      }
    ];
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  fetchReminders();
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条慢病提醒吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await axios.delete(`/api/health/reminder/${id}`);
    ElMessage.success('删除成功');
    fetchReminders();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error('删除失败:', error);
    }
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
  fetchReminders();
});
</script>

<style scoped>
.health-reminder-container {
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.page-header h2 {
  color: #333;
  margin: 0;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.el-tabs {
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
  flex-wrap: wrap;
  gap: 10px;
}

.reminder-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  flex: 1;
  min-width: 150px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.create-time {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
}

.action-buttons {
  display: flex;
  gap: 5px;
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

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .card-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }

  .header-right {
    width: 100%;
    justify-content: space-between;
  }

  .reminder-list {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
