<template>
  <div class="activity-management-container">
    <div class="page-header">
      <div class="header-content">
        <h2>活动管理</h2>
        <p class="page-description">发布、编辑和删除社区活动</p>
      </div>
      <el-button type="primary" @click="showAddDialog" size="large">
        <el-icon><Plus /></el-icon>
        发布新活动
      </el-button>
    </div>

    <!-- 活动列表 -->
    <el-card class="activity-list-card">
      <el-table 
        :data="activityList" 
        v-loading="loading"
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <el-table-column type="index" label="序号" width="60" align="center" />
        <el-table-column prop="title" label="活动标题" min-width="200" />
        <el-table-column prop="location" label="活动地点" width="150" />
        <el-table-column label="活动时间" width="200">
          <template #default="{ row }">
            <div class="time-range">
              <div>{{ formatDateTime(row.startTime) }}</div>
              <div style="font-size: 12px; color: #909399;">至</div>
              <div>{{ formatDateTime(row.endTime) }}</div>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="名额" width="100" align="center">
          <template #default="{ row }">
            <span>{{ row.enrolledCount || 0 }}/{{ row.quota }}</span>
          </template>
        </el-table-column>
        <el-table-column label="活动状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag :type="getActivityStatusType(row)">
              {{ getActivityStatusText(row) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200" fixed="right" align="center">
          <template #default="{ row }">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleEdit(row)"
              style="margin-right: 8px;"
            >
              编辑
            </el-button>
            <el-button 
              type="danger" 
              size="small" 
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 新增/编辑活动对话框 -->
    <el-dialog 
      v-model="dialogVisible" 
      :title="isEditMode ? '编辑活动' : '发布新活动'"
      width="800px"
      :close-on-click-modal="false"
    >
      <el-form 
        :model="activityForm" 
        :rules="rules" 
        ref="activityFormRef" 
        label-width="120px"
        class="activity-form"
      >
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
          <div class="poster-upload-container">
            <el-upload
              class="poster-uploader"
              action="/api/file/upload/activity"
              :show-file-list="false"
              :on-success="handlePosterSuccess"
              :before-upload="beforePosterUpload"
              accept="image/*"
            >
              <img v-if="activityForm.posterUrl" :src="activityForm.posterUrl" class="poster-image" />
              <el-icon v-else class="poster-uploader-icon">
                <Plus />
              </el-icon>
            </el-upload>
            <div class="poster-upload-tip">点击上传活动海报（支持 JPG、PNG 格式）</div>
          </div>
        </el-form-item>
        
        <div class="form-row">
          <el-form-item label="活动开始时间" prop="startTime" class="form-item-half">
            <el-date-picker
              v-model="activityForm.startTime"
              type="datetime"
              placeholder="请选择活动开始时间"
              style="width: 100%"
              size="large"
              value-format="YYYY-MM-DD HH:mm:ss"
            />
          </el-form-item>
          
          <el-form-item label="活动结束时间" prop="endTime" class="form-item-half">
            <el-date-picker
              v-model="activityForm.endTime"
              type="datetime"
              placeholder="请选择活动结束时间"
              style="width: 100%"
              size="large"
              value-format="YYYY-MM-DD HH:mm:ss"
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
      </el-form>
      
      <template #footer>
        <div class="dialog-footer">
          <el-button @click="dialogVisible = false" size="large">取消</el-button>
          <el-button type="primary" @click="handleSubmit" size="large" :loading="submitting">
            {{ isEditMode ? '保存修改' : '发布活动' }}
          </el-button>
        </div>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Plus } from '@element-plus/icons-vue';

const activityList = ref([]);
const loading = ref(false);
const dialogVisible = ref(false);
const isEditMode = ref(false);
const submitting = ref(false);

const activityForm = ref({
  id: null,
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
    { type: 'number', min: 1, message: '活动名额至少为 1', trigger: 'blur' }
  ]
};

const activityFormRef = ref(null);

// 上传海报图片前的检查
const beforePosterUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt5M = file.size / 1024 / 1024 < 5;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
  }
  if (!isLt5M) {
    ElMessage.error('图片大小不能超过 5MB!');
  }
  return isImage && isLt5M;
};

// 上传海报图片成功后的处理
const handlePosterSuccess = (response, uploadFile) => {
  if (response.code === 0 || response.success) {
    // 根据返回格式获取图片 URL
    activityForm.value.posterUrl = response.data?.url || response.url || response;
    ElMessage.success('上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

// 获取活动列表
const loadActivityList = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/activity/list');
    activityList.value = response.data || [];
  } catch (error) {
    console.error('获取活动列表失败:', error);
    ElMessage.error('获取活动列表失败');
  } finally {
    loading.value = false;
  }
};

// 显示新增对话框
const showAddDialog = () => {
  isEditMode.value = false;
  activityForm.value = {
    id: null,
    title: '',
    description: '',
    posterUrl: '',
    startTime: '',
    endTime: '',
    location: '',
    quota: 50
  };
  dialogVisible.value = true;
};

// 编辑活动
const handleEdit = (row) => {
  isEditMode.value = true;
  activityForm.value = {
    id: row.id,
    title: row.title,
    description: row.description,
    posterUrl: row.posterUrl || '',
    startTime: row.startTime,
    endTime: row.endTime,
    location: row.location,
    quota: row.quota
  };
  dialogVisible.value = true;
};

// 删除活动
const handleDelete = (row) => {
  ElMessageBox.confirm(
    `确定要删除活动"${row.title}"吗？删除后无法恢复。`,
    '删除确认',
    {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning',
    }
  ).then(async () => {
    try {
      await axios.delete(`/api/activity/${row.id}`);
      ElMessage.success('删除成功');
      loadActivityList();
    } catch (error) {
      console.error('删除失败:', error);
      ElMessage.error('删除失败');
    }
  }).catch(() => {});
};

// 提交表单
const handleSubmit = async () => {
  if (!activityFormRef.value) return;
  
  await activityFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        if (isEditMode.value) {
          // 更新活动
          await axios.put('/api/activity', activityForm.value);
          ElMessage.success('修改成功');
        } else {
          // 发布新活动
          await axios.post('/api/activity/publish', activityForm.value);
          ElMessage.success('发布成功');
        }
        dialogVisible.value = false;
        loadActivityList();
      } catch (error) {
        console.error('操作失败:', error);
        ElMessage.error(isEditMode.value ? '修改失败' : '发布失败');
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 获取活动状态类型
const getActivityStatusType = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return 'info'; // 未开始
  } else if (now >= startTime && now <= endTime) {
    return 'success'; // 进行中
  } else {
    return 'danger'; // 已结束
  }
};

// 获取活动状态文本
const getActivityStatusText = (activity) => {
  const now = new Date();
  const startTime = new Date(activity.startTime);
  const endTime = new Date(activity.endTime);
  
  if (now < startTime) {
    return '未开始';
  } else if (now >= startTime && now <= endTime) {
    return '进行中';
  } else {
    return '已结束';
  }
};

// 格式化日期时间
const formatDateTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

onMounted(() => {
  loadActivityList();
});
</script>

<style scoped>
.activity-management-container {
  padding: 30px;
  max-width: 1400px;
  margin: 0 auto;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 30px;
}

.header-content h2 {
  font-size: 28px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 8px;
}

.page-description {
  font-size: 16px;
  color: #606266;
  margin: 0;
}

.activity-list-card {
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.time-range {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 4px;
}

.activity-form {
  padding: 10px;
}

.form-row {
  display: flex;
  gap: 20px;
  margin-bottom: 24px;
}

.form-item-half {
  flex: 1;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding-top: 10px;
}

/* 海报上传样式 */
.poster-upload-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
}

.poster-uploader {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: all 0.3s;
}

.poster-uploader:hover {
  border-color: #409EFF;
}

.poster-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 178px;
  height: 178px;
  text-align: center;
  line-height: 178px;
}

.poster-image {
  width: 178px;
  height: 178px;
  display: block;
  object-fit: cover;
}

.poster-upload-tip {
  font-size: 12px;
  color: #909399;
  text-align: center;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .activity-management-container {
    padding: 20px;
  }
  
  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 16px;
  }
  
  .form-row {
    flex-direction: column;
    gap: 0;
  }
}
</style>
