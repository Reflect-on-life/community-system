<template>
  <div class="register-container">
    <!-- 适老模式切换 -->
    <div class="elder-mode-toggle">
      <span>适老模式</span>
      <el-switch v-model="elderMode" @change="toggleElderMode" />
    </div>
    <div class="login-header">
      <div class="logo">
        <svg width="64" height="64" viewBox="0 0 64 64" fill="none" xmlns="http://www.w3.org/2000/svg">
          <rect width="64" height="64" rx="16" fill="#28a745"/>
          <path d="M24 24L32 36L40 24" stroke="white" stroke-width="4" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M24 36V48" stroke="white" stroke-width="4" stroke-linecap="round"/>
          <path d="M40 36V48" stroke="white" stroke-width="4" stroke-linecap="round"/>
        </svg>
      </div>
      <h1 class="main-title">未来社区</h1>
      <p class="sub-title">智慧社区服务管理平台</p>
    </div>
    
    <div class="login-card">
      <h2 class="welcome-title">欢迎注册</h2>
      <p class="welcome-subtitle">请填写以下信息创建您的账号</p>
      
      <div class="tab-container">
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'login' }"
          @click="handleLogin"
          aria-label="登录"
        >
          登录
        </button>
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'register' }"
          @click="activeTab = 'register'"
          aria-label="注册"
        >
          注册
        </button>
      </div>
      
      <div class="form-container">
        <div class="form-group">
          <label for="phone" class="form-label">手机号码</label>
          <div class="input-wrapper">
            <el-input
              id="phone"
              v-model="registerForm.phone"
              placeholder="请输入手机号"
              maxlength="11"
              size="large"
              aria-label="手机号码"
              aria-describedby="phone-hint"
            >
              <template #prefix>
                <el-icon><Phone /></el-icon>
              </template>
            </el-input>
            <span class="char-count">{{ phoneLength }}/11</span>
          </div>
          <span id="phone-hint" class="sr-only">请输入11位手机号码</span>
        </div>
        
        <div class="form-group">
          <label for="password" class="form-label">密码</label>
          <div class="input-wrapper">
            <el-input
              id="password"
              v-model="registerForm.password"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请输入密码"
              size="large"
              aria-label="密码"
              aria-describedby="password-hint"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
              <template #suffix>
                <el-icon 
                  class="password-toggle"
                  @click="showPassword = !showPassword"
                  :aria-label="showPassword ? '隐藏密码' : '显示密码'"
                  tabindex="0"
                  role="button"
                >
                  <component :is="showPassword ? 'View' : 'Hide'" />
                </el-icon>
              </template>
            </el-input>
          </div>
          <span id="password-hint" class="sr-only">请输入密码</span>
        </div>
        
        <div class="form-group">
          <label for="confirmPassword" class="form-label">确认密码</label>
          <div class="input-wrapper">
            <el-input
              id="confirmPassword"
              v-model="registerForm.confirmPassword"
              :type="showPassword ? 'text' : 'password'"
              placeholder="请确认密码"
              size="large"
              aria-label="确认密码"
              aria-describedby="confirm-password-hint"
            >
              <template #prefix>
                <el-icon><Lock /></el-icon>
              </template>
            </el-input>
          </div>
          <span id="confirm-password-hint" class="sr-only">请确认密码</span>
        </div>
        
        <div class="form-group">
          <label for="name" class="form-label">姓名</label>
          <div class="input-wrapper">
            <el-input
              id="name"
              v-model="registerForm.name"
              placeholder="请输入姓名"
              size="large"
              aria-label="姓名"
              aria-describedby="name-hint"
            >
              <template #prefix>
                <el-icon><User /></el-icon>
              </template>
            </el-input>
          </div>
          <span id="name-hint" class="sr-only">请输入姓名</span>
        </div>
        
        <button 
          class="register-button"
          @click="handleRegister"
          :loading="loading"
          :aria-busy="loading"
          aria-label="注册"
        >
          {{ loading ? '注册中...' : '注册' }}
        </button>
      </div>
      
      <div class="login-link">
        已有账号？
        <a href="#" class="login-link-button" @click.prevent="handleLogin">立即登录</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Phone, Lock, User, View, Hide } from '@element-plus/icons-vue';

const router = useRouter();
const activeTab = ref('register');
const showPassword = ref(false);
const loading = ref(false);
const elderMode = ref(false);

// 适老模式切换
const toggleElderMode = () => {
  if (elderMode.value) {
    document.body.classList.add('elder-mode');
    localStorage.setItem('elderMode', 'true');
  } else {
    document.body.classList.remove('elder-mode');
    localStorage.setItem('elderMode', 'false');
  }
};

// 初始化适老模式状态
onMounted(() => {
  const savedElderMode = localStorage.getItem('elderMode');
  if (savedElderMode === 'true') {
    elderMode.value = true;
    document.body.classList.add('elder-mode');
  }
});

const registerForm = ref({
  phone: '',
  password: '',
  confirmPassword: '',
  name: '',
  type: 'resident'
});

const phoneLength = computed(() => registerForm.value.phone.length);

const handleRegister = async () => {
  if (!registerForm.value.phone) {
    ElMessage.warning('请输入手机号');
    return;
  }
  if (!registerForm.value.password) {
    ElMessage.warning('请输入密码');
    return;
  }
  if (registerForm.value.password !== registerForm.value.confirmPassword) {
    ElMessage.warning('两次输入的密码不一致');
    return;
  }
  if (!registerForm.value.name) {
    ElMessage.warning('请输入姓名');
    return;
  }
  
  try {
    loading.value = true;
    const response = await axios.post('/api/auth/register', {
      phone: registerForm.value.phone,
      password: registerForm.value.password,
      name: registerForm.value.name,
      type: registerForm.value.type
    });
    
    ElMessage.success('注册成功，请登录');
    router.push('/login');
  } catch (error) {
    if (error.response?.status === 409) {
      ElMessage.error('该手机号已注册');
    } else {
      ElMessage.error(error.response?.data?.message || '注册失败，请重试');
    }
  } finally {
    loading.value = false;
  }
};

const handleLogin = () => {
  router.push('/login');
};
</script>

<style scoped>
.register-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f0fdf4 0%, #dcfce7 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 20px;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
  position: relative;
}

.elder-mode-toggle {
  position: absolute;
  top: 20px;
  right: 20px;
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 14px;
  color: #666;
  z-index: 100;
}

.login-header {
  text-align: center;
  margin-bottom: 40px;
}

.logo {
  margin-bottom: 20px;
}

.main-title {
  font-size: 24px;
  font-weight: 700;
  margin: 0 0 8px 0;
  color: #333;
}

.sub-title {
  font-size: 14px;
  color: #666;
  margin: 0;
}

.login-card {
  background: white;
  border-radius: 20px;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.08);
  padding: 40px;
  width: 100%;
  max-width: 420px;
  transition: all 0.3s ease;
}

.welcome-title {
  font-size: 20px;
  font-weight: 600;
  margin: 0 0 8px 0;
  text-align: center;
  color: #333;
}

.welcome-subtitle {
  font-size: 14px;
  color: #666;
  margin: 0 0 30px 0;
  text-align: center;
}

.tab-container {
  display: flex;
  gap: 10px;
  margin-bottom: 30px;
  background: #f0f0f0;
  border-radius: 10px;
  padding: 4px;
}

.tab-button {
  flex: 1;
  padding: 12px 0;
  border: none;
  background: transparent;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  color: #666;
  cursor: pointer;
  transition: all 0.3s ease;
  text-align: center;
}

.tab-button.active {
  background: white;
  color: #28a745;
  box-shadow: 0 2px 8px rgba(40, 167, 69, 0.15);
}

.tab-button:hover:not(.active) {
  color: #28a745;
}

.form-container {
  margin-bottom: 30px;
}

.form-group {
  margin-bottom: 20px;
}

.form-label {
  display: block;
  font-size: 14px;
  font-weight: 500;
  color: #333;
  margin-bottom: 8px;
}

.input-wrapper {
  position: relative;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: #999;
}

.register-button {
  width: 100%;
  height: 48px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 16px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
  margin-top: 16px;
}

.register-button:hover {
  background: #218838;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.register-button:active {
  transform: translateY(0);
}

.register-button:disabled {
  background: #c3e6cb;
  cursor: not-allowed;
}

.login-link {
  text-align: center;
  font-size: 14px;
  color: #666;
  line-height: 1.5;
}

.login-link-button {
  color: #28a745;
  text-decoration: none;
  font-weight: 500;
  transition: all 0.3s ease;
}

.login-link-button:hover {
  text-decoration: underline;
}

.password-toggle {
  cursor: pointer;
  color: #999;
  transition: all 0.3s ease;
}

.password-toggle:hover {
  color: #28a745;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border-width: 0;
}

@media (max-width: 768px) {
  .login-card {
    width: 90%;
    padding: 30px 24px;
  }
  
  .main-title {
    font-size: 20px;
  }
  
  .welcome-title {
    font-size: 18px;
  }
  
  .register-button {
    height: 44px;
  }
  
  .form-group {
    margin-bottom: 16px;
  }
}

:deep(.el-input__wrapper) {
  border-radius: 8px;
  transition: all 0.3s ease;
}

:deep(.el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.1);
}

:deep(.el-input__inner) {
  font-size: 14px;
}

:deep(.el-icon) {
  color: #999;
}

:deep(.el-input__wrapper:focus-within .el-icon) {
  color: #28a745;
}

:deep(.el-select .el-input__wrapper) {
  border-radius: 8px;
}

:deep(.el-select .el-input__wrapper:focus-within) {
  box-shadow: 0 0 0 2px rgba(40, 167, 69, 0.1);
}
</style>