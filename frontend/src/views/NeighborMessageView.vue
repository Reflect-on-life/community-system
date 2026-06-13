<template>
  <main class="neighbor-message-container" aria-label="邻里交流">
    <header class="page-header">
      <div class="header-left">
        <h1>邻里交流</h1>
        <el-tabs v-model="activeTab" @tab-change="handleTabChange" class="message-tabs">
          <el-tab-pane label="全部消息" name="all" />
          <el-tab-pane label="我的发布" name="mine" />
        </el-tabs>
      </div>
      <el-button
        v-if="canPublish"
        type="primary"
        @click="showPublishDialog = true"
        aria-label="发布新帖"
      >
        发布新帖
      </el-button>
    </header>
    
    <!-- 消息列表 -->
    <ul class="message-list" aria-label="邻里消息列表">
      <li 
        v-for="message in messageList" 
        :key="message.id"
        class="message-item"
      >
        <el-card class="message-card" shadow="hover">
          <div class="card-content" @click="goToDetail(message.id)" style="cursor: pointer;">
            <div class="user-info">
              <el-avatar :size="40" :src="message.avatar || ''" aria-hidden="true">
                {{ message.authorName || message.userName ? (message.authorName || message.userName).charAt(0) : 'U' }}
              </el-avatar>
              <div class="user-details">
                <span class="user-name">{{ message.authorName || message.userName }}</span>
                <span class="publish-time">{{ formatTime(message.createTime || message.createdAt) }}</span>
              </div>
            </div>
            
            <h3 class="message-title">{{ message.title }}</h3>
            <p class="message-content">{{ message.content }}</p>
            
            <div class="message-stats">
              <span class="stat-item">
                <el-icon aria-hidden="true"><ChatDotRound /></el-icon>
                {{ message.replyCount || 0 }} 评论
              </span>
              <span class="stat-item">
                <el-icon aria-hidden="true"><Star /></el-icon>
                {{ message.likeCount || 0 }} 点赞
              </span>
            </div>
          </div>
          
          <!-- 操作按钮：物业经理或本人可操作 -->
          <div class="card-actions" v-if="user.type === 'property' || message.userId === user.userId">
            <el-button
              v-if="message.userId === user.userId"
              type="primary"
              size="small"
              plain
              @click.stop="handleEdit(message)"
            >
              编辑
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click.stop="handleDelete(message.id)"
            >
              删除
            </el-button>
          </div>
        </el-card>
      </li>
    </ul>
    
    <!-- 空状态 -->
    <div v-if="messageList.length === 0 && !loading" class="empty-state">
      <el-empty description="暂无消息，快来发布第一条吧！">
        <template #description>
          <span>暂无消息，快来发布第一条吧！</span>
        </template>
        <el-button type="primary" @click="showPublishDialog = true">立即发布</el-button>
      </el-empty>
    </div>
    
    <!-- 加载状态 -->
    <div v-if="loading" class="loading-state">
      <el-skeleton :rows="3" animated />
    </div>
    
    <!-- 分页 -->
    <div v-if="total > 0" class="pagination-container">
      <el-pagination
        v-model:current-page="currentPage"
        v-model:page-size="pageSize"
        :page-sizes="[10, 20, 50]"
        layout="total, sizes, prev, pager, next, jumper"
        :total="total"
        @size-change="handleSizeChange"
        @current-change="handleCurrentChange"
      />
    </div>
    
    <!-- 发布新帖弹窗 -->
    <el-dialog
      v-model="showPublishDialog"
      title="发布新帖"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="publishForm" :rules="publishRules" ref="publishFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input 
            v-model="publishForm.title" 
            placeholder="请输入帖子标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input 
            v-model="publishForm.content" 
            type="textarea"
            :rows="6"
            placeholder="请输入帖子内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showPublishDialog = false">取消</el-button>
        <el-button type="primary" @click="handlePublish" :loading="publishing">
          发布
        </el-button>
      </template>
    </el-dialog>

    <!-- 编辑帖子弹窗 -->
    <el-dialog
      v-model="showEditDialog"
      title="编辑帖子"
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="editForm" :rules="editRules" ref="editFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input
            v-model="editForm.title"
            placeholder="请输入帖子标题"
            maxlength="100"
            show-word-limit
          />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="6"
            placeholder="请输入帖子内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="showEditDialog = false">取消</el-button>
        <el-button type="primary" @click="submitEdit" :loading="editing">
          保存修改
        </el-button>
      </template>
    </el-dialog>
  </main>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { ChatDotRound, Star } from '@element-plus/icons-vue';
import router from '../router';

const user = JSON.parse(localStorage.getItem('user') || '{}');
const messageList = ref([]);
const total = ref(0);
const currentPage = ref(1);
const pageSize = ref(10);
const loading = ref(true);
const showPublishDialog = ref(false);
const publishing = ref(false);
const publishFormRef = ref(null);
const activeTab = ref('all');

const canPublish = computed(() => {
  const allowedRoles = ['resident', 'property', 'doctor'];
  return allowedRoles.includes(user.type);
});

const publishForm = ref({
  title: '',
  content: ''
});

const publishRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2到100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度在5到500个字符', trigger: 'blur' }
  ]
};

const showEditDialog = ref(false);
const editing = ref(false);
const editFormRef = ref(null);
const currentEditId = ref(null);
const editForm = ref({
  title: '',
  content: ''
});
const editRules = {
  title: [
    { required: true, message: '请输入帖子标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在2到100个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入帖子内容', trigger: 'blur' },
    { min: 5, max: 500, message: '内容长度在5到500个字符', trigger: 'blur' }
  ]
};

const loadMessageList = async () => {
  loading.value = true;
  try {
    const params = {
      page: currentPage.value,
      size: pageSize.value
    };
    if (activeTab.value === 'mine' && user.userId) {
      params.userId = user.userId;
    }
    const response = await axios.get('/api/neighbor-messages', { params });
    messageList.value = response.data.records || [];
    total.value = response.data.total || 0;
  } catch (error) {
    ElMessage.error('加载消息列表失败');
    console.error('加载消息列表失败:', error);
  } finally {
    loading.value = false;
  }
};

const handleTabChange = (tabName) => {
  activeTab.value = tabName;
  currentPage.value = 1;
  loadMessageList();
};

const handlePublish = async () => {
  if (!publishFormRef.value) return;
  
  await publishFormRef.value.validate(async (valid) => {
    if (!valid) return;
    
    if (!user.userId) {
      ElMessage.error('请先登录');
      return;
    }
    
    publishing.value = true;
    try {
      await axios.post('/api/neighbor-messages', {
        userId: user.userId,
        title: publishForm.value.title,
        content: publishForm.value.content
      });
      ElMessage.success('发布成功');
      showPublishDialog.value = false;
      publishForm.value = { title: '', content: '' };
      await loadMessageList();
    } catch (error) {
      ElMessage.error(error.response?.data?.message || '发布失败');
      console.error('发布失败:', error);
    } finally {
      publishing.value = false;
    }
  });
};

const goToDetail = (id) => {
  router.push(`/neighbor-message/${id}`);
};

const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这条帖子吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });

    await axios.delete(`/api/neighbor-messages/${id}`);
    ElMessage.success('删除成功');
    await loadMessageList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error('删除失败:', error);
    }
  }
};

const handleEdit = (message) => {
  currentEditId.value = message.id;
  editForm.value = {
    title: message.title,
    content: message.content
  };
  showEditDialog.value = true;
};

const submitEdit = async () => {
  if (!editFormRef.value) return;

  try {
    await editFormRef.value.validate();
    editing.value = true;

    await axios.put(`/api/neighbor-messages/${currentEditId.value}`, {
      title: editForm.value.title,
      content: editForm.value.content
    });

    ElMessage.success('修改成功');
    showEditDialog.value = false;
    await loadMessageList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error(error.response?.data?.message || '修改失败');
      console.error('修改失败:', error);
    }
  } finally {
    editing.value = false;
  }
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
  loadMessageList();
};

const handleCurrentChange = (page) => {
  currentPage.value = page;
  loadMessageList();
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
  loadMessageList();
});
</script>

<style scoped>
.neighbor-message-container {
  max-width: 1200px;
  margin: 0 auto;
  padding: 20px;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 24px;
}

.header-left h1 {
  font-size: 24px;
  font-weight: 600;
  color: #303133;
  margin: 0;
}

.message-tabs :deep(.el-tabs__header) {
  margin-bottom: 0;
}

.message-tabs :deep(.el-tabs__item) {
  font-size: 15px;
}

.message-list {
  list-style: none;
  padding: 0;
  margin: 0;
}

.message-item {
  margin-bottom: 16px;
  cursor: pointer;
  transition: transform 0.2s;
}

.message-item:hover {
  transform: translateY(-2px);
}

.message-card {
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  position: relative;
}

.card-actions {
  position: absolute;
  top: 16px;
  right: 16px;
  display: flex;
  gap: 8px;
}

.card-content {
  padding: 16px;
}

.user-info {
  display: flex;
  align-items: center;
  margin-bottom: 12px;
}

.user-details {
  display: flex;
  flex-direction: column;
  margin-left: 12px;
}

.user-name {
  font-size: 14px;
  font-weight: 500;
  color: #303133;
}

.publish-time {
  font-size: 12px;
  color: #909399;
  margin-top: 4px;
}

.message-title {
  font-size: 18px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 8px 0;
  line-height: 1.4;
}

.message-content {
  font-size: 14px;
  color: #606266;
  margin: 0 0 12px 0;
  line-height: 1.6;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.message-stats {
  display: flex;
  gap: 20px;
  padding-top: 12px;
  border-top: 1px solid #ebeef5;
}

.stat-item {
  display: flex;
  align-items: center;
  font-size: 13px;
  color: #909399;
  gap: 4px;
}

.empty-state {
  padding: 40px 0;
}

.loading-state {
  padding: 20px 0;
}

.pagination-container {
  display: flex;
  justify-content: center;
  margin-top: 24px;
}

/* 适老模式样式 */
.elder-mode :deep(.message-card) {
  font-size: 18px;
  padding: 24px;
  border: 1px solid #000000;
  box-shadow: none;
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
  font-size: 24px;
  font-weight: 700;
  color: #000000;
  margin: 0 0 16px 0;
}

.elder-mode :deep(.message-content) {
  font-size: 18px;
  color: #000000;
  margin: 0 0 20px 0;
  line-height: 1.8;
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

.elder-mode :deep(.el-button) {
  font-size: 20px;
  padding: 14px 24px;
}

.elder-mode :deep(.el-input__inner) {
  font-size: 20px;
  height: 50px;
}

.elder-mode :deep(.el-textarea__inner) {
  font-size: 20px;
  min-height: 150px;
}

.elder-mode :deep(.el-dialog) {
  width: 80% !important;
  max-width: 800px !important;
}

.elder-mode :deep(.el-dialog__title) {
  font-size: 24px;
  font-weight: 700;
}

.elder-mode :deep(.el-form-item__label) {
  font-size: 18px;
  font-weight: 600;
  color: #000000;
}
</style>
