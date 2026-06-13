<template>
  <div class="monitor-screen">
    <!-- 顶部标题区 -->
    <div class="header-section">
      <h1 class="screen-title">
        <el-icon><Monitor /></el-icon>
        社区智能监控中心
      </h1>
      <div class="header-info">
        <span class="current-time">{{ currentTime }}</span>
        <el-tag type="success" size="large">实时监控中</el-tag>
        <el-button type="warning" size="small" @click="handleRealtimeAnalyze" :loading="analyzing">
          <el-icon><Refresh /></el-icon>
          实时AI分析
        </el-button>
        <el-button type="primary" size="small" @click="handleSyncRisks" :loading="syncing">
          <el-icon><Refresh /></el-icon>
          同步风险事件
        </el-button>
      </div>
    </div>

    <!-- 中间 9 宫格监控画面区 -->
    <div class="camera-grid-section">
      <el-row :gutter="16" class="camera-row">
        <el-col 
          :xs="24" 
          :sm="12" 
          :md="8" 
          :lg="8" 
          v-for="camera in cameraList" 
          :key="camera.id"
        >
          <div 
            class="camera-card" 
            :class="camera.statusClass"
            @click="handleCameraClick(camera)"
          >
            <!-- 监控画面 -->
            <div class="camera-image-wrapper">
              <img :src="camera.image" :alt="camera.title" class="camera-image" />
              
              <!-- Loading 遮罩 -->
              <div class="loading-overlay" v-if="camera.loading">
                <el-icon class="is-loading"><Loading /></el-icon>
                <span>AI 识别中...</span>
              </div>
              
              <!-- 叠加层：左上角摄像头名称 -->
              <div class="camera-name-overlay">
                <el-icon><VideoCamera /></el-icon>
                <span>{{ camera.title }}</span>
              </div>

              <!-- 叠加层：右上角风险标签 -->
              <div class="risk-tag-overlay" v-if="camera.level && camera.level !== 'normal'">
                <el-tag :type="getRiskTagType(camera.level)" effect="dark" size="small">
                  {{ getRiskLevelText(camera.level) }}
                </el-tag>
              </div>

              <!-- 叠加层：底部 AI 识别结果 -->
              <div class="ai-result-overlay" :class="camera.level">
                <div class="ai-result-content">
                  <el-icon v-if="camera.level === 'danger' || camera.level === 'warning'"><Warning /></el-icon>
                  <el-icon v-else><CircleCheck /></el-icon>
                  <span>{{ camera.aiResult || '等待上传...' }}</span>
                </div>
              </div>

              <!-- 上传按钮（hover 时显示） -->
              <div class="upload-overlay">
                <el-upload
                  :show-file-list="false"
                  :http-request="(options) => handleUpload(options, camera)"
                  :before-upload="beforeUpload"
                  accept="image/*"
                  :disabled="camera.loading"
                >
                  <el-button type="primary" size="small" :loading="camera.loading">
                    <el-icon><Upload /></el-icon>
                    上传识别
                  </el-button>
                </el-upload>
              </div>
            </div>
          </div>
        </el-col>
      </el-row>
    </div>

    <!-- 点击放大弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="selectedCamera?.title"
      width="70%"
      class="camera-dialog"
      destroy-on-close
    >
      <div class="dialog-camera-view" v-if="selectedCamera">
        <img :src="selectedCamera.image" :alt="selectedCamera.title" class="dialog-image" />
        <div class="dialog-overlay">
          <div class="dialog-risk-info" :class="selectedCamera.level">
            <el-tag :type="getRiskTagType(selectedCamera.level)" effect="dark" size="large">
              {{ selectedCamera.aiResult }}
            </el-tag>
          </div>
        </div>
      </div>
      
      <!-- 风险事件详情卡片 -->
      <div class="dialog-risk-details" v-if="selectedCamera">
        <el-descriptions title="风险事件详情" :column="2" border>
          <el-descriptions-item label="风险类型">
            <el-tag :type="getRiskTagType(selectedCamera.level)" size="small">
              {{ getRiskTypeFromText(selectedCamera.aiResult) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="风险等级">
            <el-tag :type="getRiskTagType(selectedCamera.level)" effect="dark">
              {{ getRiskLevelText(selectedCamera.level) }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="发生时间">
            <el-icon><Timer /></el-icon>
            {{ currentTime }}
          </el-descriptions-item>
          <el-descriptions-item label="发生位置">
            <el-icon><Location /></el-icon>
            {{ selectedCamera.title }}
          </el-descriptions-item>
          <el-descriptions-item label="当前状态" :span="2">
            <el-tag :type="selectedCamera.level === 'normal' ? 'success' : 'warning'" size="large">
              {{ selectedCamera.level === 'normal' ? '正常场景' : '待处理' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
        
        <!-- 处理提示 -->
        <div class="dialog-actions-info">
          <el-alert
            :title="selectedCamera.level === 'normal' ? '该场景正常' : '发现风险事件（已入库）'"
            :description="selectedCamera.level !== 'normal' ? '风险事件已保存到数据库，请前往风险管理页面处理' : ''"
            :type="selectedCamera.level === 'normal' ? 'success' : 'warning'"
            :closable="false"
            show-icon
          />
          <div class="dialog-actions-hint" v-if="selectedCamera.level !== 'normal'">
            <el-button type="primary" size="large" @click="goToRiskManagement">
              <el-icon><ArrowRight /></el-icon>
              前往风险管理
            </el-button>
          </div>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { 
  Monitor, 
  VideoCamera, 
  Bell, 
  Timer, 
  Location, 
  Warning, 
  CircleCheck,
  Refresh,
  Loading,
  Upload,
  Delete,
  ArrowRight
} from '@element-plus/icons-vue'
import { ElMessage } from 'element-plus'
import { 
  uploadAndRecognize, 
  getLatestRiskEvents,
  createRiskEvent,
  RISK_LEVEL_MAP,
  RISK_TYPE_MAP
} from '../api/risk'
import axios from 'axios'
import { useRouter } from 'vue-router'

const router = useRouter()

interface Camera {
  id: number
  title: string
  image: string
  aiResult: string
  level: 'danger' | 'warning' | 'low' | 'normal'
  statusClass: string
  loading: boolean
  online: boolean
  riskEventId?: number | null // 关联的风险事件 ID
}

const currentTime = ref('')
const timeInterval = ref<ReturnType<typeof setInterval> | null>(null)
const syncInterval = ref<ReturnType<typeof setInterval> | null>(null)
const dialogVisible = ref(false)
const selectedCamera = ref<Camera | null>(null)
const syncing = ref(false)
const analyzing = ref(false)

// 9 个摄像头数据（添加 riskEventId 用于关联风险事件）
const cameraList = ref<Camera[]>([
  {
    id: 1,
    title: '摄像头 1 - 花园区域',
    image: '/upload/images/ScreenShot_2026-03-20_143104_752.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 2,
    title: '摄像头 2 - 停车场',
    image: '/upload/images/ScreenShot_2026-03-20_142808_840.png',
   aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 3,
    title: '摄像头 3 - 楼道',
    image: '/upload/images/ScreenShot_2026-03-20_124531_278.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 4,
    title: '摄像头 4 - 小区大门',
    image: '/upload/images/ScreenShot_2026-03-20_142035_336.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 5,
    title: '摄像头 5 - 健身区',
    image: '/upload/images/ScreenShot_2026-03-20_141838_041.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 6,
    title: '摄像头 6 - 儿童游乐区',
    image: '/upload/images/ScreenShot_2026-03-20_141908_426.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 7,
    title: '摄像头 7 - 游泳池',
    image: '/upload/images/ScreenShot_2026-03-20_141940_794.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 8,
    title: '摄像头 8 - 地下车库',
    image: '/upload/images/ScreenShot_2026-03-20_140446_760.png',
  aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  },
  {
    id: 9,
    title: '摄像头 9 - 后门',
    image: '/upload/images/ScreenShot_2026-03-20_142053_624.png',
    aiResult: '正常场景',
    level: 'normal',
    statusClass: 'normal',
    loading: false,
    online: true,
    riskEventId: null
  }
])

/**
 * 获取风险等级文本
 */
const getRiskLevelText = (level: string): string => {
  const levelMap: Record<string, string> = {
    'danger': '高风险',
    'warning': '中风险',
    'low': '低风险',
    'normal': '正常'
  }
  return levelMap[level] || '未知'
}

/**
 * 获取风险标签类型
 */
const getRiskTagType = (level: string): string => {
  const typeMap: Record<string, string> = {
    'danger': 'danger',
    'warning': 'warning',
    'low': 'success',
    'normal': 'success',
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'success'
  }
  return typeMap[level] || 'info'
}

/**
 * 更新时间
 */
const updateTime = (): void => {
  const now = new Date()
  currentTime.value = now.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit',
    hour12: false
  })
}

/**
 * 上传前验证
 */
const beforeUpload = (file: File): boolean => {
  const isImage = file.type.startsWith('image/')
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isImage) {
    ElMessage.error('只能上传图片文件！')
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！')
  }
  return isImage && isLt2M
}

/**
 * 处理图片上传和 AI 识别（复用现有接口）
 */
const handleUpload = async (options: any, camera: Camera): Promise<void> => {
  const { file } = options
  camera.loading = true

  try {
    // 关键修复：使用 file.raw 获取原始 File 对象
    const rawFile = file.raw || file
    
    // 提取摄像头位置信息（从标题如 "摄像头 1 - 花园区域" 中提取 "花园区域"）
    const cameraParts = camera.title.split(' - ')
    const cameraLocation = cameraParts.length > 1 ? cameraParts[1] : camera.title
    const cameraId = `camera_${camera.id}`
    
    // 调用封装好的 API，传递摄像头位置信息
    const result = await uploadAndRecognize(rawFile, cameraLocation, cameraId)
    
    // 更新摄像头状态
    camera.image = result.imageUrl
    
    // 根据风险等级显示不同结果
    if (result.riskLevel === 'LOW' || result.riskLevel === 'normal') {
      // 低风险或正常，显示正常场景
      camera.aiResult = '正常场景'
      camera.level = 'normal'
      camera.statusClass = 'normal'
      ElMessage.success('AI 识别完成，场景正常')
    } else {
      // 中高风险，创建真实风险事件
      camera.aiResult = `检测到：${mapRiskType(result.riskType)}`
      camera.level = mapRiskLevel(result.riskLevel)
      camera.statusClass = camera.level
      
      // 后端已自动创建风险事件，前端只负责本地显示和绑定ID
      if (result.riskEventId) {
        camera.riskEventId = Number(result.riskEventId)
      }
      
      ElMessage.success('AI 识别完成，风险事件已创建')
    }
  } catch (error: any) {
    console.error('上传识别失败:', error)
    ElMessage.error(error.response?.data?.message || error.message || 'AI 识别失败，请重试')
    camera.aiResult = '识别失败'
    camera.level = 'normal'
    camera.statusClass = 'normal'
  } finally {
    camera.loading = false
  }
}

/**
 * 映射风险类型（英文转中文）
 */
const mapRiskType = (type: string): string => {
  return RISK_TYPE_MAP[type as keyof typeof RISK_TYPE_MAP] || type
}

/**
 * 映射风险等级
 */
const mapRiskLevel = (level: string): 'danger' | 'warning' | 'low' | 'normal' => {
  const levelMap: Record<string, string> = {
    'HIGH': 'danger',
    'MEDIUM': 'warning',
    'LOW': 'low',
    'danger': 'danger',
    'warning': 'warning'
  }
  return (levelMap[level] || 'normal') as 'danger' | 'warning' | 'low' | 'normal'
}

/**
 * 同步最新风险事件（自动更新摄像头状态）
 */
const handleSyncRisks = async (): Promise<void> => {
  syncing.value = true
  try {
    const events = await getLatestRiskEvents(9)
    
    if (events && events.length > 0) {
      // 将风险事件映射到摄像头
      events.forEach((event: any, index: number) => {
        if (index < cameraList.value.length) {
          const camera = cameraList.value[index]
          camera.image = event.imageUrl || camera.image
          camera.aiResult = `检测到：${event.riskType || '风险事件'}`
          camera.level = mapRiskLevel(event.riskLevel || 'MEDIUM')
          camera.statusClass = camera.level
        }
      })
      
      ElMessage.success(`同步成功，发现 ${events.length} 个风险事件`)
    } else {
      ElMessage.info('暂无新风险事件')
    }
  } catch (error: any) {
    console.error('同步风险事件失败:', error)
    ElMessage.error('同步失败，请稍后重试')
  } finally {
    syncing.value = false
  }
}

/**
 * 点击摄像头查看详情
 */
const handleCameraClick = (camera: Camera): void => {
  selectedCamera.value = camera
  dialogVisible.value = true
}

/**
 * 跳转到风险管理页面
 */
const goToRiskManagement = (): void => {
  if (selectedCamera.value?.riskEventId) {
    // 有真实风险事件 ID，跳转并带上参数
    router.push({
      path: '/risk-management',
      query: {
        id: selectedCamera.value.riskEventId
      }
    })
  } else {
    // 没有风险事件 ID，直接跳转
    router.push('/risk-management')
  }
  dialogVisible.value = false
}

/**
 * 实时AI分析今日监控截图
 */
const handleRealtimeAnalyze = async (): Promise<void> => {
  try {
    await ElMessageBox.confirm(
      '将分析今日上传的监控截图进行AI风险识别，确定继续吗？',
      '实时AI分析',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
  } catch {
    return
  }

  analyzing.value = true
  try {
    const response = await axios.post('http://localhost:8080/api/file/analyze-pending', null, {
      params: {
        cameraLocation: '监控区域'
      }
    })

    const result = response.data
    if (result.code === 200 && result.success) {
      const summary = result.summary
      
      let message = `实时分析完成！\n`
      message += `共扫描 ${summary.totalScanned} 张图片\n`
      message += `成功分析 ${summary.totalSuccess} 张\n`
      if (summary.totalFailed > 0) {
        message += `失败 ${summary.totalFailed} 张\n`
      }
      message += `\n风险统计：\n`
      message += `🔴 高风险：${summary.highRiskCount} 个\n`
      message += `🟡 中风险：${summary.mediumRiskCount} 个\n`
      message += `🟢 低风险：${summary.lowRiskCount} 个`

      ElMessageBox.alert(message, '实时AI分析结果', {
        confirmButtonText: '确定',
        type: summary.highRiskCount > 0 ? 'error' : summary.mediumRiskCount > 0 ? 'warning' : 'success'
      })

      await handleSyncRisks()
    } else {
      ElMessage.error(result.message || '实时分析失败')
    }
  } catch (error: any) {
    if (error !== 'cancel') {
      console.error('实时AI分析失败:', error)
      ElMessage.error(error.response?.data?.message || error.message || '实时分析失败，请稍后重试')
    }
  } finally {
    analyzing.value = false
  }
}

/**
 * 从 AI 结果文本中提取风险类型
 */
const getRiskTypeFromText = (aiResult: string): string => {
  if (!aiResult || aiResult === '正常场景') {
    return '正常'
  }
  // 提取"检测到：XXX"中的 XXX
  const match = aiResult.match(/检测到：(.+)/)
  return match ? match[1] : aiResult
}

/**
 * 自动同步定时器
 */
const startAutoSync = (): void => {
  if (syncInterval.value) return
  
  syncInterval.value = setInterval(() => {
    handleSyncRisks()
  }, 5000) // 每 5 秒同步一次
}

const stopAutoSync = (): void => {
  if (syncInterval.value) {
    clearInterval(syncInterval.value)
    syncInterval.value = null
  }
}

onMounted(() => {
  updateTime()
  timeInterval.value = setInterval(updateTime, 1000)
  // 关闭自动同步，只保留手动同步功能
  // startAutoSync()
  // handleSyncRisks() // 首次加载时同步一次
})

onUnmounted(() => {
  if (timeInterval.value) {
    clearInterval(timeInterval.value)
  }
  stopAutoSync()
})
</script>

<style scoped>
.monitor-screen {
  min-height: 100vh;
  background: linear-gradient(135deg, #0c1022 0%, #1a1f3a 50%, #0f1428 100%);
  padding: 20px;
  color: #fff;
}

.header-section {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 30px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px;
  margin-bottom: 20px;
  backdrop-filter: blur(10px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}

.screen-title {
  font-size: 32px;
  font-weight: bold;
  color: #00d4ff;
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0;
  text-shadow: 0 0 20px rgba(0, 212, 255, 0.5);
}

.screen-title .el-icon {
  font-size: 36px;
}

.header-info {
  display: flex;
  align-items: center;
  gap: 20px;
}

.current-time {
  font-size: 18px;
  color: #a0aec0;
  font-family: 'Courier New', monospace;
}

.camera-grid-section {
  margin-bottom: 20px;
}

.camera-row {
  margin: 0 !important;
}

.camera-card {
  height: 220px;
  margin-bottom: 16px;
  border-radius: 12px;
  overflow: hidden;
  background: rgba(255, 255, 255, 0.05);
  border: 2px solid rgba(255, 255, 255, 0.1);
  transition: all 0.3s ease;
  cursor: pointer;
  position: relative;
}

.camera-card:hover {
  transform: scale(1.02);
  box-shadow: 0 10px 30px rgba(0, 212, 255, 0.3);
}

.camera-card.danger {
  border-color: #f56c6c;
  box-shadow: 0 0 20px rgba(245, 108, 108, 0.4);
  animation: pulse-danger 1.5s infinite;
}

.camera-card.warning {
  border-color: #e6a23c;
  box-shadow: 0 0 20px rgba(230, 162, 60, 0.4);
  animation: pulse-warning 2s infinite;
}

.camera-card.normal {
  border-color: #67c23a;
}

@keyframes pulse-danger {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.02);
    opacity: 0.9;
  }
}

@keyframes pulse-warning {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.01);
    opacity: 0.95;
  }
}

.camera-image-wrapper {
  position: relative;
  width: 100%;
  height: 100%;
  overflow: hidden;
}

.camera-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.3s ease;
}

.camera-card:hover .camera-image {
  transform: scale(1.1);
}

.loading-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.7);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 10px;
  color: #fff;
  font-size: 16px;
  z-index: 10;
}

.loading-overlay .el-icon {
  font-size: 40px;
  color: #409EFF;
}

.camera-name-overlay {
  position: absolute;
  top: 12px;
  left: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  background: rgba(0, 0, 0, 0.7);
  padding: 6px 12px;
  border-radius: 6px;
  font-size: 13px;
  color: #fff;
  backdrop-filter: blur(4px);
  z-index: 5;
}

.camera-name-overlay .el-icon {
  color: #00d4ff;
}

.risk-tag-overlay {
  position: absolute;
  top: 12px;
  right: 12px;
  z-index: 5;
}

.ai-result-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9), transparent);
  z-index: 5;
}

.ai-result-content {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #fff;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.ai-result-overlay.danger .ai-result-content {
  color: #f56c6c;
}

.ai-result-overlay.warning .ai-result-content {
  color: #e6a23c;
}

.ai-result-overlay.normal .ai-result-content {
  color: #67c23a;
}

.upload-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  opacity: 0;
  transition: opacity 0.3s ease;
  z-index: 6;
}

.camera-card:hover .upload-overlay {
  opacity: 1;
}

.camera-dialog :deep(.el-dialog) {
  background: #1a1f3a;
  border-radius: 12px;
}

.camera-dialog :deep(.el-dialog__header) {
  background: rgba(255, 255, 255, 0.05);
  border-radius: 12px 12px 0 0;
}

.camera-dialog :deep(.el-dialog__title) {
  color: #fff;
}

.dialog-camera-view {
  position: relative;
  width: 100%;
  aspect-ratio: 16/9;
  border-radius: 8px;
  overflow: hidden;
}

.dialog-image {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.dialog-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  padding: 20px;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.8), transparent);
}

.dialog-risk-info {
  display: flex;
  justify-content: center;
}

.dialog-risk-info.danger {
  animation: pulse-danger 1.5s infinite;
}

.dialog-risk-info.warning {
  animation: pulse-warning 2s infinite;
}

/* 风险事件详情卡片样式 */
.dialog-risk-details {
  margin-top: 20px;
  padding: 20px;
  background: rgba(255, 255, 255, 0.05);
  border-radius: 8px;
}

.dialog-risk-details :deep(.el-descriptions__label) {
  font-weight: 600;
  color: #e0e0e0;
  width: 120px;
}

.dialog-risk-details :deep(.el-descriptions__content) {
  color: #fff;
}

.dialog-actions-info {
  margin-top: 20px;
  padding: 20px;
  text-align: center;
}

.dialog-actions-hint {
  margin-top: 15px;
}

.dialog-actions-hint .el-button {
  min-width: 160px;
  font-size: 16px;
  font-weight: 600;
}

@media (max-width: 768px) {
  .header-section {
    flex-direction: column;
    gap: 15px;
    text-align: center;
  }
  
  .screen-title {
    font-size: 24px;
  }
  
  .camera-card {
    height: 180px;
  }
}
</style>
