<template>
  <div class="risk-map-wrapper">
    <!-- 默认缩略视图 -->
    <div
      class="map-thumbnail"
      @click="openFullscreen"
    >
      <div class="thumbnail-container" ref="thumbnailContainerRef">
        <img
          :src="mapImageUrl"
          class="thumbnail-image"
          ref="thumbnailImageRef"
          alt="小区平面图"
          draggable="false"
          @load="onThumbnailImageLoad"
        />
        <!-- 缩略图 Canvas 层 -->
        <canvas
          ref="thumbnailCanvasRef"
          class="thumbnail-canvas"
        />
      </div>
      <!-- 点击提示 -->
      <div class="thumbnail-overlay">
        <div class="expand-btn">
          <el-icon><ZoomIn /></el-icon>
          <span>点击放大查看</span>
        </div>
      </div>
      <!-- 风险点数量 -->
      <div v-if="imageLoaded" class="risk-count-badge">
        <el-badge :value="points.length" :max="99" type="danger">
          <span>风险点</span>
        </el-badge>
      </div>
    </div>

    <!-- 全屏弹窗 -->
    <Teleport to="body">
      <Transition name="fullscreen-fade">
        <div
          v-if="showFullscreen"
          class="fullscreen-overlay"
          @click.self="closeFullscreen"
        >
          <div class="fullscreen-modal">
            <!-- 头部 -->
            <div class="fullscreen-header">
              <h3>🗺️ 风险地图</h3>
              <div class="header-actions">
                <span class="point-count">共 {{ points.length }} 个风险点</span>
                <button class="close-btn" @click="closeFullscreen">&times;</button>
              </div>
            </div>

            <!-- 地图内容 -->
            <div class="fullscreen-body">
              <div class="map-container" ref="containerRef">
                <!-- 地图内容层 - 包含图片和 Canvas，两者由 flex 居中 -->
                <div class="map-content" :style="canvasTransformStyle">
                  <!-- 背景图片 -->
                  <img
                    ref="bgImageRef"
                    :src="mapImageUrl"
                    class="background-image"
                    alt="小区平面图"
                    draggable="false"
                    @load="onImageLoad"
                  />

                  <!-- Canvas 渲染层 - 绝对定位覆盖在图片上 -->
                  <canvas
                    ref="canvasRef"
                    class="canvas-layer"
                    @click.stop="onCanvasClick"
                  />
                </div>

                <!-- 空状态提示 -->
                <div v-if="imageLoaded && points.length === 0 && !loading" class="empty-state">
                  <span>暂无风险</span>
                </div>

                <!-- 加载状态 -->
                <div v-if="loading && !imageLoaded" class="loading-overlay">
                  <el-icon class="is-loading"><Loading /></el-icon>
                  <span>加载中...</span>
                </div>
              </div>
            </div>

            <!-- 底部图例 -->
            <div class="fullscreen-footer">
              <div class="legend-bar">
                <div class="legend-item high-risk">
                  <span class="legend-dot"></span>
                  <span>高风险</span>
                </div>
                <div class="legend-item medium-risk">
                  <span class="legend-dot"></span>
                  <span>中风险</span>
                </div>
                <div class="legend-item low-risk">
                  <span class="legend-dot"></span>
                  <span>低风险</span>
                </div>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>

    <!-- 风险详情弹窗 -->
    <Teleport to="body">
      <Transition name="dialog-fade">
        <div
          v-if="showDetailDialog && selectedPoint"
          class="detail-dialog-overlay"
          @click.self="closeDetailDialog"
        >
          <div class="detail-dialog-card">
            <div class="detail-header" :class="selectedPoint.levelClass">
              <span class="detail-title">{{ selectedPoint.desc || '未知风险' }}</span>
              <button class="detail-close-btn" @click="closeDetailDialog">&times;</button>
            </div>
            <div class="detail-body">
              <div class="detail-row">
                <span class="detail-label">风险等级</span>
                <el-tag :type="selectedPoint.tagType" size="small">{{ selectedPoint.levelText }}</el-tag>
              </div>
              <div class="detail-row">
                <span class="detail-label">所属区域</span>
                <span class="detail-value">{{ selectedPoint.areaName }}</span>
              </div>
              <div class="detail-row">
                <span class="detail-label">发生时间</span>
                <span class="detail-value">{{ selectedPoint.time }}</span>
              </div>
            </div>
          </div>
        </div>
      </Transition>
    </Teleport>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted, nextTick } from 'vue'
import axios from 'axios'
import { Loading, ZoomIn } from '@element-plus/icons-vue'

// ==================== 常量配置 ====================

const MAP_IMAGE_URL = 'http://localhost:8080/upload/images/map.png'

/** 区域配置 - 归一化坐标 0~1 */
const AREAS_CONFIG = [
  { name: '1号楼', xRange: [0.28, 0.48], yRange: [0.08, 0.20] },
  { name: '2号楼', xRange: [0.52, 0.72], yRange: [0.08, 0.20] },
  { name: '3号楼', xRange: [0.30, 0.48], yRange: [0.48, 0.62], weight: 0.2 },
  { name: '4号楼', xRange: [0.52, 0.70], yRange: [0.48, 0.62], weight: 0.4 },
  { name: '中心花园', xRange: [0.35, 0.65], yRange: [0.18, 0.45], weight: 0.1 },
  { name: '左停车场', xRange: [0.15, 0.30], yRange: [0.20, 0.60], weight: 0.1 },
  { name: '右停车场', xRange: [0.70, 0.85], yRange: [0.20, 0.60], weight: 0.1 },
  { name: '游泳池', xRange: [0.32, 0.48], yRange: [0.65, 0.80], weight: 0.05 },
  { name: '健身区', xRange: [0.48, 0.60], yRange: [0.65, 0.80], weight: 0.025 },
  { name: '儿童区', xRange: [0.60, 0.75], yRange: [0.65, 0.80], weight: 0.025 },
  { name: '前门', xRange: [0.46, 0.54], yRange: [0.85, 0.95] },
  { name: '后门', xRange: [0.46, 0.54], yRange: [0.05, 0.15] }
]

/** 风险等级样式配置 */
const RISK_LEVEL_STYLE = {
  high: {
    color: '#ff4d4f',
    radius: 12,
    levelText: '高风险',
    tagType: 'danger',
    levelClass: 'high'
  },
  medium: {
    color: '#faad14',
    radius: 8,
    levelText: '中风险',
    tagType: 'warning',
    levelClass: 'medium'
  },
  low: {
    color: '#fae069',
    radius: 5,
    levelText: '低风险',
    tagType: 'success',
    levelClass: 'low'
  }
}

// ==================== 响应式状态 ====================

const containerRef = ref(null)
const canvasRef = ref(null)
const bgImageRef = ref(null)

// 缩略图相关引用
const thumbnailContainerRef = ref(null)
const thumbnailImageRef = ref(null)
const thumbnailCanvasRef = ref(null)

const loading = ref(false)
const imageLoaded = ref(false)
const isZoomed = ref(false)
const zoomScale = ref(1)
const zoomOffsetX = ref(0)
const zoomOffsetY = ref(0)

const points = ref([])
const showDetailDialog = ref(false)
const selectedPoint = ref(null)

// 全屏弹窗状态
const showFullscreen = ref(false)

let ctx = null
let animationFrameId = null
let breathPhase = 0

// 缩略图 Canvas 渲染
let thumbnailCtx = null
let thumbnailAnimationFrameId = null
let thumbnailBreathPhase = 0
let thumbnailImageDisplayInfo = {
  left: 0,
  top: 0,
  width: 0,
  height: 0
}

// 图片 URL
const mapImageUrl = MAP_IMAGE_URL

// ResizeObserver 监听图片尺寸变化
let imageResizeObserver = null
let thumbnailResizeObserver = null

// Canvas transform 样式（用于缩放）
const canvasTransformStyle = computed(() => {
  if (!isZoomed.value) return {}
  return {
    transformOrigin: `${zoomOffsetX.value}px ${zoomOffsetY.value}px`,
    transform: `scale(${zoomScale.value})`
  }
})

// ==================== 工具函数 ====================

/**
 * 在指定范围内生成随机数
 * @param {[number, number]} range [min, max]
 * @returns {number}
 */
function randomInRange(range) {
  return range[0] + Math.random() * (range[1] - range[0])
}

/**
 * 安全点位生成器 - 确保不重叠
 * @param {Object} area 区域配置 {xRange, yRange}
 * @param {Array} existingPoints 已有点位列表 [{x, y}, ...]
 * @param {number} minDistance 最小归一化距离（默认 0.02）
 * @returns {{x: number, y: number}} 生成的归一化坐标点
 */
function generateSafePoint(area, existingPoints, minDistance = 0.02) {
  let point = null
  let safe = false
  const maxAttempts = 200
  let attempts = 0

  while (!safe && attempts < maxAttempts) {
    point = {
      x: randomInRange(area.xRange),
      y: randomInRange(area.yRange)
    }

    safe = existingPoints.every((p) => {
      const dx = p.x - point.x
      const dy = p.y - point.y
      return Math.sqrt(dx * dx + dy * dy) > minDistance
    })

    attempts++
  }

  // 如果尝试次数过多仍未找到安全位置，返回最后一次生成的点（允许微小重叠）
  if (!safe) {
    console.warn(`[RiskMap] 在区域 "${area.name}" 内经过 ${maxAttempts} 次尝试后仍存在重叠`)
  }

  return point
}

/**
 * 根据 area 名称查找区域配置（严格匹配）
 * @param {string} areaName 区域名称
 * @returns {Object|null}
 */
function findAreaByName(areaName) {
  if (!areaName) return null
  return AREAS_CONFIG.find((a) => a.name === areaName) || null
}

/**
 * 根据位置描述智能匹配区域（模糊匹配）
 * 支持包含关系，例如："小区大门" → "1号楼" 或 "2号楼"
 * @param {string} location 位置描述
 * @returns {Object|null}
 */
function findAreaByLocation(location) {
  if (!location || !location.trim()) return null

  const loc = location.trim()

  // 1. 先尝试严格匹配
  const exactMatch = findAreaByName(loc)
  if (exactMatch) return exactMatch

  // 2. 模糊匹配规则
  for (const area of AREAS_CONFIG) {
    const areaName = area.name

    // 规则1：位置包含区域名称（如"健身区"包含"健身区"）
    if (loc.includes(areaName) || areaName.includes(loc)) {
      return area
    }

    // 规则2：关键字匹配
    if ((areaName.includes('楼') && loc.includes('1号楼')) ||
        (areaName.includes('停车') && loc.includes('停车')) ||
        (areaName.includes('花园') && (loc.includes('花园') || loc.includes('中心'))) ||
        (areaName.includes('游泳') && loc.includes('游泳')) ||
        (areaName.includes('健身') && loc.includes('健身')) ||
        (areaName.includes('儿童') && (loc.includes('儿童') || loc.includes('游乐')))) {
      return area
    }
  }

  // 3. 特殊位置映射
  const specialMappings = [
    { keywords: ['大门', '主入口', '前门'], area: '前门' },
    { keywords: ['后门', '次入口'], area: '后门' },
    { keywords: ['楼道', '走廊', '通道'], area: '1号楼' },
    { keywords: ['监控区域', '其他'], area: '中心花园' }
  ]

  for (const mapping of specialMappings) {
    if (mapping.keywords.some(keyword => loc.includes(keyword))) {
      return findAreaByName(mapping.area)
    }
  }

  // 4. 默认返回 null（不匹配）
  return null
}

/**
 * 解析风险等级字符串为标准 key
 * @param {string} level 后端返回的等级字符串
 * @returns {'high'|'medium'|'low'}
 */
function parseLevel(level) {
  const str = String(level || '').toLowerCase()
  if (str.includes('high')) return 'high'
  if (str.includes('medium') || str.includes('warning') || str.includes('mid')) return 'medium'
  if (str.includes('low')) return 'low'
  // 默认返回 medium
  return 'medium'
}

/**
 * 格式化时间字符串
 * @param {string|Date} time 时间
 * @returns {string} 格式化后的时间
 */
function formatTime(time) {
  if (!time) return ''

  let date
  if (typeof time === 'string') {
    date = new Date(time)
  } else if (time instanceof Date) {
    date = time
  } else {
    return ''
  }

  // 检查日期是否有效
  if (isNaN(date.getTime())) return ''

  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')

  return `${year}-${month}-${day} ${hours}:${minutes}`
}

// ==================== 数据加载 ====================

/**
 * 从后端接口加载风险数据
 */
async function fetchRiskData() {
  loading.value = true
  try {
    const response = await axios.get('/api/risk/map')
    const rawData = Array.isArray(response.data) ? response.data : []

    console.log('[RiskMap] 后端返回数据条数:', rawData.length)
    if (rawData.length > 0) {
      console.log('[RiskMap] 示例数据:', rawData[0])
    }

    if (rawData.length === 0) {
      points.value = []
      return
    }

    // 按区域分组处理，确保同区域内点位不重叠
    const areaGroups = new Map()
    for (const item of rawData) {
      // ⚠️ 关键修复：适配后端实际字段名
      // 后端返回 riskLocation，而不是 area
      const location = item.riskLocation || item.location || item.area || ''

      // 智能匹配区域（支持模糊匹配）
      const area = findAreaByLocation(location)
      if (!area) {
        console.warn(`[RiskMap] 忽略未知区域: "${location}"`)
        continue
      }

      if (!areaGroups.has(area)) {
        areaGroups.set(area, [])
      }
      areaGroups.get(area).push(item)
    }

    console.log(`[RiskMap] 成功匹配 ${areaGroups.size} 个区域`)

    // 为每个区域内的风险项生成安全的归一化坐标
    const generatedPoints = []

    for (const [area, items] of areaGroups) {
      const existingInArea = [] // 该区域内已生成的点位

      for (const item of items) {
        // ⚠️ 关键修复：使用后端实际的字段名
        const level = parseLevel(item.riskLevel || item.level || 'MEDIUM')
        const coord = generateSafePoint(area, existingInArea)

        // 将该点加入已有点列表，防止后续重叠
        existingInArea.push(coord)

        const styleConfig = RISK_LEVEL_STYLE[level]

        generatedPoints.push({
          id: item.id,
          areaName: area.name,
          desc: item.title || item.riskType || item.desc || '未知风险',
          time: formatTime(item.createTime || item.time || new Date()),
          level,
          ...coord, // x, y 归一化坐标
          radius: styleConfig.radius,
          color: styleConfig.color,
          levelText: styleConfig.levelText,
          tagType: styleConfig.tagType,
          levelClass: styleConfig.levelClass
        })
      }
    }

    points.value = generatedPoints
    console.log(`[RiskMap] 数据加载完成，共 ${points.value.length} 个风险点`)
  } catch (error) {
    console.error('[RiskMap] 加载风险数据失败:', error)
    points.value = []
  } finally {
    loading.value = false
  }
}

// ==================== Canvas 渲染 ====================

/**
 * 图片在容器中的实际显示信息
 */
const imageDisplayInfo = ref({
  left: 0,
  top: 0,
  width: 0,
  height: 0
})

/**
 * 初始化 Canvas 尺寸和上下文
 * 关键：Canvas 必须覆盖在图片实际显示区域上，两者共享同一坐标系
 */
function initCanvas() {
  const canvas = canvasRef.value
  const img = bgImageRef.value

  if (!canvas || !img) {
    console.warn('[RiskMap] Canvas 或图片 DOM 未就绪')
    return
  }

  // 等待一帧确保图片渲染完成
  requestAnimationFrame(() => {
    // 安全检查：确保 DOM 仍然存在
    const currentCanvas = canvasRef.value
    const currentImg = bgImageRef.value
    if (!currentCanvas || !currentImg) {
      console.warn('[RiskMap] requestAnimationFrame 回调中 DOM 已失效')
      return
    }

    const mapContent = currentCanvas.parentElement
    if (!mapContent) {
      console.warn('[RiskMap] Canvas 父容器未就绪')
      return
    }

    const imgRect = currentImg.getBoundingClientRect()
    const contentRect = mapContent.getBoundingClientRect()

    // ✅ 关键修复：计算图片相对于 .map-content 的偏移
    const offsetX = imgRect.left - contentRect.left
    const offsetY = imgRect.top - contentRect.top

    // Canvas 尺寸设置为与图片实际显示尺寸一致
    currentCanvas.width = imgRect.width
    currentCanvas.height = imgRect.height

    // Canvas 定位到图片的位置
    currentCanvas.style.left = `${offsetX}px`
    currentCanvas.style.top = `${offsetY}px`

    imageDisplayInfo.value = {
      left: 0,
      top: 0,
      width: imgRect.width,
      height: imgRect.height
    }

    ctx = currentCanvas.getContext('2d')
    if (!ctx) {
      console.error('[RiskMap] 无法获取 Canvas 2D 上下文')
      return
    }

    console.log(`[RiskMap] 图片实际显示: ${imgRect.width.toFixed(0)} x ${imgRect.height.toFixed(0)}`)
    console.log(`[RiskMap] Canvas 偏移: (${offsetX.toFixed(0)}, ${offsetY.toFixed(0)})`)
    console.log(`[RiskMap] Canvas 尺寸: ${currentCanvas.width} x ${currentCanvas.height}`)
  })
}

/**
 * 绘制所有风险点（带呼吸动画）
 * ⚠️ 关键：点位坐标基于图片实际显示区域，而不是容器
 */
function drawPoints(timestamp) {
  if (!ctx || !canvasRef.value) return

  const canvas = canvasRef.value
  ctx.clearRect(0, 0, canvas.width, canvas.height)

  if (points.value.length === 0) return

  // 使用图片实际显示区域的宽高来计算点位
  const imgWidth = imageDisplayInfo.value.width
  const imgHeight = imageDisplayInfo.value.height

  // 如果图片还没加载完成，不绘制
  if (imgWidth === 0 || imgHeight === 0) return

  // 呼吸动画相位递增（每帧约增加 0.04，周期约 2.5 秒完成一次呼吸）
  breathPhase += 0.04

  for (let i = 0; i < points.value.length; i++) {
    const pt = points.value[i]

    // ✅ 关键修复：基于图片实际显示区域计算像素坐标
    // 而不是基于容器尺寸，这样点位永远落在图片上
    const px = pt.x * imgWidth
    const py = pt.y * imgHeight

    // 呼吸效果：透明度在 0.55 ~ 1.0 之间波动
    const breathAlpha = 0.72 + Math.sin(breathPhase + i * 0.8) * 0.28
    // 光晕半径随呼吸变化
    const glowRadius = pt.radius * (1.3 + Math.sin(breathPhase + i * 0.8) * 0.4)

    ctx.save()

    // 绘制外发光/光晕
    ctx.globalAlpha = breathAlpha * 0.25
    ctx.fillStyle = pt.color
    ctx.beginPath()
    ctx.arc(px, py, glowRadius, 0, Math.PI * 2)
    ctx.fill()

    // 绘制实心圆点
    ctx.globalAlpha = breathAlpha
    ctx.fillStyle = pt.color
    ctx.shadowColor = pt.color
    ctx.shadowBlur = 8
    ctx.beginPath()
    ctx.arc(px, py, pt.radius, 0, Math.PI * 2)
    ctx.fill()

    // 高光点缀（让圆点更立体）
    ctx.globalAlpha = 0.6
    ctx.fillStyle = '#ffffff'
    ctx.shadowBlur = 0
    ctx.beginPath()
    ctx.arc(px - pt.radius * 0.3, py - pt.radius * 0.3, pt.radius * 0.22, 0, Math.PI * 2)
    ctx.fill()

    ctx.restore()
  }

  // 继续下一帧动画循环
  animationFrameId = requestAnimationFrame(drawPoints)
}

/**
 * 启动渲染流程
 */
function startRendering() {
  initCanvas()
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
  }
  animationFrameId = requestAnimationFrame(drawPoints)
}

/**
 * 停止渲染
 */
function stopRendering() {
  if (animationFrameId) {
    cancelAnimationFrame(animationFrameId)
    animationFrameId = null
  }
}

// ==================== 交互事件 ====================

/**
 * 图片加载完成（全屏弹窗中的图片）
 */
function onImageLoad() {
  imageLoaded.value = true
  console.log('[RiskMap] 背景图片加载完成')

  nextTick(() => {
    startRendering()
    fetchRiskData().then(() => {
      // 数据更新后重新渲染会在 drawPoints 的下一帧自动生效
    })

    // 设置 ResizeObserver 监听图片尺寸变化
    setupImageResizeObserver()
  })
}

/**
 * 设置 ResizeObserver 监听图片尺寸变化（全屏弹窗）
 */
function setupImageResizeObserver() {
  if (!window.ResizeObserver) {
    console.warn('[RiskMap] ResizeObserver 不支持')
    return
  }

  // 清理旧的 Observer
  if (imageResizeObserver) {
    imageResizeObserver.disconnect()
  }

  const img = bgImageRef.value
  if (!img) return

  imageResizeObserver = new ResizeObserver(() => {
    console.log('[RiskMap] 图片尺寸变化，重新定位 Canvas')
    if (imageLoaded.value) {
      stopRendering()
      startRendering()
    }
  })

  imageResizeObserver.observe(img)
}

/**
 * 图片加载完成（缩略图中的图片）
 */
function onThumbnailImageLoad() {
  imageLoaded.value = true
  console.log('[RiskMap] 缩略图加载完成')

  // 立即获取数据并渲染缩略图 Canvas
  fetchRiskData().then(() => {
    nextTick(() => {
      initThumbnailCanvas()
      startThumbnailRendering()
    })
  })

  // 设置 ResizeObserver 监听缩略图图片尺寸变化
  setupThumbnailResizeObserver()
}

/**
 * 设置 ResizeObserver 监听图片尺寸变化（缩略图）
 */
function setupThumbnailResizeObserver() {
  if (!window.ResizeObserver) {
    console.warn('[RiskMap] ResizeObserver 不支持')
    return
  }

  // 清理旧的 Observer
  if (thumbnailResizeObserver) {
    thumbnailResizeObserver.disconnect()
  }

  const img = thumbnailImageRef.value
  if (!img) return

  thumbnailResizeObserver = new ResizeObserver(() => {
    console.log('[RiskMap] 缩略图图片尺寸变化，重新定位 Canvas')
    if (imageLoaded.value) {
      stopThumbnailRendering()
      initThumbnailCanvas()
      startThumbnailRendering()
    }
  })

  thumbnailResizeObserver.observe(img)
}

/**
 * 初始化缩略图 Canvas
 */
function initThumbnailCanvas() {
  const canvas = thumbnailCanvasRef.value
  const img = thumbnailImageRef.value
  const container = thumbnailContainerRef.value

  if (!canvas || !img || !container) {
    console.warn('[RiskMap] 缩略图 DOM 元素未就绪')
    return
  }

  // 等待一帧确保图片渲染完成
  requestAnimationFrame(() => {
    const rect = img.getBoundingClientRect()
    const containerRect = container.getBoundingClientRect()

    thumbnailImageDisplayInfo = {
      left: rect.left - containerRect.left,
      top: rect.top - containerRect.top,
      width: rect.width,
      height: rect.height
    }

    canvas.width = rect.width
    canvas.height = rect.height
    canvas.style.left = `${thumbnailImageDisplayInfo.left}px`
    canvas.style.top = `${thumbnailImageDisplayInfo.top}px`

    thumbnailCtx = canvas.getContext('2d')
    if (!thumbnailCtx) {
      console.error('[RiskMap] 无法获取缩略图 Canvas 2D 上下文')
      return
    }

    console.log(`[RiskMap] 缩略图图片实际显示: ${rect.width.toFixed(0)} x ${rect.height.toFixed(0)}`)
    console.log(`[RiskMap] 缩略图图片位置: (${thumbnailImageDisplayInfo.left.toFixed(0)}, ${thumbnailImageDisplayInfo.top.toFixed(0)})`)
    console.log(`[RiskMap] 缩略图 Canvas 尺寸: ${canvas.width} x ${canvas.height}`)
  })
}

/**
 * 绘制缩略图风险点
 */
function drawThumbnailPoints(timestamp) {
  if (!thumbnailCtx || !thumbnailCanvasRef.value) return

  const canvas = thumbnailCanvasRef.value
  thumbnailCtx.clearRect(0, 0, canvas.width, canvas.height)

  if (points.value.length === 0) return

  const imgWidth = thumbnailImageDisplayInfo.width
  const imgHeight = thumbnailImageDisplayInfo.height

  if (imgWidth === 0 || imgHeight === 0) return

  thumbnailBreathPhase += 0.04

  for (let i = 0; i < points.value.length; i++) {
    const pt = points.value[i]

    const px = pt.x * imgWidth
    const py = pt.y * imgHeight

    const breathAlpha = 0.72 + Math.sin(thumbnailBreathPhase + i * 0.8) * 0.28
    const glowRadius = pt.radius * (1.3 + Math.sin(thumbnailBreathPhase + i * 0.8) * 0.4)

    thumbnailCtx.save()

    thumbnailCtx.globalAlpha = breathAlpha * 0.25
    thumbnailCtx.fillStyle = pt.color
    thumbnailCtx.beginPath()
    thumbnailCtx.arc(px, py, glowRadius, 0, Math.PI * 2)
    thumbnailCtx.fill()

    thumbnailCtx.globalAlpha = breathAlpha
    thumbnailCtx.fillStyle = pt.color
    thumbnailCtx.shadowColor = pt.color
    thumbnailCtx.shadowBlur = 8
    thumbnailCtx.beginPath()
    thumbnailCtx.arc(px, py, pt.radius, 0, Math.PI * 2)
    thumbnailCtx.fill()

    thumbnailCtx.globalAlpha = 0.6
    thumbnailCtx.fillStyle = '#ffffff'
    thumbnailCtx.shadowBlur = 0
    thumbnailCtx.beginPath()
    thumbnailCtx.arc(px - pt.radius * 0.3, py - pt.radius * 0.3, pt.radius * 0.22, 0, Math.PI * 2)
    thumbnailCtx.fill()

    thumbnailCtx.restore()
  }

  thumbnailAnimationFrameId = requestAnimationFrame(drawThumbnailPoints)
}

/**
 * 启动缩略图渲染
 */
function startThumbnailRendering() {
  initThumbnailCanvas()
  if (thumbnailAnimationFrameId) {
    cancelAnimationFrame(thumbnailAnimationFrameId)
  }
  thumbnailAnimationFrameId = requestAnimationFrame(drawThumbnailPoints)
}

/**
 * 停止缩略图渲染
 */
function stopThumbnailRendering() {
  if (thumbnailAnimationFrameId) {
    cancelAnimationFrame(thumbnailAnimationFrameId)
    thumbnailAnimationFrameId = null
  }
}

/**
 * 点击地图容器 - 切换放大模式
 */
function onMapContainerClick(event) {
  // 如果点击的是弹窗或弹窗内元素则忽略
  if (showDetailDialog.value) return

  const container = containerRef.value
  if (!container || !imageLoaded.value) return

  const rect = container.getBoundingClientRect()
  const clickX = event.clientX - rect.left
  const clickY = event.clientY - rect.top

  if (!isZoomed.value) {
    // 进入放大模式：以点击位置为中心放大到 2 倍
    isZoomed.value = true
    zoomScale.value = 2
    zoomOffsetX.value = clickX
    zoomOffsetY.value = clickY
  } else {
    // 恢复原始大小
    isZoomed.value = false
    zoomScale.value = 1
    zoomOffsetX.value = 0
    zoomOffsetY.value = 0
  }
}

/**
 * 点击 Canvas 层 - 检测是否命中某个风险点
 */
function onCanvasClick(event) {
  if (!ctx || !canvasRef.value || showDetailDialog.value) return

  const canvas = canvasRef.value
  const canvasRect = canvas.getBoundingClientRect()

  // 计算点击在 Canvas 内的像素坐标
  let clickX = event.clientX - canvasRect.left
  let clickY = event.clientY - canvasRect.top

  if (isZoomed.value) {
    // 放大模式下需要将屏幕坐标还原到逻辑坐标
    clickX = (clickX - zoomOffsetX.value) / zoomScale.value + zoomOffsetX.value
    clickY = (clickY - zoomOffsetY.value) / zoomScale.value + zoomOffsetY.value
  }

  // 使用 Canvas 的宽高来计算点位位置
  const canvasWidth = canvas.width
  const canvasHeight = canvas.height

  if (canvasWidth === 0 || canvasHeight === 0) return

  console.log(`[RiskMap] 点击坐标: (${clickX.toFixed(1)}, ${clickY.toFixed(1)})`)

  // 查找被点击的风险点（从上层往下遍历，优先选中最上面的点）
  let hitPoint = null
  for (let i = points.value.length - 1; i >= 0; i--) {
    const pt = points.value[i]
    // 基于 Canvas 宽高计算点位像素坐标
    const px = pt.x * canvasWidth
    const py = pt.y * canvasHeight

    console.log(`  点位 ${pt.areaName}: (${px.toFixed(1)}, ${py.toFixed(1)})`)

    const dist = Math.sqrt(Math.pow(clickX - px, 2) + Math.pow(clickY - py, 2))

    // 点击判定范围略大于视觉半径，提升可操作性
    if (dist <= Math.max(pt.radius + 8, 16)) {
      hitPoint = pt
      console.log(`  -> 命中: ${pt.desc}`)
      break
    }
  }

  if (hitPoint) {
    selectedPoint.value = hitPoint
    showDetailDialog.value = true
  } else {
    console.log(`  -> 未命中任何点`)
  }
}

/**
 * 关闭详情弹窗
 */
function closeDetailDialog() {
  showDetailDialog.value = false
  selectedPoint.value = null
}

/**
 * 处理窗口尺寸变化
 */
function handleResize() {
  if (imageLoaded.value) {
    stopRendering()
    startRendering()
    stopThumbnailRendering()
    startThumbnailRendering()
  }
}

// ESC 键关闭弹窗
function handleKeydown(event) {
  if (event.key === 'Escape') {
    closeDetailDialog()
    if (isZoomed.value) {
      isZoomed.value = false
      zoomScale.value = 1
      zoomOffsetX.value = 0
      zoomOffsetY.value = 0
    }
  }
}

// ==================== 全屏弹窗 ====================

/**
 * 打开全屏弹窗
 */
function openFullscreen() {
  showFullscreen.value = true
  // 打开后需要等 DOM 渲染完成再初始化 Canvas
  nextTick(() => {
    startRendering()
  })
}

/**
 * 关闭全屏弹窗
 */
function closeFullscreen() {
  showFullscreen.value = false
  stopRendering()
  // 重置缩放状态
  isZoomed.value = false
  zoomScale.value = 1
  zoomOffsetX.value = 0
  zoomOffsetY.value = 0
}

// ==================== 生命周期 ====================

onMounted(() => {
  window.addEventListener('resize', handleResize)
  window.addEventListener('keydown', handleKeydown)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  window.removeEventListener('keydown', handleKeydown)
  stopRendering()
  stopThumbnailRendering()

  // 清理 ResizeObserver
  if (imageResizeObserver) {
    imageResizeObserver.disconnect()
  }
  if (thumbnailResizeObserver) {
    thumbnailResizeObserver.disconnect()
  }
})
</script>

<style scoped>
/* ==================== 整体布局 ==================== */

.risk-map-wrapper {
  width: 100%;
  height: 100%;
  display: flex;
  flex-direction: column;
  background: transparent;
  border-radius: 12px;
  overflow: visible;

  /* 移除固定高度，让缩略图自适应 */
  min-height: 0;
}

/* ==================== 缩略视图（默认显示） ==================== */

.map-thumbnail {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 500px; /* 缩略图最小高度：从 200px 改为 500px */
  max-height: 700px; /* 缩略图最大高度：从 300px 改为 700px */
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
}

.map-thumbnail:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.15);
  transform: translateY(-2px);
}

.thumbnail-container {
  position: relative;
  width: 100%;
  height: 100%;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
}

/* 缩略图背景 */
.thumbnail-image {
  position: relative;
  max-width: 100%;
  max-height: 100%;
  object-fit: contain;
  pointer-events: none;
  user-select: none;
}

/* 缩略图 Canvas */
.thumbnail-canvas {
  position: absolute;
  z-index: 10;
}
.thumbnail-overlay {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(0, 0, 0, 0);
  transition: background 0.3s ease;
}

.map-thumbnail:hover .thumbnail-overlay {
  background: rgba(0, 0, 0, 0.15);
}

.expand-btn {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 24px;
  background: rgba(255, 255, 255, 0);
  color: rgba(255, 255, 255, 0);
  border-radius: 24px;
  font-size: 15px;
  font-weight: 500;
  backdrop-filter: blur(0px);
  transition: all 0.3s ease;
  pointer-events: none;
}

.map-thumbnail:hover .expand-btn {
  background: rgba(255, 255, 255, 0.95);
  color: #409eff;
  backdrop-filter: blur(10px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.15);
}

.expand-btn .el-icon {
  font-size: 20px;
}

/* 风险点数量徽章 */
.risk-count-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: rgba(255, 255, 255, 0.9);
  padding: 4px 12px;
  border-radius: 16px;
  font-size: 12px;
  color: #606266;
  backdrop-filter: blur(8px);
  z-index: 20;
}

/* ==================== 全屏弹窗 ==================== */

.fullscreen-overlay {
  position: fixed;
  top: 0;
  left: 0;
  width: 100vw;
  height: 100vh;
  background: rgba(0, 0, 0, 0.6);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  backdrop-filter: blur(4px);
}

.fullscreen-modal {
  width: 95vw;
  height: 95vh;
  max-width: 1800px;
  background: #fff;
  border-radius: 12px;
  box-shadow: 0 24px 48px rgba(0, 0, 0, 0.2);
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

/* 弹窗头部 */
.fullscreen-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 20px; /* 减小内边距 */
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: #fff;
  flex-shrink: 0; /* 防止头部被压缩 */
}

.fullscreen-header h3 {
  margin: 0;
  font-size: 16px; /* 减小字体 */
  font-weight: 600;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 16px;
}

.header-actions .point-count {
  font-size: 14px;
  opacity: 0.9;
}

.close-btn {
  width: 32px;
  height: 32px;
  border: none;
  background: rgba(255, 255, 255, 0.2);
  color: #fff;
  font-size: 24px;
  line-height: 1;
  border-radius: 50%;
  cursor: pointer;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  justify-content: center;
}

.close-btn:hover {
  background: rgba(255, 255, 255, 0.35);
  transform: scale(1.1);
}

/* 弹窗主体 */
.fullscreen-body {
  flex: 1;
  min-height: 0;
  background: #f5f7fa;
  overflow: hidden;
  display: flex;
  flex-direction: column;
}

.map-container {
  position: relative;
  width: 100%;
  height: 100%;
  min-height: 0;
  flex: 1;
  overflow: visible;
  cursor: pointer;
  user-select: none;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #f5f7fa;
  padding: 0;
}

/* 弹窗底部图例 */
.fullscreen-footer {
  padding: 8px 20px; /* 减小内边距 */
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0; /* 防止底部被压缩 */
}

/* 全屏弹窗动画 */
.fullscreen-fade-enter-active,
.fullscreen-fade-leave-active {
  transition: opacity 0.3s ease;
}

.fullscreen-fade-enter-from,
.fullscreen-fade-leave-to {
  opacity: 0;
}

/* ==================== 地图容器 ==================== */

/* 地图内容层 - 相对定位容器，包含图片和 Canvas，居中显示 */
.map-content {
  position: relative;
  display: inline-block;
  flex-shrink: 0;

  /* 缩放动画过渡效果 */
  transition: transform 0.35s cubic-bezier(0.34, 1.56, 0.64, 1);
}

/* 背景图片 - 让图片尺寸决定容器大小 */
.background-image {
  display: block;

  /* 关键：让图片尽可能大，但完整显示不裁剪，缩小一点比例 */
  max-width: 60vw;
  max-height: 70vh;

  /* 保持原始比例，不拉伸变形 */
  object-fit: contain;

  /* 禁止用户拖拽 */
  draggable: false;
  pointer-events: none;
  user-select: none;
}

/* Canvas 层 - 绝对定位覆盖在图片上 */
.canvas-layer {
  position: absolute;
  left: 0;
  top: 0;
  z-index: 10;
  pointer-events: auto;
}

/* ==================== 状态提示 ==================== */

.empty-state {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 15;
  padding: 24px 40px;
  background: rgba(255, 255, 255, 0.92);
  border-radius: 12px;
  font-size: 16px;
  color: #999;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  pointer-events: none;
}

.loading-overlay {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  z-index: 20;
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 18px 28px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 10px;
  font-size: 14px;
  color: #555;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.08);
  pointer-events: none;
}

.zoom-hint {
  position: absolute;
  bottom: 16px;
  right: 16px;
  z-index: 25;
  padding: 6px 14px;
  background: rgba(0, 0, 0, 0.55);
  color: #fff;
  font-size: 12px;
  border-radius: 16px;
  pointer-events: none;
  opacity: 0.85;
  transition: opacity 0.3s ease;
}

/* ==================== 图例栏 ==================== */

.legend-bar {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 32px;
  padding: 14px 20px;
  background: #fff;
  border-top: 1px solid #ebeef5;
  flex-shrink: 0;
  z-index: 30;
}

.legend-item {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 13px;
  color: #666;
  font-weight: 500;
}

.legend-dot {
  width: 14px;
  height: 14px;
  border-radius: 50%;
  box-shadow: 0 2px 6px rgba(0, 0, 0, 0.15);
}

.high-risk .legend-dot {
  background: #ff4d4f;
  box-shadow: 0 2px 6px rgba(255, 77, 79, 0.4);
}

.medium-risk .legend-dot {
  background: #faad14;
  box-shadow: 0 2px 6px rgba(250, 173, 20, 0.4);
}

.low-risk .legend-dot {
  background: #fae069;
  box-shadow: 0 2px 6px rgba(250, 224, 105, 0.4);
}

.point-count {
  margin-left: auto;
  margin-right: 12px;
  font-size: 13px;
  color: #909399;
  font-weight: 600;
}

/* ==================== 详情弹窗 ==================== */

.detail-dialog-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.45);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 20000;
}

.detail-dialog-card {
  width: 380px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 12px 48px rgba(0, 0, 0, 0.18);
  animation: dialog-zoom-in 0.28s cubic-bezier(0.34, 1.56, 0.64, 1);
}

@keyframes dialog-zoom-in {
  from {
    opacity: 0;
    transform: scale(0.88);
  }
  to {
    opacity: 1;
    transform: scale(1);
  }
}

.detail-header {
  padding: 18px 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  color: #fff;
}

.detail-header.high {
  background: linear-gradient(135deg, #ff6b6b, #ff4d4f);
}

.detail-header.medium {
  background: linear-gradient(135deg, #ffc53d, #faad14);
}

.detail-header.low {
  background: linear-gradient(135deg, #bae637, #95de64);
}

.detail-title {
  font-size: 16px;
  font-weight: 700;
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.detail-close-btn {
  width: 28px;
  height: 28px;
  border: none;
  background: rgba(255, 255, 255, 0.25);
  color: #fff;
  font-size: 20px;
  line-height: 1;
  border-radius: 50%;
  cursor: pointer;
  transition: background 0.2s;
  display: flex;
  align-items: center;
  justify-content: center;
}

.detail-close-btn:hover {
  background: rgba(255, 255, 255, 0.45);
}

.detail-body {
  padding: 20px;
  background: #fff;
}

.detail-row {
  display: flex;
  align-items: center;
  gap: 12px;
  margin-bottom: 14px;
  font-size: 14px;
}

.detail-row:last-child {
  margin-bottom: 0;
}

.detail-label {
  color: #909399;
  width: 70px;
  flex-shrink: 0;
}

.detail-value {
  color: #303133;
  font-weight: 500;
}

/* ==================== 弹窗过渡动画 ==================== */

.dialog-fade-enter-active,
.dialog-fade-leave-active {
  transition: opacity 0.25s ease;
}

.dialog-fade-enter-from,
.dialog-fade-leave-to {
  opacity: 0;
}
</style>
