<template>
  <div class="risk-map-container">
    <el-card class="risk-map-card">
      <template #header>
        <div class="card-header">
          <el-icon><MapLocation /></el-icon>
          <span>风险地图</span>
        </div>
      </template>

      <!-- 图例 -->
      <div class="map-legend">
        <div class="legend-item">
          <span class="legend-dot high"></span>
          <span>高风险</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot medium"></span>
          <span>中风险</span>
        </div>
        <div class="legend-item">
          <span class="legend-dot low"></span>
          <span>低风险</span>
        </div>
      </div>

      <!-- 风险地图区域 -->
      <div class="map-area" ref="mapAreaRef">
        <!-- 背景图片：小区平面图 -->
        <div class="map-background">
          <img src="http://localhost:8080/upload/images/map.png" alt="小区平面图" class="map-image" />
        </div>
        
        <!-- 风险点展示区 -->
        <div class="risk-points-layer">
          <div
            v-for="(point, index) in riskPoints"
            :key="point.id"
            class="risk-point"
            :class="point.riskLevel.toLowerCase()"
            :style="getPointStyle(point)"
          >
            <div class="point-dot"></div>
            <div class="point-pulse"></div>
            
            <!-- 悬浮详情卡片 -->
            <div class="risk-point-tooltip">
              <div class="tooltip-header" :class="point.riskLevel.toLowerCase()">
                <span class="tooltip-title" :title="point.title">{{ truncateText(point.title, 12) }}</span>
                <span class="tooltip-level">{{ getRiskLevelText(point.riskLevel) }}</span>
              </div>
              <div class="tooltip-body">
                <div class="tooltip-item">
                  <el-icon><Warning /></el-icon>
                  <span class="tooltip-text" :title="point.riskType">{{ truncateText(point.riskType, 10) }}</span>
                </div>
                <div class="tooltip-item">
                  <el-icon><Location /></el-icon>
                  <span class="tooltip-text" :title="point.location">{{ truncateText(point.location, 10) }}</span>
                </div>
                <div class="tooltip-item">
                  <el-icon><Clock /></el-icon>
                  <span class="tooltip-text">{{ formatTime(point.createTime) || '未知时间' }}</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 地点标签区（底部） -->
        <div class="locations-layer">
          <div
            v-for="location in locations"
            :key="location.id"
            class="location-label"
            :style="getLocationStyle(location)"
          >
            <el-icon class="location-icon"><Location /></el-icon>
            <span>{{ location.name }}</span>
          </div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, onUnmounted } from 'vue'
import axios from 'axios'
import { MapLocation, Location, Warning, Clock, Picture, ChatDotRound } from '@element-plus/icons-vue'

const mapAreaRef = ref(null)
const riskPoints = ref([])
const locations = ref([])

// 地点配置（根据小区平面图实际位置）
const locationConfig = [
  { id: 1, name: '儿童游乐区', x: 78, y: 65, risks: [] },  // 右下角儿童游乐区
  { id: 2, name: '健身区', x: 55, y: 70, risks: [] },       // 中下部健身区
  { id: 3, name: '游泳池', x: 35, y: 70, risks: [] },       // 左下部游泳池
  { id: 4, name: '中心花园', x: 50, y: 40, risks: [] },     // 中心位置花园
  { id: 5, name: '1 号楼', x: 30, y: 20, risks: [] },       // 左上住宅楼
  { id: 6, name: '2 号楼', x: 70, y: 20, risks: [] },       // 右上住宅楼
  { id: 7, name: '3 号楼', x: 30, y: 50, risks: [] },       // 左中住宅楼
  { id: 8, name: '4 号楼', x: 70, y: 50, risks: [] },       // 右中住宅楼
  { id: 9, name: '前门', x: 50, y: 95, risks: [] },         // 主入口
  { id: 10, name: '后门', x: 50, y: 5, risks: [] },         // 次入口
  { id: 11, name: '停车场', x: 15, y: 35, risks: [] },      // 左侧停车场
  { id: 12, name: '停车场', x: 85, y: 35, risks: [] }       // 右侧停车场
]

// 位置名称映射（后端名称 → 配置名称）
const locationNameMapping = {
  '花园区域': '中心花园',
  '花园': '中心花园',
  '中心花园': '中心花园',
  '绿化': '中心花园',
  '儿童活动区': '儿童游乐区',
  '儿童游乐区': '儿童游乐区',
  '健身区': '健身区',
  '健身广场': '健身区',
  '游泳池': '游泳池',
  '游泳': '游泳池',
  '楼道': '1 号楼',
  '1号楼': '1 号楼',
  '1 号楼': '1 号楼',
  '2号楼': '2 号楼',
  '2 号楼': '2 号楼',
  '3号楼': '3 号楼',
  '3 号楼': '3 号楼',
  '4号楼': '4 号楼',
  '4 号楼': '4 号楼',
  '5号楼': '5 号楼',
  '5 号楼': '5 号楼',
  '前门': '前门',
  '主入口': '前门',
  '大门': '前门',
  '后门': '后门',
  '次入口': '后门',
  '停车场': '停车场',
  '道路': '其他位置',
  '通道': '其他位置',
  '监控区域': '其他位置'
}

// 加载风险数据
const loadRiskData = async () => {
  try {
    const response = await axios.get('/api/risk/map')
    const allRisks = response.data || []
    
    console.log('📦 从后端获取到', allRisks.length, '条风险数据')
    
    // 初始化地点数据
    const locationMap = new Map()
    locationConfig.forEach(loc => {
      locationMap.set(loc.name, { ...loc, risks: [] })
    })
    
    // 将风险按位置分组
    allRisks.forEach(risk => {
      const rawLocationName = risk.riskLocation || risk.location || ''
      // 使用映射表转换位置名称
      const locationName = locationNameMapping[rawLocationName] || rawLocationName
      
      if (locationMap.has(locationName)) {
        // 适配后端返回的字段，使用后端坐标
        const adaptedRisk = {
          id: risk.id,
          title: risk.title || risk.riskType,
          riskType: risk.riskType,
          riskLevel: risk.riskLevel,
          location: locationName,
          createTime: risk.createTime,
          image: risk.imageUrl,
          aiAnalysis: risk.glmResult || risk.baiduLabels,
          coordX: (risk.x || 50) / 100,
          coordY: (risk.y || 50) / 100
        }
        locationMap.get(locationName).risks.push(adaptedRisk)
      } else {
        if (!locationMap.has('其他位置')) {
          locationMap.set('其他位置', { id: 99, name: '其他位置', x: 50, y: 50, risks: [] })
        }
        const adaptedRisk = {
          id: risk.id,
          title: risk.title || risk.riskType,
          riskType: risk.riskType,
          riskLevel: risk.riskLevel,
          location: rawLocationName || '未知位置',
          createTime: risk.createTime,
          image: risk.imageUrl,
          aiAnalysis: risk.glmResult || risk.baiduLabels,
          coordX: (risk.x || 50) / 100,
          coordY: (risk.y || 50) / 100
        }
        locationMap.get('其他位置').risks.push(adaptedRisk)
      }
    })
    
    // 更新地点和风险点数据
    locations.value = Array.from(locationMap.values()).filter(loc => loc.risks.length > 0)
    
    console.log('✅ 匹配到的位置:', locations.value.map(l => `${l.name}(${l.risks.length}条)`))
    
    // 生成风险点（使用后端返回的真实坐标）
    riskPoints.value = []
    locations.value.forEach(location => {
      location.risks.forEach((risk, index) => {
        riskPoints.value.push({
          ...risk,
          locationX: risk.coordX * 100,
          locationY: risk.coordY * 100,
          pointIndex: index,
          totalPoints: location.risks.length
        })
      })
    })
    
    console.log('✅ 风险点总数:', riskPoints.value.length)
    
  } catch (error) {
    console.error('加载风险数据失败:', error)
    
    // 模拟数据 - 用于测试
    locations.value = [
      { id: 1, name: '儿童活动区', x: 20, risks: [
        { id: 1, title: '儿童活动区地面湿滑', riskType: '地面湿滑', riskLevel: 'HIGH', location: '儿童活动区', createTime: new Date().toISOString() }
      ]},
      { id: 2, name: '健身广场', x: 80, risks: [
        { id: 2, title: '健身器材损坏', riskType: '设备损坏', riskLevel: 'MEDIUM', location: '健身广场', createTime: new Date().toISOString() }
      ]},
      { id: 3, name: '休闲长廊', x: 45, risks: [
        { id: 3, title: '长廊座椅损坏', riskType: '设施损坏', riskLevel: 'LOW', location: '休闲长廊', createTime: new Date().toISOString() }
      ]},
      { id: 4, name: '中心花园', x: 60, risks: [
        { id: 4, title: '花园地砖松动', riskType: '地面不平', riskLevel: 'HIGH', location: '中心花园', createTime: new Date().toISOString() }
      ]}
    ]
    
    // 生成风险点
    riskPoints.value = []
    locations.value.forEach(location => {
      location.risks.forEach((risk, index) => {
        let adjustedX = location.x
        if (location.name === '儿童活动区') adjustedX = 20
        if (location.name === '健身广场') adjustedX = 80
        if (location.name === '休闲长廊') adjustedX = 45
        if (location.name === '中心花园') adjustedX = 60
        
        riskPoints.value.push({
          ...risk,
          locationX: adjustedX,
          pointIndex: index,
          totalPoints: location.risks.length
        })
      })
    })
  }
}

// 获取风险点样式
const getPointStyle = (point) => {
  // 使用实际的 X 和 Y 坐标定位
  return {
    left: `${point.locationX}%`,
    top: `${point.locationY}%`,
    transform: 'translate(-50%, -50%)'
  }
}

// 获取地点标签样式
const getLocationStyle = (location) => {
  return {
    left: `${location.x}%`,
    top: `${location.y}%`,
    transform: 'translate(-50%, -50%)'
  }
}

// 获取风险等级文本
const getRiskLevelText = (level) => {
  const levelMap = {
    'HIGH': '高风险',
    'MEDIUM': '中风险',
    'LOW': '低风险'
  }
  return levelMap[level] || level
}

// 格式化时间
const formatTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  const now = new Date()
  
  if (date.toDateString() === now.toDateString()) {
    const hours = date.getHours().toString().padStart(2, '0')
    const minutes = date.getMinutes().toString().padStart(2, '0')
    return `今天 ${hours}:${minutes}`
  }
  
  const yesterday = new Date(now)
  yesterday.setDate(yesterday.getDate() - 1)
  if (date.toDateString() === yesterday.toDateString()) {
    return '昨天'
  }
  
  const month = (date.getMonth() + 1).toString().padStart(2, '0')
  const day = date.getDate().toString().padStart(2, '0')
  const hours = date.getHours().toString().padStart(2, '0')
  const minutes = date.getMinutes().toString().padStart(2, '0')
  return `${month}-${day} ${hours}:${minutes}`
}

// 获取图片完整 URL
const getImageUrl = (url) => {
  if (!url) return ''
  if (url.startsWith('http')) return url
  return `http://localhost:8080${url}`
}

// 文本截断方法
const truncateText = (text, maxLength) => {
  if (!text) return ''
  if (text.length <= maxLength) return text
  return text.substring(0, maxLength) + '...'
}

onMounted(() => {
  loadRiskData()
})
</script>

<style scoped>
.risk-map-container {
  padding: 20px;
  height: 800px;
  overflow: hidden;
}

.risk-map-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  border-radius: 12px;
}

.card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 18px;
  font-weight: bold;
}

.map-legend {
  display: flex;
  justify-content: center;
  gap: 30px;
  padding: 15px 0;
  border-bottom: 1px solid #eee;
  margin-bottom: 20px;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
}

.legend-dot {
  width: 16px;
  height: 16px;
  border-radius: 50%;
  display: inline-block;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.2);
}

.legend-dot.high {
  background: #ff4d4f;
}

.legend-dot.medium {
  background: #faad14;
}

.legend-dot.low {
  background: #52c41a;
}

.map-area {
  position: relative;
  height: 700px;
  background: #f5f7fa;
  border-radius: 8px;
  overflow: hidden;
  border: 2px solid #e0e0e0;
}

/* 背景图片 */
.map-background {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
}

.map-image {
  width: 100%;
  height: 100%;
  object-fit: contain;
}

/* 风险点图层 */
.risk-points-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 60px;
  overflow: hidden;
}

.risk-point {
  position: absolute;
  cursor: pointer;
  transition: all 0.3s ease;
  z-index: 5;
}

.risk-point:hover {
  transform: translate(-50%, -50%) scale(1.3) !important;
  z-index: 100 !important;
}

/* 悬浮详情卡片 */
.risk-point-tooltip {
  position: absolute;
  bottom: 50px;
  left: 50%;
  transform: translateX(-50%) translateY(10px);
  background: white;
  border-radius: 8px;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  min-width: 300px;
  max-width: 360px;
  overflow: hidden;
  opacity: 0;
  visibility: hidden;
  transition: all 0.3s ease;
  z-index: 1000;
  pointer-events: none;
}

.risk-point:hover .risk-point-tooltip {
  opacity: 1;
  visibility: visible;
  transform: translateX(-50%) translateY(0);
}

.tooltip-header {
  padding: 12px 16px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: white;
}

.tooltip-header.high {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4d4f 100%);
}

.tooltip-header.medium {
  background: linear-gradient(135deg, #ffd93d 0%, #faad14 100%);
}

.tooltip-header.low {
  background: linear-gradient(135deg, #6bcf7f 0%, #52c41a 100%);
}

.tooltip-title {
  font-size: 14px;
  font-weight: bold;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  max-width: 180px;
}

.tooltip-level {
  padding: 2px 8px;
  border-radius: 12px;
  font-size: 11px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.3);
}

.tooltip-body {
  padding: 12px 16px;
}

.tooltip-item {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 10px;
  font-size: 13px;
  color: #666;
}

.tooltip-item:last-child {
  margin-bottom: 0;
}

.tooltip-item .el-icon {
  font-size: 16px;
  color: #1890ff;
  flex-shrink: 0;
}

.tooltip-text {
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
  flex: 1;
}

.risk-point .point-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  position: relative;
  z-index: 2;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.risk-point.high .point-dot {
  background: #ff4d4f;
}

.risk-point.medium .point-dot {
  background: #faad14;
}

.risk-point.low .point-dot {
  background: #52c41a;
}

.risk-point .point-pulse {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 24px;
  height: 24px;
  border-radius: 50%;
  z-index: 1;
  animation: pulse 2s ease-out infinite;
}

.risk-point.high .point-pulse {
  background: rgba(255, 77, 79, 0.4);
}

.risk-point.medium .point-pulse {
  background: rgba(250, 173, 20, 0.4);
}

.risk-point.low .point-pulse {
  background: rgba(82, 196, 26, 0.4);
}

@keyframes pulse {
  0% {
    width: 24px;
    height: 24px;
    opacity: 1;
  }
  100% {
    width: 48px;
    height: 48px;
    opacity: 0;
  }
}

@keyframes float {
  0%, 100% {
    transform: translateX(-50%) translateY(0);
  }
  50% {
    transform: translateX(-50%) translateY(-5px);
  }
}

/* 地点标签图层 */
.locations-layer {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  pointer-events: none;
  z-index: 3;
}

.location-label {
  position: absolute;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 4px 10px;
  background: rgba(255, 255, 255, 0.9);
  border-radius: 12px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.15);
  font-size: 12px;
  font-weight: 500;
  color: #333;
  white-space: nowrap;
  pointer-events: auto;
  transition: all 0.3s ease;
}

.location-label:hover {
  background: white;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.2);
  transform: translate(-50%, -50%) scale(1.05);
}

.location-icon {
  font-size: 14px;
  color: #1890ff;
}

/* 风险详情弹窗 */
.risk-detail-modal {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
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
  max-width: 500px;
  max-height: 80vh;
  overflow-y: auto;
  position: relative;
  animation: zoom-in 0.3s ease;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.2);
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
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(0, 0, 0, 0.5);
  color: white;
  font-size: 20px;
  border-radius: 50%;
  cursor: pointer;
  z-index: 10;
  transition: background 0.3s ease;
}

.modal-close:hover {
  background: rgba(0, 0, 0, 0.8);
}

.detail-header {
  padding: 20px;
  border-radius: 12px 12px 0 0;
  color: white;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.detail-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
}

.detail-level {
  padding: 4px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: bold;
  background: rgba(255, 255, 255, 0.3);
}

.detail-header.high {
  background: linear-gradient(135deg, #ff6b6b 0%, #ff4d4f 100%);
}

.detail-header.medium {
  background: linear-gradient(135deg, #ffd93d 0%, #faad14 100%);
}

.detail-header.low {
  background: linear-gradient(135deg, #6bcf7f 0%, #52c41a 100%);
}

.detail-body {
  padding: 20px;
}

.detail-item {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  margin-bottom: 16px;
  font-size: 14px;
  color: #666;
}

.detail-item .el-icon {
  font-size: 18px;
  color: #1890ff;
  margin-top: 2px;
}

.detail-item strong {
  color: #333;
}

.detail-image {
  flex: 1;
  margin-top: 8px;
}

.detail-image img {
  width: 100%;
  max-height: 300px;
  object-fit: contain;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.ai-analysis {
  background: #f0f9ff;
  padding: 15px;
  border-radius: 8px;
  border-left: 4px solid #1890ff;
}

.ai-content {
  flex: 1;
}

.ai-content p {
  margin: 8px 0 0 0;
  line-height: 1.6;
  color: #555;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .map-area {
    height: 300px;
  }
  
  .risk-point .point-dot {
    width: 20px;
    height: 20px;
  }
  
  .location-label {
    font-size: 12px;
    padding: 4px 8px;
  }
  
  .modal-content {
    max-width: 90%;
  }
}
</style>
