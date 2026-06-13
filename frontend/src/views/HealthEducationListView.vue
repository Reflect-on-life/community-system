<template>
  <div class="health-education-list-container">
    <div class="page-header">
      <h2>健康宣教</h2>
      <el-button v-if="isDoctor" type="primary" @click="openPublishDialog">
        <el-icon><Plus /></el-icon>
        发布宣教
      </el-button>
    </div>
    
    <!-- 标签页切换 -->
    <el-tabs v-model="activeTab" @tab-change="handleTabChange" v-if="isDoctor">
      <el-tab-pane label="全部健康宣教" name="all" />
      <el-tab-pane label="我的发布" name="my" />
    </el-tabs>
    
    <div v-if="loading" class="loading-container">
      <el-skeleton :rows="3" animated />
    </div>
    
    <div v-else-if="educations.length === 0" class="empty-container">
      <el-empty description="暂无健康宣教" />
    </div>
    
    <div v-else class="education-list">
      <el-card v-for="education in educations" :key="education.id" class="education-card" shadow="hover">
        <template #header>
          <div class="card-header">
            <span class="education-title">{{ education.title }}</span>
            <div class="header-right">
              <span class="create-time">
                <el-icon><Clock /></el-icon>
                {{ formatTime(education.createTime) }}
              </span>
              <div v-if="isDoctor && education.creatorId === currentUserId" class="action-buttons">
                <el-button size="small" @click="handleEdit(education)">
                  <el-icon><Edit /></el-icon>
                  编辑
                </el-button>
                <el-button size="small" type="danger" @click="handleDelete(education.id)">
                  <el-icon><Delete /></el-icon>
                  删除
                </el-button>
              </div>
            </div>
          </div>
        </template>
        
        <div class="education-content">
          <p v-if="!education.expanded" class="content-preview">
            {{ formatContent(education.content, 200) }}
            <span v-if="education.content && education.content.length > 200" class="expand-btn" @click.stop="education.expanded = true">
              展开
            </span>
          </p>
          <p v-else class="content-full">
            {{ formatContentWithLineBreaks(education.content) }}
            <span class="collapse-btn" @click.stop="education.expanded = false">
              收起
            </span>
          </p>
        </div>
        
        <div class="education-footer">
          <span class="creator-info">
            <el-icon><User /></el-icon>
            发布者: {{ education.creatorName || '医生' }}
          </span>
        </div>
      </el-card>
    </div>
    
    <!-- 发布宣教对话框 -->
    <el-dialog
      v-model="publishDialogVisible"
      title="发布健康宣教"
      width="600px"
    >
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="100px">
        <el-form-item label="宣教标题" prop="title">
          <el-input
            v-model="publishForm.title"
            placeholder="请输入宣教标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="宣教内容" prop="content">
          <el-input
            v-model="publishForm.content"
            type="textarea"
            placeholder="请输入宣教内容"
            maxlength="5000"
            show-word-limit
            :rows="8"
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

    <!-- 编辑模态框 -->
    <el-dialog
      v-model="dialogVisible"
      title="编辑健康宣教"
      width="600px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="100px">
        <el-form-item label="宣教标题" prop="title">
          <el-input
            v-model="editForm.title"
            placeholder="请输入宣教标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="宣教内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            placeholder="请输入宣教内容"
            maxlength="5000"
            show-word-limit
            :rows="8"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEdit">保存修改</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Clock, User, Edit, Delete, Plus } from '@element-plus/icons-vue';

const educations = ref([]);
const loading = ref(false);
const creatorCache = ref({});
const activeTab = ref('all');
const dialogVisible = ref(false);
const editFormRef = ref(null);
const editForm = ref({
  id: '',
  title: '',
  content: ''
});
const rules = {
  title: [{ required: true, message: '请输入宣教标题', trigger: 'blur' }],
  content: [{ required: true, message: '请输入宣教内容', trigger: 'blur' }]
};

// 发布相关
const publishDialogVisible = ref(false);
const publishFormRef = ref(null);
const submitting = ref(false);
const publishForm = ref({
  title: '',
  content: ''
});
const publishRules = {
  title: [
    { required: true, message: '请输入宣教标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入宣教内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度在 10 到 5000 个字符', trigger: 'blur' }
  ]
};

// 从localStorage获取用户信息
const currentUser = computed(() => {
  const userStr = localStorage.getItem('user');
  return userStr ? JSON.parse(userStr) : null;
});

const isDoctor = computed(() => {
  return currentUser.value && currentUser.value.roles && currentUser.value.roles.includes('ROLE_DOCTOR');
});

const currentUserId = computed(() => {
  return currentUser.value ? currentUser.value.id : null;
});

// 打开发布对话框
const openPublishDialog = () => {
  publishForm.value = {
    title: '',
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
    
    await axios.post('/api/health/education', publishForm.value);
    
    ElMessage.success('发布成功');
    publishDialogVisible.value = false;
    fetchEducationList();
  } catch (error) {
    ElMessage.error('发布失败');
    console.error('发布健康宣教失败:', error);
  } finally {
    submitting.value = false;
  }
};

const fetchEducationList = async () => {
  loading.value = true;
  try {
    let response;
    if (activeTab.value === 'my' && isDoctor.value) {
            response = await axios.get('/api/health/education/doctor/list');
            if (response.data.success) {
              const data = response.data.data || [];
              educations.value = await Promise.all(data.map(async (item) => {
                const creatorName = await getCreatorName(item.creatorId);
                return {
                  ...item,
                  expanded: false,
                  creatorName
                };
              }));
            } else {
              ElMessage.error(response.data.message || '获取我的发布失败');
              educations.value = [];
            }
          } else {
                response = await axios.get('/api/health/education/list');
                if (response.data.success) {
                  const data = response.data.data || [];
                  educations.value = await Promise.all(data.map(async (item) => {
                    const creatorName = await getCreatorName(item.creatorId);
                    return {
                      ...item,
                      expanded: false,
                      creatorName
                    };
                  }));
                } else {
                  ElMessage.error(response.data.message || '获取健康宣教失败');
                  educations.value = [];
                }
              }
  } catch (error) {
    ElMessage.error('获取健康宣教失败');
    console.error('获取健康宣教失败:', error);
    
    // 为模拟数据添加发布者姓名
    const mockData = [
      {
        id: 1,
        title: '春季养生指南',
        content: '春季是万物复苏的季节，要注意保暖防寒。春季气温变化大，要注意增减衣物。建议多进行户外活动，增强体质。饮食上要注意清淡，多吃蔬菜水果。',
        createTime: new Date('2026-02-19 10:00:00'),
        creatorId: 3,
        expanded: false,
        creatorName: '李医生'
      },
      {
        id: 2,
        title: '高血压患者日常护理',
        content: '高血压患者要定期测量血压，按时服药。建议每天早晚各测量一次血压，并做好记录。饮食上要低盐低脂，多吃富含钾的食物。保持良好的作息习惯，避免过度劳累。',
        createTime: new Date('2026-02-18 15:30:00'),
        creatorId: 5,
        expanded: false,
        creatorName: '王医生'
      },
      {
        id: 3,
        title: '糖尿病饮食建议',
        content: '糖尿病患者应控制碳水化合物摄入，多吃蔬菜水果，少吃高糖食物。建议每天分餐进食，避免暴饮暴食。选择低升糖指数的食物，如全麦面包、燕麦等。定期监测血糖水平。',
        createTime: new Date('2026-02-17 09:00:00'),
        creatorId: 6,
        expanded: false,
        creatorName: '张医生'
      }
    ];
    
    educations.value = mockData;
  } finally {
    loading.value = false;
  }
};

const handleTabChange = () => {
  fetchEducationList();
};

const handleEdit = (education) => {
  editForm.value = {
    id: education.id,
    title: education.title,
    content: education.content
  };
  dialogVisible.value = true;
};

const submitEdit = async () => {
  if (!editFormRef.value) return;
  
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const response = await axios.put(`/api/health/education/${editForm.value.id}`, {
          title: editForm.value.title,
          content: editForm.value.content
        });
        
        if (response.data.success) {
          ElMessage.success('修改成功');
          dialogVisible.value = false;
          fetchEducationList();
        } else {
          ElMessage.error(response.data.message || '修改失败');
        }
      } catch (error) {
        ElMessage.error('修改失败');
        console.error('修改失败:', error);
      }
    }
  });
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条健康宣教吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    const response = await axios.delete(`/api/health/education/${id}`);
    
    if (response.data.success) {
      ElMessage.success('删除成功');
      fetchEducationList();
    } else {
      ElMessage.error(response.data.message || '删除失败');
    }
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

const getCreatorName = async (creatorId) => {
  if (!creatorId) return '未知';
  if (creatorCache.value[creatorId]) {
    return creatorCache.value[creatorId];
  }
  
  try {
    const response = await axios.get(`/api/user/${creatorId}`);
    if (response.data.success && response.data.data) {
      const name = response.data.data.name || '医生';
      creatorCache.value[creatorId] = name;
      return name;
    }
  } catch (error) {
    console.error('获取发布人信息失败:', error);
  }
  return '医生';
};

// 预加载所有发布者的姓名
const preloadCreatorNames = async () => {
  const creatorIds = [...new Set(educations.value.map(item => item.creatorId).filter(Boolean))];
  for (const id of creatorIds) {
    if (!creatorCache.value[id]) {
      await getCreatorName(id);
    }
  }
};

const formatContent = (content, maxLength) => {
  if (!content) return '';
  if (content.length <= maxLength) return content;
  return content.substring(0, maxLength) + '...';
};

const formatContentWithLineBreaks = (content) => {
  if (!content) return '';
  return content;
};

onMounted(() => {
  fetchEducationList();
});
</script>

<style scoped>
.health-education-list-container {
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

.education-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(500px, 1fr));
  gap: 20px;
}

.education-card {
  transition: all 0.3s;
}

.education-card:hover {
  transform: translateY(-5px);
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  flex-wrap: wrap;
  gap: 10px;
}

.education-title {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  flex: 1;
  min-width: 200px;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 15px;
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

.education-content {
  margin: 15px 0;
  min-height: 80px;
  position: relative;
}

.education-content p {
  color: #666;
  line-height: 1.8;
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}

.content-preview {
  display: -webkit-box;
  -webkit-line-clamp: 4;
  -webkit-box-orient: vertical;
  overflow: hidden;
  position: relative;
}

.content-full {
  white-space: pre-wrap;
  word-wrap: break-word;
}

.expand-btn, .collapse-btn {
  color: #409EFF;
  cursor: pointer;
  margin-left: 5px;
  font-size: 14px;
}

.expand-btn:hover, .collapse-btn:hover {
  text-decoration: underline;
}

.education-footer {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  padding-top: 10px;
  border-top: 1px solid #eee;
}

.creator-info {
  color: #999;
  font-size: 12px;
  display: flex;
  align-items: center;
  gap: 5px;
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
  
  .education-list {
    grid-template-columns: 1fr;
  }

  .page-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 10px;
  }
}
</style>
