<template>
  <div class="doctor-workplace">
    <h1>医生工作台</h1>
    
    <!-- 预约审核列表 -->
    <el-card class="appointment-card">
      <template #header>
        <div class="card-header">
          <span>预约审核列表</span>
        </div>
      </template>
      <el-table :data="pendingAppointments" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="用户姓名" width="120">
          <template #default="scope">
            {{ scope.row.userName || '未知' }}
          </template>
        </el-table-column>
        <el-table-column prop="type" label="预约类型">
          <template #default="scope">
            {{ getAppointmentTypeText(scope.row.type) }}
          </template>
        </el-table-column>
        <el-table-column prop="date" label="预约时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.date) }}
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <!-- 待确认的预约 -->
            <template v-if="scope.row.status === 'pending'">
              <el-button 
                type="primary" 
                size="small" 
                @click="handleConfirm(scope.row.id)"
              >
                确认预约
              </el-button>
              <el-button 
                type="danger" 
                size="small" 
                @click="handleReject(scope.row.id)"
              >
                拒绝预约
              </el-button>
            </template>
            <!-- 已确认的预约 -->
            <template v-else-if="scope.row.status === 'confirmed'">
              <el-button 
                type="success" 
                size="small" 
                @click="handleComplete(scope.row.id)"
              >
                完成服务
              </el-button>
            </template>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 健康档案查询 -->
    <el-card class="health-card">
      <template #header>
        <div class="card-header">
          <span>健康档案查询</span>
        </div>
      </template>
      <el-form :inline="true" :model="searchForm" class="search-form">
        <el-form-item label="姓名">
          <el-input v-model="searchForm.userName" placeholder="请输入姓名"></el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="searchHealthRecords">查询</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="healthRecords" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="userName" label="姓名"></el-table-column>
        <el-table-column prop="doctorName" label="医生姓名"></el-table-column>
        <el-table-column prop="chronicDisease" label="慢性病"></el-table-column>
        <el-table-column prop="notes" label="备注"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 重点人群列表 -->
    <el-card class="key-persons-card">
      <template #header>
        <div class="card-header">
          <span>重点人群（高血压/糖尿病）</span>
        </div>
      </template>
      <el-table :data="keyPersons" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column label="用户姓名" width="120">
          <template #default="scope">
            {{ getUserName(scope.row.userId) }}
          </template>
        </el-table-column>
        <el-table-column label="负责医生" width="120">
          <template #default="scope">
            {{ getDoctorName(scope.row.doctorId) }}
          </template>
        </el-table-column>
        <el-table-column prop="chronicDisease" label="慢性病"></el-table-column>
        <el-table-column prop="notes" label="备注"></el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.createdAt) }}
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 健康记录表录入弹窗 -->
    <el-dialog
      v-model="healthRecordDialogVisible"
      title="录入诊疗结果"
      width="500px"
    >
      <el-form :model="healthRecordForm" :rules="healthRecordRules" ref="healthRecordFormRef">
        <el-form-item label="用户ID" prop="userId">
          <el-input v-model="healthRecordForm.userId" disabled />
        </el-form-item>
        <el-form-item label="诊疗结果" prop="diagnosisResult">
          <el-input
            v-model="healthRecordForm.diagnosisResult"
            type="textarea"
            placeholder="请输入诊疗结果"
            :rows="3"
          />
        </el-form-item>
        <el-form-item label="慢性病信息" prop="chronicDisease">
          <el-input v-model="healthRecordForm.chronicDisease" placeholder="请输入慢性病信息（如无则留空）" />
        </el-form-item>
        <el-form-item label="医生备注" prop="notes">
          <el-input
            v-model="healthRecordForm.notes"
            type="textarea"
            placeholder="请输入医生备注（可选）"
            :rows="2"
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="healthRecordDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHealthRecord" :loading="submitting">
            提交
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElDialog, ElForm, ElFormItem, ElInput, ElButton } from 'element-plus';

const pendingAppointments = ref([]);
const healthRecords = ref([]);
const keyPersons = ref([]);
const userList = ref([]);
const doctorList = ref([]);
const searchForm = ref({
  userName: ''
});

// 健康记录表录入相关数据
const healthRecordDialogVisible = ref(false);
const healthRecordForm = ref({
  userId: '',
  diagnosisResult: '',
  chronicDisease: '',
  notes: ''
});
const healthRecordFormRef = ref(null);
const submitting = ref(false);
const currentAppointmentId = ref(null);
const healthRecordRules = {
  diagnosisResult: [
    { required: true, message: '请输入诊疗结果', trigger: 'blur' }
  ]
};

// 获取待办预约列表
const loadPendingAppointments = async () => {
  try {
    // 调用后端 API 获取待办预约列表
    const response = await axios.get('/api/appointment/list', {
      params: { page: 1, size: 100 }
    });
    console.log('API 响应:', response.data);
    // 从分页数据中获取 records
    const allAppointments = response.data.records || [];
    // 过滤出状态为 pending 和 confirmed 的预约
    pendingAppointments.value = allAppointments.filter(item => 
      item.status === 'pending' || item.status === 'confirmed'
    );
    console.log('加载待办预约列表成功，响应数据:', pendingAppointments.value);
  } catch (error) {
    ElMessage.error('加载待办预约列表失败');
    console.error('加载待办预约列表失败，错误信息:', error);
    
    // 使用模拟数据
    pendingAppointments.value = [
      {
        id: 1,
        userId: 4,
        doctorId: 3,
        type: 'checkup',
        date: new Date('2026-02-10 09:00:00'),
        status: 'pending',
        createdAt: new Date()
      },
      {
        id: 2,
        userId: 5,
        doctorId: 3,
        type: 'vaccine',
        date: new Date('2026-02-11 14:00:00'),
        status: 'confirmed',
        createdAt: new Date()
      }
    ];
  }
};

// 获取健康档案列表
const searchHealthRecords = async () => {
  try {
    console.log('开始查询健康档案，userName:', searchForm.value.userName);
    
    // 先加载所有用户（居民和医生）
    if (userList.value.length === 0) {
      const userResponse = await axios.get('/api/admin/users/page', {
        params: { page: 1, size: 1000 }
      });
      if (userResponse.data && userResponse.data.records) {
        userList.value = userResponse.data.records.filter(u => u.type === 'resident');
        doctorList.value = userResponse.data.records.filter(u => u.type === 'doctor');
      }
    }
    
    // 根据姓名过滤用户
    let matchedUsers = [];
    if (searchForm.value.userName) {
      matchedUsers = userList.value.filter(user => 
        user.name.toLowerCase().includes(searchForm.value.userName.toLowerCase())
      );
    } else {
      matchedUsers = userList.value;
    }
    
    // 获取这些用户的健康档案
    const healthPromises = matchedUsers.map(async (user) => {
      try {
        const response = await axios.get('/api/health/records', {
          params: { userId: user.id }
        });
        if (response.data && response.data.success) {
          const records = response.data.data || [];
          // 为每条记录添加用户姓名和医生姓名
          return records.map(record => {
            const doctor = doctorList.value.find(d => d.id === record.doctorId);
            return {
              ...record,
              userName: user.name,
              doctorName: doctor ? doctor.name : '-'
            };
          });
        }
        return [];
      } catch (error) {
        console.error(`获取用户 ${user.name} 的健康档案失败:`, error);
        return [];
      }
    });
    
    const results = await Promise.all(healthPromises);
    healthRecords.value = results.flat();
    
    console.log('查询健康档案成功，响应数据:', healthRecords.value);
  } catch (error) {
    ElMessage.error('查询健康档案失败');
    console.error('查询健康档案失败，错误信息:', error);
  }
};

// 获取重点人群列表
const loadKeyPersons = async () => {
  try {
    console.log('开始加载重点人群列表');
    // 模拟重点人群数据
    keyPersons.value = [];
    console.log('加载重点人群列表成功，响应数据:', keyPersons.value);
  } catch (error) {
    ElMessage.error('加载重点人群列表失败');
    console.error('加载重点人群列表失败，错误信息:', error);
  }
};

// 处理确认预约
const handleConfirm = async (id) => {
  try {
    console.log('开始处理确认预约，id:', id);
    // 调用后端API确认预约
    await axios.put(`/api/appointment/confirm/${id}`);
    
    // 更新本地数据
    const appointment = pendingAppointments.value.find(item => item.id === id);
    if (appointment) {
      appointment.status = 'confirmed';
    }
    
    ElMessage.success('确认接单成功');
    console.log('确认预约成功');
    
    // 刷新列表
    await loadPendingAppointments();
  } catch (error) {
    ElMessage.error('确认接单失败');
    console.error('确认预约失败，错误信息:', error);
  }
};

// 处理拒绝预约
const handleReject = async (id) => {
  try {
    console.log('开始处理拒绝预约，id:', id);
    // 调用后端API拒绝预约（假设使用取消接口）
    await axios.put(`/api/appointment/cancel/${id}`);
    
    // 更新本地数据
    const appointmentIndex = pendingAppointments.value.findIndex(item => item.id === id);
    if (appointmentIndex !== -1) {
      pendingAppointments.value.splice(appointmentIndex, 1);
    }
    
    ElMessage.success('拒绝预约成功');
    console.log('拒绝预约成功');
  } catch (error) {
    ElMessage.error('拒绝预约失败');
    console.error('拒绝预约失败，错误信息:', error);
  }
};

// 处理完成服务
const handleComplete = async (id) => {
  try {
    console.log('开始处理完成服务，id:', id);
    
    // 弹出诊疗结果录入弹窗
    const appointmentToComplete = pendingAppointments.value.find(item => item.id === id);
    if (appointmentToComplete) {
      currentAppointmentId.value = id;
      healthRecordForm.value = {
        userId: appointmentToComplete.userId,
        diagnosisResult: '',
        chronicDisease: '',
        notes: ''
      };
      healthRecordDialogVisible.value = true;
    }
  } catch (error) {
    ElMessage.error('打开诊疗结果录入失败');
    console.error('打开诊疗结果录入失败，错误信息:', error);
  }
};

// 提交健康记录表
const submitHealthRecord = async () => {
  if (!healthRecordFormRef.value) return;
  
  try {
    await healthRecordFormRef.value.validate();
    submitting.value = true;
    
    // 调用后端API完成预约并创建健康记录
    await axios.put(`/api/appointment/complete/${currentAppointmentId.value}`, {
      diagnosisResult: healthRecordForm.value.diagnosisResult,
      chronicDisease: healthRecordForm.value.chronicDisease || '无',
      notes: healthRecordForm.value.notes
    });
    
    ElMessage.success('诊疗完成，健康记录已创建');
    healthRecordDialogVisible.value = false;
    
    // 刷新列表
    await loadPendingAppointments();
  } catch (error) {
    ElMessage.error('提交失败');
    console.error('提交健康记录表失败，错误信息:', error);
  } finally {
    submitting.value = false;
  }
};

// 获取预约类型文本
const getAppointmentTypeText = (type) => {
  switch (type) {
    case 'checkup': return '体检';
    case 'vaccine': return '疫苗接种';
    case 'other': return '其他';
    default: return type;
  }
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending': return 'warning';
    case 'confirmed': return 'success';
    case 'completed': return 'info';
    case 'cancelled': return 'danger';
    default: return '';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理';
    case 'confirmed': return '已确认';
    case 'completed': return '已完成';
    case 'cancelled': return '已取消';
    default: return status;
  }
};

// 根据 userId 获取用户姓名
const getUserName = (userId) => {
  const user = userList.value.find(u => u.id === userId);
  return user ? user.name : '未知';
};

// 根据 doctorId 获取医生姓名
const getDoctorName = (doctorId) => {
  const doctor = doctorList.value.find(d => d.id === doctorId);
  return doctor ? doctor.name : '未知';
};

// 加载用户列表
const loadUsers = async () => {
  if (userList.value.length > 0) return;
  try {
    const response = await axios.get('/api/admin/users/page', {
      params: { page: 1, size: 1000 }
    });
    if (response.data && response.data.records) {
      userList.value = response.data.records.filter(u => u.type === 'resident');
      doctorList.value = response.data.records.filter(u => u.type === 'doctor');
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
  }
};

// 格式化日期
const formatDate = (dateStr) => {
  if (!dateStr) return '';
  const date = new Date(dateStr);
  return date.toLocaleString();
};

// 页面加载时初始化数据
onMounted(() => {
  loadPendingAppointments();
  loadKeyPersons();
});
</script>

<style scoped>
.doctor-workplace {
  padding: 2rem;
}

h1 {
  color: #333;
  margin-bottom: 2rem;
}

.appointment-card,
.health-card,
.key-persons-card {
  margin-bottom: 2rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.search-form {
  margin-bottom: 1rem;
}
</style>
