<template>
  <div class="profile-container">
    <el-card class="profile-card">
      <template #header>
        <div class="card-header">
          <span>个人资料</span>
        </div>
      </template>

      <el-tabs v-model="activeTab" class="profile-tabs">
        <!-- 基本信息修改 -->
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="basicForm" :rules="basicRules" ref="basicFormRef" label-width="100px" class="profile-form">
            <el-form-item label="头像">
              <el-upload
                class="avatar-uploader"
                :action="uploadUrl"
                :show-file-list="false"
                :on-success="handleAvatarSuccess"
                :before-upload="beforeAvatarUpload"
                :headers="uploadHeaders"
              >
                <img v-if="basicForm.avatar" :src="basicForm.avatar" class="avatar" />
                <el-icon v-else class="avatar-uploader-icon"><Plus /></el-icon>
              </el-upload>
              <div class="upload-tip">支持 JPG、PNG 格式，文件大小不超过 2MB</div>
            </el-form-item>

            <el-form-item label="手机号">
              <el-input v-model="basicForm.phone" disabled />
            </el-form-item>

            <el-form-item label="姓名" prop="name">
              <el-input v-model="basicForm.name" placeholder="请输入姓名" />
            </el-form-item>

            <el-form-item label="性别" prop="gender">
              <el-radio-group v-model="basicForm.gender">
                <el-radio label="1">男</el-radio>
                <el-radio label="0">女</el-radio>
              </el-radio-group>
            </el-form-item>

            <el-form-item label="出生日期" prop="birthday">
              <el-date-picker
                v-model="basicForm.birthday"
                type="date"
                placeholder="选择出生日期"
                format="YYYY-MM-DD"
                value-format="YYYY-MM-DD"
                style="width: 100%"
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdateBasic" :loading="updating">
                保存修改
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 密码修改 -->
        <el-tab-pane label="密码修改" name="password">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="profile-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input
                v-model="passwordForm.oldPassword"
                type="password"
                placeholder="请输入原密码"
                show-password
              />
            </el-form-item>

            <el-form-item label="新密码" prop="newPassword">
              <el-input
                v-model="passwordForm.newPassword"
                type="password"
                placeholder="请输入新密码（6-20位）"
                show-password
              />
            </el-form-item>

            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input
                v-model="passwordForm.confirmPassword"
                type="password"
                placeholder="请再次输入新密码"
                show-password
              />
            </el-form-item>

            <el-form-item>
              <el-button type="primary" @click="handleUpdatePassword" :loading="updatingPassword">
                修改密码
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 外观设置 -->
        <el-tab-pane label="外观设置" name="appearance">
          <div class="appearance-settings">
            <!-- 主题模式 + 主题色彩 并排显示 -->
            <el-card class="setting-card theme-color-card">
              <template #header>
                <div class="card-header">
                  <span>外观设置</span>
                </div>
              </template>
              
              <div class="theme-color-container">
                <!-- 左侧：主题模式 -->
                <div class="theme-section">
                  <h4 class="section-title">
                    <el-icon><MoonNight /></el-icon>
                    主题模式
                  </h4>
                  <div class="mode-list">
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
                </div>

                <!-- 右侧：主题色彩 -->
                <div class="color-section">
                  <h4 class="section-title">
                    <el-icon><Brush /></el-icon>
                    主题色彩
                  </h4>
                  <div class="color-grid">
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
                </div>
              </div>
              
              <div class="current-mode-info">
                <el-icon><InfoFilled /></el-icon>
                <span>当前生效：{{ getCurrentModeText() }}</span>
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
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { Plus, MoonNight, Brush, Check, InfoFilled, WarningFilled } from '@element-plus/icons-vue';
import { useThemeStore } from '../store/useThemeStore.js'

const activeTab = ref('basic');
const updating = ref(false);
const updatingPassword = ref(false);
const uploadUrl = ref('/api/file/upload/avatar');
const uploadHeaders = ref({
  'Authorization': localStorage.getItem('token') || ''
});

const themeStore = useThemeStore()
const isElderMode = ref(false)

const basicForm = ref({
  phone: '',
  name: '',
  gender: '1',
  birthday: '',
  avatar: ''
});

const passwordForm = ref({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
});

const basicFormRef = ref(null);
const passwordFormRef = ref(null);

const basicRules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 20, message: '姓名长度在 2 到 20 个字符', trigger: 'blur' }
  ],
  gender: [
    { required: true, message: '请选择性别', trigger: 'change' }
  ],
  birthday: [
    { required: true, message: '请选择出生日期', trigger: 'change' }
  ]
};

const passwordRules = {
  oldPassword: [
    { required: true, message: '请输入原密码', trigger: 'blur' }
  ],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在 6 到 20 个字符', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请再次输入新密码', trigger: 'blur' },
    {
      validator: (rule, value, callback) => {
        if (value !== passwordForm.value.newPassword) {
          callback(new Error('两次输入的密码不一致'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

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
];

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
];

// 获取当前生效模式文本
const getCurrentModeText = () => {
  if (themeStore.theme === 'system') {
    return themeStore.resolvedTheme === 'dark' ? '深色模式' : '浅色模式'
  }
  return modeOptions.find(option => option.value === themeStore.theme)?.label || '浅色模式'
};

// 检查适老化模式
const checkElderMode = () => {
  isElderMode.value = document.body.classList.contains('elder-mode')
};

const loadUserInfo = async () => {
  try {
    const userInfo = localStorage.getItem('user');
    if (userInfo) {
      const user = JSON.parse(userInfo);
      basicForm.value = {
        phone: user.phone || '',
        name: user.name || '',
        gender: user.gender || '1',
        birthday: user.birthday || '',
        avatar: user.avatar || ''
      };
    }
  } catch (error) {
    ElMessage.error('加载用户信息失败');
    console.error('加载用户信息失败:', error);
  }
};

const handleAvatarSuccess = async (response) => {
  if (response.code === 200) {
    const avatarUrl = response.data.url || response.data;
    basicForm.value.avatar = avatarUrl;
    
    // 调用后端 API 保存头像到数据库
    try {
      await axios.put('/api/user/profile', {
        avatar: avatarUrl
      });
      ElMessage.success('头像上传并保存成功');
    } catch (error) {
      ElMessage.error('头像上传成功，但保存失败');
      console.error('保存头像失败:', error);
    }
  } else {
    ElMessage.error(response.message || '头像上传失败');
  }
};

const beforeAvatarUpload = (file) => {
  const isJPG = file.type === 'image/jpeg' || file.type === 'image/png';
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isJPG) {
    ElMessage.error('上传头像图片只能是 JPG/PNG 格式!');
  }
  if (!isLt2M) {
    ElMessage.error('上传头像图片大小不能超过 2MB!');
  }
  return isJPG && isLt2M;
};

const handleUpdateBasic = async () => {
  if (!basicFormRef.value) return;

  try {
    await basicFormRef.value.validate();
    updating.value = true;

    await axios.put('/api/user/profile', {
      name: basicForm.value.name,
      gender: basicForm.value.gender,
      birthday: basicForm.value.birthday,
      avatar: basicForm.value.avatar
    });

    ElMessage.success('个人信息修改成功');

    const userInfo = localStorage.getItem('user');
    if (userInfo) {
      const user = JSON.parse(userInfo);
      user.name = basicForm.value.name;
      user.gender = basicForm.value.gender;
      user.birthday = basicForm.value.birthday;
      user.avatar = basicForm.value.avatar;
      localStorage.setItem('user', JSON.stringify(user));
    }

    window.location.reload();
  } catch (error) {
    ElMessage.error(error.response?.data?.message || '个人信息修改失败');
    console.error('个人信息修改失败:', error);
  } finally {
    updating.value = false;
  }
};

const handleUpdatePassword = async () => {
  if (!passwordFormRef.value) return;

  try {
    await passwordFormRef.value.validate();
    updatingPassword.value = true;

    console.log('发送密码修改请求:', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });

    const response = await axios.put('/api/user/profile/password', {
      oldPassword: passwordForm.value.oldPassword,
      newPassword: passwordForm.value.newPassword
    });

    console.log('密码修改响应:', response);

    ElMessage.success('密码修改成功，请重新登录');

    passwordForm.value = {
      oldPassword: '',
      newPassword: '',
      confirmPassword: ''
    };

    localStorage.removeItem('user');
    localStorage.removeItem('token');
    window.location.href = '/login';
  } catch (error) {
    console.error('密码修改失败:', error);
    console.error('错误响应:', error.response);
    ElMessage.error(error.response?.data?.message || '密码修改失败');
  } finally {
    updatingPassword.value = false;
  }
};

onMounted(() => {
  loadUserInfo();
  checkElderMode();
  // 监听适老化模式变化
  const observer = new MutationObserver(() => {
    checkElderMode();
  });
  observer.observe(document.body, { attributes: true, attributeFilter: ['class'] });
});
</script>

<style scoped>
.profile-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
}

.profile-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.card-header {
  font-size: 18px;
  font-weight: bold;
}

.profile-tabs {
  margin-top: 20px;
}

.profile-form {
  max-width: 500px;
  margin-top: 20px;
}

.avatar-uploader {
  display: flex;
  align-items: center;
}

.avatar-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  transition: border-color 0.3s;
}

.avatar-uploader .el-upload:hover {
  border-color: #409EFF;
}

.avatar-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  width: 100px;
  height: 100px;
  line-height: 100px;
  text-align: center;
}

.avatar {
  width: 100px;
  height: 100px;
  display: block;
  object-fit: cover;
}

.upload-tip {
  margin-top: 8px;
  font-size: 12px;
  color: #909399;
}

/* 外观设置样式 */
.appearance-settings {
  padding: 20px;
}

.setting-card {
  margin-bottom: 30px;
  border-radius: 12px;
  box-shadow: var(--shadow-md);
  background-color: var(--card);
  border: 1px solid var(--border);
  transition: all 0.3s ease;
}

.setting-card .card-header {
  display: flex;
  align-items: center;
  padding: 16px 20px;
  border-bottom: 1px solid var(--border);
  background-color: var(--secondary);
  border-radius: 12px 12px 0 0;
  font-size: 18px;
  font-weight: 600;
  color: var(--foreground);
}

.header-icon {
  font-size: 20px;
  margin-right: 8px;
  color: hsl(var(--primary));
}

/* 外观设置 - 左右两列布局 */
.theme-color-card {
  max-width: 100%;
}

.theme-color-container {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 32px;
}

.theme-section,
.color-section {
  display: flex;
  flex-direction: column;
}

.section-title {
  font-size: 16px;
  font-weight: 600;
  color: var(--text);
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 2px solid var(--border);
  display: flex;
  align-items: center;
  gap: 8px;
}

.mode-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.mode-container {
  display: flex;
  flex-direction: column;
  gap: 12px;
  padding: 0;
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

.current-mode,
.current-mode-info {
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
  grid-template-columns: repeat(3, 1fr);
  gap: 12px;
  padding: 0;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 12px;
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

/* 适老模式适配 */
:deep(.elder-mode) .profile-container {
  padding: 30px;
}

:deep(.elder-mode) .profile-form {
  max-width: 600px;
}

:deep(.elder-mode) .el-form-item__label {
  font-size: 16px;
  font-weight: bold;
}

:deep(.elder-mode) .el-input__inner,
:deep(.elder-mode) .el-textarea__inner {
  font-size: 16px;
  height: 40px;
  line-height: 40px;
}

:deep(.elder-mode) .el-button {
  font-size: 16px;
  padding: 12px 24px;
  height: auto;
}

:deep(.elder-mode) .avatar-uploader-icon {
  font-size: 32px;
  width: 120px;
  height: 120px;
  line-height: 120px;
}

:deep(.elder-mode) .avatar {
  width: 120px;
  height: 120px;
}

:deep(.elder-mode) .upload-tip {
  font-size: 14px;
}

:deep(.elder-mode) .el-tabs__item {
  font-size: 16px;
  padding: 0 20px;
}

:deep(.elder-mode) .el-radio__label {
  font-size: 16px;
}

:deep(.elder-mode) .setting-card {
  background-color: #ffffff;
  border: 1px solid #000000;
}

:deep(.elder-mode) .setting-card .card-header {
  background-color: #f8f9fa;
  border-bottom: 1px solid #000000;
}

:deep(.elder-mode) .setting-card .card-header span {
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

/* 响应式设计 */
@media (max-width: 768px) {
  .appearance-settings {
    padding: 10px;
  }
  
  .mode-container {
    padding: 0;
    grid-template-columns: 1fr;
  }
  
  .mode-option {
    padding: 16px;
  }
  
  .theme-color-container {
    grid-template-columns: 1fr;
    gap: 20px;
  }
  
  .color-grid {
    grid-template-columns: repeat(3, 1fr);
  }
}
</style>
