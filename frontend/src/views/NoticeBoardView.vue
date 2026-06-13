<template>
  <div class="notice-board-container">
    <h2>社区公告栏</h2>
    
    <!-- 公告列表 -->
    <el-card class="notice-list-card">
      <template #header>
        <div class="card-header">
          <span>最新公告</span>
        </div>
      </template>
      
      <div v-if="noticeList.length === 0" class="empty-notice">
        <el-empty description="暂无公告" />
      </div>
      
      <div v-else class="notice-items">
        <el-card
          v-for="notice in noticeList"
          :key="notice.id"
          class="notice-item"
          @click="viewNoticeDetail(notice)"
        >
          <template #header>
            <div class="notice-header">
              <h3 class="notice-title">{{ notice.title }}</h3>
              <span class="notice-date">{{ formatDate(notice.createdAt) }}</span>
            </div>
          </template>
          <div class="notice-content">
            {{ truncateContent(notice.content) }}
          </div>
          <div class="notice-footer">
            <span class="notice-publisher">发布人：{{ notice.publisher }}</span>
          </div>
        </el-card>
      </div>
      
      <!-- 分页 -->
      <div v-if="noticeList.length > 0" class="pagination">
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
    </el-card>
    
    <!-- 公告详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      :title="currentNotice?.title || '公告详情'"
      width="700px"
    >
      <div v-if="currentNotice" class="notice-detail">
        <div class="detail-header">
          <h3>{{ currentNotice.title }}</h3>
          <div class="detail-meta">
            <span>发布时间：{{ formatDate(currentNotice.createdAt) }}</span>
            <span>发布人：{{ currentNotice.publisher }}</span>
          </div>
        </div>
        <div class="detail-content">
          {{ currentNotice.content }}
        </div>
        
        <!-- 维修完成通知：直接显示评价表单 -->
        <div v-if="isRepairCompletionNotice && currentNotice.repairId && !hasRated" class="quick-evaluation">
          <el-divider />
          <h4>快速评价</h4>
          <el-form :model="quickEvalForm" :rules="quickEvalRules" ref="quickEvalFormRef" label-width="80px">
            <el-form-item label="满意度" prop="rating">
              <el-rate v-model="quickEvalForm.rating" :max="5" show-text />
            </el-form-item>
            <el-form-item label="评价内容" prop="comment">
              <el-input v-model="quickEvalForm.comment" type="textarea" :rows="3" placeholder="请输入您的评价（最多100字）" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="submitQuickEvaluation" :loading="evaluating">提交评价</el-button>
            </el-form-item>
          </el-form>
        </div>
        
        <!-- 已评价提示 -->
        <div v-if="isRepairCompletionNotice && hasRated" class="rated-hint">
          <el-divider />
          <el-alert type="success" :closable="false">
            <template #default>
              <el-icon style="vertical-align: middle; margin-right: 5px;"><SuccessFilled /></el-icon>
              您已对此服务完成评价，感谢您的反馈！
            </template>
          </el-alert>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { SuccessFilled } from '@element-plus/icons-vue';

const noticeList = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const total = ref(0);
const detailDialogVisible = ref(false);
const currentNotice = ref(null);

const user = JSON.parse(localStorage.getItem('user') || '{}');

const evaluating = ref(false);
const quickEvalFormRef = ref(null);
const quickEvalForm = ref({
  repairId: '',
  rating: 0,
  comment: ''
});

const quickEvalRules = {
  rating: [
    { required: true, message: '请选择满意度', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { max: 100, message: '评价内容最多 100 个字符', trigger: 'blur' }
  ]
};

const isRepairCompletionNotice = computed(() => {
  return currentNotice.value && (
    currentNotice.value.title?.includes('维修完成') || 
    currentNotice.value.title?.includes('维修竣工') ||
    currentNotice.value.content?.includes('请对服务进行评价')
  );
});

const hasRated = ref(false);

const loadNoticeList = async () => {
  try {
    const response = await axios.get('/api/notice/user');
    noticeList.value = response.data.sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt));
    total.value = noticeList.value.length;
  } catch (error) {
    ElMessage.error('加载公告列表失败');
    console.error(error);
  }
};

const viewNoticeDetail = async (notice) => {
  currentNotice.value = notice;
  hasRated.value = false;
  quickEvalForm.value = { repairId: '', rating: 0, comment: '' };
  detailDialogVisible.value = true;
  
  if (isRepairCompletionNotice.value && notice.repairId) {
    quickEvalForm.value.repairId = notice.repairId;
    await checkRepairRatedStatus(notice.repairId);
  }
};

const checkRepairRatedStatus = async (repairId) => {
  try {
    const response = await axios.get(`/api/repair/${repairId}`);
    if (response.data && response.data.rating) {
      hasRated.value = true;
    } else {
      hasRated.value = false;
    }
  } catch (error) {
    console.error('检查评价状态失败:', error);
    hasRated.value = false;
  }
};

const submitQuickEvaluation = async () => {
  await quickEvalFormRef.value.validate(async (valid) => {
    if (valid) {
      evaluating.value = true;
      try {
        await axios.post(`/api/repair/rate/${quickEvalForm.value.repairId}`, {
          rating: quickEvalForm.value.rating,
          comment: quickEvalForm.value.comment
        });
        ElMessage.success('评价提交成功，感谢您的反馈！');
        hasRated.value = true;
        quickEvalForm.value = { repairId: '', rating: 0, comment: '' };
        
        setTimeout(() => {
          detailDialogVisible.value = false;
        }, 1500);
      } catch (error) {
        ElMessage.error('评价提交失败: ' + (error.response?.data?.message || error.message));
        console.error('评价提交失败:', error);
      } finally {
        evaluating.value = false;
      }
    }
  });
};

const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit'
  });
};

const truncateContent = (content) => {
  if (!content) return '';
  return content.length > 100 ? content.substring(0, 100) + '...' : content;
};

const getTargetTypeText = (targetType) => {
  if (!targetType) return '全体用户';
  const types = targetType.split(',');
  const typeMap = {
    'all': '全体用户',
    'resident': '居民',
    'doctor': '医生',
    'property': '物业'
  };
  return types.map(type => typeMap[type] || type).join('、');
};

const handleSizeChange = (size) => {
  pageSize.value = size;
  currentPage.value = 1;
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
};

onMounted(async () => {
  await loadNoticeList();
});
</script>

<style scoped>
.notice-board-container {
  padding: 20px;
}

.notice-list-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.empty-notice {
  padding: 40px 0;
  text-align: center;
}

.notice-items {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  cursor: pointer;
  transition: all 0.3s ease;
}

.notice-item:hover {
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  transform: translateY(-2px);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
}

.notice-title {
  font-size: 16px;
  font-weight: bold;
  margin: 0;
  color: #333;
}

.notice-date {
  font-size: 12px;
  color: #909399;
}

.notice-content {
  font-size: 14px;
  line-height: 1.5;
  color: #606266;
  margin: 12px 0;
  white-space: pre-wrap;
}

.notice-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 12px;
  color: #909399;
  margin-top: 8px;
}

.notice-publisher {
  flex: 1;
}

.notice-tag {
  background-color: #ecf5ff;
  color: #409eff;
  padding: 2px 8px;
  border-radius: 10px;
}

.pagination {
  margin-top: 24px;
  text-align: center;
}

.notice-detail {
  padding: 10px 0;
}

.detail-header {
  margin-bottom: 20px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.detail-header h3 {
  font-size: 18px;
  font-weight: bold;
  margin: 0 0 10px 0;
  color: #333;
}

.detail-meta {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
  font-size: 14px;
  color: #909399;
}

.detail-content {
  font-size: 16px;
  line-height: 1.8;
  color: #333;
  white-space: pre-wrap;
}

@media (max-width: 768px) {
  .notice-board-container {
    padding: 10px;
  }
  
  .notice-header {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }
  
  .notice-footer {
    flex-direction: column;
    align-items: flex-start;
    gap: 4px;
  }
  
  .detail-meta {
    flex-direction: column;
    gap: 8px;
  }
}

.quick-evaluation {
  margin-top: 20px;
  padding: 15px;
  background-color: #f5f7fa;
  border-radius: 8px;
}

.quick-evaluation h4 {
  margin: 0 0 15px 0;
  color: #303133;
}

.rated-hint {
  margin-top: 20px;
}
</style>