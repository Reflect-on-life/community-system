<template>
  <div class="smart-dashboard-container">
    <!-- 页面标题 -->
    <h1 class="dashboard-title">智慧社区综合驾驶舱</h1>
    
    <!-- 第一行：系统概览卡片区域（5 个卡片） -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-icon">👥</div>
        <div class="stat-content">
          <div class="stat-label">总用户数</div>
          <div class="stat-value">{{ statsData.userCount }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">👴</div>
        <div class="stat-content">
          <div class="stat-label">老年人数量</div>
          <div class="stat-value">{{ statsData.elderlyCount }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <div class="stat-label">风险事件总数</div>
          <div class="stat-value">{{ statsData.totalRiskCount }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">🚨</div>
        <div class="stat-content">
          <div class="stat-label">今日风险数量</div>
          <div class="stat-value">{{ statsData.todayRiskCount }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">🔧</div>
        <div class="stat-content">
          <div class="stat-label">今日报修数量</div>
          <div class="stat-value">{{ statsData.todayRepairCount }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 第二行：左右两列布局 -->
    <div class="main-content-row">
      <el-row :gutter="20">
        <!-- 左侧区域：风险监控模块 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="module-card risk-card-clickable" @click="navigateToRiskManagement">
            <template #header>
              <div class="card-header">
                <span class="card-icon">⚠️</span>
                <span class="card-title">风险监控</span>
                <div class="header-actions" @click.stop>
                  <el-radio-group v-model="riskDimension" size="small" @change="onRiskDimensionChange">
                    <el-radio-button value="day">天</el-radio-button>
                    <el-radio-button value="week">周</el-radio-button>
                    <el-radio-button value="month">月</el-radio-button>
                    <el-radio-button value="year">年</el-radio-button>
                  </el-radio-group>
                  <el-button 
                    type="primary" 
                    size="small" 
                    text
                    @click.stop="navigateToRiskManagement"
                  >
                    查看详情 →
                  </el-button>
                </div>
              </div>
            </template>
            <div class="module-content">
              <div class="risk-charts">
                <!-- 风险等级分布 -->
                <div class="chart-item">
                  <h3 class="chart-title">风险等级分布</h3>
                  <div ref="riskLevelChartRef" class="chart-container"></div>
                </div>
                
                <!-- 风险类型统计 -->
                <div class="chart-item">
                  <h3 class="chart-title">风险类型统计</h3>
                  <div ref="riskTypeChartRef" class="chart-container"></div>
                </div>
                
                <!-- 风险趋势图（标题动态变化） -->
                <div class="chart-item">
                  <h3 class="chart-title">{{ trendChartTitle }}</h3>
                  <div ref="riskTrendChartRef" class="chart-container"></div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
        
        <!-- 右侧区域：养老服务统计模块 -->
        <el-col :xs="24" :sm="24" :md="12" :lg="12">
          <el-card class="module-card elderly-card-clickable" @click="navigateToElderlyCare">
            <template #header>
              <div class="card-header">
                <span class="card-icon">❤️</span>
                <span class="card-title">养老服务统计</span>
                <el-button 
                  type="primary" 
                  size="small" 
                  text
                  @click.stop="navigateToElderlyCare"
                >
                  查看详情 →
                </el-button>
              </div>
            </template>
            <div class="module-content">
              <div class="elderly-stats">
                <!-- 基础统计卡片 -->
                <div class="elderly-stat-item">
                  <div class="stat-label">老人总数</div>
                  <div class="stat-value">{{ elderlyStats.elderlyCount }}</div>
                </div>
                
                <!-- 健康状态分布图 -->
                <div class="chart-item">
                  <h3 class="chart-title">健康状态分布</h3>
                  <div ref="healthStatusChartRef" class="chart-container"></div>
                </div>
                
                <!-- 活动参与趋势图 -->
                <div class="chart-item">
                  <h3 class="chart-title">近 7 日活动参与趋势</h3>
                  <div ref="activityTrendChartRef" class="chart-container"></div>
                </div>
                
                <!-- 养老服务满意度 -->
                <div class="chart-item satisfaction-item">
                  <h3 class="chart-title">养老服务满意度</h3>
                  <div class="satisfaction-content">
                    <div class="satisfaction-value">{{ satisfactionRate }}%</div>
                    <div class="progress-bar-container">
                      <div class="progress-bar" :style="{ width: satisfactionRate + '%' }"></div>
                    </div>
                  </div>
                </div>
              </div>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>
    
    <!-- 第三行：风险地图模块（占整行） -->
    <div class="risk-map-row">
      <el-card class="map-card">
        <template #header>
          <div class="card-header">
            <span class="card-icon">🗺️</span>
            <span class="card-title">风险地图</span>
          </div>
        </template>
        <div class="map-content">
          <RiskMap />
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, onUnmounted, computed } from 'vue';
import { useRouter } from 'vue-router';
import { Warning, User, Location } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import * as echarts from 'echarts';
import RiskMap from '../components/RiskMap.vue';

// 路由实例
const router = useRouter();

// 风险统计时间维度（默认按天）
const riskDimension = ref('day');

// 趋势图标题动态计算
const trendChartTitle = computed(() => {
  const titleMap = {
    day: '近 7 日风险趋势',
    week: '近 4 周风险趋势',
    month: '近 6 月风险趋势',
    year: '近 3 年风险趋势'
  };
  return titleMap[riskDimension.value] || '近 7 日风险趋势';
});

// 统计数据
const statsData = reactive({
  userCount: 0,
  elderlyCount: 0,
  totalRiskCount: 0,
  todayRiskCount: 0,
  todayRepairCount: 0
});

// 养老服务统计数据
const elderlyStats = reactive({
  elderlyCount: 0
});

// 养老服务满意度
const satisfactionRate = ref(0);

// 加载状态
const loading = ref(false);

// 图表引用
const riskLevelChartRef = ref(null);
const riskTypeChartRef = ref(null);
const riskTrendChartRef = ref(null);
const healthStatusChartRef = ref(null);
const activityTrendChartRef = ref(null);

// 图表实例
let riskLevelChart = null;
let riskTypeChart = null;
let riskTrendChart = null;
let healthStatusChart = null;
let activityTrendChart = null;
let riskMapChart = null;

/**
 * 加载系统概览数据
 */
const loadDashboardSummary = async () => {
  loading.value = true;
  try {
    const response = await axios.get('/api/admin/dashboard/summary');
    const data = response.data;
    
    // 总用户数
    statsData.userCount = data.userCount || 0;
    
    // 今日报修数量
    statsData.todayRepairCount = data.todayRepairCount || 0;
    
    // 老年人数量暂时设置为 0，后面会从 API 获取真实数据
    statsData.elderlyCount = 0;
    
    // 风险数据需要从其他接口获取
    await loadRiskStats();
    
    // 加载风险监控图表
    await loadRiskCharts();
    
    // 加载养老服务统计数据
    await loadElderlyCareStats();
    
  } catch (error) {
    console.error('加载驾驶舱数据失败:', error);
    ElMessage.error('加载数据失败');
  } finally {
    loading.value = false;
  }
};

/**
 * 加载风险统计数据
 */
const loadRiskStats = async () => {
  try {
    // 获取风险等级统计
    const [levelResponse, todayResponse] = await Promise.all([
      axios.get('/api/risk/statistics/level').catch(() => ({ data: { high: 0, medium: 0, low: 0 } })),
      axios.get('/api/risk/statistics/today').catch(() => ({ data: { todayCount: 0 } }))
    ]);
    
    // 风险总数
    statsData.totalRiskCount = (levelResponse.data.high || 0) + 
                               (levelResponse.data.medium || 0) + 
                               (levelResponse.data.low || 0);
    
    // 今日风险数 - 正确解析接口返回的对象
    statsData.todayRiskCount = todayResponse.data.todayCount || 0;
    
  } catch (error) {
    console.error('加载风险数据失败:', error);
    // 使用默认值
    statsData.totalRiskCount = 0;
    statsData.todayRiskCount = 0;
  }
};

/**
 * 加载风险监控图表数据
 */
const loadRiskCharts = async () => {
  try {
    await nextTick();
    
    // 加载风险等级数据（带时间维度参数）
    const levelResponse = await axios.get('/api/risk/statistics/level', {
      params: { dimension: riskDimension.value }
    });
    renderRiskLevelChart(levelResponse.data);
    
    // 加载风险类型数据（带时间维度参数）
    const typeResponse = await axios.get('/api/risk/statistics/type-pie', {
      params: { dimension: riskDimension.value }
    });
    renderRiskTypeChart(typeResponse.data);
    
    // 加载趋势数据（带时间维度参数）
    const trendResponse = await axios.get('/api/risk/statistics/trend', {
      params: { dimension: riskDimension.value }
    });
    renderRiskTrendChart(trendResponse.data);
    
  } catch (error) {
    console.error('加载风险图表数据失败:', error);
  }
};

/**
 * 风险统计时间维度切换
 */
const onRiskDimensionChange = async () => {
  await loadRiskCharts();
};

/**
 * 加载养老服务统计数据
 */
const loadElderlyCareStats = async () => {
  try {
    // 等待 DOM 渲染完成
    await nextTick();
    
    // 加载基础统计数据
    const basicResponse = await axios.get('/api/elderly/statistics/basic');
    const elderlyCount = basicResponse.data.elderlyCount || 0;
    // 同步更新两个数据源，确保顶部卡片和养老服务统计显示一致
    elderlyStats.elderlyCount = elderlyCount;
    statsData.elderlyCount = elderlyCount;
    
    // 加载健康状态分布数据
    const healthResponse = await axios.get('/api/elderly/statistics/health-status');
    renderHealthStatusChart(healthResponse.data);
    
    // 加载活动参与趋势数据
    const activityResponse = await axios.get('/api/elderly/statistics/activity-trend');
    renderActivityTrendChart(activityResponse.data);
    
    // 加载满意度数据
    const satisfactionResponse = await axios.get('/api/elderly/statistics/satisfaction');
    satisfactionRate.value = satisfactionResponse.data.rate || 0;
    
  } catch (error) {
    console.error('加载养老服务数据失败:', error);
    // 使用默认值
    elderlyStats.elderlyCount = 0;
    satisfactionRate.value = 0;
  }
};

/**
 * 渲染风险等级分布饼图
 */
const renderRiskLevelChart = (data) => {
  if (!riskLevelChartRef.value) return;
  
  // 销毁旧图表
  if (riskLevelChart) {
    riskLevelChart.dispose();
  }
  
  riskLevelChart = echarts.init(riskLevelChartRef.value);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['高风险', '中风险', '低风险'],
      textStyle: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '风险等级',
        type: 'pie',
        radius: '60%',
        center: ['50%', '50%'],
        data: [
          { value: data.high || 0, name: '高风险', itemStyle: { color: '#ff4d4f' } },
          { value: data.medium || 0, name: '中风险', itemStyle: { color: '#faad14' } },
          { value: data.low || 0, name: '低风险', itemStyle: { color: '#52c41a' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          fontSize: 10
        }
      }
    ]
  };
  
  riskLevelChart.setOption(option);
};

/**
 * 渲染风险类型统计饼图
 */
const renderRiskTypeChart = (data) => {
  if (!riskTypeChartRef.value) return;
  
  // 销毁旧图表
  if (riskTypeChart) {
    riskTypeChart.dispose();
  }
  
  riskTypeChart = echarts.init(riskTypeChartRef.value);
  
  // 动态生成图表数据
  const chartData = [];
  const legendData = [];
  const typeColors = {
    '老人摔倒': '#ff6b6b',
    '路面积水': '#4ecdc4',
    '设施损坏': '#ffe66d',
    '电动车违规充电': '#ff8c42',
    '火灾风险': '#ff4d4f'
  };
  
  // 遍历数据对象的所有属性
  for (const key in data) {
    if (data.hasOwnProperty(key) && data[key] > 0) {
      const displayName = key.replace(/_/g, ' '); // 将下划线转换为空格
      chartData.push({
        value: data[key],
        name: displayName,
        itemStyle: { color: typeColors[key] || generateColor(key) }
      });
      legendData.push(displayName);
    }
  }
  
  // 如果所有数据都是 0，使用默认数据
  if (chartData.length === 0) {
    const defaultData = [
      { value: 0, name: '老人摔倒', itemStyle: { color: '#ff6b6b' } },
      { value: 0, name: '路面积水', itemStyle: { color: '#4ecdc4' } },
      { value: 0, name: '设施损坏', itemStyle: { color: '#ffe66d' } },
      { value: 0, name: '电动车违规充电', itemStyle: { color: '#ff8c42' } }
    ];
    chartData.push(...defaultData);
    legendData.push('老人摔倒', '路面积水', '设施损坏', '电动车违规充电');
  }
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: legendData,
      textStyle: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '风险类型',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: chartData,
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          fontSize: 10
        }
      }
    ]
  };
  
  riskTypeChart.setOption(option);
};

/**
 * 为未知类型生成颜色
 */
const generateColor = (key) => {
  const colors = [
    '#ff6b6b', '#4ecdc4', '#ffe66d', '#ff8c42',
    '#409EFF', '#67C23A', '#E6A23C', '#F56C6C',
    '#909399', '#A0CFFF', '#D9ECFF', '#FDE2E2'
  ];
  const index = key.length % colors.length;
  return colors[index];
};

/**
 * 渲染近 7 日风险趋势折线图
 */
const renderRiskTrendChart = (data) => {
  if (!riskTrendChartRef.value) return;
  
  // 销毁旧图表
  if (riskTrendChart) {
    riskTrendChart.dispose();
  }
  
  riskTrendChart = echarts.init(riskTrendChartRef.value);
  
  const dates = data ? data.map(item => item.date) : [];
  const counts = data ? data.map(item => item.count) : [];
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        fontSize: 10,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '风险数量',
        type: 'line',
        smooth: true,
        data: counts,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(64, 158, 255, 0.5)' },
            { offset: 1, color: 'rgba(64, 158, 255, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#409EFF',
          width: 2
        },
        itemStyle: {
          color: '#409EFF'
        }
      }
    ]
  };
  
  riskTrendChart.setOption(option);
};

/**
 * 渲染健康状态分布饼图
 */
const renderHealthStatusChart = (data) => {
  if (!healthStatusChartRef.value) return;
  
  // 销毁旧图表
  if (healthStatusChart) {
    healthStatusChart.dispose();
  }
  
  healthStatusChart = echarts.init(healthStatusChartRef.value);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left',
      data: ['健康', '轻度慢病', '重度慢病'],
      textStyle: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '健康状态',
        type: 'pie',
        radius: ['40%', '70%'],
        center: ['50%', '50%'],
        data: [
          { value: data.healthy || 0, name: '健康', itemStyle: { color: '#52c41a' } },
          { value: data.mildChronic || 0, name: '轻度慢病', itemStyle: { color: '#faad14' } },
          { value: data.severeChronic || 0, name: '重度慢病', itemStyle: { color: '#ff4d4f' } }
        ],
        emphasis: {
          itemStyle: {
            shadowBlur: 10,
            shadowOffsetX: 0,
            shadowColor: 'rgba(0, 0, 0, 0.5)'
          }
        },
        label: {
          fontSize: 10
        }
      }
    ]
  };
  
  healthStatusChart.setOption(option);
};

/**
 * 渲染活动参与趋势折线图
 */
const renderActivityTrendChart = (data) => {
  if (!activityTrendChartRef.value) return;
  
  // 销毁旧图表
  if (activityTrendChart) {
    activityTrendChart.dispose();
  }
  
  activityTrendChart = echarts.init(activityTrendChartRef.value);
  
  const dates = data ? data.map(item => item.date) : [];
  const counts = data ? data.map(item => item.count) : [];
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      top: '10%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates,
      axisLabel: {
        fontSize: 10,
        rotate: 45
      }
    },
    yAxis: {
      type: 'value',
      axisLabel: {
        fontSize: 10
      }
    },
    series: [
      {
        name: '活动参与',
        type: 'line',
        smooth: true,
        data: counts,
        areaStyle: {
          color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
            { offset: 0, color: 'rgba(255, 107, 107, 0.5)' },
            { offset: 1, color: 'rgba(255, 107, 107, 0.05)' }
          ])
        },
        lineStyle: {
          color: '#ff6b6b',
          width: 2
        },
        itemStyle: {
          color: '#ff6b6b'
        }
      }
    ]
  };
  
  activityTrendChart.setOption(option);
};

/**
 * 获取风险等级颜色
 */
const getRiskLevelColor = (level) => {
  const colors = {
    'HIGH': '#ff4d4f',
    'MEDIUM': '#faad14',
    'LOW': '#52c41a'
  };
  return colors[level] || '#909399';
};

/**
 * 获取风险等级文本
 */
const getRiskLevelText = (level) => {
  const texts = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险'
  };
  return texts[level] || level;
};

/**
 * 跳转到风险事件管理页面
 */
const navigateToRiskManagement = () => {
  console.log('🚀 跳转到风险事件管理页面')
  router.push('/risk-management')
}

/**
 * 跳转到养老服务统计页面
 */
const navigateToElderlyCare = () => {
  console.log('🚀 跳转到养老服务统计页面')
  router.push('/elderly-care-stats')  // 养老服务统计专用页面
}

/**
 * 显示风险详情对话框
 */
const showRiskDetailDialog = (riskItem) => {
  let content = `
    <div style="padding: 20px;">
      <h3 style="margin: 0 0 16px 0; color: #303133;">${riskItem.title}</h3>
      
      <el-descriptions :column="1" border>
        <el-descriptions-item label="风险类型">${riskItem.riskType}</el-descriptions-item>
        <el-descriptions-item label="风险等级">
          <el-tag :type="${riskItem.riskLevel === 'HIGH' ? 'danger' : riskItem.riskLevel === 'MEDIUM' ? 'warning' : 'success'}">
            ${getRiskLevelText(riskItem.riskLevel)}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="发生位置">${riskItem.riskLocation}</el-descriptions-item>
      </el-descriptions>
      
      ${riskItem.imageUrl ? `
        <div style="margin-top: 16px;">
          <div style="margin-bottom: 8px; font-weight: bold;">风险图片</div>
          <img src="${riskItem.imageUrl}" style="width: 100%; max-width: 400px; border-radius: 8px;" />
        </div>
      ` : ''}
      
      ${riskItem.glmResult ? `
        <div style="margin-top: 16px; padding: 12px; background: #f5f7fa; border-radius: 8px;">
          <div style="margin-bottom: 8px; font-weight: bold; color: #409EFF;">系统检测</div>
          <div style="color: #606266; line-height: 1.6;">${riskItem.glmResult}</div>
        </div>
      ` : ''}
    </div>
  `;
  
  // 使用 Element Plus 的 MessageBox 显示风险详情
  ElMessageBox.alert(content, '风险详情', {
    dangerouslyUseHTMLString: true,
    confirmButtonText: '关闭',
    customClass: 'risk-detail-dialog'
  });
  
  console.log('风险详情:', riskItem);
};

/**
 * 处理窗口大小变化
 */
const handleResize = () => {
  if (riskLevelChart) {
    riskLevelChart.resize();
  }
  if (riskTypeChart) {
    riskTypeChart.resize();
  }
  if (riskTrendChart) {
    riskTrendChart.resize();
  }
  if (healthStatusChart) {
    healthStatusChart.resize();
  }
  if (activityTrendChart) {
    activityTrendChart.resize();
  }
  if (riskMapChart) {
    riskMapChart.resize();
  }
};

// 页面加载时自动调用
onMounted(() => {
  loadDashboardSummary();
  
  // 监听窗口大小变化
  window.addEventListener('resize', handleResize);
});

// 页面卸载时清理
onUnmounted(() => {
  if (riskLevelChart) {
    riskLevelChart.dispose();
    riskLevelChart = null;
  }
  if (riskTypeChart) {
    riskTypeChart.dispose();
    riskTypeChart = null;
  }
  if (riskTrendChart) {
    riskTrendChart.dispose();
    riskTrendChart = null;
  }
  if (healthStatusChart) {
    healthStatusChart.dispose();
    healthStatusChart = null;
  }
  if (activityTrendChart) {
    activityTrendChart.dispose();
    activityTrendChart = null;
  }
  if (riskMapChart) {
    riskMapChart.dispose();
    riskMapChart = null;
  }
  
  window.removeEventListener('resize', handleResize);
});
</script>

<style scoped>
.smart-dashboard-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf1 100%);
  padding: 24px;
}

/* 页面标题 */
.dashboard-title {
  font-size: 28px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 24px 0;
  text-align: center;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
}

/* 系统概览卡片网格 */
.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 24px;
}

.stat-card {
  border-radius: 16px;
  transition: all 0.3s ease;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.stat-card:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
  background: rgba(255, 255, 255, 1);
}

.stat-card {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 20px;
}

.stat-icon {
  font-size: 40px;
  flex-shrink: 0;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  color: #909399;
  margin-bottom: 8px;
}

.stat-value {
  font-size: 28px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
}

/* 主内容行 */
.main-content-row {
  margin-bottom: 24px;
}

/* 模块卡片 */
.module-card {
  border-radius: 16px;
  height: 750px; /* 增加高度：从 480px 改为 620px，确保所有图表完整显示 */
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
}

.module-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  background: rgba(255, 255, 255, 1);
}

/* 可点击卡片样式 */
.risk-card-clickable,
.elderly-card-clickable {
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.risk-card-clickable::before,
.elderly-card-clickable::before {
  content: '';
  position: absolute;
  top: 0;
  left: -100%;
  width: 100%;
  height: 100%;
  background: linear-gradient(90deg, transparent, rgba(79, 172, 254, 0.1), transparent);
  transition: left 0.5s ease;
  pointer-events: none;
}

.risk-card-clickable:hover::before,
.elderly-card-clickable:hover::before {
  left: 100%;
}

.risk-card-clickable:hover,
.elderly-card-clickable:hover {
  box-shadow: 
    0 12px 32px rgba(79, 172, 254, 0.2),
    0 0 20px rgba(79, 172, 254, 0.1);
  border-color: rgba(79, 172, 254, 0.5);
  transform: translateY(-2px);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 12px;
  flex-wrap: wrap;
}

.card-icon {
  font-size: 24px;
}

.card-title {
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-left: auto;
}

.module-content {
  height: calc(750px - 60px); /* 与卡片高度 750px 匹配 */
  display: flex;
  align-items: center;
  justify-content: center;
}

/* 风险图表区域 */
.risk-charts {
  width: 100%;
  height: 100%;
  padding: 10px;
  overflow: hidden; /* 去掉滚动条 */
  display: flex;
  flex-direction: column;
  gap: 25px; /* 增加间距：从 15px 改为 25px */
}

.chart-item {
  flex: 1;
  min-height: 170px; /* 提高最小高度：从 140px 改为 170px，匹配更高的卡片 */
  border-radius: 8px;
  background: rgba(245, 247, 250, 0.5);
  padding: 15px;
}

.chart-title {
  font-size: 15px;
  font-weight: bold;
  color: #303133;
  margin: 0 0 15px 0; /* 增加标题下方间距 */
  text-align: center;
}

.chart-container {
  width: 100%;
  height: calc(100% - 30px);
  min-height: 170px; /* 提高最小高度：从 140px 改为 170px */
}

/* 养老服务统计区域 */
.elderly-stats {
  width: 100%;
  height: 100%;
  padding: 10px;
  overflow: hidden; /* 去掉滚动条 */
  display: flex;
  flex-direction: column;
  gap: 25px; /* 增加间距：从 15px 改为 25px，与风险图表保持一致 */
}

.elderly-stat-item {
  text-align: center;
  padding: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  border-radius: 12px;
  color: white;
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.3);
}

.elderly-stat-item .stat-label {
  font-size: 14px;
  opacity: 0.9;
  margin-bottom: 8px;
}

.elderly-stat-item .stat-value {
  font-size: 36px;
  font-weight: bold;
  color: white;
}

/* 满意度样式 */
.satisfaction-item {
  background: rgba(255, 245, 224, 0.5) !important;
}

.satisfaction-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  padding: 10px;
}

.satisfaction-value {
  font-size: 32px;
  font-weight: bold;
  color: #ff6b6b;
}

.progress-bar-container {
  width: 100%;
  height: 12px;
  background: rgba(0, 0, 0, 0.1);
  border-radius: 6px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, #ff6b6b 0%, #ff8e53 100%);
  border-radius: 6px;
  transition: width 0.3s ease;
}

.placeholder-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 16px;
  color: #909399;
}

.placeholder-content p {
  margin: 0;
  font-size: 16px;
}

/* 风险地图行 - 大屏自适应 */
.risk-map-row {
  margin-bottom: 24px;
  height: calc(100vh - 200px);
  min-height: 500px; 
}

.map-card {
  border-radius: 16px;
  height: 100%; /* 使用100%高度 */
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.3);
  transition: all 0.3s ease;
  overflow: hidden; /* 关键：禁止滚动 */
}

.map-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  background: rgba(255, 255, 255, 1);
}

.map-content {
  height: 100%; /* 使用100%高度 */
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden; /* 关键：禁止滚动 */
}

.risk-map-container {
  width: 100%;
  height: 100%;
  min-height: 400px;
}

/* 响应式调整 */
@media (max-width: 768px) {
  .smart-dashboard-container {
    padding: 16px;
  }
  
  .dashboard-title {
    font-size: 24px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
    gap: 12px;
  }
  
  .stat-card {
    padding: 16px;
  }
  
  .stat-icon {
    font-size: 32px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .module-card {
    height: 580px;
    margin-bottom: 20px;
  }
  
  .module-content {
    height: calc(580px - 60px);
  }
  
  .map-card {
    height: 100%; /* 自适应 */
  }
  
  .map-content {
    height: 100%; /* 自适应 */
  }
}

@media (max-width: 480px) {
  .dashboard-title {
    font-size: 20px;
  }
  
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    padding: 14px;
  }
  
  .stat-icon {
    font-size: 28px;
  }
  
  .stat-value {
    font-size: 20px;
  }
  
  .module-card {
    height: 520px;
  }
  
  .module-content {
    height: calc(520px - 60px);
  }
  
  .map-card {
    height: 100%; /* 自适应 */
  }
  
  .map-content {
    height: 100%; /* 自适应 */
  }
}
</style>
