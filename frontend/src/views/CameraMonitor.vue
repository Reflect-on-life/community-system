<template>
  <div class="camera-monitor">
    <!-- 椤堕儴鏍囬鏍?-->
    <div class="header">
      <h1 class="title">
        <el-icon><VideoCamera /></el-icon>
        鎽勫儚澶磋涓鸿瘑鍒洃鎺?      </h1>
      <div class="header-actions">
        <!-- 模型预设选择器 -->
        <div class="model-selector">
          <el-select
            v-model="currentPreset"
            @change="handleSwitchPreset"
            :disabled="switchingPreset"
            size="default"
            class="preset-select"
            placeholder="选择模型方案"
          >
            <el-option
              v-for="preset in modelPresets"
              :key="preset.id"
              :label="preset.name"
              :value="preset.id"
            >
              <div class="preset-option">
                <span class="preset-name">{{ preset.name }}</span>
                <span class="preset-models">{{ preset.models }}</span>
              </div>
            </el-option>
          </el-select>
          <el-tooltip
            v-if="modelPresets.length > 0"
            :content="modelPresets.find(p => p.id === currentPreset)?.description || ''"
            placement="bottom"
          >
            <el-icon class="preset-info-icon"><InfoFilled /></el-icon>
          </el-tooltip>
          <el-icon v-if="switchingPreset" class="is-loading" :size="16"><Loading /></el-icon>
        </div>
        <el-tag :type="isMonitoring ? 'success' : 'info'" size="large">
          {{ isMonitoring ? '鐩戞帶涓? : '鏈洃鎺? }}
        </el-tag>
      </div>
    </div>

    <!-- 涓诲唴瀹瑰尯锛氬乏渚ф憚鍍忓ご + 鍙充晶鍛婅鍒楄〃 -->
    <div class="main-content">
      <!-- 宸︿晶锛氭憚鍍忓ご棰勮鍖猴紙60%锛?-->
      <div class="camera-section">
        <div class="camera-card">
          <!-- 瑙嗛娴佸睍绀?-->
          <div class="video-wrapper" :class="`risk-${currentRisk.type}`">
            <!-- 鐘舵€佹寚绀哄櫒锛堝乏涓婅鍦嗙偣锛?-->
            <div class="status-indicator" :class="`status-${currentRisk.type}`">
              <span class="indicator-dot"></span>
            </div>
            
            <video
              ref="videoRef"
              autoplay
              playsinline
              class="video-element"
              :class="{ 'blur-effect': isRecognizing }"
            ></video>
            
            <!-- 椋庨櫓绛夌骇鑴夊啿鍏夌幆 -->
            <div class="risk-pulse-ring" :class="`pulse-${currentRisk.type}`" v-if="currentRisk.type !== 'normal'"></div>
            
            <!-- 璇嗗埆涓姸鎬?-->
            <div class="recognizing-overlay" v-if="isRecognizing">
              <div class="recognizing-content">
                <div class="recognizing-spinner">
                  <el-icon class="is-loading" :size="50"><Loading /></el-icon>
                </div>
                <span class="recognizing-text">AI 璇嗗埆涓?..</span>
                <div class="recognizing-progress">
                  <el-progress
                    :percentage="uploadProgress"
                    :stroke-width="4"
                    :color="getProgressColor(uploadProgress)"
                    :show-text="false"
                  />
                </div>
              </div>
            </div>
            
            <!-- 涓婁紶杩涘害鏄剧ず -->
            <div class="upload-progress-overlay" v-if="isUploading && !isRecognizing">
              <div class="upload-content">
                <el-progress
                  type="circle"
                  :percentage="uploadProgress"
                  :stroke-width="6"
                  :color="getProgressColor(uploadProgress)"
                />
                <span class="upload-text">涓婁紶涓?..</span>
              </div>
            </div>
            
            <!-- 椋庨櫓鐘舵€佹爣绛撅紙鍙充笂瑙掞級 -->
            <div class="risk-badge" :class="`risk-${currentRisk.type}`">
              <el-icon v-if="currentRisk.type === 'high'"><WarningFilled /></el-icon>
              <el-icon v-else-if="currentRisk.type === 'medium'"><Warning /></el-icon>
              <el-icon v-else-if="currentRisk.type === 'low'"><InfoFilled /></el-icon>
              <el-icon v-else><CircleCheckFilled /></el-icon>
              <span>{{ currentRisk.text }}</span>
            </div>
            
            <!-- 鏃堕棿鎴虫樉绀猴紙鍙充笅瑙掞級 -->
            <div class="timestamp-badge" v-if="currentRisk.timestamp">
              <el-icon><Clock /></el-icon>
              <span>{{ formatTime(currentRisk.timestamp) }}</span>
            </div>
            
            <!-- 璇嗗埆缁撴灉鍙犲姞灞傦紙搴曢儴锛?-->
            <div class="recognition-result-overlay" v-if="currentRisk.type !== 'normal' && currentRisk.description">
              <div class="result-content">
                <div class="result-title">
                  <el-icon><InfoFilled /></el-icon>
                  <span>璇嗗埆缁撴灉</span>
                </div>
                <div class="result-description">{{ currentRisk.description }}</div>
                <div class="result-confidence">
                  缃俊搴︼細{{ (currentRisk.confidence * 100).toFixed(1) }}%
                </div>
              </div>
            </div>
            
            <!-- 鍔犺浇鐘舵€侀鏋跺睆 -->
            <div class="loading-skeleton" v-if="!cameraAvailable && isMonitoring">
              <div class="skeleton-content">
                <el-icon class="skeleton-icon" :size="48"><Camera /></el-icon>
                <span class="skeleton-text">鎽勫儚澶村惎鍔ㄤ腑...</span>
              </div>
            </div>
          </div>
          
          <!-- 鎺у埗鎸夐挳 -->
          <div class="control-buttons">
            <el-button
              v-if="!isMonitoring"
              type="success"
              size="large"
              @click="startMonitoring"
            >
              <el-icon><VideoPlay /></el-icon>
              寮€濮嬬洃鎺?            </el-button>
            
            <el-button
              v-else
              type="danger"
              size="large"
              @click="stopMonitoring"
            >
              <el-icon><VideoPause /></el-icon>
              鍋滄鐩戞帶
            </el-button>
            
            <el-button
              size="large"
              @click="switchCamera"
              :disabled="!isMonitoring"
            >
              <el-icon><Switch /></el-icon>
              鍒囨崲鎽勫儚澶?            </el-button>
            
            <!-- 涓婁紶鏈€鏂版埅鍥炬寜閽?-->
            <el-button
              size="large"
              type="primary"
              @click="uploadLatestSnapshot"
              :disabled="!isMonitoring || isUploading"
            >
              <el-icon><Upload /></el-icon>
              涓婁紶鎴浘
            </el-button>
            
            <!-- 鎵归噺涓婁紶鎸夐挳 -->
            <el-button
              size="large"
              type="warning"
              @click="uploadAllSnapshots"
              :disabled="!isMonitoring || isUploading"
            >
              <el-icon><FolderOpened /></el-icon>
              鎵归噺涓婁紶
            </el-button>
            
            <!-- 妯℃嫙鍛婅鎸夐挳锛堜粎寮€鍙戠幆澧冩樉绀猴級 -->
            <el-button
              v-if="isDevEnvironment"
              type="warning"
              size="large"
              @click="triggerMockAlarm"
              :disabled="!isMonitoring"
            >
              <el-icon><Bell /></el-icon>
              妯℃嫙鍛婅
            </el-button>
          </div>
          
          <!-- 鎽勫儚澶撮€夋嫨 -->
          <div class="camera-select" v-if="cameras.length > 0">
            <el-select v-model="selectedCameraId" placeholder="閫夋嫨鎽勫儚澶? @change="switchCamera">
              <el-option
                v-for="camera in cameras"
                :key="camera.deviceId"
                :label="camera.label"
                :value="camera.deviceId"
              />
            </el-select>
          </div>
        </div>
      </div>

      <!-- 鍙充晶锛氬憡璀﹀巻鍙插垪琛紙40%锛?-->
      <div class="alarm-section">
        <div class="alarm-header">
          <h2 class="section-title">
            <el-icon><Bell /></el-icon>
            鍛婅鍘嗗彶
          </h2>
          <el-badge :value="unreadCount" :hidden="unreadCount === 0" type="danger">
            <el-button size="small" @click="markAllAsRead">鍏ㄩ儴宸茶</el-button>
          </el-badge>
        </div>
        
        <div class="alarm-list">
          <el-scrollbar height="calc(100vh - 200px)">
            <div
              v-for="alarm in alarmList"
              :key="alarm.id"
              class="alarm-item"
              :class="{ 'unread': alarm.isRead === 0 }"
              @click="showAlarmDetail(alarm)"
            >
              <!-- 鍛婅鎴浘 -->
              <div class="alarm-image-wrapper">
                <el-image
                  :src="`http://localhost:8080${alarm.imagePath}`"
                  fit="cover"
                  class="alarm-image"
                >
                  <template #placeholder>
                    <div class="image-placeholder">
                      <el-icon><Picture /></el-icon>
                    </div>
                  </template>
                  <template #error>
                    <div class="image-placeholder">
                      <span>鍔犺浇澶辫触</span>
                    </div>
                  </template>
                </el-image>
              </div>
              
              <!-- 鍒犻櫎鎸夐挳 -->
              <el-button
                class="alarm-delete-btn"
                type="danger"
                link
                size="small"
                @click.stop="deleteAlarm(alarm.id)"
              >
                <el-icon><Delete /></el-icon>
              </el-button>
              
              <!-- 鍛婅淇℃伅 -->
              <div class="alarm-info">
                <div class="alarm-header-row">
                  <el-tag :type="getAlarmTypeTag(alarm.alarmType)" size="small">
                    {{ alarm.alarmType }}
                  </el-tag>
                  <span class="alarm-time">{{ formatDateTime(alarm.alarmTime) }}</span>
                </div>
                
                <div class="alarm-content">
                  <div class="alarm-location">
                    <el-icon><Location /></el-icon>
                    <span>{{ alarm.location || '鏈煡浣嶇疆' }}</span>
                  </div>
                  
                  <div class="alarm-confidence">
                    缃俊搴︼細{{ (alarm.confidence * 100).toFixed(1) }}%
                  </div>
                </div>
                
                <div class="alarm-footer">
                  <el-tag size="small" :type="alarm.isRead === 1 ? 'success' : 'warning'">
                    {{ alarm.isRead === 1 ? '宸茶' : '鏈' }}
                  </el-tag>
                  <el-button
                    v-if="alarm.isRead === 0"
                    type="primary"
                    link
                    size="small"
                    @click.stop="markAsRead(alarm.id)"
                  >
                    鏍囦负宸茶
                  </el-button>
                </div>
              </div>
            </div>
            
            <!-- 绌虹姸鎬?-->
            <el-empty v-if="alarmList.length === 0" description="鏆傛棤鍛婅璁板綍" />
          </el-scrollbar>
        </div>
      </div>
    </div>

    <!-- 鍛婅璇︽儏寮圭獥 -->
    <Transition name="alarm-dialog">
      <div v-if="dialogVisible" class="alarm-dialog-overlay" @click.self="closeDialog">
        <div class="alarm-dialog">
          <!-- 鍏抽棴鎸夐挳 -->
          <button class="dialog-close" @click="closeDialog" aria-label="鍏抽棴">
            <svg viewBox="0 0 24 24" width="24" height="24">
              <path fill="currentColor" d="M19 6.41L17.59 5 12 10.59 6.41 5 5 6.41 10.59 12 5 17.59 6.41 19 12 13.41 17.59 19 19 17.59 13.41 12z"/>
            </svg>
          </button>
          
          <!-- 鍥剧墖灞曠ず鍖哄煙 -->
          <div class="dialog-image-section">
            <img
              v-if="selectedAlarm?.imagePath"
              :src="`http://localhost:8080${selectedAlarm.imagePath}`"
              :alt="selectedAlarm.alarmType"
              class="dialog-image"
            />
            <div v-else class="dialog-image-placeholder">
              <el-icon :size="48"><Picture /></el-icon>
              <span>鏆傛棤鍥剧墖</span>
            </div>
          </div>
          
          <!-- 淇℃伅灞曠ず鍖哄煙 -->
          <div class="dialog-info-section">
            <!-- 椋庨櫓绛夌骇鏍囩 -->
            <div class="dialog-risk-badge">
              <el-tag :type="getAlarmTypeTag(selectedAlarm?.alarmType)" size="large">
                {{ selectedAlarm?.alarmType }}
              </el-tag>
            </div>
            
            <!-- 鏃堕棿淇℃伅 -->
            <div class="dialog-time-info">
              <div class="info-item">
                <el-icon><Clock /></el-icon>
                <span>鍛婅鏃堕棿</span>
                <span class="info-value">{{ selectedAlarm?.alarmTime ? formatDateTime(selectedAlarm.alarmTime) : '--' }}</span>
              </div>
              <div class="info-item">
                <el-icon><Location /></el-icon>
                <span>鍛婅浣嶇疆</span>
                <span class="info-value">{{ selectedAlarm?.location || '鏈煡浣嶇疆' }}</span>
              </div>
              <div class="info-item">
                <el-icon><DataAnalysis /></el-icon>
                <span>缃俊搴?/span>
                <span class="info-value">{{ selectedAlarm ? (selectedAlarm.confidence * 100).toFixed(1) + '%' : '--' }}</span>
              </div>
            </div>
            
            <!-- 椋庨櫓鍒嗘瀽璇︽儏 -->
            <div class="dialog-risk-analysis">
              <h3 class="analysis-title">
                <el-icon><WarningFilled /></el-icon>
                椋庨櫓鍒嗘瀽璇︽儏
              </h3>
              <div class="analysis-content">
                <p v-if="selectedAlarm?.description">{{ selectedAlarm.description }}</p>
                <p v-else-if="selectedAlarm?.aiResult">{{ selectedAlarm.aiResult }}</p>
                <p v-else class="no-analysis">鏆傛棤璇︾粏椋庨櫓鍒嗘瀽淇℃伅銆?/p>
              </div>
            </div>
            
            <!-- 鎿嶄綔鎸夐挳 -->
            <div class="dialog-actions">
              <el-button
                v-if="selectedAlarm?.isRead === 0"
                type="primary"
                @click="markAsReadAndClose"
              >
                鏍囪涓哄凡璇?              </el-button>
              <el-button @click="closeDialog">鍏抽棴</el-button>
            </div>
          </div>
        </div>
      </div>
    </Transition>

    <!-- 闅愯棌鐨?Canvas 鐢ㄤ簬鎴彇瑙嗛甯?-->
    <canvas ref="canvasRef" style="display: none;"></canvas>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  VideoCamera,
  VideoPlay,
  VideoPause,
  Loading,
  Bell,
  Picture,
  Location,
  Switch,
  Refresh,
  Upload,
  FolderOpened,
  Delete,
  Cpu,
  Warning,
  WarningFilled,
  Clock,
  InfoFilled,
  CircleCheckFilled,
  Camera,
  DataAnalysis
} from '@element-plus/icons-vue'
import axios from 'axios'
import { getModelPresets, switchModelPreset } from '../api/modelConfig'
import { Client } from '@stomp/stompjs'

// ========== 鍝嶅簲寮忔暟鎹?==========
const videoRef = ref(null)
const canvasRef = ref(null)
const isMonitoring = ref(false)
const isDetecting = ref(false)
const cameraAvailable = ref(false)
const currentStream = ref(null)
const detectTimer = ref(null)
const snapshotTimer = ref(null) // 鎴浘瀹氭椂鍣?const stompClient = ref(null)
const isConnected = ref(false)

// 鎽勫儚澶村垪琛?const cameras = ref([])
const selectedCameraId = ref('')

// ========== 模型预设选择 ==========
const modelPresets = ref([])
const currentPreset = ref('ORIGINAL')
const switchingPreset = ref(false)

// 褰撳墠鐘舵€?const currentStatus = reactive({
  type: 'info',
  text: '绛夊緟寮€濮?
})

// 鍛婅鍒楄〃
const alarmList = ref([])
const unreadCount = computed(() => {
  return alarmList.value.filter(item => item.isRead === 0).length
})

// 鍛婅璇︽儏寮圭獥
const dialogVisible = ref(false)
const selectedAlarm = ref(null)

// 鎴浘鏁扮粍锛堟渶澶氫繚鐣欐渶杩?3 寮狅級
const snapshots = ref([])
const uploadProgress = ref(0) // 涓婁紶杩涘害
const isUploading = ref(false) // 鏄惁姝ｅ湪涓婁紶
const isRecognizing = ref(false) // 鏄惁姝ｅ湪 AI 璇嗗埆
const isBatchAnalyzing = ref(false) // 鏄惁姝ｅ湪鎵归噺AI鍒嗘瀽
const lastRecognizeTime = ref(0) // 涓婃璇嗗埆鏃堕棿鎴筹紙鐢ㄤ簬鑺傛祦锛?const recognizeInterval = 5000 // AI 璇嗗埆鑺傛祦闂撮殧锛? 绉掞級
const motionAnalyzeInterval = 3000 // 鍔ㄤ綔鍒嗘瀽闂撮殧锛? 绉掞級
const lastMotionAnalyzeTime = ref(0) // 涓婃鍔ㄤ綔鍒嗘瀽鏃堕棿

// 褰撳墠璇嗗埆缁撴灉
const currentRisk = reactive({
  type: 'normal', // normal, low, medium, high
  level: 'LOW',
  text: '姝ｅ父鍦烘櫙',
  description: '', // 璇嗗埆缁撴灉鎻忚堪
  confidence: 0, // 缃俊搴?  imageUrl: '',
  timestamp: null
})

// 鍔ㄤ綔璇嗗埆缁撴灉
const motionAnalysis = reactive({
  isAnalyzing: false,
  actionType: '',
  riskLevel: '',
  description: '',
  frameUrls: []
})

// 寮€鍙戠幆澧冩爣蹇楋紙鐢ㄤ簬鏄剧ず妯℃嫙鍛婅鎸夐挳锛?const isDevEnvironment = import.meta.env.DEV

// ========== 杈呭姪鍑芥暟 ==========

/**
 * 鏍煎紡鍖栨椂闂存樉绀? */
const formatTime = (timestamp) => {
  if (!timestamp) return ''
  const date = new Date(timestamp)
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')
  return `${hours}:${minutes}:${seconds}`
}

/**
 * 鑾峰彇杩涘害鏉￠鑹诧紙鏍规嵁椋庨櫓绛夌骇锛? */
const getProgressColor = (progress) => {
  if (currentRisk.type === 'high') {
    return '#F56C6C'
  } else if (currentRisk.type === 'medium') {
    return '#E6A23C'
  } else if (currentRisk.type === 'low') {
    return '#909399'
  }
  return progress === 100 ? '#67C23A' : '#409EFF'
}

// ========== 鎽勫儚澶寸浉鍏虫柟娉?==========

// 鑾峰彇鎽勫儚澶村垪琛?const getCameras = async () => {
  try {
    const devices = await navigator.mediaDevices.enumerateDevices()
    cameras.value = devices
      .filter(device => device.kind === 'videoinput')
      .map((device, index) => ({
        deviceId: device.deviceId,
        label: device.label || `鎽勫儚澶?${index + 1}`
      }))
    
    if (cameras.value.length > 0) {
      selectedCameraId.value = cameras.value[0].deviceId
    }
  } catch (error) {
    console.error('鑾峰彇鎽勫儚澶村垪琛ㄥけ璐?', error)
  }
}

// 鎵撳紑鎽勫儚澶?const openCamera = async (deviceId) => {
  try {
    // 鍏堝叧闂箣鍓嶇殑娴?    if (currentStream.value) {
      currentStream.value.getTracks().forEach(track => track.stop())
    }
    
    const constraints = {
      video: {
        deviceId: deviceId ? { exact: deviceId } : undefined,
        width: { ideal: 640 },
        height: { ideal: 480 }
      },
      audio: false
    }
    
    const stream = await navigator.mediaDevices.getUserMedia(constraints)
    currentStream.value = stream
    
    if (videoRef.value) {
      videoRef.value.srcObject = stream
    }
    
    cameraAvailable.value = true
    currentStatus.type = 'success'
    currentStatus.text = '鎽勫儚澶村凡寮€鍚?
    
    return true
  } catch (error) {
    console.error('鎵撳紑鎽勫儚澶村け璐?', error)
    cameraAvailable.value = false
    
    if (error.name === 'NotAllowedError') {
      currentStatus.type = 'danger'
      currentStatus.text = '鎽勫儚澶存潈闄愯鎷掔粷'
      ElMessage.error('鎽勫儚澶存潈闄愯鎷掔粷锛岃鍦ㄦ祻瑙堝櫒璁剧疆涓厑璁歌闂憚鍍忓ご')
    } else if (error.name === 'NotFoundError') {
      currentStatus.type = 'danger'
      currentStatus.text = '鏈壘鍒版憚鍍忓ご'
      ElMessage.error('鏈壘鍒板彲鐢ㄧ殑鎽勫儚澶磋澶?)
    } else {
      currentStatus.type = 'danger'
      currentStatus.text = '鎽勫儚澶存墦寮€澶辫触'
      ElMessage.error('鎽勫儚澶存墦寮€澶辫触锛? + error.message)
    }
    
    return false
  }
}

// 鍒囨崲鎽勫儚澶?const switchCamera = async () => {
  if (!isMonitoring.value) return
  
  await openCamera(selectedCameraId.value)
}

// ========== 鐩戞帶鎺у埗鏂规硶 ==========

// 寮€濮嬬洃鎺?const startMonitoring = async () => {
  // 1. 鎵撳紑鎽勫儚澶?  const success = await openCamera(selectedCameraId.value)
  if (!success) return
  
  // 2. 璁剧疆鐩戞帶鐘舵€?  isMonitoring.value = true
  currentStatus.type = 'success'
  currentStatus.text = '鐩戞帶涓?
  
  // 3. 杩炴帴 WebSocket
  connectWebSocket()
  
  // 4. 鍚姩瀹氭椂妫€娴嬶紙姣?1 绉掍竴娆★級
  detectTimer.value = setInterval(() => {
    captureAndDetect()
  }, 1000)
  
  // 5. 鍚姩鑷姩鎴浘鍔熻兘锛堟瘡 2 绉掍竴娆★級
  startAutoSnapshot()
  
  ElMessage.success('鐩戞帶宸插惎鍔?)
}

// 鍋滄鐩戞帶
const stopMonitoring = () => {
  // 1. 娓呴櫎瀹氭椂鍣?  if (detectTimer.value) {
    clearInterval(detectTimer.value)
    detectTimer.value = null
  }
  
  // 2. 鍋滄鎴浘瀹氭椂鍣?  stopAutoSnapshot()
  
  // 3. 鍏抽棴鎽勫儚澶?  if (currentStream.value) {
    currentStream.value.getTracks().forEach(track => track.stop())
    currentStream.value = null
  }
  
  // 4. 鏂紑 WebSocket
  disconnectWebSocket()
  
  // 5. 閲嶇疆鐘舵€?  isMonitoring.value = false
  cameraAvailable.value = false
  currentStatus.type = 'info'
  currentStatus.text = '鐩戞帶宸插仠姝?
  
  // 6. 娓呴櫎鎴浘
  clearSnapshots()
  
  ElMessage.info('鐩戞帶宸插仠姝?)
}

// ========== 瑙嗛甯ч噰闆嗗拰璇嗗埆 ==========

/**
 * 鑷姩鎴浘鍔熻兘
 * 鎴彇褰撳墠瑙嗛甯у苟瀛樺偍涓?Blob 瀵硅薄
 */
const captureSnapshot = () => {
  if (!videoRef.value) {
    console.warn('瑙嗛鍏冪礌鏈噯澶囧ソ锛屾棤娉曟埅鍥?)
    return
  }
  
  try {
    const canvas = canvasRef.value
    const video = videoRef.value
    
    // 1. 璁剧疆 canvas 灏哄
    canvas.width = 640
    canvas.height = 480
    
    // 2. 缁樺埗褰撳墠瑙嗛甯?    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    // 3. 灏?Canvas 杞崲涓?Blob锛圝PEG 鏍煎紡锛岃川閲?80%锛?    canvas.toBlob(
      async (blob) => {
        if (!blob) {
          console.error('Canvas 杞崲涓?Blob 澶辫触')
          return
        }
        
        // 4. 瀛樺偍鎴浘锛堟渶澶氫繚鐣欐渶杩?3 寮狅級
        snapshots.value.push(blob)
        if (snapshots.value.length > 3) {
          snapshots.value.shift() // 绉婚櫎鏈€鏃╃殑鎴浘
        }
        
        // 5. 鍦ㄥ紑鍙戣€呮帶鍒跺彴鎵撳嵃鎴浘淇℃伅
        const timestamp = new Date().toISOString()
        console.log(`[${timestamp}] 鎴浘鎴愬姛`)
        console.log(`  - 鍥剧墖灏哄锛?{canvas.width}x${canvas.height}`)
        console.log(`  - Blob 澶у皬锛?{(blob.size / 1024).toFixed(2)} KB`)
        console.log(`  - 褰撳墠鎴浘鏁伴噺锛?{snapshots.value.length}`)
        
        // 6. 鏅鸿兘 AI 璇嗗埆锛堣妭娴佹帶鍒讹紝姣?5 绉掍竴娆★級
        const now = Date.now()
        if (isMonitoring.value && !isRecognizing.value && (now - lastRecognizeTime.value) >= recognizeInterval) {
          await recognizeSnapshot(blob)
        }
      },
      'image/jpeg',
      0.8 // 璐ㄩ噺 80%
    )
    
  } catch (error) {
    console.error('鎴浘澶辫触:', error)
  }
}

/**
 * 灏?Blob 杞崲涓?File 瀵硅薄
 */
const blobToFile = (blob, filename) => {
  return new File([blob], filename, {
    type: blob.type,
    lastModified: Date.now()
  })
}

/**
 * 鎴浘骞?AI 璇嗗埆锛堟暣鍚堜笂浼犲拰璇嗗埆锛? */
const recognizeSnapshot = async (blob) => {
  if (isRecognizing.value || isUploading.value) {
    console.log('姝ｅ湪璇嗗埆鎴栦笂浼犱腑锛岃烦杩?)
    return
  }
  
  try {
    isRecognizing.value = true
    isDetecting.value = true
    lastRecognizeTime.value = Date.now()
    
    console.log('=== 寮€濮嬫憚鍍忓ご AI 璇嗗埆娴佺▼ ===')
    
    // 1. 灏?Blob 杞崲涓?File 瀵硅薄
    const filename = `snapshot_${Date.now()}.jpg`
    const file = blobToFile(blob, filename)
    
    // 2. 鍒涘缓 FormData
    const formData = new FormData()
    formData.append('file', file)
    
    // 3. 涓婁紶鍒版湇鍔″櫒
    console.log('姝ラ 1: 涓婁紶鍥剧墖...')
    const uploadResponse = await axios.post('http://localhost:8080/api/file/upload/monitor', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      },
      onUploadProgress: (progressEvent) => {
        if (progressEvent.total) {
          uploadProgress.value = Math.round((progressEvent.loaded * 100) / progressEvent.total)
        }
      }
    })
    
    if (!uploadResponse.data || (!uploadResponse.data.success && uploadResponse.data.code !== 200)) {
      throw new Error('涓婁紶澶辫触')
    }
    
    const imageUrl = uploadResponse.data.data || uploadResponse.data.url
    console.log('涓婁紶鎴愬姛锛屽浘鐗?URL:', imageUrl)
    
    // 4. 璋冪敤鏂扮殑鎽勫儚澶?AI 璇嗗埆鎺ュ彛锛堜紶閫掓湰鍦版枃浠惰矾寰勶級
    console.log('姝ラ 2: 璋冪敤鎽勫儚澶?AI 璇嗗埆...')
    const currentCamera = cameras.value.find(c => c.deviceId === selectedCameraId.value)
    const cameraLocation = currentCamera ? currentCamera.label : '鏈湴鎽勫儚澶?
    
    // 璋冪敤鏂扮殑鎽勫儚澶?AI 璇嗗埆鎺ュ彛
    const recognizeResponse = await axios.post('http://localhost:8080/api/camera-ai/analyze-file', {
      localFilePath: imageUrl,
      cameraLocation: cameraLocation
    })
    
    if (!recognizeResponse.data || recognizeResponse.data.code !== 200) {
      throw new Error(recognizeResponse.data?.message || 'AI 璇嗗埆澶辫触')
    }
    
    const aiResult = recognizeResponse.data.data
    console.log('鎽勫儚澶?AI 璇嗗埆缁撴灉:', aiResult)
    
    // 5. 杞崲缁撴灉涓虹粺涓€鏍煎紡
    const formattedResult = {
      riskType: aiResult.riskType || '鏃犻闄?,
      riskLevel: aiResult.riskLevel || 'LOW',
      description: aiResult.description || '鍦烘櫙姝ｅ父锛屾棤鏄庢樉瀹夊叏椋庨櫓',
      isRisk: aiResult.isRisk === 'true',
      cameraLocation: aiResult.cameraLocation || cameraLocation
    }
    
    // 6. 鏇存柊璇嗗埆缁撴灉鍜?UI 鐘舵€?    updateRiskStatus(formattedResult, imageUrl)
    
    // 7. 濡傛灉鏄腑楂橀闄╋紝娣诲姞鏈湴鍛婅鎻愮ず
    if (formattedResult.riskLevel === 'MEDIUM' || formattedResult.riskLevel === 'HIGH') {
      addAlertLocally(formattedResult, imageUrl)
    }
    
    // 8. 妫€鏌ユ槸鍚﹂渶瑕佽繘琛屽姩浣滃垎鏋愶紙姣?3 绉掞紝涓旀湁 3 寮犺繛缁抚锛?    const now = Date.now()
    if (snapshots.value.length >= 3 && (now - lastMotionAnalyzeTime.value) >= motionAnalyzeInterval) {
      await analyzeMotionWithFrames(snapshots.value)
    }
    
    console.log('=== 鎽勫儚澶?AI 璇嗗埆娴佺▼瀹屾垚 ===')
    
  } catch (error) {
    console.error('鎽勫儚澶?AI 璇嗗埆澶辫触:', error)
    // 璇嗗埆澶辫触鏃堕噸缃负姝ｅ父鐘舵€?    currentRisk.type = 'normal'
    currentRisk.text = '璇嗗埆澶辫触'
    currentStatus.type = 'warning'
    currentStatus.text = 'AI 璇嗗埆澶辫触'
  } finally {
    isRecognizing.value = false
    isDetecting.value = false
    setTimeout(() => {
      uploadProgress.value = 0
    }, 1000)
  }
}

/**
 * 浣跨敤杩炵画 3 甯ц繘琛屽姩浣滃垎鏋? */
const analyzeMotionWithFrames = async (frameBlobs) => {
  if (motionAnalysis.isAnalyzing || frameBlobs.length < 3) {
    console.log('姝ｅ湪鍒嗘瀽涓垨甯ф暟涓嶈冻锛岃烦杩?)
    return
  }
  
  try {
    motionAnalysis.isAnalyzing = true
    lastMotionAnalyzeTime.value = Date.now()
    
    console.log('=== 寮€濮嬭繛缁抚鍔ㄤ綔鍒嗘瀽 ===')
    console.log(`浣跨敤 ${frameBlobs.length} 寮犺繛缁抚杩涜鍒嗘瀽`)
    
    // 1. 涓婁紶 3 寮犺繛缁抚
    console.log('姝ラ 1: 涓婁紶 3 寮犺繛缁抚...')
    const uploadPromises = frameBlobs.map((blob, index) => {
      const file = blobToFile(blob, `frame_${Date.now()}_${index}.jpg`)
      const formData = new FormData()
      formData.append('file', file)
      
      return axios.post('http://localhost:8080/api/file/upload/monitor', formData, {
        headers: { 'Content-Type': 'multipart/form-data' }
      }).then(response => {
        if (response.data.data || response.data.url) {
          return response.data.data || response.data.url
        }
        throw new Error(`绗?${index + 1} 甯т笂浼犲け璐)
      })
    })
    
    const frameUrls = await Promise.all(uploadPromises)
    console.log('3 寮犲抚涓婁紶鎴愬姛:', frameUrls)
    motionAnalysis.frameUrls = frameUrls
    
    // 2. 璋冪敤鍔ㄤ綔鍒嗘瀽鎺ュ彛
    console.log('姝ラ 2: 璋冪敤鍔ㄤ綔鍒嗘瀽鎺ュ彛...')
    const motionResponse = await axios.post('http://localhost:8080/api/risk/recognize-motion', {
      imageUrls: frameUrls,
      location: '鏈湴鎽勫儚澶?
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    const motionResult = motionResponse.data
    console.log('鍔ㄤ綔鍒嗘瀽缁撴灉:', motionResult)
    
    // 3. 鏇存柊鍔ㄤ綔璇嗗埆缁撴灉
    motionAnalysis.actionType = motionResult.actionType || ''
    motionAnalysis.riskLevel = motionResult.riskLevel || 'LOW'
    motionAnalysis.description = motionResult.description || ''
    
    // 4. 濡傛灉妫€娴嬪埌楂橀闄╁姩浣滐紝鏇存柊 UI 鐘舵€?    if (motionResult.riskLevel === 'HIGH' || motionResult.riskLevel === 'MEDIUM') {
      const aiResult = {
        riskType: motionResult.actionType,
        riskLevel: motionResult.riskLevel,
        description: motionResult.description
      }
      updateRiskStatus(aiResult, frameUrls[2]) // 浣跨敤鏈€鍚庝竴甯т綔涓哄皝闈?      
      // 5. 鑷姩鍒涘缓椋庨櫓浜嬩欢
      await createRiskEvent(aiResult, frameUrls[2])
    }
    
    console.log('=== 鍔ㄤ綔鍒嗘瀽瀹屾垚 ===')
    
  } catch (error) {
    console.error('鍔ㄤ綔鍒嗘瀽澶辫触:', error)
    motionAnalysis.actionType = ''
    motionAnalysis.riskLevel = ''
    motionAnalysis.description = '鍒嗘瀽澶辫触'
  } finally {
    motionAnalysis.isAnalyzing = false
  }
}

/**
 * 鏇存柊椋庨櫓鐘舵€佸拰 UI 鏄剧ず
 */
const updateRiskStatus = (aiResult, imageUrl) => {
  // 鏇存柊褰撳墠椋庨櫓鐘舵€?  currentRisk.imageUrl = imageUrl
  currentRisk.timestamp = new Date()
  currentRisk.level = aiResult.riskLevel || 'LOW'
  currentRisk.description = aiResult.description || ''
  currentRisk.confidence = aiResult.confidence || 0.9
  
  // 鏍规嵁椋庨櫓绛夌骇璁剧疆鐘舵€?  const riskLevel = (aiResult.riskLevel || 'LOW').toUpperCase()
  
  switch (riskLevel) {
    case 'HIGH':
      currentRisk.type = 'high'
      currentRisk.text = aiResult.riskType || '楂橀闄?
      currentStatus.type = 'danger'
      currentStatus.text = `楂橀闄╋細${aiResult.riskType}`
      
      // 鎾斁鍛婅澹伴煶
      playAlarmSound()
      
      // 鏄剧ず寮烘彁绀?      ElMessage.error({
        message: `鈿狅笍 妫€娴嬪埌楂橀闄╋細${aiResult.riskType}锛乣,
        duration: 5000,
        showClose: true
      })
      break
      
    case 'MEDIUM':
      currentRisk.type = 'medium'
      currentRisk.text = aiResult.riskType || '涓闄?
      currentStatus.type = 'warning'
      currentStatus.text = `涓闄╋細${aiResult.riskType}`
      
      // 鏄剧ず鎻愮ず
      ElMessage.warning({
        message: `妫€娴嬪埌涓闄╋細${aiResult.riskType}`,
        duration: 3000
      })
      break
      
    case 'LOW':
      // 濡傛灉鏄槑纭殑浣庨闄╃被鍨嬶紙闈炴甯稿満鏅級
      if (aiResult.riskType && aiResult.riskType !== '姝ｅ父鍦烘櫙' && aiResult.riskType !== '鏈煡椋庨櫓') {
        currentRisk.type = 'low'
        currentRisk.text = aiResult.riskType
        currentStatus.type = 'info'
        currentStatus.text = `浣庨闄╋細${aiResult.riskType}`
      } else {
        currentRisk.type = 'normal'
        currentRisk.text = '姝ｅ父鍦烘櫙'
        currentStatus.type = 'success'
        currentStatus.text = '姝ｅ父鍦烘櫙'
      }
      break
      
    default:
      currentRisk.type = 'normal'
      currentRisk.text = '姝ｅ父鍦烘櫙'
      currentStatus.type = 'success'
      currentStatus.text = '姝ｅ父鍦烘櫙'
  }
  
  console.log('椋庨櫓鐘舵€佸凡鏇存柊:', currentRisk)
}

/**
 * 鑷姩鍒涘缓椋庨櫓浜嬩欢
 */
const createRiskEvent = async (aiResult, imageUrl) => {
  try {
    console.log('鑷姩鍒涘缓椋庨櫓浜嬩欢:', aiResult.riskType)
    
    const response = await axios.post('http://localhost:8080/api/risk/report', {
      title: `鎽勫儚澶磋嚜鍔ㄦ娴嬶細${aiResult.riskType}`,
      location: '鏈湴鎽勫儚澶?,
      imageUrl: imageUrl,
      riskType: aiResult.riskType,
      riskLevel: aiResult.riskLevel,
      description: aiResult.description || `AI 鑷姩妫€娴嬪埌鐨?{aiResult.riskType}浜嬩欢`,
      cameraId: selectedCameraId.value || 'camera_default' // 浣跨敤褰撳墠鎽勫儚澶?ID
    }, {
      headers: {
        'Content-Type': 'application/json'
      }
    })
    
    console.log('椋庨櫓浜嬩欢鍒涘缓鎴愬姛:', response.data)
    
    // 娣诲姞鍒板憡璀﹀垪琛?    if (response.data) {
      const newAlarm = {
        id: response.data.id || Date.now(),
        alarmType: response.data.riskType || aiResult.riskType,
        confidence: 0.9,
        imagePath: response.data.imageUrl || imageUrl,
        location: '鏈湴鎽勫儚澶?,
        alarmTime: new Date().toISOString(),
        isRead: 0
      }
      alarmList.value.unshift(newAlarm)
    }
    
  } catch (error) {
    console.error('鍒涘缓椋庨櫓浜嬩欢澶辫触:', error)
  }
}

/**
 * 鎾斁鍛婅澹伴煶锛堜腑楂橀闄╋級
 */
const playAlarmSound = () => {
  try {
    const AudioContext = window.AudioContext || window.webkitAudioContext
    const audioContext = new AudioContext()
    
    // 鍒涘缓鎸崱鍣?    const oscillator = audioContext.createOscillator()
    const gainNode = audioContext.createGain()
    
    oscillator.connect(gainNode)
    gainNode.connect(audioContext.destination)
    
    // 璁剧疆棰戠巼鍜岄煶閲忥紙鎬ヤ績鐨勮鎶ュ０锛?    oscillator.frequency.value = 800
    oscillator.type = 'sine'
    gainNode.gain.value = 0.3
    
    // 鎾斁 0.5 绉?    oscillator.start()
    setTimeout(() => {
      oscillator.stop()
    }, 500)
    
    console.log('鍛婅澹伴煶宸叉挱鏀?)
    
  } catch (error) {
    console.warn('鎾斁闊虫晥澶辫触:', error)
  }
}

/**
 * 鍚姩鑷姩鎴浘鍔熻兘
 * 姣忛殧 2 绉掕嚜鍔ㄦ埅鍥? */
const startAutoSnapshot = () => {
  if (snapshotTimer.value) {
    console.warn('鎴浘瀹氭椂鍣ㄥ凡鍦ㄨ繍琛?)
    return
  }
  
  console.log('=== 鍚姩鑷姩鎴浘鍔熻兘 ===')
  console.log('鎴浘闂撮殧锛? 绉?)
  console.log('鍥剧墖鏍煎紡锛欽PEG, 璐ㄩ噺 80%')
  console.log('鏈€澶氫繚鐣欙細3 寮犳埅鍥?)
  console.log('========================')
  
  // 姣忛殧 2 绉掓埅鍥句竴娆?  snapshotTimer.value = setInterval(() => {
    if (isMonitoring.value && videoRef.value) {
      captureSnapshot()
    }
  }, 2000)
}

/**
 * 鍋滄鑷姩鎴浘鍔熻兘
 */
const stopAutoSnapshot = () => {
  if (snapshotTimer.value) {
    clearInterval(snapshotTimer.value)
    snapshotTimer.value = null
    console.log('=== 鑷姩鎴浘鍔熻兘宸插仠姝?===')
  }
}

/**
 * 鑾峰彇鏈€鏂版埅鍥? */
const getLatestSnapshot = () => {
  if (snapshots.value.length === 0) {
    return null
  }
  return snapshots.value[snapshots.value.length - 1]
}

/**
 * 鑾峰彇鎵€鏈夋埅鍥? */
const getAllSnapshots = () => {
  return snapshots.value
}

/**
 * 娓呴櫎鎵€鏈夋埅鍥? */
const clearSnapshots = () => {
  snapshots.value = []
  console.log('宸叉竻闄ゆ墍鏈夋埅鍥?)
}

// 鎴彇瑙嗛甯у苟鍙戦€佽瘑鍒?const captureAndDetect = async () => {
  if (!videoRef.value || isDetecting.value) return
  
  try {
    const canvas = canvasRef.value
    const video = videoRef.value
    
    // 1. 璁剧疆 canvas 灏哄
    canvas.width = 640
    canvas.height = 480
    
    // 2. 缁樺埗褰撳墠瑙嗛甯?    const ctx = canvas.getContext('2d')
    ctx.drawImage(video, 0, 0, canvas.width, canvas.height)
    
    // 3. 杞负 Base64 JPEG锛堣川閲?0.7锛?    const base64Image = canvas.toDataURL('image/jpeg', 0.7)
    
    // 4. 鍙戦€佸埌鍚庣璇嗗埆
    isDetecting.value = true
    
    const response = await axios.post('http://localhost:8080/api/alarm/detect', {
      image: base64Image,
      location: '鏈湴鎽勫儚澶?
    })
    
    const result = response.data
    
    // 5. 鏇存柊鐘舵€?    if (result.isAlarm) {
      currentStatus.type = 'danger'
      currentStatus.text = `妫€娴嬪埌锛?{result.alarmType}`
    } else {
      currentStatus.type = 'success'
      currentStatus.text = '鍦烘櫙姝ｅ父'
    }
    
  } catch (error) {
    // 鎺ュ彛璋冪敤澶辫触鏃朵笉涓柇鐩戞帶锛屽彧鎵撳嵃璀﹀憡
    console.warn('璇嗗埆澶辫触:', error)
    currentStatus.type = 'warning'
    currentStatus.text = '璇嗗埆澶辫触'
  } finally {
    isDetecting.value = false
  }
}

// ========== WebSocket 鐩稿叧鏂规硶 ==========

// 杩炴帴 WebSocket
const connectWebSocket = () => {
  try {
    // 鍒涘缓 STOMP 瀹㈡埛绔?    stompClient.value = new Client({
      brokerURL: 'ws://localhost:8080/ws',
      reconnectDelay: 5000, // 5 绉掑悗閲嶈繛
      debug: (str) => {
        // console.log('STOMP: ' + str)
      }
    })
    
    // 杩炴帴鎴愬姛鍥炶皟
    stompClient.value.onConnect = () => {
      console.log('WebSocket 杩炴帴鎴愬姛')
      isConnected.value = true
      
      // 璁㈤槄鍛婅棰戦亾
      stompClient.value.subscribe('/topic/alarm', (message) => {
        onAlarmReceived(message.body)
      })
    }
    
    // 杩炴帴澶辫触鍥炶皟
    stompClient.value.onStompError = (frame) => {
      console.error('STOMP 閿欒:', frame)
      isConnected.value = false
    }
    
    // 杩炴帴鏂紑鍥炶皟
    stompClient.value.onWebSocketError = () => {
      console.warn('WebSocket 杩炴帴鏂紑锛? 绉掑悗閲嶈繛')
      isConnected.value = false
    }
    
    // 婵€娲昏繛鎺?    stompClient.value.activate()
    
  } catch (error) {
    console.error('WebSocket 杩炴帴澶辫触:', error)
    isConnected.value = false
  }
}

// 鏂紑 WebSocket
const disconnectWebSocket = () => {
  if (stompClient.value) {
    stompClient.value.deactivate()
    stompClient.value = null
    isConnected.value = false
  }
}

// 瑙﹀彂妯℃嫙鍛婅锛堝紑鍙戠幆澧冧笓鐢級
const triggerMockAlarm = () => {
  if (!isDevEnvironment) return
  
  // 妯℃嫙鍛婅绫诲瀷
  const mockTypes = ['鎵撴灦', '鍙戜紶鍗?]
  const mockType = mockTypes[Math.floor(Math.random() * mockTypes.length)]
  const mockConfidence = 0.75 + Math.random() * 0.2 // 0.75-0.95
  
  // 鏋勫缓妯℃嫙鍛婅娑堟伅
  const mockAlarm = {
    type: mockType,
    confidence: mockConfidence,
    imageUrl: '/monitor/alarm_mock_' + Date.now() + '.jpg',
    location: '妯℃嫙鎽勫儚澶?
  }
  
  console.log('瑙﹀彂妯℃嫙鍛婅:', mockAlarm)
  
  // 閫氳繃 WebSocket 鍙戦€佹ā鎷熷憡璀︼紙鐢ㄤ簬娴嬭瘯 WebSocket 鎺ユ敹锛?  if (stompClient.value && isConnected.value) {
    // 鐩存帴瑙﹀彂鎺ユ敹澶勭悊
    onAlarmReceived(JSON.stringify(mockAlarm))
    
    ElMessage.success(`宸茶Е鍙?{mockType}妯℃嫙鍛婅`)
  } else {
    ElMessage.warning('WebSocket 鏈繛鎺ワ紝鏃犳硶瑙﹀彂妯℃嫙鍛婅')
  }
}

// 鏀跺埌鍛婅娑堟伅
const onAlarmReceived = (messageBody) => {
  try {
    const alarm = JSON.parse(messageBody)
    
    console.log('鏀跺埌鍛婅:', alarm)
    
    // 1. 鎾斁鍛婅闊虫晥
    playAlarmSound()
    
    // 2. 鏄剧ず鍛婅寮圭獥
    ElMessage({
      message: `鍙戠幇${alarm.type}琛屼负锛佺疆淇″害锛?{(alarm.confidence * 100).toFixed(1)}%`,
      type: 'error',
      duration: 5000,
      showClose: true
    })
    
    // 3. 娣诲姞鍒板憡璀﹀垪琛?    const newAlarm = {
      id: Date.now(), // 涓存椂 ID
      alarmType: alarm.type,
      confidence: alarm.confidence,
      imagePath: alarm.imageUrl,
      location: alarm.location,
      alarmTime: new Date().toISOString(),
      isRead: 0
    }
    
    alarmList.value.unshift(newAlarm)
    
    // 4. 鍒锋柊鍒楄〃锛堜粠鍚庣鑾峰彇鏈€鏂版暟鎹級
    loadAlarmList()
    
  } catch (error) {
    console.error('瑙ｆ瀽鍛婅娑堟伅澶辫触:', error)
  }
}

// ========== 鍛婅闊虫晥 ==========
// playAlarmSound 鍑芥暟宸插湪鍓嶉潰瀹氫箟锛堢 818 琛岋級

// ========== 鍛婅鍒楄〃鐩稿叧鏂规硶 ==========

// 鍔犺浇鍛婅鍒楄〃
const loadAlarmList = async () => {
  try {
    const response = await axios.get('http://localhost:8080/api/alarm/list', {
      params: {
        page: 1,
        size: 50
      }
    })
    
    if (response.data && response.data.records) {
      // 缁熶竴瀛楁鍚嶏紙risk_event 鍜?alarm_record 瀛楁鍚嶄笉鍚岋級
      alarmList.value = response.data.records.map(item => ({
        id: item.id,
        alarmType: item.riskType || item.alarmType,
        confidence: item.aiConfidence || item.confidence || 0.9,
        imagePath: item.imageUrl || item.imagePath,
        location: item.location || '鏈煡浣嶇疆',
        alarmTime: item.createTime || item.alarmTime,
        isRead: item.isRead !== undefined ? item.isRead : 0,
        description: item.description || item.aiResult || item.glmResult || '',
        aiResult: item.aiResult || ''
      }))
    }
  } catch (error) {
    console.error('鍔犺浇鍛婅鍒楄〃澶辫触:', error)
  }
}

// 鏍囪涓哄凡璇?const markAsRead = async (id) => {
  try {
    // Ensure ID is a number
    const numericId = Number(id);
    if (isNaN(numericId)) {
      ElMessage.error('鏃犳晥鐨勫憡璀?ID');
      return;
    }
    
    await axios.post(`http://localhost:8080/api/alarm/read/${numericId}`)
    
    const alarm = alarmList.value.find(item => Number(item.id) === numericId)
    if (alarm) {
      alarm.isRead = 1
    }
    
    ElMessage.success('鏍囪鎴愬姛')
  } catch (error) {
    console.error('鏍囪宸茶澶辫触:', error)
    ElMessage.error('鎿嶄綔澶辫触')
  }
}

// 鍏ㄩ儴鏍囪涓哄凡璇?const markAllAsRead = async () => {
  try {
    const ids = alarmList.value
      .filter(item => item.isRead === 0)
      .map(item => Number(item.id))
      .filter(id => !isNaN(id))
    
    if (ids.length === 0) return
    
    await axios.post('http://localhost:8080/api/alarm/read/batch', ids)
    
    alarmList.value.forEach(item => {
      item.isRead = 1
    })
    
    ElMessage.success('鍏ㄩ儴鏍囪涓哄凡璇?)
  } catch (error) {
    console.error('鎵归噺鏍囪澶辫触:', error)
    ElMessage.error('鎿嶄綔澶辫触')
  }
}

// 鍒犻櫎鍛婅璁板綍
const deleteAlarm = async (id) => {
  try {
    await ElMessageBox.confirm(
      '纭畾瑕佸垹闄よ繖鏉″憡璀﹁褰曞悧锛熷垹闄ゅ悗鏃犳硶鎭㈠銆?,
      '鍒犻櫎纭',
      {
        confirmButtonText: '纭畾鍒犻櫎',
        cancelButtonText: '鍙栨秷',
        type: 'warning'
      }
    )
    
    const numericId = Number(id)
    if (isNaN(numericId)) {
      ElMessage.error('鏃犳晥鐨勫憡璀?ID')
      return
    }
    
    await axios.delete(`http://localhost:8080/api/alarm/${numericId}`)
    
    // 浠庡垪琛ㄤ腑绉婚櫎
    alarmList.value = alarmList.value.filter(item => Number(item.id) !== numericId)
    
    // 濡傛灉褰撳墠寮圭獥鏄剧ず鐨勬槸琚垹闄ょ殑璁板綍锛屽叧闂脊绐?    if (selectedAlarm.value && Number(selectedAlarm.value.id) === numericId) {
      closeDialog()
    }
    
    ElMessage.success('鍒犻櫎鎴愬姛')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('鍒犻櫎鍛婅澶辫触:', error)
      ElMessage.error('鍒犻櫎澶辫触')
    }
  }
}

// ========== 宸ュ叿鏂规硶 ==========

// 鏍煎紡鍖栨椂闂达紙瀹屾暣鏃ユ湡鏃堕棿锛?const formatDateTime = (timeStr) => {
  if (!timeStr) return ''
  const date = new Date(timeStr)
  return date.toLocaleString('zh-CN', {
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// 鑾峰彇鍛婅绫诲瀷鏍囩
const getAlarmTypeTag = (type) => {
  switch (type) {
    case '鎵撴灦':
      return 'danger'
    case '鍙戜紶鍗?:
      return 'warning'
    default:
      return 'info'
  }
}

// ========== 鐢熷懡鍛ㄦ湡 ==========

/**
 * 鎵归噺AI鍒嗘瀽鏈鐞嗙殑鐩戞帶鎴浘
 */
const batchAnalyzeSnapshots = async () => {
  try {
    // 纭瀵硅瘽妗?    await ElMessageBox.confirm(
      '灏嗗垎鏋愪粖鏃ヤ笂浼犵殑鐩戞帶鎴浘杩涜AI椋庨櫓璇嗗埆锛岀‘瀹氱户缁悧锛?,
      '瀹炴椂AI鍒嗘瀽',
      {
        confirmButtonText: '纭畾',
        cancelButtonText: '鍙栨秷',
        type: 'warning'
      }
    )
  } catch {
    return // 鐢ㄦ埛鍙栨秷
  }

  isBatchAnalyzing.value = true
  try {
    console.log('=== 寮€濮嬪疄鏃禔I鍒嗘瀽 ===')
    
    const response = await axios.post('http://localhost:8080/api/file/analyze-pending', null, {
      params: {
        cameraLocation: '鏈湴鎽勫儚澶?
      }
    })

    const result = response.data
    console.log('瀹炴椂鍒嗘瀽缁撴灉:', result)
    
    if (result.code === 200 && result.success) {
      const summary = result.summary
      
      // 鏄剧ず鍒嗘瀽缁撴灉
      let message = `瀹炴椂鍒嗘瀽瀹屾垚锛乗n`
      message += `鍏辨壂鎻?${summary.totalScanned} 寮犲浘鐗嘰n`
      message += `鎴愬姛鍒嗘瀽 ${summary.totalSuccess} 寮燶n`
      if (summary.totalFailed > 0) {
        message += `澶辫触 ${summary.totalFailed} 寮燶n`
      }
      message += `\n椋庨櫓缁熻锛歕n`
      message += `馃敶 楂橀闄╋細${summary.highRiskCount} 涓猏n`
      message += `馃煛 涓闄╋細${summary.mediumRiskCount} 涓猏n`
      message += `馃煝 浣庨闄╋細${summary.lowRiskCount} 涓猔

      await ElMessageBox.alert(message, '瀹炴椂AI鍒嗘瀽缁撴灉', {
        confirmButtonText: '纭畾',
        type: summary.highRiskCount > 0 ? 'error' : summary.mediumRiskCount > 0 ? 'warning' : 'success'
      })

      // 閲嶆柊鍔犺浇鍛婅鍒楄〃
      await loadAlarmList()
      
      ElMessage.success('瀹炴椂AI鍒嗘瀽瀹屾垚')
    } else {
      ElMessage.error(result.message || '瀹炴椂鍒嗘瀽澶辫触')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('瀹炴椂AI鍒嗘瀽澶辫触:', error)
      ElMessage.error(error.response?.data?.message || error.message || '瀹炴椂鍒嗘瀽澶辫触锛岃绋嶅悗閲嶈瘯')
    }
  } finally {
    isBatchAnalyzing.value = false
    console.log('=== 瀹炴椂AI鍒嗘瀽瀹屾垚 ===')
  }
}

// ========== 鍛婅璇︽儏寮圭獥 ==========

/**
 * 鏄剧ず鍛婅璇︽儏寮圭獥
 */
const showAlarmDetail = (alarm) => {
  selectedAlarm.value = alarm
  dialogVisible.value = true
  // 闃绘鑳屾櫙婊氬姩
  document.body.style.overflow = 'hidden'
}

/**
 * 鍏抽棴鍛婅璇︽儏寮圭獥
 */
const closeDialog = () => {
  dialogVisible.value = false
  selectedAlarm.value = null
  // 鎭㈠鑳屾櫙婊氬姩
  document.body.style.overflow = ''
}

/**
 * 鏍囪涓哄凡璇诲苟鍏抽棴寮圭獥
 */
const markAsReadAndClose = async () => {
  if (!selectedAlarm.value) return
  
  try {
    // Ensure ID is a number
    const numericId = Number(selectedAlarm.value.id);
    if (isNaN(numericId)) {
      ElMessage.error('鏃犳晥鐨勫憡璀?ID');
      return;
    }
    
    await axios.post(`http://localhost:8080/api/alarm/read/${numericId}`)
    
    // 鏇存柊鏈湴鍒楄〃
    const alarm = alarmList.value.find(item => Number(item.id) === numericId)
    if (alarm) {
      alarm.isRead = 1
    }
    
    // 鏇存柊閫変腑鐨勫憡璀?    if (selectedAlarm.value) {
      selectedAlarm.value.isRead = 1
    }
    
    ElMessage.success('鏍囪鎴愬姛')
    closeDialog()
  } catch (error) {
    console.error('鏍囪宸茶澶辫触:', error)
    ElMessage.error('鎿嶄綔澶辫触')
  }
}

/**
 * ESC 閿叧闂脊绐? */
const handleKeyDown = (e) => {
  if (e.key === 'Escape' && dialogVisible.value) {
    closeDialog()
  }
}

// ========== 鐢熷懡鍛ㄦ湡 ==========


// ========== 模型预设方法 ==========
const fetchModelPresets = async () => {
  try {
    const res = await getModelPresets()
    modelPresets.value = res.presets || []
    currentPreset.value = res.current || 'ORIGINAL'
  } catch (e) {
    console.error('获取模型预设失败:', e)
  }
}

const handleSwitchPreset = async (presetId) => {
  if (presetId === currentPreset.value) return
  switchingPreset.value = true
  try {
    const res = await switchModelPreset(presetId)
    if (res.success) {
      currentPreset.value = presetId
      ElMessage.success(res.message || '模型切换成功')
    } else {
      ElMessage.error(res.message || '切换失败')
    }
  } catch (e) {
    ElMessage.error('模型切换失败: ' + (e.message || '网络错误'))
  } finally {
    switchingPreset.value = false
  }
}
onMounted(async () => {`n  fetchModelPresets()
  // 1. 鑾峰彇鎽勫儚澶村垪琛?  await getCameras()
  
  // 2. 鍔犺浇鍛婅鍒楄〃
  loadAlarmList()
  
  // 3. 妫€鏌ユ祻瑙堝櫒鏀寔
  if (!navigator.mediaDevices || !navigator.mediaDevices.getUserMedia) {
    ElMessage.warning('鎮ㄧ殑娴忚鍣ㄤ笉鏀寔鎽勫儚澶磋闂?)
    currentStatus.type = 'warning'
    currentStatus.text = '娴忚鍣ㄤ笉鏀寔'
  }
  
  // 4. 鐩戝惉 ESC 閿叧闂脊绐?  window.addEventListener('keydown', handleKeyDown)
})

onUnmounted(() => {
  // 娓呯悊璧勬簮
  stopMonitoring()
  
  if (detectTimer.value) {
    clearInterval(detectTimer.value)
  }
  
  if (snapshotTimer.value) {
    clearInterval(snapshotTimer.value)
  }
  
  // 绉婚櫎閿洏鐩戝惉
  window.removeEventListener('keydown', handleKeyDown)
  
  // 鎭㈠鑳屾櫙婊氬姩
  document.body.style.overflow = ''
})
</script>

<style scoped>
.camera-monitor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

/* 椤堕儴鏍囬鏍?*/
.header {
  height: 60px;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.05);
}

.title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

/* 淇 Element Plus 鍥剧墖棰勮闂儊闂 */
.el-image-viewer__wrapper {
  animation: none !important;
  transition: opacity 0.2s ease !important;
}

.el-image-viewer__mask {
  animation: none !important;
  transition: opacity 0.2s ease !important;
}

.el-image-viewer__canvas {
  animation: none !important;
}

.el-image-viewer__close {
  animation: none !important;
}

/* 淇 Element Plus 鍥剧墖棰勮闂儊闂 */
.el-image-viewer__wrapper {
  animation: none !important;
  transition: none !important;
}

.el-image-viewer__mask {
  animation: none !important;
  transition: none !important;
}

.el-image-viewer__canvas {
  animation: none !important;
  transition: none !important;
}

.el-image-viewer__close {
  animation: none !important;
}

.el-image-viewer__actions {
  animation: none !important;
  transition: none !important;
}

.el-image-viewer__btn {
  transition: none !important;
}

/* 涓诲唴瀹瑰尯 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
  padding: 20px;
  gap: 20px;
}

/* 宸︿晶锛氭憚鍍忓ご棰勮鍖猴紙60%锛?*/
.camera-section {
  flex: 0 0 60%;
  display: flex;
}

.camera-card {
  flex: 1;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.video-wrapper {
  flex: 1;
  position: relative;
  background: #000;
  border-radius: 8px;
  overflow: hidden;
  min-height: 400px;
  transition: all 0.3s ease;
}

/* 椋庨櫓鐘舵€佽竟妗?*/
.video-wrapper.risk-normal {
  border: 3px solid #67C23A;
  box-shadow: 0 0 10px rgba(103, 194, 58, 0.3);
}

.video-wrapper.risk-low {
  border: 3px solid #E6A23C;
  box-shadow: 0 0 15px rgba(230, 162, 60, 0.4);
  animation: pulse-low 3s infinite;
}

.video-wrapper.risk-medium {
  border: 3px solid #E6A23C;
  box-shadow: 0 0 25px rgba(230, 162, 60, 0.6);
  animation: pulse-medium 2s infinite;
}

.video-wrapper.risk-high {
  border: 3px solid #F56C6C;
  box-shadow: 0 0 35px rgba(245, 108, 108, 0.8);
  animation: pulse-high 1s infinite;
}

/* 鑴夊啿鍔ㄧ敾 - 浣庨闄╋紙鎱㈤€燂級 */
@keyframes pulse-low {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 15px rgba(230, 162, 60, 0.4);
  }
  50% {
    transform: scale(1.005);
    box-shadow: 0 0 20px rgba(230, 162, 60, 0.5);
  }
}

/* 鑴夊啿鍔ㄧ敾 - 涓闄╋紙涓€燂級 */
@keyframes pulse-medium {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 25px rgba(230, 162, 60, 0.6);
  }
  50% {
    transform: scale(1.015);
    box-shadow: 0 0 35px rgba(230, 162, 60, 0.8);
  }
}

/* 鑴夊啿鍔ㄧ敾 - 楂橀闄╋紙蹇€燂級 */
@keyframes pulse-high {
  0%, 100% {
    transform: scale(1);
    box-shadow: 0 0 35px rgba(245, 108, 108, 0.8);
  }
  50% {
    transform: scale(1.02);
    box-shadow: 0 0 55px rgba(245, 108, 108, 1);
  }
}

/* 鐘舵€佹寚绀哄櫒锛堝乏涓婅鍦嗙偣锛?*/
.status-indicator {
  position: absolute;
  top: 12px;
  left: 12px;
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 6px;
}

.indicator-dot {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #67C23A;
  box-shadow: 0 0 8px rgba(103, 194, 58, 0.6);
  animation: dot-pulse 2s infinite;
}

.status-indicator.status-low .indicator-dot {
  background: #E6A23C;
  box-shadow: 0 0 8px rgba(230, 162, 60, 0.6);
  animation: dot-pulse 2s infinite;
}

.status-indicator.status-medium .indicator-dot {
  background: #E6A23C;
  box-shadow: 0 0 10px rgba(230, 162, 60, 0.8);
  animation: dot-pulse 1.5s infinite;
}

.status-indicator.status-high .indicator-dot {
  background: #F56C6C;
  box-shadow: 0 0 12px rgba(245, 108, 108, 0.9);
  animation: dot-pulse 0.8s infinite;
}

@keyframes dot-pulse {
  0%, 100% {
    transform: scale(1);
    opacity: 1;
  }
  50% {
    transform: scale(1.3);
    opacity: 0.7;
  }
}

/* 椋庨櫓绛夌骇鑴夊啿鍏夌幆 */
.risk-pulse-ring {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  border-radius: 8px;
  pointer-events: none;
  z-index: 5;
}

.risk-pulse-ring.pulse-low {
  animation: ring-pulse-low 3s infinite;
}

.risk-pulse-ring.pulse-medium {
  animation: ring-pulse-medium 2s infinite;
}

.risk-pulse-ring.pulse-high {
  animation: ring-pulse-high 1s infinite;
}

@keyframes ring-pulse-low {
  0%, 100% {
    box-shadow: inset 0 0 20px rgba(230, 162, 60, 0.3);
  }
  50% {
    box-shadow: inset 0 0 40px rgba(230, 162, 60, 0.5);
  }
}

@keyframes ring-pulse-medium {
  0%, 100% {
    box-shadow: inset 0 0 30px rgba(230, 162, 60, 0.5);
  }
  50% {
    box-shadow: inset 0 0 60px rgba(230, 162, 60, 0.7);
  }
}

@keyframes ring-pulse-high {
  0%, 100% {
    box-shadow: inset 0 0 40px rgba(245, 108, 108, 0.6);
  }
  50% {
    box-shadow: inset 0 0 80px rgba(245, 108, 108, 0.9);
  }
}

/* 妯＄硦鏁堟灉锛堣瘑鍒腑锛?*/
.video-element.blur-effect {
  filter: blur(2px);
}

/* 璇嗗埆涓彔鍔犲眰 */
.recognizing-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.75);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 25;
  color: #fff;
  backdrop-filter: blur(4px);
}

.recognizing-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 20px;
}

.recognizing-spinner {
  display: flex;
  align-items: center;
  justify-content: center;
  animation: spinner-rotate 1.5s linear infinite;
}

@keyframes spinner-rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.recognizing-text {
  font-size: 18px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

.recognizing-progress {
  width: 300px;
}

/* 椋庨櫓鐘舵€佹爣绛撅紙鍙充笂瑙掞級 */
.risk-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 14px;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 600;
  color: #fff;
  z-index: 20;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.3);
  transition: all 0.3s ease;
}

.risk-badge.risk-normal {
  background: rgba(103, 194, 58, 0.95);
  box-shadow: 0 4px 12px rgba(103, 194, 58, 0.4);
}

.risk-badge.risk-low {
  background: rgba(230, 162, 60, 0.95);
  box-shadow: 0 4px 12px rgba(230, 162, 60, 0.4);
  animation: badge-pulse 2s infinite;
}

.risk-badge.risk-medium {
  background: rgba(230, 162, 60, 0.98);
  box-shadow: 0 4px 16px rgba(230, 162, 60, 0.6);
  animation: badge-pulse 1.5s infinite;
}

.risk-badge.risk-high {
  background: rgba(245, 108, 108, 0.98);
  box-shadow: 0 4px 20px rgba(245, 108, 108, 0.7);
  animation: badge-pulse 0.8s infinite;
}

@keyframes badge-pulse {
  0%, 100% {
    transform: scale(1);
  }
  50% {
    transform: scale(1.05);
  }
}

/* 鏃堕棿鎴虫爣绛撅紙鍙充笅瑙掞級 */
.timestamp-badge {
  position: absolute;
  bottom: 12px;
  right: 12px;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 6px 12px;
  background: rgba(0, 0, 0, 0.75);
  border-radius: 8px;
  font-size: 13px;
  color: #fff;
  z-index: 20;
  backdrop-filter: blur(8px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.3);
}

.video-element {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
  color: #fff;
  gap: 10px;
  font-size: 14px;
}

.upload-progress-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.8);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  z-index: 25;
  backdrop-filter: blur(4px);
}

.upload-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
}

.upload-text {
  color: #fff;
  font-size: 16px;
  font-weight: 600;
  letter-spacing: 1px;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.5);
}

/* 璇嗗埆缁撴灉鍙犲姞灞傦紙搴曢儴锛?*/
.recognition-result-overlay {
  position: absolute;
  bottom: 0;
  left: 0;
  right: 0;
  background: linear-gradient(to top, rgba(0, 0, 0, 0.9) 0%, rgba(0, 0, 0, 0.7) 70%, transparent 100%);
  padding: 60px 20px 20px;
  z-index: 15;
  animation: slide-up 0.5s ease-out;
}

@keyframes slide-up {
  from {
    transform: translateY(20px);
    opacity: 0;
  }
  to {
    transform: translateY(0);
    opacity: 1;
  }
}

.result-content {
  background: rgba(255, 255, 255, 0.95);
  border-radius: 12px;
  padding: 16px 20px;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.3);
}

.result-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 15px;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
}

.result-description {
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  margin-bottom: 8px;
}

.result-confidence {
  font-size: 13px;
  color: #909399;
  font-weight: 500;
}

/* 鍔犺浇鐘舵€侀鏋跺睆 */
.loading-skeleton {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(90deg, #1a1a1a 25%, #2a2a2a 50%, #1a1a1a 75%);
  background-size: 200% 100%;
  animation: skeleton-loading 1.5s infinite;
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 30;
}

@keyframes skeleton-loading {
  0% {
    background-position: 200% 0;
  }
  100% {
    background-position: -200% 0;
  }
}

.skeleton-content {
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 15px;
  color: #fff;
}

.skeleton-icon {
  color: #909399;
  animation: skeleton-breathe 2s infinite;
}

@keyframes skeleton-breathe {
  0%, 100% {
    opacity: 0.6;
    transform: scale(1);
  }
  50% {
    opacity: 1;
    transform: scale(1.1);
  }
}

.skeleton-text {
  font-size: 16px;
  font-weight: 500;
  letter-spacing: 1px;
}

.control-buttons {
  display: flex;
  gap: 10px;
  margin-top: 20px;
  justify-content: center;
}

.camera-select {
  margin-top: 10px;
  display: flex;
  justify-content: center;
}

/* 鍙充晶锛氬憡璀﹀巻鍙插垪琛紙40%锛?*/
.alarm-section {
  flex: 0 0 40%;
  background: #fff;
  border-radius: 8px;
  padding: 20px;
  display: flex;
  flex-direction: column;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.1);
}

.alarm-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 15px;
  padding-bottom: 15px;
  border-bottom: 1px solid #e4e7ed;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

.alarm-list {
  flex: 1;
  overflow: hidden;
}

.alarm-item {
  display: flex;
  gap: 12px;
  padding: 12px;
  margin-bottom: 12px;
  background: #f5f7fa;
  border-radius: 8px;
  transition: all 0.3s;
  border-left: 4px solid transparent;
  cursor: pointer;
  position: relative;
}

.alarm-item.unread {
  background: #fef0f0;
  border-left-color: #f56c6c;
}

.alarm-item:hover {
  transform: translateX(4px);
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
}

.alarm-delete-btn {
  position: absolute;
  top: 8px;
  right: 8px;
  z-index: 5;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.alarm-item:hover .alarm-delete-btn {
  opacity: 1;
}

.alarm-image-wrapper {
  width: 100px;
  height: 100px;
  flex-shrink: 0;
  border-radius: 6px;
  overflow: hidden;
  background: #e4e7ed;
}

.alarm-image {
  width: 100%;
  height: 100%;
  cursor: pointer;
}

.image-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #e4e7ed;
  color: #909399;
  font-size: 24px;
}

.alarm-info {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 8px;
  min-width: 0;
}

.alarm-header-row {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.alarm-time {
  font-size: 12px;
  color: #909399;
}

.alarm-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 4px;
  font-size: 13px;
  color: #606266;
}

.alarm-location {
  display: flex;
  align-items: center;
  gap: 4px;
}

.alarm-confidence {
  font-size: 12px;
  color: #909399;
}

.alarm-footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ========== 鍛婅璇︽儏寮圭獥 ========== */

.alarm-dialog-overlay {
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
  padding: 20px;
}

.alarm-dialog {
  background: #fff;
  border-radius: 16px;
  box-shadow: 0 20px 60px rgba(0, 0, 0, 0.3);
  width: 100%;
  max-width: 700px;
  max-height: 90vh;
  overflow-y: auto;
  position: relative;
}

.dialog-close {
  position: absolute;
  top: 12px;
  right: 12px;
  width: 36px;
  height: 36px;
  border: none;
  background: rgba(0, 0, 0, 0.06);
  border-radius: 50%;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #606266;
  transition: all 0.2s ease;
  z-index: 10;
}

.dialog-close:hover {
  background: rgba(0, 0, 0, 0.12);
  color: #303133;
}

.dialog-image-section {
  width: 100%;
  background: #000;
  border-radius: 16px 16px 0 0;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  min-height: 200px;
  max-height: 400px;
}

.dialog-image {
  width: 100%;
  height: auto;
  max-height: 400px;
  object-fit: contain;
  display: block;
}

.dialog-image-placeholder {
  width: 100%;
  height: 200px;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 12px;
  color: #909399;
  font-size: 14px;
}

.dialog-info-section {
  padding: 24px;
}

.dialog-risk-badge {
  text-align: center;
  margin-bottom: 20px;
}

.dialog-time-info {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-bottom: 24px;
  padding: 16px;
  background: #f5f7fa;
  border-radius: 12px;
}

.info-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #606266;
}

.info-item .el-icon {
  color: #909399;
  font-size: 16px;
  flex-shrink: 0;
}

.info-item span:first-of-type {
  min-width: 70px;
  color: #909399;
}

.info-value {
  color: #303133;
  font-weight: 500;
  margin-left: auto;
}

.dialog-risk-analysis {
  margin-bottom: 24px;
}

.analysis-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
  color: #303133;
  margin: 0 0 12px 0;
}

.analysis-title .el-icon {
  color: #E6A23C;
  font-size: 18px;
}

.analysis-content {
  padding: 16px;
  background: #fef0f0;
  border-radius: 8px;
  border-left: 4px solid #F56C6C;
}

.analysis-content p {
  margin: 0;
  font-size: 14px;
  line-height: 1.6;
  color: #606266;
  white-space: pre-wrap;
  word-break: break-word;
}

.dialog-actions {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
}

/* 寮圭獥杩囨浮鍔ㄧ敾 - 浣跨敤绾€忔槑搴﹀彉鍖?閬垮厤缂╂斁瀵艰嚧鐨勯棯鐑?*/
.alarm-dialog-enter-active {
  transition: opacity 0.25s ease;
}

.alarm-dialog-leave-active {
  transition: opacity 0.2s ease;
}

.alarm-dialog-enter-from,
.alarm-dialog-leave-to {
  opacity: 0;
}

.alarm-dialog-enter-active .alarm-dialog {
  transition: opacity 0.25s ease;
}

.alarm-dialog-leave-active .alarm-dialog {
  transition: opacity 0.2s ease;
}

.alarm-dialog-enter-from .alarm-dialog,
.alarm-dialog-leave-to .alarm-dialog {
  opacity: 0;
}

/* 绉诲姩绔搷搴斿紡 */
@media (max-width: 768px) {
  .alarm-dialog-overlay {
    padding: 10px;
    align-items: flex-end;
  }

  .alarm-dialog {
    max-width: 100%;
    max-height: 85vh;
    border-radius: 16px 16px 0 0;
  }

  .dialog-image-section {
    max-height: 250px;
    border-radius: 16px 16px 0 0;
  }

  .dialog-info-section {
    padding: 16px;
  }

  .dialog-actions {
    flex-direction: column-reverse;
  }

  .dialog-actions .el-button {
    width: 100%;
  }
}

/* 鍝嶅簲寮忚璁?*/
@media (max-width: 1200px) {
  .main-content {
    flex-direction: column;
  }
  
  .camera-section,
  .alarm-section {
    flex: none;
    width: 100%;
  }
  
  .camera-section {
    min-height: 500px;
  }
}

/* ===== 模型选择器样式 ===== */
.model-selector {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-right: 16px;
}

.preset-select {
  width: 200px;
}

.preset-option {
  display: flex;
  flex-direction: column;
  line-height: 1.3;
}

.preset-name {
  font-weight: 600;
  font-size: 14px;
  color: #303133;
}

.preset-models {
  font-size: 12px;
  color: #909399;
  margin-top: 2px;
}

.preset-info-icon {
  color: #909399;
  cursor: help;
  font-size: 16px;
  transition: color 0.2s;
}

.preset-info-icon:hover {
  color: #409EFF;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 12px;
}
</style>





