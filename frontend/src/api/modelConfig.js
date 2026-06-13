import axios from 'axios'

const request = axios.create({
  baseURL: '/api',
  timeout: 10000
})

request.interceptors.request.use(config => {
  const token = localStorage.getItem('token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

/**
 * 获取所有模型预置方案及当前选中
 */
export const getModelPresets = () => {
  return request.get('/model-config/presets').then(res => res.data)
}

/**
 * 获取当前模型预置
 */
export const getCurrentPreset = () => {
  return request.get('/model-config/current').then(res => res.data)
}

/**
 * 切换模型预置方案
 * @param {string} preset - ORIGINAL / ECONOMY / PREMIUM
 */
export const switchModelPreset = (preset) => {
  return request.post('/model-config/switch', { preset }).then(res => res.data)
}
