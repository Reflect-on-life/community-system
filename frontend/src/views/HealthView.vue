<template>
  <div class="health-container">
    <h2>健康管理</h2>
    
    <!-- 健康状态概览 -->
    <el-card class="overview-card">
      <template #header>
        <div class="card-header">
          <span>健康状态概览</span>
          <div style="display: flex; gap: 10px; margin-left: auto;">
            <!-- 姓名模糊查询 -->
            <el-input 
              v-model="searchName" 
              placeholder="请输入姓名查询"
              clearable
              style="width: 200px;"
            >
              <template #prefix>
                <el-icon><Search /></el-icon>
              </template>
            </el-input>
            <!-- 医生可以选择居民 -->
            <el-select 
              v-if="isDoctor" 
              v-model="selectedUserId" 
              placeholder="选择居民"
              @change="handleResidentChange"
              clearable
              style="width: 200px;"
            >
              <el-option
                v-for="user in residentList"
                :key="user.id"
                :label="user.name"
                :value="user.id"
              />
            </el-select>
          </div>
        </div>
      </template>
      <div class="overview-stats" v-if="healthStats">
        <div class="stat-item">
          <div class="stat-label">体检次数</div>
          <div class="stat-value">{{ healthStats.recordCount || 0 }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最近BMI</div>
          <div class="stat-value">{{ healthStats.latestBMI || '-' }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最近血压</div>
          <div class="stat-value">{{ healthStats.latestBloodPressure || '-' }}</div>
        </div>
        <div class="stat-item">
          <div class="stat-label">最近血糖</div>
          <div class="stat-value">{{ healthStats.latestBloodSugar || '-' }}</div>
        </div>
      </div>
      <div v-else class="loading-overview">
        <el-skeleton :rows="4" animated />
      </div>
    </el-card>

    <!-- 健康趋势图表 -->
    <el-card class="chart-card" v-if="healthStats && healthStats.trends && healthStats.trends.length > 0">
      <template #header>
        <div class="card-header">
          <span>健康趋势</span>
        </div>
      </template>
      <div ref="trendChart" class="trend-chart"></div>
    </el-card>

    <!-- 健康档案卡片列表 -->
    <div class="records-grid">
      <template v-if="filteredRecords.length > 0">
        <el-card 
          v-for="record in filteredRecords" 
          :key="record.id" 
          class="health-record-card"
          shadow="hover"
          @click="viewDetail(record)"
        >
          <template #header>
            <div class="card-header-content" :class="{ 'center-content': !isDoctor }">
              <!-- 医生端显示居民头像和信息 -->
              <el-avatar v-if="isDoctor" :size="50" :src="getAvatarUrl(record.userId)">
                {{ getInitials(getUserName(record.userId)) }}
              </el-avatar>
              <div v-if="isDoctor" class="user-info">
                <div class="user-name">{{ getUserName(record.userId) }}</div>
                <div class="user-age">{{ getAge(record.userId) }}岁 | {{ getGender(record.userId) }}</div>
              </div>
              <!-- 居民端只显示日期 -->
              <div v-else class="record-date-header">
                <el-icon><Clock /></el-icon>
                {{ formatDate(record.createdAt) }}
              </div>
            </div>
          </template>
          
          <div class="record-content">
            <div class="info-row">
              <div class="info-item">
                <div class="info-label">身高</div>
                <div class="info-value">{{ record.height }}<span class="unit">cm</span></div>
              </div>
              <div class="info-item">
                <div class="info-label">体重</div>
                <div class="info-value">{{ record.weight }}<span class="unit">kg</span></div>
              </div>
              <div class="info-item">
                <div class="info-label">BMI</div>
                <div class="info-value">{{ record.bmi }}</div>
              </div>
            </div>
            
            <div class="info-row">
              <div class="info-item">
                <div class="info-label">血压</div>
                <div class="info-value">{{ record.bloodPressure || '-' }}</div>
              </div>
              <div class="info-item">
                <div class="info-label">血糖</div>
                <div class="info-value">{{ record.bloodSugar || '-' }}</div>
              </div>
            </div>
            
            <div class="chronic-disease" v-if="record.chronicDisease">
              <el-tag type="danger" size="small">
                <el-icon><Warning /></el-icon>
                {{ record.chronicDisease }}
              </el-tag>
            </div>
            
            <div v-if="isDoctor" class="record-date">
              <el-icon><Clock /></el-icon>
              {{ formatDate(record.createdAt) }}
            </div>
          </div>
        </el-card>
      </template>
      <div v-else-if="!loading" class="no-records">
        <el-empty description="暂无健康档案记录" />
      </div>
      <div v-else class="loading-records">
        <el-skeleton :rows="5" animated />
      </div>
    </div>

    <!-- 健康档案详情对话框 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="健康档案详情"
      width="500px"
    >
      <div v-if="selectedRecord" class="record-detail">
        <el-form label-position="top">
          <el-form-item label="体检时间">
            {{ formatDate(selectedRecord.createdAt) }}
          </el-form-item>
          <el-form-item label="基本信息">
            <div class="info-row">
              <span>身高: {{ selectedRecord.height }} cm</span>
              <span>体重: {{ selectedRecord.weight }} kg</span>
              <span>BMI: {{ selectedRecord.bmi }}</span>
            </div>
          </el-form-item>
          <el-form-item label="健康指标">
            <div class="info-row">
              <span>血压: {{ selectedRecord.bloodPressure }}</span>
              <span>血糖: {{ selectedRecord.bloodSugar }}</span>
            </div>
          </el-form-item>
          <el-form-item label="慢性病">
            {{ selectedRecord.chronicDisease || '无' }}
          </el-form-item>
          <el-form-item label="医生建议">
            {{ selectedRecord.doctorSuggestion || '无' }}
          </el-form-item>
          <el-form-item label="备注">
            {{ selectedRecord.notes || '无' }}
          </el-form-item>
        </el-form>
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
import { ref, computed, onMounted, watch, nextTick } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { Search, Refresh, Warning, Document, Clock } from '@element-plus/icons-vue';

// 响应式数据
const healthRecords = ref([]);
const healthStats = ref(null);
const loading = ref(true);
const trendChart = ref(null);
const chartInstance = ref(null);
const detailDialogVisible = ref(false);
const selectedRecord = ref(null);
const isDoctor = ref(false);
const selectedUserId = ref(null);
const residentList = ref([]);
const userList = ref([]);
const searchName = ref('');
const filteredRecords = ref([]);

// 监听姓名搜索框变化
watch(searchName, () => {
  handleNameSearch();
});

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '-';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

// 获取头像 URL
const getAvatarUrl = (userId) => {
  // 可以根据 userId 获取头像，这里返回空使用默认头像
  return '';
};

// 获取姓名首字母
const getInitials = (name) => {
  if (!name) return '居';
  return name.charAt(0);
};

// 获取年龄
const getAge = (userId) => {
  const user = userList.value.find(u => u.id === userId);
  if (!user || !user.birthday) return '-';
  const birthDate = new Date(user.birthday);
  const today = new Date();
  let age = today.getFullYear() - birthDate.getFullYear();
  const monthDiff = today.getMonth() - birthDate.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birthDate.getDate())) {
    age--;
  }
  return age;
};

// 获取性别
const getGender = (userId) => {
  const user = userList.value.find(u => u.id === userId);
  if (!user) return '-';
  return user.gender === '1' ? '男' : '女';
};

// 姓名搜索处理
const handleNameSearch = () => {
  // 确保有数据才进行筛选
  if (!healthRecords.value || healthRecords.value.length === 0) {
    return;
  }
  
  // 从所有健康档案中筛选
  if (!searchName.value) {
    // 如果没有搜索条件，显示所有记录
    filteredRecords.value = [...healthRecords.value];
  } else {
    // 有搜索条件，从健康档案中筛选
    filteredRecords.value = healthRecords.value.filter(record => {
      const user = userList.value.find(u => u.id === record.userId);
      return user && user.name.toLowerCase().includes(searchName.value.toLowerCase());
    });
  }
};

// 居民选择处理
const handleResidentChange = (userId) => {
  // 清空姓名搜索
  searchName.value = '';
  // 重新加载数据
  loadHealthRecords();
};

// 加载健康档案
const loadHealthRecords = async () => {
  loading.value = true;
  try {
    const params = {};
    // 如果是医生且选择了居民，只加载该居民的记录
    if (isDoctor.value && selectedUserId.value) {
      params.userId = selectedUserId.value;
    }
    const response = await axios.get('/api/health/records', { params });
    if (response.data && response.data.success) {
      // 加载原始数据
      healthRecords.value = response.data.data || [];
      // 先加载用户信息，再重置为所有数据
      if (healthRecords.value.length > 0 && userList.value.length === 0) {
        await loadUsers();
      }
      filteredRecords.value = healthRecords.value;
      // 如果姓名搜索框有内容，应用姓名筛选
      if (searchName.value) {
        handleNameSearch();
      }
    }
  } catch (error) {
    console.error('加载健康档案失败:', error);
  } finally {
    loading.value = false;
  }
};

// 加载健康统计信息
const loadHealthStats = async () => {
  try {
    const response = await axios.get('/api/health/stats');
    if (response.data && response.data.success) {
      healthStats.value = response.data.data || null;
      nextTick(() => {
        initChart();
      });
    }
  } catch (error) {
    console.error('加载健康统计信息失败:', error);
  }
};

// 初始化图表
const initChart = () => {
  if (!trendChart.value || !healthStats.value || !healthStats.value.trends) return;
  
  if (chartInstance.value) {
    chartInstance.value.dispose();
  }
  
  chartInstance.value = echarts.init(trendChart.value);
  
  // 准备图表数据
  const trends = healthStats.value.trends || [];
  const dates = trends.map(item => formatDate(item.date)).reverse();
  const bloodSugarData = trends.map(item => item.bloodSugar).reverse();
  
  // 提取血压数据（ systolic/diastolic ）
  const systolicData = trends.map(item => {
    if (item.bloodPressure) {
      const match = item.bloodPressure.match(/(\d+)\/(\d+)/);
      return match ? parseInt(match[1]) : null;
    }
    return null;
  }).reverse();
  
  const diastolicData = trends.map(item => {
    if (item.bloodPressure) {
      const match = item.bloodPressure.match(/(\d+)\/(\d+)/);
      return match ? parseInt(match[2]) : null;
    }
    return null;
  }).reverse();
  
  // 设置图表配置
  const option = {
    title: {
      text: '健康趋势',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis'
    },
    legend: {
      data: ['血糖', '收缩压', '舒张压'],
      bottom: 10
    },
    grid: {
      left: '3%',
      right: '7%',
      bottom: '15%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: dates
    },
    yAxis: [
      {
        type: 'value',
        name: '血糖',
        position: 'left'
      },
      {
        type: 'value',
        name: '血压',
        position: 'right'
      }
    ],
    series: [
      {
        name: '血糖',
        type: 'line',
        data: bloodSugarData,
        smooth: true,
        itemStyle: { color: '#5470C6' }
      },
      {
        name: '收缩压',
        type: 'line',
        yAxisIndex: 1,
        data: systolicData,
        smooth: true,
        itemStyle: { color: '#91CC75' }
      },
      {
        name: '舒张压',
        type: 'line',
        yAxisIndex: 1,
        data: diastolicData,
        smooth: true,
        itemStyle: { color: '#FAC858' }
      }
    ]
  };
  
  chartInstance.value.setOption(option);
  
  // 监听窗口大小变化
  window.addEventListener('resize', () => {
    if (chartInstance.value) {
      chartInstance.value.resize();
    }
  });
};

// 查看详情
const viewDetail = (record) => {
  selectedRecord.value = record;
  detailDialogVisible.value = true;
};

// 刷新数据
const refreshRecords = () => {
  loadHealthRecords();
  loadHealthStats();
};

// 获取用户姓名
const getUserName = (userId) => {
  const user = userList.value.find(u => u.id === userId);
  return user ? user.name : '居民';
};

// 加载居民列表（仅医生）
const loadResidentList = async () => {
  try {
    const response = await axios.get('/api/admin/users/page', {
      params: { type: 'resident', page: 1, size: 100 }
    });
    if (response.data && response.data.records) {
      residentList.value = response.data.records || [];
      // 如果有居民，默认选择第一个
      if (residentList.value.length > 0) {
        selectedUserId.value = residentList.value[0].id;
        // 重新加载健康档案
        loadHealthRecords();
        loadHealthStats();
      }
    }
  } catch (error) {
    console.error('加载居民列表失败:', error);
  }
};

// 加载所有用户（仅医生角色调用）
const loadUsers = async () => {
  // 只有医生角色才需要加载用户列表
  if (!isDoctor.value) {
    return;
  }
  
  try {
    const response = await axios.get('/api/admin/users/page', {
      params: { page: 1, size: 1000 }
    });
    if (response.data && response.data.records) {
      userList.value = response.data.records.filter(u => u.type === 'resident');
    }
  } catch (error) {
    console.error('加载用户列表失败:', error);
  }
};

// 检测用户角色
const checkUserRole = () => {
  const user = localStorage.getItem('user');
  if (user) {
    const userInfo = JSON.parse(user);
    isDoctor.value = userInfo.type === 'doctor';
  }
};

// 组件挂载时加载数据
onMounted(async () => {
  checkUserRole();
  // 先加载用户信息
  await loadUsers();
  if (isDoctor.value) {
    loadResidentList();
  } else {
    loadHealthRecords();
    loadHealthStats();
  }
});
</script>

<style scoped>
.health-container {
  padding: 20px;
}

.overview-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.overview-stats {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 20px;
}

.stat-item {
  text-align: center;
  padding: 15px;
  background-color: #f9f9f9;
  border-radius: 8px;
}

.stat-label {
  font-size: 14px;
  color: #666;
  margin-bottom: 5px;
}

.stat-value {
  font-size: 24px;
  font-weight: bold;
  color: #333;
}

.records-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 20px;
  margin-top: 20px;
}

.health-record-card {
  cursor: pointer;
  transition: all 0.3s ease;
  border-radius: 12px;
}

.health-record-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
}

.card-header-content {
  display: flex;
  align-items: center;
  gap: 12px;
}

.user-info {
  flex: 1;
}

.user-name {
  font-size: 16px;
  font-weight: bold;
  color: #333;
  margin-bottom: 4px;
}

.record-date {
  font-size: 13px;
  color: #999;
}

.user-age {
  font-size: 13px;
  color: #999;
}

.record-date-header {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
}

.center-content {
  justify-content: center;
}

.record-date {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  font-size: 13px;
  color: #999;
  margin-top: 10px;
}

.record-content {
  padding: 10px 0;
}

.info-row {
  display: flex;
  gap: 15px;
  margin-bottom: 15px;
}

.info-item {
  flex: 1;
  text-align: center;
  padding: 10px;
  background: #f5f7fa;
  border-radius: 8px;
}

.info-label {
  font-size: 12px;
  color: #999;
  margin-bottom: 6px;
}

.info-value {
  font-size: 18px;
  font-weight: bold;
  color: #333;
}

.info-value .unit {
  font-size: 12px;
  color: #999;
  margin-left: 2px;
}

.chronic-disease {
  margin-top: 10px;
  text-align: center;
}

.card-footer {
  text-align: center;
}

.no-records {
  padding: 40px 0;
  text-align: center;
}

.loading-records,
.loading-overview {
  padding: 20px 0;
}

.trend-chart {
  width: 100%;
  height: 300px;
  margin-top: 20px;
}

.record-detail {
  padding: 10px 0;
}

.info-row-detail {
  display: flex;
  gap: 20px;
  margin-bottom: 10px;
}

.info-row-detail span {
  flex: 1;
}

@media (max-width: 768px) {
  .overview-stats {
    grid-template-columns: repeat(2, 1fr);
  }
  
  .records-grid {
    grid-template-columns: 1fr;
  }
  
  .card-header {
    flex-direction: column;
    gap: 10px;
  }
  
  .card-header > div {
    width: 100%;
  }
}
</style>
