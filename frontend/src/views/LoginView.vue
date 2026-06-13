<template>
  <div class="login-container">
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
      <h2 class="welcome-title">欢迎使用</h2>
      <p class="welcome-subtitle">请登录或注册您的账号</p>
      
      <div class="tab-container">
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'login' }"
          @click="activeTab = 'login'"
          aria-label="登录"
        >
          登录
        </button>
        <button 
          class="tab-button" 
          :class="{ active: activeTab === 'register' }"
          @click="handleRegister"
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
              v-model="loginForm.phone"
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
              v-model="loginForm.password"
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
            <span class="char-count password-char-count">{{ passwordLength }}</span>
          </div>
          <span id="password-hint" class="sr-only">请输入密码</span>
        </div>
        
        <div class="form-group">
          <label for="verificationCode" class="form-label">验证码</label>
          <div class="input-wrapper verification-code-wrapper">
            <el-input
              id="verificationCode"
              v-model="loginForm.verificationCode"
              placeholder="请输入验证码"
              maxlength="6"
              size="large"
              aria-label="验证码"
              aria-describedby="code-hint"
            >
              <template #prefix>
                <el-icon><Key /></el-icon>
              </template>
            </el-input>
            <button 
              class="send-code-button"
              @click="handleSendCode"
              :disabled="sendingCode || countdown > 0"
              :loading="sendingCode"
            >
              {{ countdown > 0 ? `${countdown}秒后重试` : '发送验证码' }}
            </button>
          </div>
          <span id="code-hint" class="sr-only">请输入 6 位数字验证码</span>
        </div>
        
        <button 
          class="login-button"
          @click="handleLogin"
          :loading="loading"
          :aria-busy="loading"
          aria-label="登录"
        >
          {{ loading ? '登录中...' : '登录' }}
        </button>
      </div>
      
      <div class="agreement">
        登录即表示您同意我们的
        <a href="#" class="agreement-link" @click.prevent="showTerms">服务条款</a>
        和
        <a href="#" class="agreement-link" @click.prevent="showPrivacy">隐私政策</a>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Phone, Lock, View, Hide, Key } from '@element-plus/icons-vue';

const router = useRouter();
const activeTab = ref('login');
const showPassword = ref(false);
const loading = ref(false);
const elderMode = ref(false);
const sendingCode = ref(false);
const countdown = ref(0);

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

const loginForm = ref({
  phone: '',
  password: '',
  verificationCode: ''
});

const phoneLength = computed(() => loginForm.value.phone.length);
const passwordLength = computed(() => loginForm.value.password.length);

const handleLogin = async () => {
  if (!loginForm.value.phone) {
    ElMessage.warning('请输入手机号');
    return;
  }
  
  // 验证码登录或密码登录
  const hasVerificationCode = loginForm.value.verificationCode && loginForm.value.verificationCode.length === 6;
  const hasPassword = loginForm.value.password && loginForm.value.password.length > 0;
  
  if (!hasVerificationCode && !hasPassword) {
    ElMessage.warning('请输入密码或验证码');
    return;
  }
  
  try {
    loading.value = true;
    const loginData = {
      phone: loginForm.value.phone
    };
    
    // 如果有验证码，优先使用验证码登录
    if (hasVerificationCode) {
      loginData.verificationCode = loginForm.value.verificationCode;
    } else {
      loginData.password = loginForm.value.password;
    }
    
    const response = await axios.post('/api/auth/login', loginData);
    
    const { token, userId, type, name, roles, tags, phone, gender, birthday, avatar } = response.data;
    localStorage.setItem('user', JSON.stringify({ userId, type, name, roles, tags, phone, gender, birthday, avatar }));
    localStorage.setItem('token', token);
    
    ElMessage.success('登录成功');
    router.push('/');
  } catch (error) {
    if (error.response?.status === 401) {
      ElMessage.error('账号或密码错误，请重试');
    } else if (error.response?.status === 404) {
      ElMessage.error('账号不存在，请先注册');
    } else {
      ElMessage.error(error.response?.data?.message || '登录失败，请重试');
    }
  } finally {
    loading.value = false;
  }
};

// 发送验证码
const handleSendCode = async () => {
  if (!loginForm.value.phone) {
    ElMessage.warning('请先输入手机号');
    return;
  }
  
  if (loginForm.value.phone.length !== 11) {
    ElMessage.warning('请输入正确的11位手机号');
    return;
  }
  
  try {
    sendingCode.value = true;
    const response = await axios.post('/api/auth/send-code', {
      phone: loginForm.value.phone
    });
    
    // 获取返回的验证码（模拟短信接收）
    const code = response.data.code;
    
    // 自动填充验证码到输入框
    loginForm.value.verificationCode = code;
    
    ElMessage.success('验证码已发送');
    
    // 开始倒计时
    countdown.value = 60;
    const timer = setInterval(() => {
      countdown.value--;
      if (countdown.value <= 0) {
        clearInterval(timer);
      }
    }, 1000);
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '发送验证码失败');
  } finally {
    sendingCode.value = false;
  }
};

const handleRegister = () => {
  router.push('/register');
};

const showTerms = () => {
  ElMessage.info('服务条款功能开发中');
};

const showPrivacy = () => {
  ElMessage.info('隐私政策功能开发中');
};
</script>

<style scoped>
.login-container {
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
  margin-bottom: 24px;
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

.verification-code-wrapper {
  display: flex;
  gap: 10px;
}

.verification-code-wrapper .el-input {
  flex: 1;
}

.send-code-button {
  width: 140px;
  height: 40px;
  background: #28a745;
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 14px;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.3s ease;
  white-space: nowrap;
}

.send-code-button:hover:not(:disabled) {
  background: #218838;
}

.send-code-button:disabled {
  background: #c3e6cb;
  cursor: not-allowed;
}

.char-count {
  position: absolute;
  right: 12px;
  bottom: 12px;
  font-size: 12px;
  color: #999;
}

.password-char-count {
  right: 50px;
}

.elder-mode .char-count {
  bottom: 16px;
  font-size: 16px;
}

.elder-mode .password-char-count {
  right: 60px;
}

.login-button {
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

.login-button:hover {
  background: #218838;
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(40, 167, 69, 0.3);
}

.login-button:active {
  transform: translateY(0);
}

.login-button:disabled {
  background: #c3e6cb;
  cursor: not-allowed;
}

.agreement {
  text-align: center;
  font-size: 12px;
  color: #666;
  line-height: 1.5;
}

.agreement-link {
  color: #28a745;
  text-decoration: none;
  transition: all 0.3s ease;
}

.agreement-link:hover {
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
  
  .login-button {
    height: 44px;
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
</style>