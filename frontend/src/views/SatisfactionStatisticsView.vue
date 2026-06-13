<template>
  <div class="satisfaction-statistics-container">
    <h2>服务满意度统计</h2>
    
    <!-- 平均分卡片 -->
    <el-card class="average-score-card">
      <div class="score-display">
        <div class="score-value">{{ statisticsData.avgScore }}</div>
        <div class="score-label">平均满意度评分</div>
        <div class="score-range">满分 5.0 分</div>
        <div class="total-reviews">总评价数: {{ statisticsData.total }}</div>
      </div>
    </el-card>
    
    <!-- ECharts柱状图 -->
    <el-card class="chart-card">
      <template #header>
        <div class="card-header">
          <span>评分分布</span>
        </div>
      </template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onBeforeUnmount } from 'vue';
import axios from 'axios';
import * as echarts from 'echarts';
import { ElMessage } from 'element-plus';

const chartRef = ref(null);
let chartInstance = null;
const statisticsData = ref({
  total: 0,
  avgScore: 0,
  scoreDistribution: [
    { score: 1, count: 0 },
    { score: 2, count: 0 },
    { score: 3, count: 0 },
    { score: 4, count: 0 },
    { score: 5, count: 0 }
  ]
});

const fetchSatisfactionStatistics = async () => {
  try {
    const response = await axios.get('/api/statistics/satisfaction');
    statisticsData.value = response.data;
    updateChart();
  } catch (error) {
    ElMessage.error('获取满意度统计数据失败');
    console.error('获取满意度统计数据失败:', error);
  }
};

const initChart = () => {
  if (!chartRef.value) return;
  
  chartInstance = echarts.init(chartRef.value);
  updateChart();
  
  window.addEventListener('resize', handleResize);
};

const updateChart = () => {
  if (!chartInstance) return;
  
  const scores = statisticsData.value.scoreDistribution.map(item => item.score + '星');
  const counts = statisticsData.value.scoreDistribution.map(item => item.count);
  
  const option = {
    title: {
      text: '服务评分分布',
      left: 'center'
    },
    tooltip: {
      trigger: 'axis',
      axisPointer: {
        type: 'shadow'
      }
    },
    grid: {
      left: '3%',
      right: '4%',
      bottom: '3%',
      containLabel: true
    },
    xAxis: {
      type: 'category',
      data: scores,
      axisLabel: {
        interval: 0
      }
    },
    yAxis: {
      type: 'value',
      name: '评价数量'
    },
    series: [
      {
        name: '评价数量',
        type: 'bar',
        data: counts,
        itemStyle: {
          color: function(params) {
            const colors = ['#FF6B6B', '#FFA07A', '#FFD93D', '#6BCB77', '#4D96FF'];
            return colors[params.dataIndex];
          }
        },
        label: {
          show: true,
          position: 'top',
          formatter: '{c}'
        },
        barWidth: '50%'
      }
    ]
  };
  
  chartInstance.setOption(option);
};

const handleResize = () => {
  if (chartInstance) {
    chartInstance.resize();
  }
};

onMounted(() => {
  fetchSatisfactionStatistics();
  initChart();
});

onBeforeUnmount(() => {
  if (chartInstance) {
    chartInstance.dispose();
    window.removeEventListener('resize', handleResize);
  }
});
</script>

<style scoped>
.satisfaction-statistics-container {
  padding: 20px;
}

.average-score-card {
  margin-bottom: 20px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
}

.score-display {
  text-align: center;
  padding: 20px;
}

.score-value {
  font-size: 72px;
  font-weight: bold;
  margin-bottom: 10px;
}

.score-label {
  font-size: 24px;
  margin-bottom: 5px;
}

.score-range {
  font-size: 16px;
  opacity: 0.9;
  margin-bottom: 10px;
}

.total-reviews {
  font-size: 14px;
  opacity: 0.8;
}

.chart-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: bold;
}

.chart-container {
  width: 100%;
  height: 400px;
}
</style>
