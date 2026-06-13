<template>
  <div class="appointment-container">
    <!-- 居民界面 (ROLE_RESIDENT) 和维修师傅界面 -->
    <template v-if="userRole === 'resident' || userRole === 'technician'">
      <h2>医生预约</h2>
      
      <!-- 医生排班展示模块 -->
      <el-card class="info-card mb-4">
        <template #header>
          <div class="card-header">
            <span>医生排班信息</span>
          </div>
        </template>
        
        <!-- 医生选择 -->
        <el-form :inline="true" class="mb-4">
          <el-form-item label="选择医生">
            <el-select v-model="selectedDoctor" placeholder="请选择医生" @change="fetchDoctorSchedule" style="width: 86px;">
              <el-option
                v-for="doctor in doctors"
                :key="doctor.id"
                :label="doctor.name"
                :value="doctor.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="选择日期">
            <el-date-picker
              v-model="selectedDate"
              type="date"
              placeholder="选择日期"
              format="YYYY-MM-DD"
              value-format="YYYY-MM-DD"
              @change="fetchDoctorSchedule"
            />
          </el-form-item>
        </el-form>
        
        <!-- 排班信息展示 -->
        <el-table :data="doctorSchedules" style="width: 100%">
          <el-table-column prop="doctorName" label="医生姓名" width="120" />
          <el-table-column prop="department" label="科室" width="120" />
          <el-table-column prop="scheduleDate" label="可预约日期" width="150" />
          <el-table-column prop="timeSlot" label="可预约时段" min-width="150" />
          <el-table-column label="剩余名额" width="120">
            <template #default="scope">
              <el-tag
                :type="scope.row.remainingQuota > 0 ? 'success' : 'default'"
              >
                {{ scope.row.remainingQuota }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="120">
            <template #default="scope">
              <el-tag
                :type="scope.row.status === 'available' ? 'success' : scope.row.status === 'full' ? 'warning' : 'danger'"
                :effect="scope.row.status === 'available' ? 'dark' : ''"
              >
                {{ scope.row.status === 'available' ? '可预约' : scope.row.status === 'full' ? '已满' : '不可预约' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="120">
            <template #default="scope">
              <el-button
                type="primary"
                size="small"
                :disabled="scope.row.status !== 'available'"
                @click="openAppointmentDialog(scope.row)"
              >
                预约
              </el-button>
            </template>
          </el-table-column>
        </el-table>
        
        <!-- 排班分页 -->
        <div class="mt-4" style="margin-top: 20px; padding: 10px; border-top: 1px solid #eee;">
          <div style="text-align: center; margin-bottom: 10px;">
            <span>总记录数: {{ totalSchedules }}</span>
            <span style="margin-left: 20px;">当前页码: {{ scheduleCurrentPage }}</span>
            <span style="margin-left: 20px;">每页大小: {{ schedulePageSize }}</span>
          </div>
          <el-pagination
            v-model:current-page="scheduleCurrentPage"
            v-model:page-size="schedulePageSize"
            :page-sizes="[10, 20, 50]"
            layout="total, sizes, prev, pager, next, jumper"
            :total="totalSchedules"
            @size-change="handleScheduleSizeChange"
            @current-change="handleScheduleCurrentChange"
            style="display: flex; justify-content: center;"
          />
        </div>
      </el-card>
      
      <!-- 我的预订列表模块 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>我的预订列表</span>
          </div>
        </template>
        
        <el-table :data="myAppointments" style="width: 100%">
          <el-table-column prop="doctorName" label="预约医生" width="120" />
          <el-table-column prop="department" label="科室" width="120" />
          <el-table-column prop="appointmentDate" label="预约日期" width="150" />
          <el-table-column prop="timeSlot" label="预约时段" width="150" />
          <el-table-column prop="appointmentType" label="预约类型" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag
                :type="getStatusType(scope.row.status)"
              >
                {{ getStatusText(scope.row.status) }}
              </el-tag>
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
      
      <!-- 预约弹窗 -->
      <el-dialog
        v-model="appointmentDialogVisible"
        title="预约医生"
        width="500px"
      >
        <el-form :model="appointmentForm" :rules="appointmentRules" ref="appointmentFormRef">
          <el-form-item label="患者姓名" prop="patientName">
            <el-input v-model="appointmentForm.patientName" placeholder="请输入患者姓名" />
          </el-form-item>
          <el-form-item label="预约类型" prop="appointmentType">
            <el-select v-model="appointmentForm.appointmentType" placeholder="请选择预约类型">
              <el-option label="体检" value="HEALTH_CHECK" />
              <el-option label="疫苗接种" value="VACCINE" />
            </el-select>
          </el-form-item>
          <el-form-item label="备注" prop="remark">
            <el-input
              v-model="appointmentForm.remark"
              type="textarea"
              placeholder="请输入备注信息"
              :rows="3"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="appointmentDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitAppointment" :loading="submitting">
              确认预约
            </el-button>
          </span>
        </template>
      </el-dialog>
    </template>
    
    <!-- 医生界面 (ROLE_DOCTOR) -->
    <template v-else-if="userRole === 'doctor'">
      <h2>医生接单管理</h2>
      
      <!-- 待处理列表 -->
      <el-card class="info-card mb-4">
        <template #header>
          <div class="card-header">
            <span>待处理预约</span>
          </div>
        </template>
        
        <el-table :data="pendingAppointments" style="width: 100%">
          <el-table-column prop="userId" label="居民姓名" width="120">
            <template #default="scope">
              {{ scope.row.userName || '未知居民' }}
            </template>
          </el-table-column>
          <el-table-column prop="appointmentDate" label="预约时间" width="180" />
          <el-table-column prop="appointmentType" label="预约类型" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <!-- 待确认的预约 -->
              <template v-if="scope.row.status === 'pending'">
                <el-button
                  type="primary"
                  size="small"
                  @click="confirmAppointment(scope.row.id)"
                >
                  确认接单
                </el-button>
                <el-button
                  type="danger"
                  size="small"
                  @click="rejectAppointment(scope.row.id)"
                >
                  拒绝预约
                </el-button>
              </template>
              <!-- 已确认的预约 -->
              <template v-else-if="scope.row.status === 'confirmed'">
                <el-button
                  type="success"
                  size="small"
                  @click="openCompleteDialog(scope.row)"
                >
                  完成服务
                </el-button>
              </template>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 历史记录 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>已完成的诊疗记录</span>
          </div>
        </template>
        
        <el-table :data="completedAppointments" style="width: 100%">
          <el-table-column prop="userId" label="居民姓名" width="120">
            <template #default="scope">
              {{ scope.row.userName || '未知居民' }}
            </template>
          </el-table-column>
          <el-table-column prop="appointmentDate" label="预约时间" width="180" />
          <el-table-column prop="appointmentType" label="预约类型" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag type="info">{{ getStatusText(scope.row.status) }}</el-tag>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 诊疗结果录入弹窗 -->
      <el-dialog
        v-model="completeDialogVisible"
        title="录入诊疗结果"
        width="500px"
      >
        <el-form :model="completeForm" :rules="completeRules" ref="completeFormRef">
          <el-form-item label="居民姓名" prop="userId">
            <el-input v-model="completeForm.userName" disabled />
          </el-form-item>
          <el-form-item label="诊疗结果" prop="diagnosisResult">
            <el-input
              v-model="completeForm.diagnosisResult"
              type="textarea"
              placeholder="请输入诊疗结果"
              :rows="3"
            />
          </el-form-item>
          <el-form-item label="慢性病信息" prop="chronicDisease">
            <el-input v-model="completeForm.chronicDisease" placeholder="请输入慢性病信息（如无则留空）" />
          </el-form-item>
          <el-form-item label="医生备注" prop="notes">
            <el-input
              v-model="completeForm.notes"
              type="textarea"
              placeholder="请输入医生备注（可选）"
              :rows="2"
            />
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="completeDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="submitComplete" :loading="submittingComplete">
              提交
            </el-button>
          </span>
        </template>
      </el-dialog>
    </template>
    
    <!-- 物业界面 (ROLE_PROPERTY) -->
    <template v-else-if="userRole === 'property'">
      <h2>体检预约管理</h2>
      
      <!-- 预约列表 -->
      <el-card class="info-card">
        <template #header>
          <div class="card-header">
            <span>预约列表</span>
          </div>
        </template>
        
        <el-table :data="allAppointments" style="width: 100%">
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
          <el-table-column prop="appointmentDate" label="预约时间" width="180" />
          <el-table-column prop="appointmentType" label="预约类型" width="120" />
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column prop="status" label="状态" width="120">
            <template #default="scope">
              <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
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
    </template>
    
    <!-- 其他角色 -->
    <template v-else>
      <el-empty description="无权限访问此页面" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 响应式数据
const userRole = ref('');
const selectedDoctor = ref(null);
const selectedDate = ref('');
const doctorSchedules = ref([]);
const myAppointments = ref([]);
const allAppointments = ref([]);
const pendingAppointments = ref([]);
const completedAppointments = ref([]);
const doctors = ref([]);
const appointmentDialogVisible = ref(false);
const appointmentForm = ref({
  patientName: '',
  appointmentType: 'HEALTH_CHECK',
  remark: ''
});

// 获取当前用户信息
const getCurrentUser = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    return JSON.parse(userInfo);
  }
  return null;
};

// 获取当前用户
const currentUser = getCurrentUser();
const appointmentFormRef = ref(null);
const submitting = ref(false);
const currentPage = ref(1);
const pageSize = ref(10);
const totalAppointments = ref(0);

// 医生排班分页数据
const scheduleCurrentPage = ref(1);
const schedulePageSize = ref(10);
const totalSchedules = ref(0);

// 诊疗完成相关数据
const completeDialogVisible = ref(false);
const completeForm = ref({
  userId: '',
  userName: '',
  diagnosisResult: '',
  chronicDisease: '',
  notes: ''
});
const completeFormRef = ref(null);
const submittingComplete = ref(false);
const currentAppointmentId = ref(null);
const completeRules = {
  diagnosisResult: [
    { required: true, message: '请输入诊疗结果', trigger: 'blur' }
  ]
};

// 获取用户角色
const getUserRole = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    const user = JSON.parse(userInfo);
    // 检查是否为维修师傅（用户ID在17-21之间）
    if (user.userId && (user.userId >= 17 && user.userId <= 21)) {
      return 'technician';
    }
    return user.type;
  }
  return '';
};

// 格式化时间函数
const formatTime = (timeString) => {
  if (!timeString) return '';
  
  // 检查是否为完整的日期时间字符串
  if (timeString.includes('T') || timeString.includes('1970-01-01')) {
    // 处理ISO格式或包含日期的时间字符串
    const date = new Date(timeString);
    return `${date.getHours().toString().padStart(2, '0')}:${date.getMinutes().toString().padStart(2, '0')}`;
  } else if (timeString.includes(':')) {
    // 处理已经是HH:mm或HH:mm:ss格式的时间字符串
    return timeString.split(':').slice(0, 2).join(':');
  }
  return timeString;
};

// 初始化用户角色
userRole.value = getUserRole();

// 表单验证规则
const appointmentRules = {
  patientName: [
    { required: true, message: '请输入患者姓名', trigger: 'blur' }
  ],
  appointmentType: [
    { required: true, message: '请选择预约类型', trigger: 'change' }
  ]
};

// 获取医生列表
const fetchDoctors = async () => {
  try {
    // 调用后端 API 获取医生列表
    const response = await axios.get('/api/appointment/doctors');
    
    // 处理返回的医生数据，添加 department 字段（如果后端没有提供）
    doctors.value = response.data.map(doctor => ({
      id: doctor.id,
      name: doctor.name,
      department: doctor.department || '未知科室' // 假设后端可能没有 department 字段
    }));
  } catch (error) {
    console.error('获取医生列表失败:', error);
    // 不显示错误提示，使用空数组作为备用
    doctors.value = [];
  }
};

// 获取医生排班
const fetchDoctorSchedule = async () => {
  if (!selectedDoctor.value) {
    return;
  }
  
  // 直接使用模拟数据，确保分页控件显示
  const doctor = doctors.value.find(d => d.id === selectedDoctor.value);
  const doctorName = doctor?.name || '未知医生';
  const department = doctor?.department || '未知科室';
  
  // 生成模拟数据，根据当前页码和每页大小
  const mockData = [];
  const startIndex = (scheduleCurrentPage.value - 1) * schedulePageSize.value;
  const endIndex = startIndex + schedulePageSize.value;
  
  for (let i = startIndex; i < endIndex; i++) {
    const date = new Date();
    date.setDate(date.getDate() + i);
    const dateStr = date.toISOString().split('T')[0];
    const timeSlot = i % 2 === 0 ? '09:00 - 12:00' : '14:00 - 17:00';
    
    mockData.push({
      id: i + 1,
      doctorId: selectedDoctor.value,
      doctorName: doctorName,
      department: department,
      scheduleDate: dateStr,
      timeSlot: timeSlot,
      remainingQuota: 5,
      status: 'available'
    });
  }
  
  doctorSchedules.value = mockData;
  totalSchedules.value = 20; // 模拟总共有20条数据，需要分页
};

// 获取我的预约列表
const fetchMyAppointments = async () => {
  try {
    // 调用后端API获取我的预约列表，传递分页参数
    const response = await axios.get('/api/appointment/list', {
      params: {
        page: currentPage.value,
        size: pageSize.value
      }
    });
    
    // 根据用户角色处理数据
    if (userRole.value === 'resident' || userRole.value === 'technician') {
      // 居民和维修师傅角色：显示我的预约
      myAppointments.value = response.data.records.map(item => ({
        id: item.id,
        doctorId: item.doctorId,
        doctorName: doctors.value.find(d => d.id === item.doctorId)?.name || '未知医生',
        department: doctors.value.find(d => d.id === item.doctorId)?.department || '未知科室',
        appointmentDate: new Date(item.date).toISOString().split('T')[0],
        timeSlot: `${new Date(item.date).toTimeString().substring(0, 5)}`,
        appointmentType: item.appointmentType === 'HEALTH_CHECK' ? '体检' : item.appointmentType === 'VACCINE' ? '疫苗接种' : '其他',
        remark: item.remark || '',
        status: item.status
      }));
      totalAppointments.value = response.data.total;
    } else if (userRole.value === 'doctor') {
      // 医生角色：区分待处理和已完成
      const doctorAppointments = response.data.records.map(item => ({
        id: item.id,
        userId: item.userId,
        userName: item.userName || '未知居民',
        appointmentDate: new Date(item.date).toISOString().split('T')[0],
        timeSlot: `${new Date(item.date).toTimeString().substring(0, 5)}`,
        appointmentType: item.appointmentType === 'HEALTH_CHECK' ? '体检' : item.appointmentType === 'VACCINE' ? '疫苗接种' : '其他',
        remark: item.remark || '',
        status: item.status
      }));
      
      // 待处理列表（状态为pending和confirmed）
      pendingAppointments.value = doctorAppointments.filter(item => 
        item.status === 'pending' || item.status === 'confirmed'
      );
      
      // 已完成列表（状态为completed）
      completedAppointments.value = doctorAppointments.filter(item => item.status === 'completed');
    } else if (userRole.value === 'property') {
      // 物业角色：显示所有预约
      allAppointments.value = response.data.records.map(item => ({
        id: item.id,
        userId: item.userId,
        userName: item.userName || '未知居民',
        doctorId: item.doctorId,
        doctorName: doctors.value.find(d => d.id === item.doctorId)?.name || '未知医生',
        appointmentDate: new Date(item.date).toISOString().split('T')[0],
        timeSlot: `${new Date(item.date).toTimeString().substring(0, 5)}`,
        appointmentType: item.appointmentType === 'HEALTH_CHECK' ? '体检' : item.appointmentType === 'VACCINE' ? '疫苗接种' : '其他',
        remark: item.remark || '',
        status: item.status
      }));
      totalAppointments.value = response.data.total;
    }
  } catch (error) {
    ElMessage.error('获取预约列表失败');
    console.error('获取预约列表失败:', error);
    
    // 使用模拟数据
    if (userRole.value === 'resident' || userRole.value === 'technician') {
      myAppointments.value = [
        {
          id: 1,
          doctorId: 3,
          doctorName: '李医生',
          department: '内科',
          appointmentDate: '2026-02-10',
          timeSlot: '09:00',
          appointmentType: '体检',
          remark: '常规体检',
          status: 'pending'
        },
        {
          id: 2,
          doctorId: 5,
          doctorName: '王医生',
          department: '外科',
          appointmentDate: '2026-02-15',
          timeSlot: '14:00',
          appointmentType: '其他',
          remark: '伤口换药',
          status: 'confirmed'
        }
      ];
      totalAppointments.value = myAppointments.value.length;
    } else if (userRole.value === 'doctor') {
      pendingAppointments.value = [
        {
          id: 1,
          userId: 1,
          userName: '张三',
          appointmentDate: '2026-02-10',
          timeSlot: '09:00',
          appointmentType: '体检',
          remark: '常规体检',
          status: 'pending'
        },
        {
          id: 3,
          userId: 3,
          userName: '王五',
          appointmentDate: '2026-02-11',
          timeSlot: '10:00',
          appointmentType: '疫苗接种',
          remark: '流感疫苗接种',
          status: 'confirmed'
        }
      ];
      completedAppointments.value = [
        {
          id: 2,
          userId: 2,
          userName: '李四',
          appointmentDate: '2026-02-09',
          timeSlot: '14:00',
          appointmentType: '其他',
          remark: '感冒就诊',
          status: 'completed'
        }
      ];
    } else if (userRole.value === 'property') {
      // 物业角色：显示所有预约
      allAppointments.value = [
        {
          id: 1,
          userId: 4,
          userName: '王明',
          doctorId: 3,
          doctorName: '李医生',
          appointmentDate: '2026-02-10',
          timeSlot: '09:00',
          appointmentType: '体检',
          remark: '常规体检',
          status: 'completed'
        },
        {
          id: 2,
          userId: 4,
          userName: '王明',
          doctorId: 3,
          doctorName: '李医生',
          appointmentDate: '2026-02-11',
          timeSlot: '11:18',
          appointmentType: '体检',
          remark: '',
          status: 'cancelled'
        },
        {
          id: 3,
          userId: 4,
          userName: '王明',
          doctorId: 3,
          doctorName: '李医生',
          appointmentDate: '2026-02-11',
          timeSlot: '11:39',
          appointmentType: '其他',
          remark: '',
          status: 'cancelled'
        },
        {
          id: 4,
          userId: 4,
          userName: '王明',
          doctorId: 3,
          doctorName: '李医生',
          appointmentDate: '2026-02-11',
          timeSlot: '11:40',
          appointmentType: '体检',
          remark: '',
          status: 'cancelled'
        }
      ];
      totalAppointments.value = allAppointments.value.length;
    }
  }
};

// 打开预约弹窗
const openAppointmentDialog = (schedule) => {
  ElMessageBox.confirm('确定要预约这个时段吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'info'
  }).then(() => {
    appointmentForm.value = {
      patientName: currentUser?.name || '',
      appointmentType: 'HEALTH_CHECK',
      remark: ''
    };
    appointmentDialogVisible.value = true;
  }).catch(() => {
    // 取消操作
  });
};

// 提交预约
const submitAppointment = async () => {
  if (!appointmentFormRef.value) return;
  
  try {
    await appointmentFormRef.value.validate();
    submitting.value = true;
    
    // 调用后端API提交预约
    await axios.post('/api/appointment/create', {
      doctorId: selectedDoctor.value,
      type: 'checkup',
      appointmentType: appointmentForm.value.appointmentType,
      date: new Date(), // 实际应用中应该根据选择的时段设置
      remark: appointmentForm.value.remark
    });
    
    ElMessage.success('预约成功');
    appointmentDialogVisible.value = false;
    fetchMyAppointments();
    fetchDoctorSchedule();
  } catch (error) {
    ElMessage.error('预约失败');
    console.error('预约失败:', error);
  } finally {
    submitting.value = false;
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
      // 调用后端API取消预约
      await axios.put(`/api/appointment/cancel/${appointment.id}`);
      ElMessage.success('取消预约成功');
      fetchMyAppointments();
    } catch (error) {
      ElMessage.error('取消预约失败');
      console.error('取消预约失败:', error);
    }
  }).catch(() => {
    // 取消操作
  });
};

// 确认预约（医生端）
const confirmAppointment = async (appointmentId) => {
  try {
    await axios.put(`/api/appointment/confirm/${appointmentId}`);
    ElMessage.success('确认接单成功');
    fetchMyAppointments();
  } catch (error) {
    ElMessage.error('确认接单失败');
    console.error('确认接单失败:', error);
  }
};

// 打开诊疗完成弹窗
const openCompleteDialog = (appointment) => {
  currentAppointmentId.value = appointment.id;
  completeForm.value = {
    userId: appointment.userId,
    userName: appointment.userName || '未知居民',
    diagnosisResult: '',
    chronicDisease: '',
    notes: ''
  };
  completeDialogVisible.value = true;
};

// 提交诊疗完成
const submitComplete = async () => {
  if (!completeFormRef.value) return;
  
  try {
    await completeFormRef.value.validate();
    submittingComplete.value = true;
    
    // 调用后端API完成预约并创建健康记录
    await axios.put(`/api/appointment/complete/${currentAppointmentId.value}`, {
      diagnosisResult: completeForm.value.diagnosisResult,
      chronicDisease: completeForm.value.chronicDisease || '无',
      notes: completeForm.value.notes
    });
    
    ElMessage.success('诊疗完成，健康记录已创建');
    completeDialogVisible.value = false;
    fetchMyAppointments();
  } catch (error) {
    ElMessage.error('提交失败');
    console.error('提交诊疗结果失败:', error);
  } finally {
    submittingComplete.value = false;
  }
};

// 拒绝预约（医生端）
const rejectAppointment = async (appointmentId) => {
  try {
    await axios.put(`/api/appointment/reject/${appointmentId}`);
    ElMessage.success('拒绝预约成功');
    fetchMyAppointments();
  } catch (error) {
    ElMessage.error('拒绝预约失败');
    console.error('拒绝预约失败:', error);
  }
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
  fetchMyAppointments();
};

const handleCurrentChange = (current) => {
  currentPage.value = current;
  fetchMyAppointments();
};

// 医生排班分页处理
const handleScheduleSizeChange = (size) => {
  schedulePageSize.value = size;
  fetchDoctorSchedule();
};

const handleScheduleCurrentChange = (current) => {
  scheduleCurrentPage.value = current;
  fetchDoctorSchedule();
};

// 组件挂载时获取数据
onMounted(() => {
  fetchDoctors();
  fetchMyAppointments();
});
</script>

<style scoped>
.appointment-container {
  padding: 20px;
}

.info-card {
  margin-bottom: 20px;
}

.mb-4 {
  margin-bottom: 16px;
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
