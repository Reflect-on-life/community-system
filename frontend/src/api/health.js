import axios from 'axios'

const baseURL = '/api'

const request = axios.create({
  baseURL,
  timeout: 10000
})

request.interceptors.request.use(
  config => {
    const token = localStorage.getItem('token')
    if (token) {
      config.headers.Authorization = `Bearer ${token}`
    }
    return config
  },
  error => {
    return Promise.reject(error)
  }
)

request.interceptors.response.use(
  response => {
    return response.data
  },
  error => {
    if (error.response && error.response.status === 401) {
      localStorage.removeItem('user')
      localStorage.removeItem('token')
      window.location.href = '/login'
    }
    return Promise.reject(error)
  }
)

export const getHealthRecords = (params) => {
  return request({
    url: '/doctor-residents/list',
    method: 'get',
    params
  })
}

export const createHealthRecord = (data) => {
  return request({
    url: '/doctor-residents/health-check',
    method: 'post',
    data
  })
}

export const updateKeyGroup = (userId, tags) => {
  return request({
    url: `/doctor-residents/tags/${userId}`,
    method: 'put',
    data: { tags }
  })
}

export const getHealthHistory = (userId) => {
  return request({
    url: `/doctor-residents/health-records/${userId}`,
    method: 'get'
  })
}

export const getHealthStats = () => {
  return request({
    url: '/doctor-residents/health-stats',
    method: 'get'
  })
}

export default {
  getHealthRecords,
  createHealthRecord,
  updateKeyGroup,
  getHealthHistory,
  getHealthStats
}
