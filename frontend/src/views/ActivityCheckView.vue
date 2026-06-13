<template>
  <div class="activity-check-container">
    <h2>活动报名核销</h2>
    
    <!-- 顶部查询区域 -->
    <el-card class="query-card">
      <el-form :inline="true" :model="queryForm" class="demo-form-inline">
        <el-form-item label="活动">
          <el-select v-model="queryForm.activityId" placeholder="请选择活动" style="width: 300px;">
            <el-option
              v-for="activity in activityList"
              :key="activity.id"
              :label="activity.title"
              :value="activity.id"
            >
              <div class="option-content">
                <span class="option-title">{{ activity.title }}</span>
                <span class="option-date">{{ formatDate(activity.startTime) }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery" :loading="loading">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 统计卡片区域 -->
    <el-card class="stats-card">
      <div class="stats-container">
        <div class="stat-item">
          <div class="stat-title">总报名人数</div>
          <div class="stat-value">{{ totalSignups }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-title">已核销人数</div>
          <div class="stat-value">{{ checkedSignups }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-title">未核销人数</div>
          <div class="stat-value">{{ pendingSignups }}</div>
        </div>
      </div>
    </el-card>
    
    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="signupList" style="width: 100%" v-loading="loading">
        <el-table-column prop="signupId" label="报名ID" width="100" />
        <el-table-column prop="userId" label="用户ID" width="100" />
        <el-table-column prop="userName" label="用户姓名" width="150" />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报名时间">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="primary" 
              size="small" 
              @click="handleCheck(scope.row.signupId)"
            >
              核销
            </el-button>
            <el-tag v-else type="info" size="small">已核销</el-tag>
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
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 查询表单
const queryForm = reactive({
  activityId: ''
});

// 活动列表
const activityList = ref([]);

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 报名列表
const signupList = ref([]);

// 加载状态
const loading = ref(false);

// 计算统计数据
const totalSignups = computed(() => {
  return signupList.value.length;
});

const checkedSignups = computed(() => {
  return signupList.value.filter(item => item.status === 'confirmed').length;
});

const pendingSignups = computed(() => {
  return signupList.value.filter(item => item.status === 'pending').length;
});

// 加载活动列表
const loadActivityList = async () => {
  try {
    const response = await axios.get('/api/activity/list');
    activityList.value = response.data;
  } catch (error) {
    ElMessage.error('加载活动列表失败');
    console.error(error);
  }
};

// 加载报名列表
const loadSignupList = async () => {
  try {
    loading.value = true;
    
    const params = {
      page: pagination.current,
      size: pagination.size
    };
    
    // 添加活动ID筛选
    if (queryForm.activityId) {
      params.activityId = queryForm.activityId;
    }
    
    const response = await axios.get('/api/admin/activity-check/page', {
      params
    });
    
    signupList.value = response.data.records;
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('加载报名列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  loadSignupList();
};

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.size = size;
  loadSignupList();
};

// 当前页变更
const handleCurrentChange = (current) => {
  pagination.current = current;
  loadSignupList();
};

// 处理核销
const handleCheck = async (signupId) => {
  try {
    await ElMessageBox.confirm('确定要核销这个报名吗？', '核销确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await axios.put(`/api/admin/activity-check/${signupId}/check`);
    ElMessage.success('核销成功');
    loadSignupList();
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessage.error('核销失败');
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
    case 'confirmed':
      return 'success';
    case 'cancelled':
      return 'danger';
    default:
      return 'info';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待核销';
    case 'confirmed':
      return '已核销';
    case 'cancelled':
      return '已取消';
    default:
      return status;
  }
};

// 初始化
onMounted(() => {
  loadActivityList();
  loadSignupList();
});
</script>

<style scoped>
.activity-check-container {
  padding: 20px;
  overflow: hidden;
}

.query-card {
  margin-bottom: 20px;
}

.stats-card {
  margin-bottom: 20px;
}

.stats-container {
  display: flex;
  gap: 20px;
}

.stat-item {
  flex: 1;
  text-align: center;
  padding: 20px;
  background-color: #f9fafb;
  border-radius: 8px;
}

.stat-title {
  font-size: 14px;
  color: #666;
  margin-bottom: 10px;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: #333;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

/* 下拉选项样式 */
.option-content {
  display: flex;
  flex-direction: column;
  align-items: flex-start;
}

.option-title {
  font-weight: 500;
  margin-bottom: 4px;
}

.option-date {
  font-size: 12px;
  color: #909399;
}

@media (max-width: 768px) {
  .activity-check-container {
    padding: 10px;
  }
  
  .query-card {
    margin-bottom: 15px;
  }
  
  .stats-card {
    margin-bottom: 15px;
  }
  
  .stats-container {
    flex-direction: column;
    gap: 10px;
  }
  
  .stat-item {
    padding: 15px;
  }
  
  .table-card {
    margin-bottom: 15px;
  }
}
</style>