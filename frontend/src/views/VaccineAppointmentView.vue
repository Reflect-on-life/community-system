<template>
  <div class="vaccine-appointment-container">
    <h2>疫苗接种预约</h2>
    
    <!-- 疫苗预约列表 -->
    <el-card class="info-card">
      <template #header>
        <div class="card-header">
          <span>疫苗预约记录</span>
        </div>
      </template>
      
      <el-table :data="vaccineAppointments" style="width: 100%">
        <el-table-column prop="userId" label="居民姓名" width="120">
          <template #default="scope">
            {{ scope.row.userName || '未知居民' }}
          </template>
        </el-table-column>
        <el-table-column prop="doctorId" label="预约医生" width="120">
          <template #default="scope">
            {{ scope.row.doctorName || '未知医生' }}
          </template>
        </el-table-column>
        <el-table-column prop="appointmentDate" label="预约日期" width="150" />
        <el-table-column prop="timeSlot" label="预约时段" width="150" />
        <el-table-column prop="appointmentType" label="预约类型" width="120">
          <template #default="scope">
            <el-tag type="success">疫苗接种</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button
              type="danger"
              size="small"
              :disabled="!canCancelAppointment(scope.row.status)"
              @click="cancelAppointment(scope.row)"
            >
              取消预约
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="mt-4">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="totalAppointments"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 响应式数据
const vaccineAppointments = ref([]);
const doctors = ref([]);
const currentPage = ref(1);
const pageSize = ref(10);
const totalAppointments = ref(0);

// 获取用户角色
const getUserRole = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    const user = JSON.parse(userInfo);
    return user.type;
  }
  return '';
};

const userRole = getUserRole();

// 获取医生列表
const fetchDoctors = async () => {
  try {
    const response = await axios.get('/api/appointment/doctors');
    doctors.value = response.data.map(doctor => ({
      id: doctor.id,
      name: doctor.name,
      department: doctor.department || '未知科室'
    }));
  } catch (error) {
    ElMessage.error('获取医生列表失败');
    console.error('获取医生列表失败:', error);
    doctors.value = [];
  }
};

// 获取疫苗预约列表
const fetchVaccineAppointments = async () => {
  try {
    const response = await axios.get('/api/appointment/vaccine/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    });
    
    vaccineAppointments.value = response.data.records.map(item => ({
      id: item.id,
      userId: item.userId,
      userName: item.userName || '未知居民',
      doctorId: item.doctorId,
      doctorName: doctors.value.find(d => d.id === item.doctorId)?.name || '未知医生',
      appointmentDate: new Date(item.date).toISOString().split('T')[0],
      timeSlot: `${new Date(item.date).toTimeString().substring(0, 5)}`,
      appointmentType: '疫苗接种',
      remark: item.remark || '',
      status: item.status
    }));
    totalAppointments.value = response.data.total;
  } catch (error) {
    ElMessage.error('获取疫苗预约列表失败');
    console.error('获取疫苗预约列表失败:', error);
    
    // 使用模拟数据
    vaccineAppointments.value = [
      {
        id: 1,
        userId: 1,
        userName: '张三',
        doctorId: 3,
        doctorName: '李医生',
        appointmentDate: '2026-02-20',
        timeSlot: '09:00',
        appointmentType: '疫苗接种',
        remark: '流感疫苗接种',
        status: 'confirmed'
      },
      {
        id: 2,
        userId: 2,
        userName: '李四',
        doctorId: 5,
        doctorName: '王医生',
        appointmentDate: '2026-02-21',
        timeSlot: '10:00',
        appointmentType: '疫苗接种',
        remark: '新冠疫苗第二针',
        status: 'pending'
      },
      {
        id: 3,
        userId: 3,
        userName: '王五',
        doctorId: 3,
        doctorName: '李医生',
        appointmentDate: '2026-02-22',
        timeSlot: '14:00',
        appointmentType: '疫苗接种',
        remark: '乙肝疫苗接种',
        status: 'completed'
      }
    ];
    totalAppointments.value = vaccineAppointments.value.length;
  }
};

// 取消预约
const cancelAppointment = (appointment) => {
  ElMessageBox.confirm('确定要取消这个预约吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      await axios.put(`/api/appointment/cancel/${appointment.id}`);
      ElMessage.success('取消预约成功');
      fetchVaccineAppointments();
    } catch (error) {
      ElMessage.error('取消预约失败');
      console.error('取消预约失败:', error);
    }
  }).catch(() => {
    // 取消操作
  });
};

// 获取状态类型
const getStatusType = (status) => {
  switch (status) {
    case 'pending':
      return 'warning';
    case 'confirmed':
      return 'success';
    case 'completed':
      return 'info';
    case 'cancelled':
      return 'danger';
    default:
      return '';
  }
};

// 获取状态文本
const getStatusText = (status) => {
  switch (status) {
    case 'pending':
      return '待确认';
    case 'confirmed':
      return '已确认';
    case 'completed':
      return '已完成';
    case 'cancelled':
      return '已取消';
    default:
      return '未知状态';
  }
};

// 判断是否可以取消预约
const canCancelAppointment = (status) => {
  return status === 'pending' || status === 'confirmed';
};

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size;
  fetchVaccineAppointments();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchVaccineAppointments();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchDoctors();
  fetchVaccineAppointments();
});
</script>

<style scoped>
.vaccine-appointment-container {
  padding: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.mt-4 {
  margin-top: 16px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}
</style>
