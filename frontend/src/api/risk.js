import axios from 'axios'

/**
 * 风险事件管理 API
 * 复用现有的 AI 识别能力
 */

const request = axios.create({
  baseURL: '/api',
  timeout: 60000 // AI 识别可能需要较长时间，增加到 60 秒
})

// 关键修复：添加请求拦截器，自动添加 token
request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    console.error('Request Error:', error)
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => response.data,
  error => {
    console.error('API Error:', error)
    return Promise.reject(error)
  }
)

/**
 * 上传图片文件
 * @param {File} file - 图片文件
 * @returns {Promise<string>} - 返回图片 URL
 */
export const uploadImage = (file) => {
  const formData = new FormData()
  formData.append('file', file)
  return request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
}

/**
 * AI 风险识别
 * 复用 RiskReportView 中的识别能力
 * @param {string} imageUrl - 图片 URL（相对路径或绝对路径）
 * @param {string} riskTitle - 风险标题（可选，用于辅助识别）
 * @param {string} cameraLocation - 摄像头位置（可选）
 * @param {string} cameraId - 摄像头ID（可选）
 * @returns {Promise<Object>} - 识别结果 { riskType, riskLevel, description, glmResult, baiduLabels, riskEventId }
 */
export const recognizeRisk = (imageUrl, riskTitle = '', cameraLocation = '', cameraId = '') => {
  return request({
    url: '/risk/recognize',
    method: 'post',
    data: {
      imageUrl,
      riskTitle,
      cameraLocation,
      cameraId
    }
  })
}

/**
 * 上传图片并自动识别风险
 * @param {File} file - 图片文件
 * @param {string} cameraLocation - 摄像头位置（可选）
 * @param {string} cameraId - 摄像头ID（可选）
 * @returns {Promise<Object>} - 识别结果
 */
export const uploadAndRecognize = async (file, cameraLocation = '', cameraId = '') => {
  // 关键修复：确保使用正确的 File 对象
  const formData = new FormData()
  formData.append('file', file)
  
  // 直接调用上传接口
  const uploadRes = await request({
    url: '/file/upload',
    method: 'post',
    data: formData,
    headers: {
      'Content-Type': 'multipart/form-data'
    }
  })
  
  // 修复：axios 拦截器已返回 response.data，不需要再访问 .data
  const imageUrl = uploadRes.data || uploadRes.url
  
  if (!imageUrl) {
    console.error('图片上传成功但未返回 URL:', uploadRes)
    throw new Error('图片上传失败')
  }
  
  // 调用 AI 识别接口（传递摄像头位置信息）
  const riskTitle = cameraLocation ? `监控识别 - ${cameraLocation}` : '监控识别'
  const recognizeRes = await recognizeRisk(imageUrl, riskTitle, cameraLocation, cameraId)
  
  return {
    imageUrl,
    ...recognizeRes
  }
}

/**
 * 创建风险事件（用于监控大屏自动生成）
 * @param {Object} data - 风险事件数据
 * @param {string} data.imageUrl - 图片 URL
 * @param {string} data.riskType - 风险类型
 * @param {string} data.riskLevel - 风险等级
 * @param {string} data.cameraTitle - 摄像头标题
 * @param {string} data.location - 位置
 * @returns {Promise<Object>} - 创建的风险事件
 */
export const createRiskEvent = (data) => {
  return request({
    url: '/risk/report',
    method: 'post',
    data: {
      imageUrl: data.imageUrl,
      riskType: data.riskType,
      riskLevel: data.riskLevel,
      location: data.location || data.cameraTitle,
      title: `${data.cameraTitle} - ${data.riskType}`
    }
  })
}

/**
 * 获取风险事件列表
 * @param {Object} params - 查询参数
 * @param {number} params.page - 页码
 * @param {number} params.pageSize - 每页数量
 * @param {string} params.status - 状态筛选
 * @param {string} params.riskLevel - 风险等级筛选
 * @returns {Promise<Object>} - 风险事件列表
 */
export const getRiskList = (params) => {
  return request({
    url: '/risk/list',
    method: 'get',
    params
  })
}

/**
 * 获取最新的风险事件（用于自动同步）
 * @param {number} limit - 返回数量限制
 * @returns {Promise<Array>} - 最新风险事件列表
 */
export const getLatestRiskEvents = (limit = 9) => {
  return request({
    url: '/risk/images/latest',
    method: 'get',
    params: { limit }
  })
}

/**
 * 获取风险事件统计
 * @returns {Promise<Object>} - 统计数据
 */
export const getRiskStatistics = () => {
  return request({
    url: '/risk/statistics',
    method: 'get'
  })
}

/**
 * 获取待处理风险事件
 * @returns {Promise<Array>} - 待处理风险事件列表
 */
export const getPendingRisks = () => {
  return request({
    url: '/risk/statistics/pending',
    method: 'get'
  })
}

/**
 * 提交风险事件上报
 * @param {Object} data - 风险事件数据
 * @returns {Promise<Object>} - 上报结果
 */
export const reportRisk = (data) => {
  return request({
    url: '/risk/report',
    method: 'post',
    data
  })
}

/**
 * 风险等级映射
 */
export const RISK_LEVEL_MAP = {
  'HIGH': { text: '高风险', type: 'danger', class: 'danger' },
  'MEDIUM': { text: '中风险', type: 'warning', class: 'warning' },
  'LOW': { text: '低风险', type: 'success', class: 'low' },
  'danger': { text: '高风险', type: 'danger', class: 'danger' },
  'warning': { text: '中风险', type: 'warning', class: 'warning' },
  'normal': { text: '正常', type: 'success', class: 'normal' }
}

/**
 * 风险类型中文映射
 */
export const RISK_TYPE_MAP = {
  'FALL_DOWN': '老人摔倒',
  'WATER_LOGGING': '路面积水',
  'FACILITY_DAMAGE': '设施损坏',
  'EV_CHARGING': '电动车违规充电',
  'FIRE_RISK': '火灾风险',
  'GARBAGE_PILE': '垃圾堆积',
  'ILLEGAL_POSTING': '非法张贴',
  'CHILD_SAFETY': '儿童安全风险',
  'PET_SAFETY': '宠物安全风险',
  'CONSTRUCTION_RISK': '施工维修风险',
  'OTHER': '其他',
  'elder_fall': '老人摔倒',
  'road_water': '路面积水',
  'fire_risk': '火灾风险'
}

export default {
  uploadImage,
  recognizeRisk,
  uploadAndRecognize,
  getRiskList,
  getLatestRiskEvents,
  getRiskStatistics,
  getPendingRisks,
  reportRisk,
  RISK_LEVEL_MAP,
  RISK_TYPE_MAP
}
