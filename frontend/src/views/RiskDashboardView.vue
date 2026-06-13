<template>
  <div class="risk-dashboard">
    <h1 class="dashboard-title">风险统计大屏</h1>
    
    <!-- 顶部统计卡片 -->
    <div class="stats-cards">
      <div class="stat-card pending-risk">
        <div class="stat-icon">⏳</div>
        <div class="stat-content">
          <h3>待处理风险</h3>
          <p class="stat-value">{{ pendingRiskCount }}</p>
        </div>
      </div>
    
      <div class="stat-card high-risk">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>高风险数量</h3>
          <p class="stat-value">{{ highRiskCount }}</p>
        </div>
      </div>
      
      <div class="stat-card medium-risk">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>中风险数量</h3>
          <p class="stat-value">{{ mediumRiskCount }}</p>
        </div>
      </div>
      
      <div class="stat-card low-risk">
        <div class="stat-icon">⚠️</div>
        <div class="stat-content">
          <h3>低风险数量</h3>
          <p class="stat-value">{{ lowRiskCount }}</p>
        </div>
      </div>
      
      <div class="stat-card today-risk">
        <div class="stat-icon">📊</div>
        <div class="stat-content">
          <h3>今日风险总数</h3>
          <p class="stat-value">{{ todayRiskCount }}</p>
        </div>
      </div>
    </div>
    
    <!-- 紧急风险预警区域 -->
    <div class="urgent-alerts" v-if="urgentRisks.length > 0">
      <h2 class="alerts-title">🚨 紧急风险预警</h2>
      <div class="alerts-list">
        <div 
          v-for="risk in urgentRisks" 
          :key="risk.id" 
          class="alert-item urgent-high"
        >
          <div class="alert-icon">🔴</div>
          <div class="alert-content">
            <h4>{{ risk.title }}</h4>
            <p class="alert-location">📍 {{ risk.location }}</p>
            <p class="alert-time">⏰ {{ formatTime(risk.createTime) }}</p>
          </div>
          <div class="alert-badge">紧急</div>
        </div>
      </div>
    </div>
    
    <!-- 图表区域 -->
    <div class="charts-area">
      <!-- 风险类型统计饼图 -->
      <!-- <div class="chart-card">
        <h2>风险类型分布</h2>
        <div ref="typePieChartRef" class="chart-container"></div>
      </div> -->
      
      <!-- 风险等级分布饼图 -->
      <div class="chart-card">
        <h2>风险等级分布</h2>
        <div ref="levelChartRef" class="chart-container"></div>
      </div>
      
      <!-- 近 7 日风险趋势折线图 -->
      <div class="chart-card">
        <h2>近 7 日风险趋势</h2>
        <div ref="trendChartRef" class="chart-container"></div>
      </div>
      
      <!-- 风险位置分布柱状图 -->
      <div class="chart-card">
        <h2>风险发生位置统计</h2>
        <div ref="locationChartRef" class="chart-container"></div>
      </div>
    </div>
    
    <!-- 最新风险图片展示墙 -->
    <div class="image-wall">
      <h2 class="wall-title">📸 最新风险图片</h2>
      <div class="wall-grid">
        <div 
          v-for="image in riskImages" 
          :key="image.id" 
          class="image-item"
          @click="showImageDetail(image)"
        >
          <img :src="getImageUrl(image.url)" :alt="image.title" />
          <div class="image-overlay">
            <p class="image-title">{{ image.title }}</p>
            <p class="image-time">{{ formatTime(image.createTime) }}</p>
            <span class="image-level" :class="image.riskLevel.toLowerCase()">
              {{ getRiskLevelText(image.riskLevel) }}
            </span>
          </div>
        </div>
      </div>
    </div>
    
    <!-- 图片详情弹窗 -->
    <div v-if="selectedImage" class="image-modal" @click="closeImageDetail">
      <div class="modal-content" @click.stop>
        <button class="modal-close" @click="closeImageDetail">×</button>
        <img :src="getImageUrl(selectedImage.url)" :alt="selectedImage.title" />
        <div class="modal-info">
          <h3>{{ selectedImage.title }}</h3>
          <p><strong>风险等级：</strong>
            <span class="modal-level" :class="selectedImage.riskLevel.toLowerCase()">
              {{ getRiskLevelText(selectedImage.riskLevel) }}
            </span>
          </p>
          <p><strong>位置：</strong>{{ selectedImage.location }}</p>
          <p><strong>时间：</strong>{{ formatTime(selectedImage.createTime) }}</p>
          <p v-if="selectedImage.description"><strong>描述：</strong>{{ selectedImage.description }}</p>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import * as echarts from 'echarts'
import axios from 'axios'

export default {
  name: 'RiskDashboardView',
  data() {
    return {
      pendingRiskCount: 0,
      highRiskCount: 0,
      mediumRiskCount: 0,
      lowRiskCount: 0,
      todayRiskCount: 0,
      levelChart: null,
      trendChart: null,
      typePieChart: null,
      locationChart: null,
      levelChartRef: null,
      trendChartRef: null,
      typePieChartRef: null,
      locationChartRef: null,
      refreshTimer: null,
      urgentRisks: [], // 紧急风险列表
      riskImages: [], // 风险图片列表
      selectedImage: null // 当前选中的图片
    }
  },
  mounted() {
    // 初始化图表引用
    this.levelChartRef = this.$refs.levelChartRef
    this.trendChartRef = this.$refs.trendChartRef
    this.typePieChartRef = this.$refs.typePieChartRef
    this.locationChartRef = this.$refs.locationChartRef
    
    // 加载数据并渲染图表
    this.loadData()
    
    // 每 30 秒自动刷新
    this.refreshTimer = setInterval(() => {
      this.loadData()
    }, 30000)
    
    // 监听窗口大小变化，调整图表大小
    window.addEventListener('resize', this.handleResize)
  },
  beforeUnmount() {
    // 清理定时器
    if (this.refreshTimer) {
      clearInterval(this.refreshTimer)
      this.refreshTimer = null
    }
    
    // 清理图表实例
    if (this.levelChart) {
      this.levelChart.dispose()
      this.levelChart = null
    }
    if (this.trendChart) {
      this.trendChart.dispose()
      this.trendChart = null
    }
    if (this.typePieChart) {
      this.typePieChart.dispose()
      this.typePieChart = null
    }
    if (this.locationChart) {
      this.locationChart.dispose()
      this.locationChart = null
    }
    
    // 移除事件监听器
    window.removeEventListener('resize', this.handleResize)
  },
  methods: {
    // 加载所有数据
    async loadData() {
      try {
        console.log('=== 开始加载风险统计数据 ===')
        
        // 加载待处理风险数据
        const pendingResponse = await axios.get('/api/risk/statistics/pending')
        this.pendingRiskCount = pendingResponse.data.pending || 0
        console.log('待处理风险:', this.pendingRiskCount)
        
        // 加载风险等级数据
        const levelResponse = await axios.get('/api/risk/statistics/level')
        const levelData = levelResponse.data
        console.log('风险等级数据:', levelData)
        
        // 更新统计卡片数据
        this.highRiskCount = levelData.high || 0
        this.mediumRiskCount = levelData.medium || 0
        this.lowRiskCount = levelData.low || 0
        
        // 渲染风险等级分布饼图
        this.renderLevelChart(levelData)
        
        // 加载近 7 日趋势数据
        const trendResponse = await axios.get('/api/risk/statistics/trend')
        const trendData = trendResponse.data
        console.log('近 7 日趋势数据:', trendData)
        
        // 计算今日风险总数
        const today = new Date().toISOString().split('T')[0]
        const todayData = trendData.find(item => item.date === today)
        this.todayRiskCount = todayData ? todayData.count : 0
        console.log('今日风险:', this.todayRiskCount)
        
        // 渲染近 7 日风险趋势折线图
        this.renderTrendChart(trendData)
        
        // 加载风险类型数据（饼图）
        const typePieResponse = await axios.get('/api/risk/statistics/type-pie')
        const typePieData = typePieResponse.data
        console.log('风险类型数据:', typePieData)
        this.renderTypePieChart(typePieData)
        
        // 加载紧急风险列表
        const urgentResponse = await axios.get('/api/risk/urgent')
        this.urgentRisks = urgentResponse.data || []
        console.log('紧急风险:', this.urgentRisks.length)
        
        // 加载最新风险图片
        const imagesResponse = await axios.get('/api/risk/images/latest')
        this.riskImages = imagesResponse.data || []
        console.log('风险图片:', this.riskImages.length)
        
        // 加载位置统计数据
        const locationResponse = await axios.get('/api/risk/statistics/location')
        const locationData = locationResponse.data
        console.log('位置统计数据:', locationData)
        this.renderLocationChart(locationData)
        
        console.log('=== 数据加载完成 ===')
        
      } catch (error) {
        console.error('加载数据失败:', error)
        console.error('错误详情:', error.response?.data)
      }
    },
    
    // 渲染风险类型分布饼图
    renderTypePieChart(data) {
      if (!this.typePieChartRef) return
      
      this.typePieChart = echarts.init(this.typePieChartRef)
      
      const typeColors = {
        '老人摔倒': '#ff6b6b',
        '路面积水': '#4ecdc4',
        '设施损坏': '#ffe66d',
        '电动车违规充电': '#ff8c42'
      }
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['老人摔倒', '路面积水', '设施损坏', '电动车违规充电']
        },
        series: [
          {
            name: '风险类型',
            type: 'pie',
            radius: ['40%', '70%'],
            center: ['50%', '50%'],
            data: [
              { 
                value: data.elderFall || 0, 
                name: '老人摔倒',
                itemStyle: { color: typeColors['老人摔倒'] }
              },
              { 
                value: data.roadWater || 0, 
                name: '路面积水',
                itemStyle: { color: typeColors['路面积水'] }
              },
              { 
                value: data.facilityDamage || 0, 
                name: '设施损坏',
                itemStyle: { color: typeColors['设施损坏'] }
              },
              { 
                value: data.ebikeCharging || 0, 
                name: '电动车违规充电',
                itemStyle: { color: typeColors['电动车违规充电'] }
              }
            ],
            emphasis: {
              itemStyle: {
                shadowBlur: 10,
                shadowOffsetX: 0,
                shadowColor: 'rgba(0, 0, 0, 0.5)'
              }
            },
            label: {
              fontSize: 12
            }
          }
        ]
      }
      
      this.typePieChart.setOption(option)
    },
    
    // 渲染风险等级分布饼图
    renderLevelChart(data) {
      if (!this.levelChartRef) return
      
      this.levelChart = echarts.init(this.levelChartRef)
      
      const option = {
        tooltip: {
          trigger: 'item',
          formatter: '{a} <br/>{b}: {c} ({d}%)'
        },
        legend: {
          orient: 'vertical',
          left: 'left',
          data: ['高风险', '中风险', '低风险']
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
            }
          }
        ]
      }
      
      this.levelChart.setOption(option)
    },
    
    // 渲染近 7 日风险趋势折线图
    renderTrendChart(data) {
      if (!this.trendChartRef) return
      
      this.trendChart = echarts.init(this.trendChartRef)
      
      const dates = data ? data.map(item => item.date) : []
      const counts = data ? data.map(item => item.count) : []
      
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
          data: dates
        },
        yAxis: {
          type: 'value',
          min: 0
        },
        series: [
          {
            name: '风险事件数',
            type: 'line',
            data: counts,
            smooth: true,
            lineStyle: {
              color: '#1890ff',
              width: 3
            },
            areaStyle: {
              color: (() => {
                try {
                  return new echarts.graphic.LinearGradient(0, 0, 0, 1, [
                    {
                      offset: 0,
                      color: 'rgba(24, 144, 255, 0.5)'
                    },
                    {
                      offset: 1,
                      color: 'rgba(24, 144, 255, 0.1)'
                    }
                  ]);
                } catch (e) {
                  return 'rgba(24, 144, 255, 0.5)';
                }
              })()
            },
            itemStyle: {
              color: '#1890ff'
            }
          }
        ]
      }
      
      this.trendChart.setOption(option)
    },
    
    // 渲染风险位置分布柱状图
    renderLocationChart(data) {
      if (!this.locationChartRef) return
      
      this.locationChart = echarts.init(this.locationChartRef)
      
      const locations = data ? data.map(item => item.location) : []
      const counts = data ? data.map(item => item.count) : []
      
      // 为不同位置分配鲜明的颜色
      const locationColors = [
        '#5470c6', '#91cc75', '#fac858', '#ee6666', 
        '#73c0de', '#3ba272', '#fc8452', '#9a60b4',
        '#ea7ccc', '#2ab7ca', '#f7b5d1', '#d48265'
      ]
      
      const option = {
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
          data: locations,
          axisLabel: {
            interval: 0,
            rotate: 30,
            fontSize: 11
          }
        },
        yAxis: {
          type: 'value',
          min: 0
        },
        series: [
          {
            name: '风险数量',
            type: 'bar',
            data: counts.map((count, index) => ({
              value: count,
              itemStyle: {
                color: locationColors[index % locationColors.length]
              }
            })),
            barWidth: '50%',
            label: {
              show: true,
              position: 'top',
              fontSize: 12,
              fontWeight: 'bold'
            },
            itemStyle: {
              borderRadius: [5, 5, 0, 0]
            }
          }
        ]
      }
      
      this.locationChart.setOption(option)
    },
    
    // 处理窗口大小变化
    handleResize() {
      if (this.levelChart) {
        this.levelChart.resize()
      }
      if (this.trendChart) {
        this.trendChart.resize()
      }
      if (this.typePieChart) {
        this.typePieChart.resize()
      }
      if (this.locationChart) {
        this.locationChart.resize()
      }
    },
    
    // 格式化时间
    formatTime(timeStr) {
      if (!timeStr) return ''
      const date = new Date(timeStr)
      const now = new Date()
      const diff = now - date
      
      // 今天
      if (date.toDateString() === now.toDateString()) {
        const hours = date.getHours().toString().padStart(2, '0')
        const minutes = date.getMinutes().toString().padStart(2, '0')
        return `今天 ${hours}:${minutes}`
      }
      
      // 昨天
      const yesterday = new Date(now)
      yesterday.setDate(yesterday.getDate() - 1)
      if (date.toDateString() === yesterday.toDateString()) {
        return '昨天'
      }
      
      // 其他日期
      const month = (date.getMonth() + 1).toString().padStart(2, '0')
      const day = date.getDate().toString().padStart(2, '0')
      return `${month}-${day}`
    },
    
    // 获取风险等级文本
    getRiskLevelText(level) {
      const levelMap = {
        'HIGH': '高风险',
        'MEDIUM': '中风险',
        'LOW': '低风险'
      }
      return levelMap[level] || level
    },
    
    // 获取图片完整 URL
    getImageUrl(url) {
      if (!url) return ''
      if (url.startsWith('http')) return url
      return `http://localhost:8080${url}`
    },
    
    // 显示图片详情
    showImageDetail(image) {
      this.selectedImage = image
    },
    
    // 关闭图片详情
    closeImageDetail() {
      this.selectedImage = null
    }
  }
}
</script>

<style scoped>
.risk-dashboard {
  padding: 20px;
  min-height: 100vh;
  background-color: #f0f2f5;
}

.dashboard-title {
  text-align: center;
  font-size: 2.5rem;
  margin-bottom: 30px;
  color: #333;
}

.stats-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(250px, 1fr));
  gap: 20px;
  margin-bottom: 30px;
}

.stat-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  display: flex;
  align-items: center;
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.stat-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
}

.stat-icon {
  font-size: 2.5rem;
  margin-right: 20px;
}

.stat-content h3 {
  margin: 0 0 10px 0;
  font-size: 1.1rem;
  color: #666;
}

.stat-value {
  margin: 0;
  font-size: 2rem;
  font-weight: bold;
  color: #333;
}

.high-risk {
  border-left: 4px solid #ff4d4f;
}

.high-risk .stat-value {
  color: #ff4d4f;
}

.medium-risk {
  border-left: 4px solid #faad14;
}

.medium-risk .stat-value {
  color: #faad14;
}

.low-risk {
  border-left: 4px solid #52c41a;
}

.low-risk .stat-value {
  color: #52c41a;
}

.today-risk {
  border-left: 4px solid #1890ff;
}

.today-risk .stat-value {
  color: #1890ff;
}

.pending-risk {
  border-left: 4px solid #faad14;
}

.pending-risk .stat-value {
  color: #faad14;
}

.charts-area {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(350px, 1fr));
  gap: 20px;
}

.chart-card {
  background: white;
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.chart-card h2 {
  margin: 0 0 20px 0;
  font-size: 1.3rem;
  color: #333;
  text-align: center;
}

.chart-container {
  width: 100%;
  height: 300px;
  border-radius: 8px;
  overflow: hidden;
}

.urgent-alerts {
  background: linear-gradient(135deg, #fff5f5 0%, #ffe6e6 100%);
  border: 2px solid #ff4d4f;
  border-radius: 12px;
  padding: 20px;
  margin-bottom: 30px;
  animation: pulse-border 2s infinite;
}

@keyframes pulse-border {
  0%, 100% {
    box-shadow: 0 0 10px rgba(255, 77, 79, 0.3);
  }
  50% {
    box-shadow: 0 0 20px rgba(255, 77, 79, 0.6);
  }
}

.alerts-title {
  margin: 0 0 15px 0;
  font-size: 1.5rem;
  color: #d93025;
  text-align: center;
  font-weight: bold;
}

.alerts-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 15px;
}

.alert-item {
  background: white;
  border-radius: 8px;
  padding: 15px;
  display: flex;
  align-items: center;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  border-left: 4px solid #ff4d4f;
  animation: slide-in 0.3s ease;
}

@keyframes slide-in {
  from {
    opacity: 0;
    transform: translateX(-20px);
  }
  to {
    opacity: 1;
    transform: translateX(0);
  }
}

.alert-item.urgent-high {
  border-left-color: #ff4d4f;
}

.alert-icon {
  font-size: 2rem;
  margin-right: 15px;
}

.alert-content {
  flex: 1;
}

.alert-content h4 {
  margin: 0 0 8px 0;
  font-size: 1rem;
  color: #333;
  font-weight: 600;
}

.alert-location,
.alert-time {
  margin: 4px 0;
  font-size: 0.85rem;
  color: #666;
}

.alert-badge {
  background: #ff4d4f;
  color: white;
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 0.8rem;
  font-weight: bold;
  animation: blink 1.5s infinite;
}

@keyframes blink {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.6;
  }
}

.image-wall {
  background: white;
  border-radius: 12px;
  padding: 20px;
  margin-top: 30px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.wall-title {
  margin: 0 0 20px 0;
  font-size: 1.5rem;
  color: #333;
  text-align: center;
}

.wall-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 15px;
}

.image-item {
  position: relative;
  border-radius: 8px;
  overflow: hidden;
  cursor: pointer;
  aspect-ratio: 16/10;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  transition: transform 0.3s ease, box-shadow 0.3s ease;
}

.image-item:hover {
  transform: scale(1.05);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.2);
}

.image-item img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0,0,0,0.8) 0%, rgba(0,0,0,0.4) 70%, transparent 100%);
  padding: 10px;
  color: white;
  transform: translateY(100%);
  transition: transform 0.3s ease;
}

.image-item:hover .image-overlay {
  transform: translateY(0);
}

.image-title {
  margin: 0 0 5px 0;
  font-size: 0.9rem;
  font-weight: 600;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.image-time {
  margin: 0;
  font-size: 0.75rem;
  opacity: 0.9;
}

.image-level {
  position: absolute;
  top: 10px;
  right: 10px;
  padding: 3px 8px;
  border-radius: 4px;
  font-size: 0.7rem;
  font-weight: bold;
  color: white;
}

.image-level.high {
  background: #ff4d4f;
}

.image-level.medium {
  background: #faad14;
}

.image-level.low {
  background: #52c41a;
}

/* 图片详情弹窗 */
.image-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.85);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 1000;
  animation: fade-in 0.3s ease;
}

@keyframes fade-in {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

.modal-content {
  background: white;
  border-radius: 12px;
  max-width: 800px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
  animation: zoom-in 0.3s ease;
}

@keyframes zoom-in {
  from {
    transform: scale(0.9);
    opacity: 0;
  }
  to {
    transform: scale(1);
    opacity: 1;
  }
}

.modal-close {
  position: absolute;
  top: 10px;
  right: 10px;
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 24px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  transition: background 0.3s ease;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.8);
}

.modal-content img {
  width: 100%;
  max-height: 60vh;
  object-fit: contain;
}

.modal-info {
  padding: 20px;
}

.modal-info h3 {
  margin: 0 0 15px 0;
  color: #333;
}

.modal-info p {
  margin: 10px 0;
  color: #666;
  line-height: 1.6;
}

.modal-level {
  padding: 3px 10px;
  border-radius: 4px;
  font-size: 0.85rem;
  font-weight: bold;
  color: white;
}

.modal-level.high {
  background: #ff4d4f;
}

.modal-level.medium {
  background: #faad14;
}

.modal-level.low {
  background: #52c41a;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .dashboard-title {
    font-size: 2rem;
  }
  
  .stats-cards {
    grid-template-columns: 1fr;
  }
  
  .alerts-list {
    grid-template-columns: 1fr;
  }
  
  .charts-area {
    grid-template-columns: 1fr;
  }
  
  .wall-grid {
    grid-template-columns: repeat(auto-fill, minmax(150px, 1fr));
  }
  
  .chart-container {
    height: 250px;
  }
}
</style>