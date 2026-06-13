<template>
  <div class="repair-management-container">
    <h2>报修工单管理</h2>
    
    <!-- 报修工单卡片 -->
    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>报修工单管理</span>
        </div>
      </template>
      
      <!-- 状态筛选按钮组 -->
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
          :type="filterForm.status === 'processing' ? 'primary' : 'default'"
          @click="handleStatusFilter('processing')"
        >
          处理中
        </el-button>
        <el-button 
          :type="filterForm.status === 'completed' ? 'primary' : 'default'"
          @click="handleStatusFilter('completed')"
        >
          已完成
        </el-button>
        <el-button 
          :type="filterForm.status === 'cancelled' ? 'primary' : 'default'"
          @click="handleStatusFilter('cancelled')"
        >
          已取消
        </el-button>
      </div>
      
      <!-- 表格区域 -->
      <el-table :data="repairList" style="width: 100%">
        <el-table-column prop="id" label="工单ID" min-width="80" />
        <el-table-column label="房产" min-width="120">
          <template #default="scope">
            {{ scope.row.houseNo || '未知房产' }}
          </template>
        </el-table-column>
        <el-table-column label="申请人" min-width="100">
          <template #default="scope">
            {{ scope.row.userName || '未知用户' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" min-width="200" />
        <el-table-column prop="status" label="状态" min-width="100">
          <template #default="scope">
            <el-tag
              :type="{
                'pending': 'warning',
                'processing': 'info',
                'completed': 'success',
                'cancelled': 'danger'
              }[scope.row.status]"
            >
              {{ {
                'pending': '待处理',
                'processing': '处理中',
                'completed': '已完成',
                'cancelled': '已取消'
              }[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" min-width="160">
          <template #default="scope">
            {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="250">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'pending' && userType === 'property'" 
              type="primary" 
              size="small" 
              @click="handleAssign(scope.row)"
            >
              分配师傅
            </el-button>
            <el-button 
              v-if="scope.row.status === 'pending' " 
              type="warning" 
              size="small" 
              @click="handleStartProcessing(scope.row.id)"
            >
              开始处理
            </el-button>
            <el-button 
              v-else-if="scope.row.status === 'processing' " 
              type="success" 
              size="small" 
              @click="handleComplete(scope.row.id)"
            >
              完成
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="handleViewDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 分配师傅弹窗 -->
    <el-dialog
      v-model="assignDialogVisible"
      title="分配维修师傅"
      width="40%"
    >
      <div v-if="selectedRepairForAssign">
        <p>工单ID：{{ selectedRepairForAssign.id }}</p>
        <p>问题描述：{{ selectedRepairForAssign.description }}</p>
        <el-form :model="assignForm" :rules="assignRules" ref="assignFormRef" style="margin-top: 20px;">
          <el-form-item label="维修师傅" prop="technicianId">
            <el-select v-model="assignForm.technicianId" placeholder="请选择维修师傅">
              <el-option 
                v-for="technician in technicians" 
                :key="technician.id" 
                :label="technician.name" 
                :value="technician.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmitAssign">确认分配</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
    
    <!-- 工单详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      title="工单详情"
      width="50%"
    >
      <div v-if="selectedRepair">
        <h3>{{ selectedRepair.description }}</h3>
        <p>工单ID：{{ selectedRepair.id }}</p>
        <p>申请人：{{ selectedRepair.userName || '未知用户' }}</p>
        <p>房产：{{ selectedRepair.houseNo || '未知房产' }}</p>
        <p>申请时间：{{ new Date(selectedRepair.createdAt || Date.now()).toLocaleString() }}</p>
        <p>完成时间：{{ selectedRepair.completedAt ? new Date(selectedRepair.completedAt).toLocaleString() : '未完成' }}</p>
        <p>状态：{{ {
          'pending': '待处理',
          'processing': '处理中',
          'completed': '已完成',
          'cancelled': '已取消'
        }[selectedRepair.status] }}</p>
        
        <!-- 处理记录 -->
        <h4 style="margin-top: 20px;">处理记录</h4>
        <el-timeline>
          <el-timeline-item
            type="primary"
            size="large"
            timestamp="提交申请"
          >
            居民提交了报修申请
          </el-timeline-item>
          <el-timeline-item
            v-if="selectedRepair.status !== 'pending'"
            type="info"
            size="large"
            timestamp="开始处理"
          >
            物业开始处理报修
          </el-timeline-item>
          <el-timeline-item
            v-if="selectedRepair.status === 'completed'"
            type="success"
            size="large"
            timestamp="处理完成"
          >
            物业已完成报修处理
          </el-timeline-item>
          <el-timeline-item
            v-if="selectedRepair.status === 'cancelled'"
            type="danger"
            size="large"
            timestamp="已取消"
          >
            报修已被取消
          </el-timeline-item>
        </el-timeline>
        
        <!-- 处理结果 -->
        <el-form v-if="selectedRepair.status === 'processing'" :model="processForm" :rules="processRules" ref="processFormRef" style="margin-top: 20px;">
          <el-form-item label="处理结果" prop="result">
            <el-input v-model="processForm.result" type="textarea" :rows="3" placeholder="请输入处理结果" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSubmitResult">提交处理结果</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const filterForm = ref({
  status: ''
});

const repairList = ref([]);
const dialogVisible = ref(false);
const assignDialogVisible = ref(false);
const selectedRepair = ref(null);
const selectedRepairForAssign = ref(null);
const processForm = ref({
  repairId: '',
  result: ''
});
const assignForm = ref({
  technicianId: ''
});
const technicians = ref([]);
const userType = ref('');

const processRules = {
  result: [
    { required: true, message: '请输入处理结果', trigger: 'blur' },
    { min: 5, message: '处理结果至少5个字符', trigger: 'blur' }
  ]
};

const assignRules = {
  technicianId: [
    { required: true, message: '请选择维修师傅', trigger: 'change' }
  ]
};

const processFormRef = ref(null);
const assignFormRef = ref(null);

// 加载工单列表
const loadRepairs = async () => {
  try {
    // 模拟工单列表数据
    repairList.value = [
      {
        id: 1,
        userId: 4,
        userName: '居民王小明',
        houseId: 1,
        houseNo: '1号楼 101室',
        description: '厨房水龙头漏水严重，请尽快维修。',
        status: 'pending',
        createdAt: new Date(),
        completedAt: null
      }
    ];
    console.log('加载工单列表成功，响应数据:', repairList.value);
  } catch (error) {
    ElMessage.error('加载工单列表失败');
    console.error('加载工单列表失败，错误信息:', error);
  }
};

// 状态筛选
const handleStatusFilter = (status) => {
  filterForm.value.status = status;
  loadRepairs();
};

// 刷新列表
const handleRefresh = () => {
  filterForm.value.status = '';
  loadRepairs();
};

// 开始处理
const handleStartProcessing = async (id) => {
  try {
    // 模拟开始处理
    console.log('开始处理:', id);
    const repair = repairList.value.find(item => item.id === id);
    if (repair) {
      repair.status = 'processing';
    }
    ElMessage.success('开始处理成功');
  } catch (error) {
    ElMessage.error('开始处理失败');
    console.error('开始处理失败，错误信息:', error);
  }
};

// 完成
const handleComplete = (id) => {
  selectedRepair.value = repairList.value.find(item => item.id === id);
  processForm.value.repairId = id;
  dialogVisible.value = true;
};

// 查看详情
const handleViewDetail = (repair) => {
  selectedRepair.value = repair;
  dialogVisible.value = true;
};

// 加载维修师傅列表
const loadTechnicians = async () => {
  try {
    // 模拟维修师傅列表数据
    technicians.value = [
      { id: 17, name: '王师傅' },
      { id: 18, name: '李师傅' },
      { id: 19, name: '张师傅' },
      { id: 20, name: '刘师傅' },
      { id: 21, name: '陈师傅' }
    ];
    console.log('加载维修师傅列表成功:', technicians.value);
  } catch (error) {
    ElMessage.error('加载维修师傅列表失败');
    console.error('加载维修师傅列表失败，错误信息:', error);
  }
};

// 分配师傅
const handleAssign = (repair) => {
  selectedRepairForAssign.value = repair;
  assignForm.value.technicianId = '';
  assignDialogVisible.value = true;
};

// 提交分配
const handleSubmitAssign = async () => {
  await assignFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 模拟提交分配
        console.log('提交分配:', assignForm.value);
        const repair = repairList.value.find(item => item.id === selectedRepairForAssign.value.id);
        if (repair) {
          repair.status = 'processing';
          repair.assignedTo = assignForm.value.technicianId;
          repair.assignedAt = new Date();
        }
        ElMessage.success('分配成功');
        assignDialogVisible.value = false;
      } catch (error) {
        ElMessage.error('分配失败');
        console.error('分配失败，错误信息:', error);
      }
    }
  });
};

// 提交处理结果
const handleSubmitResult = async () => {
  await processFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        // 模拟提交处理结果
        console.log('提交处理结果:', processForm.value);
        const repair = repairList.value.find(item => item.id === processForm.value.repairId);
        if (repair) {
          repair.status = 'completed';
          repair.completedAt = new Date();
        }
        ElMessage.success('提交处理结果成功');
        dialogVisible.value = false;
      } catch (error) {
        ElMessage.error('提交处理结果失败');
        console.error('提交处理结果失败，错误信息:', error);
      }
    }
  });
};

// 初始化数据
onMounted(async () => {
  // 获取用户类型
  const userStr = localStorage.getItem('user');
  if (userStr) {
    try {
      const user = JSON.parse(userStr);
      userType.value = user.type;
    } catch (error) {
      console.error('解析用户信息失败:', error);
    }
  }
  await loadTechnicians();
  await loadRepairs();
});
</script>

<style scoped>
.repair-management-container {
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
}

.status-filters .el-button {
  margin-right: 10px;
}
</style>
