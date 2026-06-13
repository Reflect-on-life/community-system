<template>
  <div class="risk-management-container">
    <h2>风险管理</h2>
    
    <!-- 风险管理卡片 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>风险管理</span>
        </div>
      </template>
      
      <!-- 筛选区域 -->
      <div class="filters-container">
        <!-- 状态筛选按钮组 -->
        <div class="status-filters">
          <span class="filter-label">状态：</span>
          <el-button
            :type="filterForm.status === '' ? 'primary' : 'default'"
            @click="handleStatusFilter('')"
            size="small"
          >
            全部
          </el-button>
          <el-button
            :type="filterForm.status === 'PENDING' ? 'primary' : 'default'"
            @click="handleStatusFilter('PENDING')"
            size="small"
          >
            待处理
          </el-button>
          <el-button
            :type="filterForm.status === 'PROCESSING' ? 'primary' : 'default'"
            @click="handleStatusFilter('PROCESSING')"
            size="small"
          >
            处理中
          </el-button>
          <el-button
            :type="filterForm.status === 'DONE' ? 'primary' : 'default'"
            @click="handleStatusFilter('DONE')"
            size="small"
          >
            已完成
          </el-button>
        </div>

        <!-- 风险等级筛选按钮组 -->
        <div class="risk-level-filters">
          <span class="filter-label">风险等级：</span>
          <el-button
            :type="filterForm.riskLevel === '' ? 'danger' : 'default'"
            @click="handleRiskLevelFilter('')"
            size="small"
          >
            全部
          </el-button>
          <el-button
            :type="filterForm.riskLevel === 'HIGH' ? 'danger' : 'default'"
            @click="handleRiskLevelFilter('HIGH')"
            size="small"
          >
            高风险
          </el-button>
          <el-button
            :type="filterForm.riskLevel === 'MEDIUM' ? 'warning' : 'default'"
            @click="handleRiskLevelFilter('MEDIUM')"
            size="small"
          >
            中风险
          </el-button>
          <el-button
            :type="filterForm.riskLevel === 'LOW' ? 'success' : 'default'"
            @click="handleRiskLevelFilter('LOW')"
            size="small"
          >
            低风险
          </el-button>
        </div>
      </div>
      
      <!-- 表格区域 -->
      <el-table :data="riskList" style="width: 100%" v-loading="loading">
        <el-table-column prop="id" label="ID" min-width="80" />
        <el-table-column prop="title" label="风险标题" min-width="180" />
        <el-table-column prop="riskType" label="风险类型" min-width="120" />
        <el-table-column prop="riskLevel" label="风险等级" min-width="100">
          <template #default="scope">
            <el-tag 
              :type="getRiskLevelType(scope.row.riskLevel)"
              effect="dark"
            >
              {{ getRiskLevelText(scope.row.riskLevel) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="location" label="风险位置" min-width="150">
          <template #default="scope">
            {{ scope.row.location || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="当前状态" min-width="100">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">
              {{ getStatusText(scope.row.status) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="上报时间" min-width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createTime) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="200" fixed="right">
          <template #default="scope">
            <el-button
              type="primary"
              size="small"
              @click="viewDetail(scope.row)"
            >
              详情
            </el-button>
            <el-button
              v-if="scope.row.status !== 'DONE'"
              type="success"
              size="small"
              @click="handleRisk(scope.row)"
            >
              处理
            </el-button>
            <el-button
              type="danger"
              size="small"
              @click="deleteRisk(scope.row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.page"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadRiskList"
          @current-change="loadRiskList"
        />
      </div>
    </el-card>
    
    <!-- 详情对话框 -->
    <el-dialog v-model="detailVisible" title="风险事件详情" width="800px">
      <el-descriptions :column="2" border v-if="currentRisk">
        <el-descriptions-item label="风险标题" :span="2">{{ currentRisk.title }}</el-descriptions-item>
        <el-descriptions-item label="风险类型">{{ currentRisk.riskType }}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="getRiskLevelType(currentRisk.riskLevel)" effect="dark">
            {{ getRiskLevelText(currentRisk.riskLevel) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="风险发生位置">{{ currentRisk.location || '未知' }}</el-descriptions-item>
        <el-descriptions-item label="当前状态">
          <el-tag :type="getStatusType(currentRisk.status)">
            {{ getStatusText(currentRisk.status) }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="上报时间">{{ formatDate(currentRisk.createTime) }}</el-descriptions-item>
        <el-descriptions-item label="事件描述" :span="2">{{ currentRisk.description || '无' }}</el-descriptions-item>
        <el-descriptions-item label="现场图片" :span="2" v-if="currentRisk.imageUrl">
          <el-image
            :src="getImageUrl(currentRisk.imageUrl)"
            alt="风险图片"
            style="max-width: 100%; border-radius: 4px;"
            :preview-src-list="[getImageUrl(currentRisk.imageUrl)]"
            fit="contain"
          >
            <template #error>
              <div style="display: flex; justify-content: center; align-items: center; width: 100%; height: 200px; background: #f5f7fa; color: #909399;">
                <el-icon><Picture /></el-icon>
                <span style="margin-left: 8px;">图片加载失败</span>
              </div>
            </template>
          </el-image>
        </el-descriptions-item>
        <el-descriptions-item label="AI 识别结果" :span="2" v-if="currentRisk.aiResult">
          <el-alert :title="currentRisk.aiResult" type="warning" :closable="false" show-icon />
        </el-descriptions-item>
        <el-descriptions-item label="GLM-4V 识别详情" :span="2" v-if="currentRisk.glmResult">
          <el-card shadow="never" style="background: #f8f9fa;">
            <pre style="margin: 0; white-space: pre-wrap; word-break: break-word; font-size: 13px; line-height: 1.6;">{{ currentRisk.glmResult }}</pre>
          </el-card>
        </el-descriptions-item>
        <el-descriptions-item label="百度 AI 识别标签" :span="2" v-if="currentRisk.baiduLabels">
          <div style="display: flex; flex-wrap: wrap; gap: 8px;">
            <el-tag 
              v-for="(label, index) in parseBaiduLabels(currentRisk.baiduLabels)" 
              :key="index" 
              type="info" 
              effect="plain"
            >
              {{ label }}
            </el-tag>
          </div>
        </el-descriptions-item>
        <el-descriptions-item label="处理备注" :span="2" v-if="currentRisk.remark">
          {{ currentRisk.remark }}
        </el-descriptions-item>
      </el-descriptions>
      <template #footer>
        <el-button @click="detailVisible = false">关闭</el-button>
      </template>
    </el-dialog>
    
    <!-- 处理对话框 -->
    <el-dialog v-model="handleDialogVisible" title="处理风险事件" width="500px">
      <el-form :model="handleForm" label-width="80px">
        <el-form-item label="处理状态">
          <el-select v-model="handleForm.status" placeholder="请选择处理状态" style="width: 100%">
            <el-option label="待处理" value="PENDING" />
            <el-option label="处理中" value="PROCESSING" />
            <el-option label="已解决" value="DONE" />
          </el-select>
        </el-form-item>
        <el-form-item label="处理备注">
          <el-input
            v-model="handleForm.remark"
            type="textarea"
            rows="4"
            placeholder="请输入处理备注"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="handleDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="confirmHandle" :loading="handling">确认</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue';
import { useRoute } from 'vue-router'
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Picture } from '@element-plus/icons-vue';

const route = useRoute()

// 筛选表单
const filterForm = reactive({
  status: '',
  riskLevel: ''
});

// 分页数据
const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
});

// 风险列表
const riskList = ref([]);
const loading = ref(false);

// 对话框
const detailVisible = ref(false);
const handleDialogVisible = ref(false);
const currentRisk = ref(null);
const handling = ref(false);

const handleForm = reactive({
  status: 'PROCESSING',
  remark: ''
});

// 拼接图片完整 URL
const getImageUrl = (imageUrl) => {
  if (!imageUrl) return '';
  // 如果已经是完整 URL，直接返回
  if (imageUrl.startsWith('http://') || imageUrl.startsWith('https://')) {
    return imageUrl;
  }
  return `http://localhost:8080${imageUrl}`;
};

const loadRiskList = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/risk/list', {
      params: {
        page: pagination.page,
        size: pagination.size,
        status: filterForm.status,
        riskLevel: filterForm.riskLevel
      }
    });
    
    riskList.value = response.data.records;
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('加载风险列表失败');
    console.error(error);
  } finally {
    loading.value = false;
  }
};

const viewDetail = async (risk) => {
  try {
    const response = await axios.get(`/api/risk/${risk.id}`);
    currentRisk.value = response.data;
    detailVisible.value = true;
  } catch (error) {
    ElMessage.error('加载详情失败');
    console.error(error);
  }
};

const handleRisk = (risk) => {
  currentRisk.value = risk;
  handleForm.status = risk.status;
  handleForm.remark = risk.remark || '';
  handleDialogVisible.value = true;
};

const confirmHandle = async () => {
  if (!currentRisk.value) return;
  
  handling.value = true;
  try {
    await axios.put(`/api/risk/${currentRisk.value.id}/status`, {
      status: handleForm.status,
      remark: handleForm.remark
    });
    ElMessage.success('处理成功');
    handleDialogVisible.value = false;
    loadRiskList();
  } catch (error) {
    ElMessage.error('处理失败');
    console.error(error);
  } finally {
    handling.value = false;
  }
};

const deleteRisk = async (risk) => {
  try {
    await ElMessageBox.confirm('确定要删除这条风险记录吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    });
    
    await axios.delete(`/api/risk/${risk.id}`);
    ElMessage.success('删除成功');
    loadRiskList();
  } catch (error) {
    if (error !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

// 状态筛选
const handleStatusFilter = (status) => {
  filterForm.status = status;
  pagination.page = 1;
  loadRiskList();
};

// 风险等级筛选
const handleRiskLevelFilter = (riskLevel) => {
  filterForm.riskLevel = riskLevel;
  pagination.page = 1;
  loadRiskList();
};

// 重置筛选
const handleReset = () => {
  filterForm.status = '';
  filterForm.riskLevel = '';
  pagination.page = 1;
  loadRiskList();
};

const getRiskLevelType = (level) => {
  switch (level) {
    case 'LOW':
      return 'success';
    case 'MEDIUM':
      return 'warning';
    case 'HIGH':
      return 'danger';
    default:
      return 'info';
  }
};

const getRiskLevelText = (level) => {
  switch (level) {
    case 'LOW':
      return '低';
    case 'MEDIUM':
      return '中';
    case 'HIGH':
      return '高';
    default:
      return level;
  }
};

const getStatusType = (status) => {
  switch (status) {
    case 'PENDING':
      return 'warning';
    case 'PROCESSING':
      return 'info';
    case 'DONE':
      return 'success';
    default:
      return 'info';
  }
};

const getStatusText = (status) => {
  switch (status) {
    case 'PENDING':
      return '待处理';
    case 'PROCESSING':
      return '处理中';
    case 'DONE':
      return '已完成';
    default:
      return status;
  }
};

const formatDate = (date) => {
  if (!date) return '';
  return new Date(date).toLocaleString();
};

const parseBaiduLabels = (labels) => {
  if (!labels) return [];
  try {
    // 百度标签可能是逗号分隔的字符串或 JSON 数组
    if (labels.startsWith('[')) {
      return JSON.parse(labels);
    }
    return labels.split(',').filter(label => label.trim());
  } catch (e) {
    return [labels];
  }
};

onMounted(() => {
  loadRiskList();
});
</script>

<style scoped>
.risk-management-container {
  padding: 20px;
}

.list-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.status-filters {
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #ebeef5;
  display: flex;
  align-items: center;
  gap: 10px;
}

.risk-level-filters {
  display: flex;
  align-items: center;
  gap: 10px;
}

.filter-label {
  font-size: 14px;
  font-weight: 600;
  color: #606266;
  white-space: nowrap;
}

.filters-container {
  margin-bottom: 20px;
  padding: 15px;
  background: #fafafa;
  border-radius: 8px;
  border: 1px solid #ebeef5;
}

.filters-container .el-button {
  margin-right: 0;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>