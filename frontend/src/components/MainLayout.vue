<template>
  <div class="main-layout">
    <!-- 侧边栏 -->
    <el-aside :width="isCollapse ? '64px' : '200px'" class="sidebar">
      <div class="sidebar-logo-container">
        <transition name="logoFade">
          <div v-if="!isCollapse" class="logo-full" key="full">
            <el-icon class="logo-img"><House /></el-icon>
            <span class="logo-title">未来社区</span>
          </div>
          <div v-else class="logo-mini" key="mini">
            <el-icon :size="28"><House /></el-icon>
          </div>
        </transition>
      </div>
      <el-menu
        :default-active="activeMenu"
        class="sidebar-menu"
        :collapse="isCollapse"
        background-color="#1f2329"
        text-color="#fff"
        active-text-color="#409EFF"
      >
        <!-- 1. 首页 -->
        <el-menu-item index="/" @click="handleMenuClick({ path: '/' })">
          <el-icon :size="20"><HomeFilled /></el-icon>
          <template #title>首页</template>
        </el-menu-item>

        <!-- 2. 物业管理 (仅 property 和 admin 可见) -->
        <el-sub-menu v-if="showPropertyMenu" index="property">
          <template #title>
            <el-icon :size="20"><OfficeBuilding /></el-icon>
            <span>物业管理</span>
          </template>
          <el-menu-item index="/smart-dashboard" @click="handleMenuClick({ path: '/smart-dashboard' })">
            <el-icon :size="20"><DataAnalysis /></el-icon>
            <template #title>智慧驾驶舱</template>
          </el-menu-item>
          <el-menu-item index="/monitor-screen" @click="handleMenuClick({ path: '/monitor-screen' })">
            <el-icon :size="20"><VideoCamera /></el-icon>
            <template #title>监控大屏</template>
          </el-menu-item>
          <el-menu-item index="/camera-monitor" @click="handleMenuClick({ path: '/camera-monitor' })">
            <el-icon :size="20"><VideoCameraFilled /></el-icon>
            <template #title>摄像头监控</template>
          </el-menu-item>
          <el-menu-item index="/house" @click="handleMenuClick({ path: '/house' })">
            <el-icon :size="20"><OfficeBuilding /></el-icon>
            <template #title>房产管理</template>
          </el-menu-item>
          <el-menu-item index="/certification-audit" @click="handleMenuClick({ path: '/certification-audit' })">
            <el-icon :size="20"><Checked /></el-icon>
            <template #title>认证审核</template>
          </el-menu-item>
          <!-- <el-menu-item index="/elderly-care-stats" @click="handleMenuClick({ path: '/elderly-care-stats' })">
            <el-icon :size="20"><UserFilled /></el-icon>
            <template #title>养老服务统计</template>
          </el-menu-item> -->
        </el-sub-menu>

        <!-- 3. 风险管理 -->
        <el-sub-menu v-if="showRiskMenu" index="risk">
          <template #title>
            <el-icon :size="20"><Warning /></el-icon>
            <span>风险管理</span>
          </template>
          <!-- 居民只能看到风险上报 -->
          <el-menu-item v-if="userRole === 'resident'" index="/risk-report" @click="handleMenuClick({ path: '/risk-report' })">
            <el-icon :size="20"><WarningFilled /></el-icon>
            <template #title>风险上报</template>
          </el-menu-item>
          <!-- 物业和管理员可以看到管理和统计 -->
          <template v-if="userRole === 'property' || userRole === 'admin'">
            <el-menu-item index="/risk-management" @click="handleMenuClick({ path: '/risk-management' })">
              <el-icon :size="20"><Management /></el-icon>
              <template #title>风险事件管理</template>
            </el-menu-item>
            <el-menu-item index="/risk-dashboard" @click="handleMenuClick({ path: '/risk-dashboard' })">
              <el-icon :size="20"><DataAnalysis /></el-icon>
              <template #title>风险统计大屏</template>
            </el-menu-item>
            <el-menu-item index="/alarm/history" @click="handleMenuClick({ path: '/alarm/history' })">
              <el-icon :size="20"><Bell /></el-icon>
              <template #title>告警记录</template>
            </el-menu-item>
          </template>
        </el-sub-menu>

        <!-- 4. 社区服务 (重点合并模块) -->
        <el-sub-menu v-if="showCommunityServiceMenu" index="community">
          <template #title>
            <el-icon :size="20"><ChatDotRound /></el-icon>
            <span>社区服务</span>
          </template>
          <!-- 居民可以看到活动报名 -->
          <el-menu-item v-if="userRole === 'resident'" index="/activity" @click="handleMenuClick({ path: '/activity' })">
            <el-icon :size="20"><Ticket /></el-icon>
            <template #title>活动报名</template>
          </el-menu-item>
          <!-- 居民可以看到我的报名 -->
          <el-menu-item v-if="userRole === 'resident'" index="/my-signups" @click="handleMenuClick({ path: '/my-signups' })">
            <el-icon :size="20"><Document /></el-icon>
            <template #title>我的报名</template>
          </el-menu-item>
          <!-- 物业和管理员可以看到活动管理 -->
          <el-menu-item v-if="userRole === 'property' || userRole === 'admin'" index="/activity-management" @click="handleMenuClick({ path: '/activity-management' })">
            <el-icon :size="20"><Ticket /></el-icon>
            <template #title>活动管理</template>
          </el-menu-item>
          <!-- 物业和管理员可以看到活动查看 -->
          <el-menu-item v-if="userRole === 'property' || userRole === 'admin'" index="/activity-view" @click="handleMenuClick({ path: '/activity-view' })">
            <el-icon :size="20"><View /></el-icon>
            <template #title>活动查看</template>
          </el-menu-item>
          <!-- 居民查看公告 -->
          <el-menu-item v-if="userRole === 'resident'" index="/notice-board" @click="handleMenuClick({ path: '/notice-board' })">
            <el-icon :size="20"><Bell /></el-icon>
            <template #title>公告通知</template>
          </el-menu-item>
          <!-- 医生查看公告 -->
          <el-menu-item v-if="userRole === 'doctor'" index="/notice-board" @click="handleMenuClick({ path: '/notice-board' })">
            <el-icon :size="20"><Bell /></el-icon>
            <template #title>公告通知</template>
          </el-menu-item>
          <!-- 物业和管理员发布公告 -->
          <el-menu-item v-if="userRole === 'property' || userRole === 'admin'" index="/notice" @click="handleMenuClick({ path: '/notice' })">
            <el-icon :size="20"><Bell /></el-icon>
            <template #title>公告通知</template>
          </el-menu-item>
          <!-- 邻里交流：所有用户可见 -->
          <el-menu-item index="/neighbor-messages" @click="handleMenuClick({ path: '/neighbor-messages' })">
            <el-icon :size="20"><ChatLineRound /></el-icon>
            <template #title>邻里交流</template>
          </el-menu-item>
          <!-- 快递管理：居民和物业可见 -->
          <el-menu-item v-if="userRole === 'resident' || userRole === 'property' || userRole === 'admin'" index="/express" @click="handleMenuClick({ path: '/express' })">
            <el-icon :size="20"><Present /></el-icon>
            <template #title>快递管理</template>
          </el-menu-item>
        </el-sub-menu>

        <!-- 5. 健康管理 -->
        <el-sub-menu v-if="showHealthMenu" index="health">
          <template #title>
            <el-icon :size="20"><Avatar /></el-icon>
            <span>健康管理</span>
          </template>
          <!-- 居民看到的菜单 -->
          <template v-if="userRole === 'resident'">
            <el-menu-item index="/health" @click="handleMenuClick({ path: '/health' })">
              <el-icon :size="20"><Files /></el-icon>
              <template #title>健康档案</template>
            </el-menu-item>
            <el-menu-item index="/appointment" @click="handleMenuClick({ path: '/appointment' })">
              <el-icon :size="20"><DocumentCopy /></el-icon>
              <template #title>预约管理</template>
            </el-menu-item>
            <el-menu-item index="/health-reminder" v-if="hasChronicDisease" @click="handleMenuClick({ path: '/health-reminder' })">
              <el-icon :size="20"><Bell /></el-icon>
              <template #title>慢病提醒</template>
            </el-menu-item>
            <el-menu-item index="/health-education-list" @click="handleMenuClick({ path: '/health-education-list' })">
              <el-icon :size="20"><Reading /></el-icon>
              <template #title>健康宣教</template>
            </el-menu-item>
          </template>
          <!-- 医生看到的菜单 -->
          <template v-else-if="userRole === 'doctor'">
            <el-menu-item index="/doctor-workplace" @click="handleMenuClick({ path: '/doctor-workplace' })">
              <el-icon :size="20"><Monitor /></el-icon>
              <template #title>医生工作台</template>
            </el-menu-item>
            <el-menu-item index="/health" @click="handleMenuClick({ path: '/health' })">
              <el-icon :size="20"><Files /></el-icon>
              <template #title>居民健康档案</template>
            </el-menu-item>
            <el-menu-item index="/appointment" @click="handleMenuClick({ path: '/appointment' })">
              <el-icon :size="20"><DocumentCopy /></el-icon>
              <template #title>预约管理</template>
            </el-menu-item>
            <el-menu-item index="/vaccine-appointment" @click="handleMenuClick({ path: '/vaccine-appointment' })">
              <el-icon :size="20"><Ticket /></el-icon>
              <template #title>疫苗预约</template>
            </el-menu-item>
            <el-menu-item index="/health-reminder" @click="handleMenuClick({ path: '/health-reminder' })">
              <el-icon :size="20"><Bell /></el-icon>
              <template #title>慢病提醒</template>
            </el-menu-item>
            <el-menu-item index="/health-education-list" @click="handleMenuClick({ path: '/health-education-list' })">
              <el-icon :size="20"><Reading /></el-icon>
              <template #title>健康宣教</template>
            </el-menu-item>
          </template>
        </el-sub-menu>

        <!-- 6. 报事管理 (投诉 + 维修合并) -->
        <el-sub-menu v-if="showRepairComplaintMenu" index="repair-complaint">
          <template #title>
            <el-icon :size="20"><Tools /></el-icon>
            <span>报事管理</span>
          </template>
          <!-- 居民可以看到报修申请和投诉建议 -->
          <el-menu-item v-if="userRole === 'resident' && hasCertifiedHouse" index="/repair" @click="handleMenuClick({ path: '/repair' })">
            <el-icon :size="20"><Edit /></el-icon>
            <template #title>报修申请</template>
          </el-menu-item>
          <!-- 居民可以看到投诉建议 -->
          <el-menu-item v-if="userRole === 'resident'" index="/complaint" @click="handleMenuClick({ path: '/complaint' })">
            <el-icon :size="20"><Bell /></el-icon>
            <template #title>投诉建议</template>
          </el-menu-item>
          <!-- 物业可以看到投诉管理和报修管理 -->
          <template v-if="userRole === 'property' || userRole === 'admin'">
            <el-menu-item index="/complaint-management" @click="handleMenuClick({ path: '/complaint-management' })">
              <el-icon :size="20"><Bell /></el-icon>
              <template #title>投诉管理</template>
            </el-menu-item>
            <el-menu-item index="/repair-management" @click="handleMenuClick({ path: '/repair-management' })">
              <el-icon :size="20"><Tools /></el-icon>
              <template #title>报修管理</template>
            </el-menu-item>
          </template>
          <!-- 维修师傅可以看到报修处理 -->
          <el-menu-item v-if="isTechnicianRole" index="/repair" @click="handleMenuClick({ path: '/repair' })">
            <el-icon :size="20"><Edit /></el-icon>
            <template #title>报修处理</template>
          </el-menu-item>
        </el-sub-menu>

        <!-- 7. 系统管理 (仅 admin 可见) -->
        <el-sub-menu v-if="showSystemMenu" index="system">
          <template #title>
            <el-icon :size="20"><Setting /></el-icon>
            <span>系统管理</span>
          </template>
          <el-menu-item index="/user" @click="handleMenuClick({ path: '/user' })">
            <el-icon :size="20"><User /></el-icon>
            <template #title>用户管理</template>
          </el-menu-item>
          <el-menu-item index="/role" @click="handleMenuClick({ path: '/role' })">
            <el-icon :size="20"><UserFilled /></el-icon>
            <template #title>权限管理</template>
          </el-menu-item>
          <el-menu-item index="/dict" @click="handleMenuClick({ path: '/dict' })">
            <el-icon :size="20"><Collection /></el-icon>
            <template #title>字典管理</template>
          </el-menu-item>
          <el-menu-item index="/log" @click="handleMenuClick({ path: '/log' })">
            <el-icon :size="20"><Document /></el-icon>
            <template #title>操作日志</template>
          </el-menu-item>
        </el-sub-menu>

        <!-- 8. 个人中心 -->
        <el-menu-item index="/profile" @click="handleMenuClick({ path: '/profile' })">
          <el-icon :size="20"><UserFilled /></el-icon>
          <template #title>个人中心</template>
        </el-menu-item>
      </el-menu>
    </el-aside>
    
    <!-- 主内容区域 -->
    <el-container class="main-content">
      <!-- 顶部导航栏 -->
      <el-header height="60px" class="header">
        <div class="header-left">
          <el-button
            type="text"
            class="collapse-btn"
            @click="isCollapse = !isCollapse"
          >
            <el-icon :size="20">
              <component :is="isCollapse ? Expand : Fold" />
            </el-icon>
          </el-button>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item>{{ currentPage }}</el-breadcrumb-item>
          </el-breadcrumb>
        </div>
        <div class="header-right">
          <div class="elder-mode-switch">
            <span>适老模式</span>
            <el-switch v-model="elderMode" @change="toggleElderMode" />
          </div>
          <el-dropdown>
            <span class="user-info">
              <el-avatar v-if="user.avatar" :src="user.avatar" :size="32" />
              <el-avatar v-else :size="32">
                {{ user.name ? user.name.charAt(0) : 'U' }}
              </el-avatar>
              <span class="user-name">{{ user.name || '未登录' }}</span>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item @click="handleProfile">个人中心</el-dropdown-item>
                <el-dropdown-item @click="handleLogout">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>
      
      <!-- 内容区域 -->
      <el-main>
        <router-view />
      </el-main>
    </el-container>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import { useRouter, useRoute } from 'vue-router';
import axios from 'axios';
import {
  HomeFilled,
  Present,
  House,
  DocumentCopy,
  Avatar,
  UserFilled,
  Setting,
  Bell,
  Fold,
  Expand,
  Odometer,
  OfficeBuilding,
  Checked,
  Tools,
  Ticket,
  Management,
  DataAnalysis,
  ChatDotRound,
  PictureFilled,
  Brush,
  Warning,
  WarningFilled,
  VideoCamera,
  VideoCameraFilled,
  ChatLineRound,
  Reading,
  Files,
  Edit,
  User,
  Collection,
  Document,
  Monitor
} from '@element-plus/icons-vue';

const router = useRouter();
const route = useRoute();
const isCollapse = ref(false);
const user = ref({});
const elderMode = ref(false);

// 用户角色简化判断
const userRole = computed(() => {
  return user.value.type || '';
});

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

// 计算当前激活的菜单
const activeMenu = computed(() => {
  return route.path;
});

// 计算当前页面标题（支持二级菜单）
const currentPage = computed(() => {
  const pathToLabel = {
    '/': '首页',
    '/smart-dashboard': '智慧驾驶舱',
    '/monitor-screen': '监控大屏',
    '/camera-monitor': '摄像头监控',
    '/house': '房产管理',
    '/certification-audit': '认证审核',
    '/risk-report': '风险上报',
    '/risk-management': '风险事件管理',
    '/risk-dashboard': '风险统计大屏',
    '/alarm/history': '告警记录',
    '/activity': '活动报名',
    '/activity-management': '活动管理',
    '/activity-view': '活动查看',
    '/my-signups': '我的报名',
    '/notice-board': '公告通知',
    '/neighbor-messages': '邻里交流',
    '/express': '快递管理',
    '/health': '健康档案',
    '/appointment': '预约管理',
    '/health-reminder': '慢病提醒',
    '/health-education-list': '健康宣教',
    '/complaint': '投诉建议',
    '/complaint-management': '投诉管理',
    '/repair-management': '报修管理',
    '/repair': '报修申请',
    '/user': '用户管理',
    '/role': '权限管理',
    '/dict': '字典管理',
    '/log': '操作日志',
    '/profile': '个人中心',
    '/doctor-workplace': '医生工作台',
    '/vaccine-appointment': '疫苗预约',
    '/health-reminder': '慢病提醒',
    '/doctor-health-records': '医生健康档案',
    '/doctor/health-records': '居民健康档案',
    '/doctor-health-records-manage': '健康档案管理'
  };
  return pathToLabel[route.path] || '首页';
});

// 用户房产认证状态
const hasCertifiedHouse = ref(false);

// 判断当前用户是否为维修师傅
const isTechnician = computed(() => {
  // 维修师傅的 ID 范围是 17-21（根据数据库中的用户数据）
  return user.value.userId >= 17 && user.value.userId <= 21;
});

// 判断当前用户是否为维修师傅角色（用于菜单显示）
const isTechnicianRole = computed(() => {
  return user.value.type === 'technician' || isTechnician.value;
});

// 判断当前用户是否为慢性病患者
const hasChronicDisease = computed(() => {
  // 检查用户的tags字段是否包含'chronic_disease'
  return user.value.tags && user.value.tags.includes('chronic_disease');
});

// 加载用户房产认证状态
const loadUserHouseStatus = async () => {
  if (!user.value.userId) return;
  try {
    console.log('开始加载用户房产认证状态，userId:', user.value.userId);
    // 模拟用户房产认证状态
    hasCertifiedHouse.value = true;
    console.log('加载用户房产认证状态成功，hasCertifiedHouse:', hasCertifiedHouse.value);
  } catch (error) {
    console.error('加载用户房产认证状态失败:', error);
    hasCertifiedHouse.value = false;
  }
};

// 菜单显示控制 - 根据用户角色判断
const showPropertyMenu = computed(() => {
  return user.value.type === 'property' || user.value.type === 'admin';
});

const showRiskMenu = computed(() => {
  return user.value.type === 'resident' || user.value.type === 'property' || user.value.type === 'admin';
});

const showCommunityServiceMenu = computed(() => {
  // 所有用户都可以看到社区服务
  return true;
});

const showHealthMenu = computed(() => {
  return user.value.type === 'resident' || user.value.type === 'doctor';
});

const showRepairComplaintMenu = computed(() => {
  return user.value.type === 'resident' || user.value.type === 'property' || 
         user.value.type === 'technician' || user.value.type === 'admin';
});

const showSystemMenu = computed(() => {
  return user.value.type === 'admin';
});

// 菜单点击事件
const handleMenuClick = (item) => {
  router.push(item.path);
};

// 退出登录
const handleLogout = () => {
  // 清除用户信息和token
  localStorage.removeItem('user');
  localStorage.removeItem('token');
  user.value = {};
  // 跳转到登录页面
  router.push('/login');
};

// 跳转到个人中心
const handleProfile = () => {
  router.push('/profile');
};

// 初始化用户信息
onMounted(async () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    user.value = JSON.parse(userInfo);
    // 加载用户房产认证状态
    await loadUserHouseStatus();
  }
  
  // 恢复适老模式状态
  const savedElderMode = localStorage.getItem('elderMode');
  if (savedElderMode === 'true') {
    elderMode.value = true;
    document.body.classList.add('elder-mode');
  }
});
</script>

<style scoped>
.main-layout {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

.sidebar {
  background-color: #1f2329;
  color: #fff;
  transition: width 0.3s;
}

/* Logo 容器基础样式 */
.sidebar-logo-container {
  height: 60px;
  line-height: 60px;
  background-color: #1f2329;
  text-align: center;
  overflow: hidden; /* 关键：防止文字溢出 */
  border-bottom: 1px solid #30363d;
}

/* 展开时的完整 Logo */
.logo-full {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  white-space: nowrap; /* 关键：防止文字在缩小过程中换行 */
}

.logo-img {
  color: hsl(var(--primary));
  font-size: 24px;
}

.logo-title {
  font-size: 18px;
  font-weight: bold;
  color: #fff;
}

/* 按钮旋转与过渡动画 */
.collapse-btn {
  padding: 0 15px;
  height: 100%;
  cursor: pointer;
  transition: background 0.3s;
  display: flex;
  align-items: center;
}

.collapse-btn:hover {
  background-color: rgba(0, 0, 0, 0.05);
}

/* Logo 切换的淡入淡出效果 */
.logoFade-enter-active,
.logoFade-leave-active {
  transition: opacity 0.3s ease;
}
.logoFade-enter-from,
.logoFade-leave-to {
  opacity: 0;
}

.sidebar-menu {
  margin-top: 20px;
  border: none;
}

.sidebar-menu .el-menu-item {
  height: 56px;
  line-height: 56px;
  margin: 4px 8px;
  border-radius: 8px;
  transition: all 0.3s ease;
}

.sidebar-menu .el-menu-item:hover {
  background-color: hsl(var(--accent) / 0.1) !important;
}

.sidebar-menu .el-menu-item.is-active {
  background-color: hsl(var(--accent) / 0.2) !important;
  color: hsl(var(--primary)) !important;
  font-weight: bold;
}

.sidebar-menu .el-menu-item .el-icon {
  margin-right: 12px;
}

/* 2. 核心修复：折叠状态下的样式优化 */
.sidebar-menu.el-menu--collapse .el-menu-item {
  margin: 4px 0 !important; /* 关键：左右 margin 设为 0，上下保留 4px */
  width: 100%;             /* 确保菜单项占满 64px 宽度 */
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 0 !important;
}

/* 3. 彻底移除图标右边距 */
.sidebar-menu.el-menu--collapse .el-menu-item .el-icon {
  margin: 0 !important;
  font-size: 20px;
}

/* 4. 解决菜单背景色在折叠时的残留白边 */
.sidebar-menu.el-menu--collapse {
  width: 64px;
}

.main-content {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.header {
  background-color: hsl(var(--card));
  border-bottom: 1px solid hsl(var(--border));
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0 20px;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
}

.header-left {
  display: flex;
  align-items: center;
}

.header-right {
  display: flex;
  align-items: center;
  gap: 20px;
}

.elder-mode-switch {
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-info {
  cursor: pointer;
  display: flex;
  align-items: center;
  gap: 8px;
}

.user-name {
  font-size: 14px;
  color: var(--foreground);
}

.el-main {
  padding: 20px;
  overflow-y: auto;
  background-color: hsl(var(--background));
  transition: background-color 0.3s ease;
}
</style>

<style>
/* ========== 适老模式菜单样式覆盖（全局样式，非 scoped） ========== */
/* 必须用全局样式才能匹配 body.elder-mode */
body.elder-mode .sidebar {
  background-color: #f5f7fa !important;
  color: #303133 !important;
  border-right: 1px solid #e4e7ed !important;
}

body.elder-mode .sidebar-logo-container {
  background-color: #f5f7fa !important;
  border-bottom-color: #e4e7ed !important;
  height: 70px !important;
  line-height: 70px !important;
}

body.elder-mode .logo-title {
  color: #303133 !important;
  font-size: 22px !important;
}

body.elder-mode .sidebar-menu .el-menu-item {
  height: 70px !important;
  line-height: 70px !important;
  font-size: 20px !important;
  color: #303133 !important;
  background-color: transparent !important;
}

body.elder-mode .sidebar-menu .el-menu-item .el-icon {
  font-size: 24px !important;
  color: #303133 !important;
}

body.elder-mode .sidebar-menu .el-menu-item:hover {
  color: #409eff !important;
  background-color: #ecf5ff !important;
}

body.elder-mode .sidebar-menu .el-menu-item:hover .el-icon {
  color: #409eff !important;
}

body.elder-mode .sidebar-menu .el-menu-item.is-active {
  color: #ffffff !important;
  background-color: #409eff !important;
}

body.elder-mode .sidebar-menu .el-menu-item.is-active .el-icon {
  color: #ffffff !important;
}

body.elder-mode .sidebar-menu .el-sub-menu__title {
  height: 70px !important;
  line-height: 70px !important;
  font-size: 20px !important;
  color: #303133 !important;
}

body.elder-mode .sidebar-menu .el-sub-menu__title .el-icon {
  font-size: 24px !important;
  color: #303133 !important;
}

body.elder-mode .sidebar-menu .el-sub-menu .el-menu-item {
  height: 60px !important;
  line-height: 60px !important;
  font-size: 18px !important;
}

body.elder-mode .sidebar-menu .el-sub-menu__icon-arrow {
  color: #909399 !important;
  font-size: 20px !important;
}

body.elder-mode .el-menu--collapse .el-menu-item {
  height: 64px !important;
  line-height: 64px !important;
}
</style>
