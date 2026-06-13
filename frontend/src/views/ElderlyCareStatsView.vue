<template>
  <div class="elderly-stats-container">
    <h1 class="page-title">养老服务统计</h1>
    
    <!-- 顶部统计卡片 -->
    <div class="stats-grid">
      <el-card class="stat-card">
        <div class="stat-icon">👴</div>
        <div class="stat-content">
          <div class="stat-label">老人总数</div>
          <div class="stat-value">{{ basicStats.elderlyCount || 0 }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">🏃</div>
        <div class="stat-content">
          <div class="stat-label">今日活动报名</div>
          <div class="stat-value">{{ basicStats.todayActivitySignups || 0 }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">❤️</div>
        <div class="stat-content">
          <div class="stat-label">今日健康检查</div>
          <div class="stat-value">{{ basicStats.todayHealthChecks || 0 }}</div>
        </div>
      </el-card>
      
      <el-card class="stat-card">
        <div class="stat-icon">📞</div>
        <div class="stat-content">
          <div class="stat-label">紧急呼叫</div>
          <div class="stat-value">{{ basicStats.emergencyCalls || 0 }}</div>
        </div>
      </el-card>
    </div>
    
    <!-- 中部图表区域 -->
    <div class="charts-row">
      <!-- 活动参与趋势折线图 -->
      <el-card class="chart-card">
        <div class="chart-header">
          <span class="chart-title">近7日活动参与趋势</span>
          <span class="chart-icon">📈</span>
        </div>
        <div ref="activityChartRef" class="chart-container"></div>
      </el-card>
      
      <!-- 健康状态分布饼图 -->
      <el-card class="chart-card">
        <div class="chart-header">
          <span class="chart-title">健康状态分布</span>
          <span class="chart-icon">📊</span>
        </div>
        <div ref="healthChartRef" class="chart-container"></div>
      </el-card>
    </div>
    
    <!-- 底部满意度卡片 -->
    <div class="satisfaction-section">
      <el-card class="satisfaction-card">
        <div class="satisfaction-header">养老服务满意度</div>
        <div class="satisfaction-value">{{ satisfactionRate || 0 }}%</div>
        <div class="progress-container">
          <div class="progress-bar" :style="{ width: (satisfactionRate || 0) + '%' }"></div>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick, computed } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import * as echarts from 'echarts';
import { useThemeStore } from '../store/useThemeStore.js';

// 主题 store
const themeStore = useThemeStore();

// 响应式数据
const basicStats = reactive({
  elderlyCount: 0,
  todayActivitySignups: 0,
  todayHealthChecks: 0,
  emergencyCalls: 0
});
const activityTrendData = ref([]);
const healthStatusData = ref([]);
const satisfactionRate = ref(0);

// 图表引用
const activityChartRef = ref(null);
const healthChartRef = ref(null);

// 图表实例
let activityChart = null;
let healthChart = null;

// 计算属性：获取主题颜色
const primaryColor = computed(() => {
  const accent = themeStore.currentAccentColor;
  return `hsl(${accent.primary})`;
});

// 加载数据
const loadData = async () => {
  try {
    // 加载基础统计数据
    const basicResponse = await axios.get('/api/elderly/statistics/basic');
    Object.assign(basicStats, basicResponse.data);
    
    // 加载活动参与趋势数据
    const activityResponse = await axios.get('/api/elderly/statistics/activity-trend');
    activityTrendData.value = activityResponse.data;
    
    // 加载健康状态分布数据
    const healthResponse = await axios.get('/api/elderly/statistics/health-status');
    healthStatusData.value = healthResponse.data;
    
    // 加载满意度数据
    const satisfactionResponse = await axios.get('/api/elderly/statistics/satisfaction');
    satisfactionRate.value = satisfactionResponse.data.rate || 0;
    
    // 等待DOM更新后初始化图表
    await nextTick();
    initCharts();
  } catch (error) {
    ElMessage.error('加载数据失败');
    console.error('加载数据失败:', error);
    
    // 使用默认数据
    basicStats.elderlyCount = 50;
    basicStats.todayActivitySignups = 12;
    basicStats.todayHealthChecks = 8;
    basicStats.emergencyCalls = 2;
    
    activityTrendData.value = [
      { date: '2026-02-19', count: 8 },
      { date: '2026-02-20', count: 10 },
      { date: '2026-02-21', count: 6 },
      { date: '2026-02-22', count: 12 },
      { date: '2026-02-23', count: 9 },
      { date: '2026-02-24', count: 11 },
      { date: '2026-02-25', count: 12 }
    ];
    
    healthStatusData.value = [
      { status: '健康', count: 30 },
      { status: '轻度慢病', count: 15 },
      { status: '重度慢病', count: 5 }
    ];
    
    satisfactionRate.value = 92;
    
    // 等待DOM更新后初始化图表
    await nextTick();
    initCharts();
  }
};

// 初始化图表
const initCharts = () => {
  // 初始化活动参与趋势图表
  if (activityChartRef.value) {
    activityChart = echarts.init(activityChartRef.value);
    updateActivityChart();
  }
  
  // 初始化健康状态分布图表
  if (healthChartRef.value) {
    healthChart = echarts.init(healthChartRef.value);
    updateHealthChart();
  }
};

// 更新活动参与趋势图表
const updateActivityChart = () => {
  if (!activityChart) return;
  
  const data = activityTrendData.value || [];
  const validData = data.filter(item => item && item.date !== undefined && item.count !== undefined);
  const dates = validData.map(item => item.date);
  const counts = validData.map(item => item.count);
  
  const option = {
    tooltip: {
      trigger: 'axis'
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      boundaryGap: false,
      data: dates.length > 0 ? dates : []
    },
    yAxis: {
      type: 'value',
      minInterval: 1
    },
    series: [{
      name: '参与人数',
      type: 'line',
      data: counts.length > 0 ? counts : [],
      smooth: true,
      lineStyle: {
        color: primaryColor.value
      },
      areaStyle: {
        color: new echarts.graphic.LinearGradient(0, 0, 0, 1, [
          { offset: 0, color: primaryColor.value.replace(')', ' / 0.8)') },
          { offset: 1, color: primaryColor.value.replace(')', ' / 0.2)') }
        ])
      }
    }]
  };
  
  activityChart.setOption(option);
};

// 更新健康状态分布图表
const updateHealthChart = () => {
  if (!healthChart) return;
  
  const data = healthStatusData.value || [];
  const validData = data.filter(item => item && item.status !== undefined && item.count !== undefined);
  
  const option = {
    tooltip: {
      trigger: 'item',
      formatter: '{a} <br/>{b}: {c} ({d}%)'
    },
    legend: {
      orient: 'vertical',
      left: 'left'
    },
    series: [{
      name: '健康状态',
      type: 'pie',
      radius: '60%',
      center: ['50%', '50%'],
      data: validData.length > 0 ? validData : [],
      itemStyle: {
        color: function(params) {
          const colors = [primaryColor.value, '#4CAF50', '#FFC107'];
          return colors[params.dataIndex % colors.length];
        }
      },
      emphasis: {
        itemStyle: {
          shadowBlur: 10,
          shadowOffsetX: 0,
          shadowColor: 'rgba(0, 0, 0, 0.5)'
        }
      }
    }]
  };
  
  healthChart.setOption(option);
};

// 处理窗口 resize
const handleResize = () => {
  activityChart?.resize();
  healthChart?.resize();
};

// 组件挂载时
onMounted(() => {
  loadData();
  window.addEventListener('resize', handleResize);
});

// 组件卸载时
onUnmounted(() => {
  window.removeEventListener('resize', handleResize);
  activityChart?.dispose();
  healthChart?.dispose();
});
</script>

<style scoped>
.elderly-stats-container {
  padding: 20px;
  overflow: hidden;
  min-height: 100vh;
  background: var(--background);
}

.page-title {
  font-size: 28px;
  font-weight: 700;
  margin-bottom: 30px;
  color: var(--foreground);
  text-align: center;
}

.stats-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  min-height: 180px;
  height: auto;
  padding: 20px;
  display: flex;
  align-items: center;
  justify-content: space-around;
  background: var(--card);
  color: var(--foreground);
  border-radius: 12px;
  box-shadow: 0 8px 16px rgba(0, 0, 0, 0.1);
  transition: all 0.3s ease;
  border: 1px solid hsl(var(--primary) / 0.2);
  overflow: hidden;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 12px 24px rgba(0, 0, 0, 0.15);
  border-color: hsl(var(--primary));
}

.stat-icon {
  font-size: 36px;
  margin-right: 20px;
}

.stat-content {
  flex: 1;
}

.stat-label {
  font-size: 14px;
  margin-bottom: 10px;
  opacity: 0.8;
  color: var(--muted-foreground);
}

.stat-value {
  font-size: 28px;
  font-weight: 700;
  color: var(--foreground);
}

.charts-row {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(45%, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.chart-card {
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: var(--card);
  border: 1px solid hsl(var(--primary) / 0.2);
}

.chart-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 20px;
  color: var(--foreground);
}

.chart-icon {
  font-size: 20px;
}

.chart-container {
  width: 100%;
  height: 350px;
}

.satisfaction-section {
  margin-bottom: 30px;
}

.satisfaction-card {
  padding: 20px;
  border-radius: 12px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  background: var(--card);
  border: 1px solid hsl(var(--primary) / 0.2);
}

.satisfaction-header {
  font-size: 18px;
  font-weight: 600;
  margin-bottom: 15px;
  color: var(--foreground);
}

.satisfaction-value {
  font-size: 36px;
  font-weight: 700;
  margin-bottom: 20px;
  color: hsl(var(--primary));
}

.progress-container {
  width: 100%;
  height: 10px;
  background: var(--muted);
  border-radius: 5px;
  overflow: hidden;
}

.progress-bar {
  height: 100%;
  background: linear-gradient(90deg, hsl(var(--primary)), hsl(var(--primary-glow)));
  border-radius: 5px;
  transition: width 1s ease;
}

@media (max-width: 768px) {
  .elderly-stats-container {
    padding: 15px;
  }
  
  .page-title {
    font-size: 24px;
    margin-bottom: 20px;
  }
  
  .stats-grid {
    grid-template-columns: repeat(2, 1fr);
    gap: 15px;
    margin-bottom: 20px;
  }
  
  .stat-card {
    min-height: 140px;
    flex-direction: column;
    text-align: center;
    padding: 15px;
  }
  
  .stat-icon {
    font-size: 28px;
    margin-right: 0;
    margin-bottom: 10px;
  }
  
  .stat-value {
    font-size: 24px;
  }
  
  .charts-row {
    grid-template-columns: 1fr;
    gap: 15px;
  }
  
  .chart-container {
    height: 300px;
  }
}

@media (max-width: 480px) {
  .stats-grid {
    grid-template-columns: 1fr;
  }
  
  .stat-card {
    min-height: 120px;
    padding: 12px;
  }
  
  .page-title {
    font-size: 20px;
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>