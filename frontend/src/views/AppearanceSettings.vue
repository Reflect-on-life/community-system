<template>
  <div class="appearance-settings">
    <div class="page-header">
      <h2>外观主题</h2>
      <p class="page-description">自定义系统的外观主题和色彩</p>
      
      <!-- 同步状态提示 -->
      <div v-if="syncStatusText" class="sync-status" :class="syncStatusClass">
        <el-icon class="sync-icon">
          <component :is="syncStatusIcon" />
        </el-icon>
        <span>{{ syncStatusText }}</span>
        <span v-if="lastSyncTime" class="sync-time">（{{ formatSyncTime(lastSyncTime) }}）</span>
      </div>
    </div>
    
    <!-- 主题模式设置 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><MoonNight /></el-icon>
          <span>主题模式</span>
        </div>
      </template>
      
      <div class="mode-container">
        <div 
          v-for="modeOption in modeOptions" 
          :key="modeOption.value"
          class="mode-option"
          :class="{ active: themeStore.theme === modeOption.value }"
          @click="themeStore.setTheme(modeOption.value)"
        >
          <div class="mode-icon">{{ modeOption.icon }}</div>
          <div class="mode-info">
            <div class="mode-name">{{ modeOption.label }}</div>
            <div class="mode-desc">{{ modeOption.description }}</div>
          </div>
          <div v-if="themeStore.theme === modeOption.value" class="mode-check">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>
      
      <div class="current-mode">
        <el-icon><InfoFilled /></el-icon>
        <span>当前生效：{{ getCurrentModeText() }}</span>
      </div>
    </el-card>
    
    <!-- 主题色彩设置 -->
    <el-card class="setting-card">
      <template #header>
        <div class="card-header">
          <el-icon class="header-icon"><Brush /></el-icon>
          <span>主题色彩</span>
        </div>
      </template>
      
      <div class="color-container">
        <div 
          v-for="colorOption in colorOptions" 
          :key="colorOption.value"
          class="color-option"
          :class="{ active: themeStore.accentColor === colorOption.value }"
          @click="themeStore.setAccentColor(colorOption.value)"
          :style="{ '--color': colorOption.preview }"
        >
          <div class="color-circle" :style="{ backgroundColor: colorOption.preview }"></div>
          <div class="color-name">{{ colorOption.label }}</div>
          <div v-if="themeStore.accentColor === colorOption.value" class="color-check">
            <el-icon><Check /></el-icon>
          </div>
        </div>
      </div>
    </el-card>
    
    <!-- 适老化模式提示 -->
    <el-card class="setting-card" v-if="isElderMode">
      <div class="elder-mode-tip">
        <el-icon class="tip-icon"><WarningFilled /></el-icon>
        <div class="tip-content">
          <div class="tip-title">适老化模式</div>
          <div class="tip-desc">当开启适老化模式时，系统会自动使用浅色模式和高对比度色彩，以提供更好的可读性。</div>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useThemeStore } from '../store/useThemeStore.js'
import { MoonNight, Brush, Check, InfoFilled, WarningFilled, Loading, SuccessFilled, CircleCloseFilled } from '@element-plus/icons-vue'

const themeStore = useThemeStore()
const isElderMode = ref(false)

// 同步状态计算属性
const syncStatusText = computed(() => {
  switch (themeStore.syncStatus) {
    case 'syncing':
      return '正在同步到服务器...'
    case 'success':
      return '已同步到服务器'
    case 'error':
      return '同步失败，已保存到本地'
    default:
      return ''
  }
})

const syncStatusClass = computed(() => {
  return `sync-${themeStore.syncStatus}`
})

const syncStatusIcon = computed(() => {
  switch (themeStore.syncStatus) {
    case 'syncing':
      return Loading
    case 'success':
      return SuccessFilled
    case 'error':
      return CircleCloseFilled
    default:
      return null
  }
})

const lastSyncTime = computed(() => themeStore.lastSyncTime)

// 格式化同步时间
const formatSyncTime = (time) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now - date
  
  if (diff < 60000) {
    return '刚刚'
  } else if (diff < 3600000) {
    return `${Math.floor(diff / 60000)}分钟前`
  } else if (diff < 86400000) {
    return `${Math.floor(diff / 3600000)}小时前`
  } else {
    return date.toLocaleString('zh-CN', {
      month: '2-digit',
      day: '2-digit',
      hour: '2-digit',
      minute: '2-digit'
    })
  }
}

// 主题模式选项
const modeOptions = [
  {
    value: 'light',
    label: '浅色模式',
    description: '明亮、清爽的界面风格',
    icon: '☀️'
  },
  {
    value: 'dark',
    label: '深色模式',
    description: '护眼、现代的界面风格',
    icon: '🌙'
  },
  {
    value: 'system',
    label: '跟随系统',
    description: '根据系统设置自动切换',
    icon: '💻'
  }
]

// 主题色彩选项（使用 HSL 格式预览）
const colorOptions = [
  {
    value: 'teal',
    label: '青色',
    preview: 'hsl(175 70% 41%)'
  },
  {
    value: 'blue',
    label: '蓝色',
    preview: 'hsl(205 85% 55%)'
  },
  {
    value: 'purple',
    label: '紫色',
    preview: 'hsl(265 70% 55%)'
  },
  {
    value: 'rose',
    label: '玫红',
    preview: 'hsl(350 70% 55%)'
  },
  {
    value: 'orange',
    label: '橙色',
    preview: 'hsl(25 90% 55%)'
  },
  {
    value: 'green',
    label: '绿色',
    preview: 'hsl(145 65% 40%)'
  }
]

// 获取当前生效模式文本
const getCurrentModeText = () => {
  if (themeStore.theme === 'system') {
    return themeStore.resolvedTheme === 'dark' ? '深色模式' : '浅色模式'
  }
  return modeOptions.find(option => option.value === themeStore.theme)?.label || '浅色模式'
}

// 检查适老化模式
const checkElderMode = () => {
  isElderMode.value = document.body.classList.contains('elder-mode')
}

onMounted(() => {
  checkElderMode()
  // 监听适老化模式变化
  const observer = new MutationObserver(() => {
    checkElderMode()
  })
  observer.observe(document.body, { attributes: true, attributeFilter: ['class'] })
})
</script>

<style scoped>
.appearance-settings {
  padding: 30px;
  max-width: 1000px;
  margin: 0 auto;
}

.page-header {
  margin-bottom: 30px;
  text-align: center;
}

.page-header h2 {
  font-size: 28px;
  font-weight: 600;
  color: var(--foreground);
  margin-bottom: 10px;
}

.page-description {
  font-size: 16px;
  color: var(--muted-foreground);
  margin: 0;
}

.sync-status {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 20px;
  margin-top: 16px;
  border-radius: 8px;
  font-size: 14px;
  transition: all 0.3s ease;
}

.sync-status.sync-syncing {
  background-color: hsl(var(--primary) / 0.1);
  color: hsl(var(--primary));
  border: 1px solid hsl(var(--primary) / 0.3);
}

.sync-status.sync-success {
  background-color: hsl(145 65% 40% / 0.1);
  color: hsl(145 65% 40%);
  border: 1px solid hsl(145 65% 40% / 0.3);
}

.sync-status.sync-error {
  background-color: hsl(var(--destructive) / 0.1);
  color: hsl(var(--destructive));
  border: 1px solid hsl(var(--destructive) / 0.3);
}

.sync-icon {
  font-size: 16px;
}

.sync-icon.is-loading {
  animation: rotate 1s linear infinite;
}

@keyframes rotate {
  from {
    transform: rotate(0deg);
  }
  to {
    transform: rotate(360deg);
  }
}

.sync-time {
  font-size: 12px;
  opacity: 0.8;
  margin-left: 4px;
}

.setting-card {
  margin-bottom: 30px;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  background-color: var(--card);
  border: 1px solid var(--border);
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  background-color: var(--secondary);
  border-radius: 12px 12px 0 0;
}

.header-icon {
  font-size: 20px;
  margin-right: 8px;
  color: hsl(var(--primary));
}

.card-header span {
  font-size: 18px;
  font-weight: 600;
  color: var(--foreground);
}

.mode-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 24px;
}

.mode-option {
  display: flex;
  align-items: center;
  padding: 20px;
  border: 2px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--background);
  position: relative;
}

.mode-option:hover {
  border-color: hsl(var(--primary));
  box-shadow: var(--shadow-sm);
}

.mode-option.active {
  border-color: hsl(var(--primary));
  background-color: var(--secondary);
  box-shadow: var(--shadow-md);
}

.mode-icon {
  font-size: 32px;
  margin-right: 20px;
  width: 48px;
  text-align: center;
}

.mode-info {
  flex: 1;
}

.mode-name {
  font-size: 16px;
  font-weight: 600;
  color: var(--foreground);
  margin-bottom: 4px;
}

.mode-desc {
  font-size: 14px;
  color: var(--muted-foreground);
}

.mode-check {
  font-size: 24px;
  color: hsl(var(--primary));
}

.current-mode {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
  background-color: var(--secondary);
  border-radius: 0 0 12px 12px;
  font-size: 14px;
  color: var(--muted-foreground);
}

.color-container {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(120px, 1fr));
  gap: 20px;
  padding: 24px;
}

.color-option {
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 20px;
  border: 2px solid var(--border);
  border-radius: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
  background-color: var(--background);
  position: relative;
}

.color-option:hover {
  border-color: hsl(var(--primary));
  box-shadow: var(--shadow-sm);
}

.color-option.active {
  border-color: hsl(var(--primary));
  box-shadow: var(--shadow-glow);
}

.color-circle {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  margin-bottom: 12px;
  position: relative;
  box-shadow: var(--shadow-glow);
}

.color-circle::after {
  content: '';
  position: absolute;
  top: -2px;
  left: -2px;
  right: -2px;
  bottom: -2px;
  border-radius: 50%;
  background: linear-gradient(45deg, var(--color), transparent);
  opacity: 0.6;
  z-index: -1;
}

.color-name {
  font-size: 14px;
  font-weight: 500;
  color: var(--foreground);
  margin-bottom: 8px;
}

.color-check {
  position: absolute;
  top: 10px;
  right: 10px;
  font-size: 18px;
  color: hsl(var(--primary));
  background-color: var(--background);
  border-radius: 50%;
  width: 24px;
  height: 24px;
  display: flex;
  align-items: center;
  justify-content: center;
  border: 1px solid hsl(var(--primary));
}

.elder-mode-tip {
  display: flex;
  align-items: flex-start;
  gap: 12px;
  padding: 20px;
  background-color: hsl(var(--warning) / 0.1);
  border: 1px solid hsl(var(--warning) / 0.3);
  border-radius: 8px;
}

.tip-icon {
  font-size: 24px;
  color: hsl(var(--warning));
  margin-top: 2px;
}

.tip-content {
  flex: 1;
}

.tip-title {
  font-size: 16px;
  font-weight: 600;
  color: hsl(var(--warning));
  margin-bottom: 8px;
}

.tip-desc {
  font-size: 14px;
  color: hsl(var(--warning));
  line-height: 1.5;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .appearance-settings {
    padding: 20px;
  }
  
  .page-header h2 {
    font-size: 24px;
  }
  
  .mode-container {
    padding: 20px;
  }
  
  .mode-option {
    padding: 16px;
  }
  
  .color-container {
    grid-template-columns: repeat(2, 1fr);
    padding: 20px;
  }
}

/* 适老化模式 */
:deep(.elder-mode) .setting-card {
  background-color: #ffffff;
  border: 1px solid #000000;
}

:deep(.elder-mode) .card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #000000;
}

:deep(.elder-mode) .card-header span {
  font-size: 20px;
  color: #000000;
}

:deep(.elder-mode) .mode-name,
:deep(.elder-mode) .color-name {
  font-size: 18px;
  color: #000000;
}

:deep(.elder-mode) .mode-desc {
  font-size: 16px;
  color: #333333;
}

:deep(.elder-mode) .mode-option,
:deep(.elder-mode) .color-option {
  border: 2px solid #000000;
  padding: 24px;
}

:deep(.elder-mode) .mode-option.active,
:deep(.elder-mode) .color-option.active {
  border-color: #22c55e;
}

:deep(.elder-mode) .current-mode {
  font-size: 16px;
  color: #333333;
  border-top: 1px solid #000000;
  background-color: #f8f9fa;
}
</style>
