<template>
  <div class="complaint-container">
    <h2>投诉建议</h2>
    
    <!-- 提交投诉区域 -->
    <el-card class="submit-card">
      <h3>提交投诉</h3>
      <el-form :model="submitForm" label-width="80px">
        <el-form-item label="投诉内容">
          <el-input
            v-model="submitForm.content"
            type="textarea"
            rows="4"
            placeholder="请输入您的投诉内容"
          />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交投诉</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 我的投诉列表 -->
    <el-card class="list-card">
      <h3>我的投诉</h3>
      
      <el-empty v-if="complaintList.length === 0" description="暂无投诉记录" />
      
      <el-timeline v-else>
        <el-timeline-item
          v-for="complaint in complaintList"
          :key="complaint.id"
          :timestamp="formatDate(complaint.createdAt)"
          :type="getStatusType(complaint.status)"
        >
          <el-card>
            <div class="complaint-header">
              <span class="complaint-id">投诉ID: {{ complaint.id }}</span>
              <el-tag :type="getStatusType(complaint.status)">{{ getStatusText(complaint.status) }}</el-tag>
            </div>
            <div class="complaint-content">
              {{ complaint.content }}
            </div>
            
            <!-- 物业回复 -->
            <div v-if="complaint.replies && complaint.replies.length > 0" class="complaint-reply">
              <h4>物业回复：</h4>
              <div v-for="reply in complaint.replies" :key="reply.id" class="reply-item">
                <div class="reply-time">{{ formatDate(reply.createdAt) }}</div>
                <div class="reply-content">{{ reply.content }}</div>
              </div>
            </div>
          </el-card>
        </el-timeline-item>
      </el-timeline>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 提交表单
const submitForm = reactive({
  content: ''
});

// 投诉列表
const complaintList = ref([]);

// 提交投诉
const handleSubmit = async () => {
  try {
    if (!submitForm.content.trim()) {
      ElMessage.warning('请输入投诉内容');
      return;
    }
    
    await axios.post('/api/complaints', {
      content: submitForm.content
    });
    
    ElMessage.success('投诉提交成功');
    submitForm.content = '';
    loadComplaints();
  } catch (error) {
    ElMessage.error('投诉提交失败');
    console.error(error);
  }
};

// 加载我的投诉列表
const loadComplaints = async () => {
  try {
    // 获取投诉列表
    const complaintsResponse = await axios.get('/api/complaints/my');
    const complaints = complaintsResponse.data;
    
    // 为每个投诉获取回复
    const complaintsWithReplies = await Promise.all(
      complaints.map(async (complaint) => {
        try {
          // 这里假设后端有一个获取投诉回复的接口
          // 如果没有，需要后端添加
          const repliesResponse = await axios.get(`/api/complaints/${complaint.id}/replies`);
          return {
            ...complaint,
            replies: repliesResponse.data
          };
        } catch (error) {
          // 如果获取回复失败，返回原投诉
          return {
            ...complaint,
            replies: []
          };
        }
      })
    );
    
    complaintList.value = complaintsWithReplies;
  } catch (error) {
    ElMessage.error('加载投诉列表失败');
    console.error(error);
  }
};

// 格式化日期
const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

// 获取状态类型（用于标签和时间线）
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
.complaint-container {
  padding: 20px;
}

.submit-card {
  margin-bottom: 30px;
}

.list-card {
  margin-bottom: 20px;
}

.complaint-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 10px;
}

.complaint-id {
  font-size: 14px;
  color: #606266;
}

.complaint-content {
  margin-bottom: 15px;
  line-height: 1.5;
}

.complaint-reply {
  margin-top: 15px;
  padding-top: 15px;
  border-top: 1px solid #ebeef5;
}

.complaint-reply h4 {
  margin: 0 0 10px 0;
  font-size: 14px;
  color: #303133;
}

.reply-item {
  margin-bottom: 10px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.reply-time {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.reply-content {
  font-size: 14px;
  line-height: 1.4;
}

.el-timeline {
  padding-left: 20px;
}

.el-timeline-item {
  padding-bottom: 20px;
}
</style>