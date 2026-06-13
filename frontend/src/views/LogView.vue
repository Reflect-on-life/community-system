<template>
  <div class="log-container">
    <h2>操作日志监控</h2>
    
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="用户姓名">
            <el-input v-model="filterForm.userName" placeholder="请输入用户姓名" style="width: 120px;" />
          </el-form-item>
          <el-form-item label="操作类型">
            <el-input v-model="filterForm.action" placeholder="请输入操作类型" style="width: 150px;" />
          </el-form-item>
          <el-form-item label="时间范围">
            <el-date-picker
              v-model="filterForm.dateRange"
              type="daterange"
              range-separator="至"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              style="width: 250px;"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="logList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column label="用户姓名" width="120">
          <template #default="scope">
            {{ getUserName(scope.row.userId) }}
          </template>
        </el-table-column>
        <el-table-column label="操作类型" width="150">
          <template #default="scope">
            {{ getActionText(scope.row.action) }}
          </template>
        </el-table-column>
        <el-table-column label="操作详情" min-width="300">
          <template #default="scope">
            {{ getDetailText(scope.row.detail) }}
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="200">
          <template #default="scope">
            {{ new Date(scope.row.createdAt).toLocaleString() }}
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
import { ref, reactive, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 筛选表单
const filterForm = reactive({
  userName: '',
  action: '',
  dateRange: []
});

// 用户列表（用于 ID 到姓名的映射和搜索筛选）
const userList = ref([]);

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 日志列表
const logList = ref([]);
const allLogs = ref([]);

// 加载日志列表
const loadLogList = async () => {
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    };
    
    if (filterForm.action) {
      params.action = filterForm.action;
    }
    
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0];
      params.endTime = filterForm.dateRange[1];
    }
    
    const response = await axios.get('/api/admin/logs/page', {
      params
    });
    
    allLogs.value = response.data.records;
    logList.value = filterByUserName(allLogs.value);
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('加载日志列表失败');
    console.error(error);
  }
};

// 根据用户姓名筛选日志
const filterByUserName = (logs) => {
  if (!filterForm.userName) return logs;
  return logs.filter(log => {
    const name = getUserName(log.userId);
    return name.includes(filterForm.userName);
  });
};

// 根据 userId 获取用户姓名
const getUserName = (userId) => {
  if (!userId) return '-';
  const user = userList.value.find(u => u.id === userId);
  return user ? user.name : `用户${userId}`;
};

// 加载用户列表
const loadUsers = async () => {
  try {
    const response = await axios.get('/api/admin/users/page', {
      params: { page: 1, size: 1000 }
    });
    if (response.data && response.data.records) {
      userList.value = response.data.records;
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
  }
};

// 操作类型中文映射（key 为后端返回的实际值）
const actionMap = {
  'AlarmdeleteAlarm': '删除告警',
  'AlarmcreateAlarm': '创建告警',
  'AlarmupdateAlarm': '更新告警',
  'RepaircreateRepair': '提交报修',
  'RepairupdateRepair': '更新报修',
  'RepaircompleteRepair': '完成报修',
  'RepairassignRepair': '分配报修',
  'RepairrateRepair': '评价报修',
  'ComplaintcreateComplaint': '提交投诉',
  'ComplaintupdateComplaint': '更新投诉',
  'ComplaintresolveComplaint': '处理投诉',
  'NoticereleaseNotice': '发布公告',
  'NoticeupdateNotice': '更新公告',
  'NoticedeleteNotice': '删除公告',
  'ActivitycreateActivity': '创建活动',
  'ActivityupdateActivity': '更新活动',
  'ActivitydeleteActivity': '删除活动',
  'ActivitysignupActivity': '报名活动',
  'ExpresscreateExpress': '登记快递',
  'ExpressupdateExpress': '更新快递',
  'ExpresspickupExpress': '领取快递',
  'HealthcreateRecord': '添加健康档案',
  'HealthupdateRecord': '更新健康档案',
  'RiskreportRisk': '上报风险事件',
  'RiskhandleRisk': '处理风险事件',
  'UserupdateProfile': '修改个人信息',
  'UserchangePassword': '修改密码',
  'Userlogin': '用户登录',
  'Userlogout': '用户登出',
  'AppointmentcreateAppointment': '预约服务',
  'AppointmentconfirmAppointment': '确认预约',
  'AppointmentcancelAppointment': '取消预约',
  'AppointmentcompleteAppointment': '完成预约',
  'VaccinesignupVaccine': '预约疫苗',
  'FileuploadFile': '上传文件',
  'ProfileupdateProfile': '修改个人信息',
  'ProfileupdateUserThemePreference': '修改主题偏好',
  'ProfileuploadAvatar': '上传头像',
  'HealthEducationcreateEducation': '创建健康宣教',
  'HealthEducationupdateEducation': '更新健康宣教',
  'HealthEducationdeleteEducation': '删除健康宣教',
  'HealthRemindercreateReminder': '创建慢病提醒',
  'HealthReminderupdateReminder': '更新慢病提醒',
  'HealthReminderdeleteReminder': '删除慢病提醒',
  'Certificationlogin': '认证登录',
  'CertificationsendCode': '认证发送验证码',
  'HouseapplyHouse': '申请房产',
  'HouseapproveHouse': '审批房产',
  'AlarmdetectBehavior': '行为检测',
  'AlarmmarkAsRead': '标记告警已读',
  'AlarmmarkBatchAsRead': '批量标记告警已读',
  'RiskrecognizeMotion': '行为识别',
  'RiskreportRisk': '上报风险事件',
  'RiskhandleRisk': '处理风险事件',
  'CameraAIanalyzeCameraImageFromFile': '摄像头分析本地图片',
  'CameraAIanalyzeCameraImage': '摄像头分析图片',
  'FileuploadMonitorSnapshot': '上传监控截图',
  // 后端返回的中英文混合格式
  '物业AlarmdetectBehavior': '物业行为检测',
  '物业AlarmmarkAsRead': '物业标记告警已读',
  '物业AlarmmarkBatchAsRead': '物业批量标记告警已读',
  '物业Risk事件上报风险': '物业上报风险事件',
  '物业Risk事件recognizeMotion': '物业行为识别',
  '物业FileuploadMonitorSnapshot': '物业上传监控截图',
  '物业CameraAIanalyzeCameraImageFromFile': '物业摄像头分析本地图片',
  '认证sendCode': '认证发送验证码',
  '认证登录': '认证登录',
  '报修rateRepair': '评价报修',
  '管理员认证sendCode': '管理员认证发送验证码',
  '管理员认证登录': '管理员认证登录',
  'FileuploadAvatar': '上传头像',
  // 后端返回的缩写格式
  '预约createAp': '预约服务',
  '预约cancelAppointment': '取消预约'
};

const getActionText = (action) => {
  if (!action) return '-';
  // 精确匹配
  if (actionMap[action]) {
    return actionMap[action];
  }
  // 先匹配中英文混合的关键词（更长、更具体的匹配优先）
  const mixedKeys = ['管理员认证sendCode', '管理员认证登录', '认证sendCode', '认证登录', '报修rateRepair', 'FileuploadAvatar', '预约createAp', '预约cancelAppointment', '物业AlarmdetectBehavior', '物业AlarmmarkAsRead', '物业AlarmmarkBatchAsRead', '物业Risk事件上报风险', '物业Risk事件recognizeMotion', '物业FileuploadMonitorSnapshot', '物业CameraAIanalyzeCameraImageFromFile'];
  for (const key of mixedKeys) {
    if (action.includes(key)) {
      return actionMap[key];
    }
  }
  // 模糊匹配（纯英文关键词）
  const englishKeys = ['AlarmdeleteAlarm', 'AlarmcreateAlarm', 'AlarmupdateAlarm', 'AlarmdetectBehavior', 'RepaircreateRepair', 'RepairupdateRepair', 'RepaircompleteRepair', 'RepairassignRepair', 'RepairrateRepair', 'ComplaintcreateComplaint', 'ComplaintupdateComplaint', 'ComplaintresolveComplaint', 'NoticereleaseNotice', 'NoticeupdateNotice', 'NoticedeleteNotice', 'ActivitycreateActivity', 'ActivityupdateActivity', 'ActivitydeleteActivity', 'ActivitysignupActivity', 'ExpresscreateExpress', 'ExpressupdateExpress', 'ExpresspickupExpress', 'HealthcreateRecord', 'HealthupdateRecord', 'RiskreportRisk', 'RiskhandleRisk', 'UserupdateProfile', 'UserchangePassword', 'Userlogin', 'Userlogout', 'AppointmentcreateAppointment', 'AppointmentconfirmAppointment', 'AppointmentcancelAppointment', 'AppointmentcompleteAppointment', 'VaccinesignupVaccine', 'FileuploadFile', 'ProfileupdateProfile', 'ProfileupdateUserThemePreference', 'ProfileuploadAvatar', 'HealthEducationcreateEducation', 'HealthEducationupdateEducation', 'HealthEducationdeleteEducation', 'HealthRemindercreateReminder', 'HealthReminderupdateReminder', 'HealthReminderdeleteReminder', 'Certificationlogin', 'CertificationsendCode', 'HouseapplyHouse', 'HouseapproveHouse'];
  for (const key of englishKeys) {
    if (action.includes(key)) {
      return actionMap[key];
    }
  }
  return action;
};

// 操作详情中文转换
const getDetailText = (detail) => {
  if (!detail) return '-';
  
  let result = detail;
  
  // 按顺序替换：优先匹配更长的字符串，避免部分替换冲突
  // 中英文混合格式
  result = result.replace(/管理员认证sendCode/g, '管理员认证发送验证码');
  result = result.replace(/管理员认证登录/g, '管理员认证登录');
  result = result.replace(/认证sendCode/g, '认证发送验证码');
  result = result.replace(/报修rateRepair/g, '评价报修');
  result = result.replace(/FileuploadAvatar/g, '上传头像');
  result = result.replace(/物业AlarmdetectBehavior/g, '物业行为检测');
  result = result.replace(/物业AlarmmarkAsRead/g, '物业标记告警已读');
  result = result.replace(/物业AlarmmarkBatchAsRead/g, '物业批量标记告警已读');
  result = result.replace(/物业Risk事件上报风险/g, '物业上报风险事件');
  result = result.replace(/物业Risk事件recognizeMotion/g, '物业行为识别');
  result = result.replace(/物业FileuploadMonitorSnapshot/g, '物业上传监控截图');
  result = result.replace(/物业CameraAIanalyzeCameraImageFromFile/g, '物业摄像头分析本地图片');
  result = result.replace(/预约createAp/g, '预约服务');
  result = result.replace(/预约cancelAppointment/g, '取消预约');
  
  // 纯英文格式
  result = result.replace(/AlarmdeleteAlarm/g, '删除告警');
  result = result.replace(/AlarmcreateAlarm/g, '创建告警');
  result = result.replace(/AlarmupdateAlarm/g, '更新告警');
  result = result.replace(/AlarmdetectBehavior/g, '行为检测');
  result = result.replace(/RepaircreateRepair/g, '提交报修');
  result = result.replace(/RepairupdateRepair/g, '更新报修');
  result = result.replace(/RepaircompleteRepair/g, '完成报修');
  result = result.replace(/RepairassignRepair/g, '分配报修');
  result = result.replace(/RepairrateRepair/g, '评价报修');
  result = result.replace(/ComplaintcreateComplaint/g, '提交投诉');
  result = result.replace(/ComplaintupdateComplaint/g, '更新投诉');
  result = result.replace(/ComplaintresolveComplaint/g, '处理投诉');
  result = result.replace(/NoticereleaseNotice/g, '发布公告');
  result = result.replace(/NoticeupdateNotice/g, '更新公告');
  result = result.replace(/NoticedeleteNotice/g, '删除公告');
  result = result.replace(/ActivitycreateActivity/g, '创建活动');
  result = result.replace(/ActivityupdateActivity/g, '更新活动');
  result = result.replace(/ActivitydeleteActivity/g, '删除活动');
  result = result.replace(/ActivitysignupActivity/g, '报名活动');
  result = result.replace(/ExpresscreateExpress/g, '登记快递');
  result = result.replace(/ExpressupdateExpress/g, '更新快递');
  result = result.replace(/ExpresspickupExpress/g, '领取快递');
  result = result.replace(/HealthcreateRecord/g, '添加健康档案');
  result = result.replace(/HealthupdateRecord/g, '更新健康档案');
  result = result.replace(/RiskreportRisk/g, '上报风险事件');
  result = result.replace(/RiskhandleRisk/g, '处理风险事件');
  result = result.replace(/UserupdateProfile/g, '修改个人信息');
  result = result.replace(/UserchangePassword/g, '修改密码');
  result = result.replace(/Userlogin/g, '登录');
  result = result.replace(/Userlogout/g, '登出');
  result = result.replace(/AppointmentcreateAppointment/g, '预约服务');
  result = result.replace(/AppointmentconfirmAppointment/g, '确认预约');
  result = result.replace(/AppointmentcancelAppointment/g, '取消预约');
  result = result.replace(/AppointmentcompleteAppointment/g, '完成预约');
  result = result.replace(/VaccinesignupVaccine/g, '预约疫苗');
  result = result.replace(/FileuploadFile/g, '上传文件');
  result = result.replace(/ProfileupdateProfile/g, '修改个人信息');
  result = result.replace(/ProfileupdateUserThemePreference/g, '修改主题偏好');
  result = result.replace(/ProfileuploadAvatar/g, '上传头像');
  result = result.replace(/HealthEducationcreateEducation/g, '创建健康宣教');
  result = result.replace(/HealthEducationupdateEducation/g, '更新健康宣教');
  result = result.replace(/HealthEducationdeleteEducation/g, '删除健康宣教');
  result = result.replace(/HealthRemindercreateReminder/g, '创建慢病提醒');
  result = result.replace(/HealthReminderupdateReminder/g, '更新慢病提醒');
  result = result.replace(/HealthReminderdeleteReminder/g, '删除慢病提醒');
  result = result.replace(/Certificationlogin/g, '认证登录');
  result = result.replace(/CertificationsendCode/g, '认证发送验证码');
  result = result.replace(/HouseapplyHouse/g, '申请房产');
  result = result.replace(/HouseapproveHouse/g, '审批房产');
  
  return result;
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  loadLogList();
};

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.size = size;
  loadLogList();
};

// 当前页变更
const handleCurrentChange = (current) => {
  pagination.current = current;
  loadLogList();
};

// 初始化加载
onMounted(async () => {
  await loadUsers();
  loadLogList();
});
</script>

<style scoped>
.log-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}
</style>