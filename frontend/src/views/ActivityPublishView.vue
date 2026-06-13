<template>
  <div class="activity-publish-container">
    <div class="page-header">
      <h2>发布社区活动</h2>
      <p class="page-description">创建新的社区活动，丰富居民的业余生活</p>
    </div>
    
    <!-- 活动发布表单 -->
    <el-card class="publish-card">
      <el-form :model="activityForm" :rules="rules" ref="activityFormRef" label-width="120px" class="activity-form">
        <el-form-item label="活动标题" prop="title">
          <el-input v-model="activityForm.title" placeholder="请输入活动标题" size="large" />
        </el-form-item>
        
        <el-form-item label="活动描述" prop="description">
          <el-input
            v-model="activityForm.description"
            type="textarea"
            :rows="5"
            placeholder="请输入活动描述，包括活动内容、流程、注意事项等"
            size="large"
          />
        </el-form-item>
        
        <el-form-item label="活动海报" prop="posterUrl">
          <el-input v-model="activityForm.posterUrl" placeholder="请输入活动海报URL" size="large" />
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="活动开始时间" prop="startTime" class="form-item-half">
            <el-date-picker
              v-model="activityForm.startTime"
              type="datetime"
              placeholder="请选择活动开始时间"
              style="width: 100%"
              size="large"
            />
          </el-form-item>
          
          <el-form-item label="活动结束时间" prop="endTime" class="form-item-half">
            <el-date-picker
              v-model="activityForm.endTime"
              type="datetime"
              placeholder="请选择活动结束时间"
              style="width: 100%"
              size="large"
            />
          </el-form-item>
        </div>
        
        <el-form-item label="活动地点" prop="location">
          <el-input v-model="activityForm.location" placeholder="请输入活动地点" size="large" />
        </el-form-item>
        
        <el-form-item label="活动名额" prop="quota">
          <el-input-number
            v-model="activityForm.quota"
            :min="1"
            :max="1000"
            :step="1"
            placeholder="请输入活动名额"
            style="width: 200px"
            size="large"
          />
        </el-form-item>
        
        <el-form-item class="form-actions">
          <el-button type="primary" @click="handlePublish" size="large" :loading="publishing">
            <el-icon><Check /></el-icon>
            发布活动
          </el-button>
          <el-button @click="activityFormRef.resetFields()" size="large">
            <el-icon><Refresh /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Ticket, Check, Refresh } from '@element-plus/icons-vue';

const activityForm = ref({
  title: '',
  description: '',
  posterUrl: '',
  startTime: '',
  endTime: '',
  location: '',
  quota: 50
});

const rules = {
  title: [
    { required: true, message: '请输入活动标题', trigger: 'blur' }
  ],
  description: [
    { required: true, message: '请输入活动描述', trigger: 'blur' }
  ],
  startTime: [
    { required: true, message: '请选择活动开始时间', trigger: 'change' }
  ],
  endTime: [
    { required: true, message: '请选择活动结束时间', trigger: 'change' }
  ],
  location: [
    { required: true, message: '请输入活动地点', trigger: 'blur' }
  ],
  quota: [
    { required: true, message: '请输入活动名额', trigger: 'blur' },
    { type: 'number', min: 1, message: '活动名额至少为1', trigger: 'blur' }
  ]
};

const activityFormRef = ref(null);
const publishing = ref(false);

// 发布活动
const handlePublish = async () => {
  await activityFormRef.value.validate(async (valid) => {
    if (valid) {
      publishing.value = true;
      try {
        await axios.post('/api/activity/publish', activityForm.value);
        ElMessage.success('发布活动成功');
        // 重置表单
        activityFormRef.value.resetFields();
      } catch (error) {
        ElMessage.error('发布活动失败');
        console.error(error);
      } finally {
        publishing.value = false;
      }
    }
  });
};
</script>

<style scoped>
.activity-publish-container {
  padding: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.publish-card {
  margin-top: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.activity-form {
  padding: 30px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.form-item-half {
  flex: 1;
}

.form-actions {
  display: flex;
  justify-content: center;
  gap: 20px;
  margin-top: 40px;
  padding-top: 20px;
  border-top: 1px solid #ebeef5;
}

.form-actions .el-button {
  min-width: 150px;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-publish-container {
    padding: 20px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
  
  .form-actions {
    flex-direction: column;
    align-items: center;
    gap: 16px;
  }
  
  .form-actions .el-button {
    width: 200px;
  }
  
  .activity-form {
    padding: 20px;
  }
  
  .card-header {
    padding: 12px 16px;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
}

/* 适老模式样式 */
.elder-mode :deep(.activity-form) {
  font-size: 18px;
}

.elder-mode :deep(.el-form-item__label) {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}

.elder-mode :deep(.el-input__inner) {
  font-size: 20px;
  height: 50px;
  padding: 0 16px;
}

.elder-mode :deep(.el-textarea__inner) {
  font-size: 20px;
  min-height: 180px;
  padding: 16px;
}

.elder-mode :deep(.el-date-picker__input) {
  font-size: 20px;
  height: 50px;
}

.elder-mode :deep(.el-input-number) {
  font-size: 20px;
}

.elder-mode :deep(.el-input-number__decrease),
.elder-mode :deep(.el-input-number__increase) {
  font-size: 20px;
  width: 40px;
}

.elder-mode :deep(.el-button) {
  font-size: 22px;
  padding: 16px 32px;
  min-width: 180px;
}

.elder-mode :deep(.page-header h2) {
  font-size: 32px;
  color: #000000;
}

.elder-mode :deep(.page-description) {
  font-size: 20px;
  color: #000000;
}
</style>