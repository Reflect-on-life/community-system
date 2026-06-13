<template>
  <div class="complaint-management-container">
    <h2>投诉管理</h2>
    
    <!-- 投诉管理卡片 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>投诉管理</span>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="status-filters">
        <el-button 
          :type="filterForm.status === '' ? 'primary' : 'default'"
          @click="handleStatusFilter('')"
        >
          全部
        </el-button>
        <el-button 
          :type="filterForm.status === 'pending' ? 'primary' : 'default'"
          @click="handleStatusFilter('pending')"
        >
          待处理
        </el-button>
        <el-button 
          :type="filterForm.status === 'replied' ? 'primary' : 'default'"
          @click="handleStatusFilter('replied')"
        >
          已回复
        </el-button>
        <el-button 
          :type="filterForm.status === 'closed' ? 'primary' : 'default'"
          @click="handleStatusFilter('closed')"
        >
          已关闭
        </el-button>
      </div>
      
      <!-- 表格区域 -->
      <el-table :data="complaintList" style="width: 100%">
        <el-table-column prop="id" label="投诉ID" min-width="80" />
        <el-table-column prop="userId" label="用户ID" min-width="80" />
        <el-table-column prop="content" label="投诉内容" min-width="200" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" min-width="160">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="150">
          <template #default="scope">
            <el-button 
              type="primary" 
              size="small" 
              @click="handleReply(scope.row)"
              :disabled="scope.row.status === 'closed'"
            >
              回复
            </el-button>
            <el-button 
              type="success" 
              size="small" 
              @click="handleClose(scope.row.id)"
              :disabled="scope.row.status === 'closed'"
            >
              关闭
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 回复对话框 -->
    <el-dialog
      v-model="replyDialogVisible"
      title="回复投诉"
      width="500px"
    >
      <el-form :model="replyForm" label-width="80px">
        <el-form-item label="投诉内容">
          <el-input
            v-model="replyForm.complaintContent"
            type="textarea"
            rows="4"
            readonly
          />
        </el-form-item>
        <el-form-item label="回复内容">
          <el-input
            v-model="replyForm.content"
            type="textarea"
            rows="4"
            placeholder="请输入回复内容"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="replyDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmitReply">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 筛选表单
const filterForm = ref({
  status: ''
});

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 投诉列表
const complaintList = ref([]);

// 回复对话框
const replyDialogVisible = ref(false);
const replyForm = reactive({
  complaintId: '',
  complaintContent: '',
  content: ''
});

// 加载投诉列表
const loadComplaints = async () => {
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    };
    
    // 添加状态筛选
    if (filterForm.value.status) {
      params.status = filterForm.value.status;
    }
    
    const response = await axios.get('/api/admin/complaints/page', {
      params
    });
    
    complaintList.value = response.data.records;
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('加载投诉列表失败');
    console.error(error);
  }
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  loadComplaints();
};

// 刷新列表
const handleRefresh = () => {
  filterForm.value.status = '';
  handleQuery();
};

// 状态筛选
const handleStatusFilter = (status) => {
  filterForm.value.status = status;
  handleQuery();
};

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.size = size;
  loadComplaints();
};

// 当前页变更
const handleCurrentChange = (current) => {
  pagination.current = current;
  loadComplaints();
};

// 处理回复
const handleReply = (complaint) => {
  replyForm.complaintId = complaint.id;
  replyForm.complaintContent = complaint.content;
  replyForm.content = '';
  replyDialogVisible.value = true;
};

// 提交回复
const handleSubmitReply = async () => {
  try {
    if (!replyForm.content.trim()) {
      ElMessage.warning('请输入回复内容');
      return;
    }
    
    await axios.post(`/api/admin/complaints/${replyForm.complaintId}/reply`, {
      content: replyForm.content
    });
    
    ElMessage.success('回复成功');
    replyDialogVisible.value = false;
    loadComplaints();
  } catch (error) {
    ElMessage.error('回复失败');
    console.error(error);
  }
};

// 处理关闭投诉
const handleClose = async (id) => {
  try {
    await ElMessageBox.confirm('确定要关闭这个投诉吗？', '关闭确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await axios.put(`/api/admin/complaints/${id}/close`);
    ElMessage.success('关闭成功');
    loadComplaints();
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessage.error('关闭失败');
      console.error(error);
    }
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning';
    case 'replied':
      return 'info';
    case 'closed':
      return 'success';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待处理';
    case 'replied':
      return '已回复';
    case 'closed':
      return '已关闭';
    default:
      return status;
  }
};

// 初始化加载
onMounted(() => {
  loadComplaints();
});
</script>

<style scoped>
.complaint-management-container {
  padding: 20px;
}

.list-card {
  margin-top: 20px;
}

.status-filters {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
}

.status-filters .el-button {
  margin-right: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>