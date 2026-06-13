import { defineStore } from 'pinia'
import { ref, computed, onMounted, onUnmounted } from 'vue'
import axios from 'axios'

// 颜色配置映射（使用 HSL 格式，便于调整亮度和饱和度）
const accentColors = {
  teal: { primary: '175 70% 41%', primaryGlow: '175 80% 55%' },
  blue: { primary: '205 85% 55%', primaryGlow: '205 90% 65%' },
  purple: { primary: '265 70% 55%', primaryGlow: '265 80% 65%' },
  rose: { primary: '350 70% 55%', primaryGlow: '350 80% 65%' },
  orange: { primary: '25 90% 55%', primaryGlow: '25 95% 65%' },
  green: { primary: '145 65% 40%', primaryGlow: '145 75% 50%' }
}

// 定义主题存储
export const useThemeStore = defineStore('theme', () => {
  // 状态
  const theme = ref('system') // 'light' | 'dark' | 'system'
  const accentColor = ref('teal') // 'teal' | 'blue' | 'purple' | 'rose' | 'orange' | 'green'
  const resolvedTheme = ref('light') // 实际应用的主题 'light' | 'dark'
  const syncStatus = ref('idle') // 'idle' | 'syncing' | 'success' | 'error'
  const lastSyncTime = ref(null)
  
  // 防抖定时器
  let saveDebounceTimer = null
  
  // 计算属性
  const currentAccentColor = computed(() => {
    return accentColors[accentColor.value] || accentColors.teal
  })
  
  // 计算当前实际应该显示的主题
  const updateResolvedTheme = () => {
    if (theme.value === 'system') {
      resolvedTheme.value = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
    } else {
      resolvedTheme.value = theme.value
    }
  }
  
  // 应用主题到 DOM（核心逻辑）
  const applyThemeToDOM = () => {
    const root = document.documentElement
    
    // 1. 处理明暗模式
    if (resolvedTheme.value === 'dark') {
      root.classList.add('dark')
    } else {
      root.classList.remove('dark')
    }
    
    // 2. 处理强调色（通过 CSS 变量）
    const colors = currentAccentColor.value
    root.style.setProperty('--primary', colors.primary)
    root.style.setProperty('--primary-glow', colors.primaryGlow)
    root.style.setProperty('--ring', colors.primary)
    root.style.setProperty('--sidebar-primary', colors.primary)
    root.style.setProperty('--sidebar-ring', colors.primary)
    
    // 3. 设置 data 属性（用于 CSS 选择器）
    root.setAttribute('data-accent', accentColor.value)
  }
  
  // 持久化到 localStorage
  const saveToLocalStorage = () => {
    localStorage.setItem('theme', theme.value)
    localStorage.setItem('accentColor', accentColor.value)
  }
  
  // 从 localStorage 加载
  const loadFromLocalStorage = () => {
    const savedTheme = localStorage.getItem('theme')
    const savedColor = localStorage.getItem('accentColor')
    
    if (savedTheme && ['light', 'dark', 'system'].includes(savedTheme)) {
      theme.value = savedTheme
    }
    
    if (savedColor && Object.keys(accentColors).includes(savedColor)) {
      accentColor.value = savedColor
    }
    
    updateResolvedTheme()
    applyThemeToDOM()
  }
  
  // 从服务器加载主题偏好
  const loadFromServer = async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('未登录，跳过从服务器加载主题')
      return false
    }
    
    try {
      const response = await axios.get('/api/user/theme', {
        headers: {
          'Authorization': `Bearer ${token}`
        }
      })
      
      if (response.data.success && response.data.data) {
        const { mode, color } = response.data.data
        
        // 更新主题状态
        if (mode && ['light', 'dark', 'system'].includes(mode)) {
          theme.value = mode
        }
        
        if (color && Object.keys(accentColors).includes(color)) {
          accentColor.value = color
        }
        
        // 应用主题
        updateResolvedTheme()
        applyThemeToDOM()
        
        // 保存到 localStorage
        saveToLocalStorage()
        
        console.log('从服务器加载主题成功:', { mode, color })
        return true
      }
    } catch (error) {
      console.warn('从服务器加载主题失败，降级到 localStorage:', error.message)
      return false
    }
    
    return false
  }
  
  // 保存主题到服务器（带防抖）
  const saveToServer = async () => {
    const token = localStorage.getItem('token')
    if (!token) {
      console.log('未登录，跳过保存主题到服务器')
      return
    }
    
    // 清除之前的防抖定时器
    if (saveDebounceTimer) {
      clearTimeout(saveDebounceTimer)
    }
    
    // 设置新的防抖定时器（500ms）
    saveDebounceTimer = setTimeout(async () => {
      try {
        syncStatus.value = 'syncing'
        
        await axios.put('/api/user/theme', {
          mode: theme.value,
          color: accentColor.value
        }, {
          headers: {
            'Authorization': `Bearer ${token}`
          }
        })
        
        syncStatus.value = 'success'
        lastSyncTime.value = new Date().toISOString()
        
        console.log('主题保存到服务器成功:', { mode: theme.value, color: accentColor.value })
        
        // 2秒后重置状态
        setTimeout(() => {
          if (syncStatus.value === 'success') {
            syncStatus.value = 'idle'
          }
        }, 2000)
      } catch (error) {
        console.warn('主题保存到服务器失败（静默失败，不影响本地体验）:', error.message)
        syncStatus.value = 'error'
        
        // 2秒后重置状态
        setTimeout(() => {
          if (syncStatus.value === 'error') {
            syncStatus.value = 'idle'
          }
        }, 2000)
      }
    }, 500)
  }
  
  // 设置主题模式
  const setTheme = (newTheme) => {
    theme.value = newTheme
    updateResolvedTheme()
    applyThemeToDOM()
    saveToLocalStorage()
    saveToServer()
  }
  
  // 设置强调色
  const setAccentColor = (newColor) => {
    accentColor.value = newColor
    applyThemeToDOM()
    saveToLocalStorage()
    saveToServer()
  }
  
  // 系统主题监听器（带清理）
  let mediaQueryCleanup = null
  
  const setupSystemThemeListener = () => {
    const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
    
    const handleChange = () => {
      if (theme.value === 'system') {
        updateResolvedTheme()
        applyThemeToDOM()
      }
    }
    
    mediaQuery.addEventListener('change', handleChange)
    
    // 返回清理函数
    mediaQueryCleanup = () => {
      mediaQuery.removeEventListener('change', handleChange)
    }
  }
  
  // 初始化主题（优先从服务器加载）
  const initTheme = async () => {
    // 1. 尝试从服务器加载
    const loadedFromServer = await loadFromServer()
    
    // 2. 如果服务器加载失败或未登录，降级到 localStorage
    if (!loadedFromServer) {
      loadFromLocalStorage()
    }
    
    // 3. 设置系统主题监听
    setupSystemThemeListener()
  }
  
  // 初始化
  onMounted(() => {
    initTheme()
  })
  
  // 清理（防止内存泄漏）
  onUnmounted(() => {
    if (mediaQueryCleanup) {
      mediaQueryCleanup()
    }
    if (saveDebounceTimer) {
      clearTimeout(saveDebounceTimer)
    }
  })
  
  return {
    theme,
    accentColor,
    resolvedTheme,
    currentAccentColor,
    syncStatus,
    lastSyncTime,
    setTheme,
    setAccentColor,
    loadFromLocalStorage,
    loadFromServer,
    saveToServer,
    setupSystemThemeListener,
    initTheme
  }
})
