<template>
  <div class="home-container">
    <!-- 欢迎横幅 -->
    <div class="hero-section">
      <div class="hero-content">
        <h1>欢迎回来，{{ user.name || '用户' }}</h1>
        <div class="hero-info">
          <el-tag :type="getUserRoleType(user.type)" size="large">{{ getUserRoleText(user.type) }}</el-tag>
          <span class="hero-subtitle">未来社区服务管理系统</span>
        </div>
      </div>
      <div class="hero-avatar">
        <el-avatar v-if="user.avatar" :src="user.avatar" :size="96" />
        <el-avatar v-else :size="96" :icon="UserFilled" />
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 居民端内容 -->
      <template v-if="user.type === 'resident'">
        <!-- 功能磁贴 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>社区服务</span>
          </div>
          <div class="feature-grid">
            <!-- 快递服务 -->
            <div class="feature-tile" @click="navigateTo('/express')">
              <div class="feature-icon">
                <el-icon :size="40"><Box /></el-icon>
              </div>
              <div class="feature-content">
                <h3>快递服务</h3>
                <p class="feature-brief">您有 {{ expressStats.pending || 0 }} 件待取快递</p>
              </div>
            </div>
            
            <!-- 报修服务 -->
            <div class="feature-tile" @click="navigateTo('/repair')">
              <div class="feature-icon">
                <el-icon :size="40"><DocumentCopy /></el-icon>
              </div>
              <div class="feature-content">
                <h3>报修服务</h3>
                <p class="feature-brief">快速提交维修申请</p>
              </div>
            </div>
            
            <!-- 房产认证 -->
            <div class="feature-tile" @click="navigateTo('/house')">
              <div class="feature-icon">
                <el-icon :size="40"><House /></el-icon>
              </div>
              <div class="feature-content">
                <h3>房产认证</h3>
                <p class="feature-brief">管理您的房产信息</p>
              </div>
            </div>
            
            <!-- 活动中心 -->
            <div class="feature-tile" @click="navigateTo('/activity')">
              <div class="feature-icon">
                <el-icon :size="40"><Calendar /></el-icon>
              </div>
              <div class="feature-content">
                <h3>活动中心</h3>
                <p class="feature-brief">参与社区精彩活动</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 我的快递卡片 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Box /></el-icon>
            <span>我的快递</span>
          </div>
          <div class="express-card">
            <div class="express-info">
              <h3>待取件</h3>
              <p class="express-count">{{ expressStats.pending || 0 }}</p>
              <el-button type="primary" @click="navigateTo('/express')">查看详情</el-button>
            </div>
            <div class="express-icon">
              <el-icon :size="60"><Box /></el-icon>
            </div>
          </div>
        </div>

        <!-- 报修进度条 -->
        <div class="section">
          <div class="section-header">
            <el-icon><DocumentCopy /></el-icon>
            <span>报修进度</span>
          </div>
          <div class="repair-progress">
            <div 
              v-for="repair in recentRepairs" 
              :key="repair.id"
              class="repair-item"
            >
              <div class="repair-header">
                <h3>{{ repair.title }}</h3>
                <span class="repair-status">{{ getRepairStatusText(repair.status) }}</span>
              </div>
              <div class="repair-progress-bar">
                <el-progress 
                  :percentage="getRepairProgress(repair.status)" 
                  :status="getRepairProgressStatus(repair.status)"
                />
              </div>
              <div class="repair-date">{{ formatDate(repair.createdAt) }}</div>
            </div>
            <div v-if="recentRepairs.length === 0" class="empty-repair">
              <el-empty description="暂无报修记录" />
            </div>
          </div>
        </div>

        <!-- 最新公告列表 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Bell /></el-icon>
            <span>最新公告</span>
            <el-button type="primary" size="small" @click="navigateTo('/notice-board')" style="margin-left: auto;">
              查看更多
            </el-button>
          </div>
          <div class="notice-list">
            <div 
              v-for="notice in recentNotices" 
              :key="notice.id"
              class="notice-item"
              @click="navigateTo('/notice-board')"
            >
              <div class="notice-header">
                <h3>{{ notice.title }}</h3>
                <span class="notice-date">{{ formatDate(notice.createdAt) }}</span>
              </div>
              <div class="notice-content">
                <p>{{ truncateContent(notice.content) }}</p>
              </div>
            </div>
            <div v-if="recentNotices.length === 0" class="empty-notice">
              <el-empty description="暂无公告" />
            </div>
          </div>
        </div>
      </template>

      <!-- 物业端内容 -->
      <template v-else-if="user.type === 'property'">
        <!-- 待办提醒卡片 (移到最上面) -->
        <div class="section">
          <div class="section-header">
            <el-icon><Bell /></el-icon>
            <span>待办提醒</span>
          </div>
          <div class="todo-grid">
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><Stamp /></el-icon>
              </div>
              <div class="todo-content">
                <h3>待审核认证</h3>
                <p class="todo-count">{{ propertyStats.pendingCertifications || 0 }}</p>
                <el-button type="primary" @click="navigateTo('/certification-audit')">处理</el-button>
              </div>
            </div>
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><DocumentCopy /></el-icon>
              </div>
              <div class="todo-content">
                <h3>待处理报修</h3>
                <p class="todo-count">{{ propertyStats.pendingRepairs || 0 }}</p>
                <el-button type="primary" @click="navigateTo('/repair-management')">处理</el-button>
              </div>
            </div>
          </div>
        </div>

        <!-- 功能磁贴 (物业服务) -->
        <div class="section">
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>物业服务</span>
          </div>
          <div class="feature-grid">
            <!-- 认证审核 -->
            <div class="feature-tile" @click="navigateTo('/certification-audit')">
              <div class="feature-icon">
                <el-icon :size="40"><Stamp /></el-icon>
              </div>
              <div class="feature-content">
                <h3>认证审核</h3>
                <p class="feature-brief">处理居民认证申请</p>
              </div>
              <div v-if="propertyStats.pendingCertifications > 0" class="feature-badge">
                {{ propertyStats.pendingCertifications }}
              </div>
            </div>
            
            <!-- 报修管理 -->
            <div class="feature-tile" @click="navigateTo('/repair-management')">
              <div class="feature-icon">
                <el-icon :size="40"><DocumentCopy /></el-icon>
              </div>
              <div class="feature-content">
                <h3>报修管理</h3>
                <p class="feature-brief">处理维修申请</p>
              </div>
              <div v-if="propertyStats.pendingRepairs > 0" class="feature-badge">
                {{ propertyStats.pendingRepairs }}
              </div>
            </div>
            
            <!-- 活动管理 -->
            <div class="feature-tile" @click="navigateTo('/activity-management')">
              <div class="feature-icon">
                <el-icon :size="40"><Calendar /></el-icon>
              </div>
              <div class="feature-content">
                <h3>活动管理</h3>
                <p class="feature-brief">发布和管理社区活动</p>
              </div>
            </div>
            
            <!-- 快递管理 -->
            <div class="feature-tile" @click="navigateTo('/express')">
              <div class="feature-icon">
                <el-icon :size="40"><Box /></el-icon>
              </div>
              <div class="feature-content">
                <h3>快递管理</h3>
                <p class="feature-brief">管理快递收发</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 社区活动 (只显示未过期的活动) -->
        <div class="section">
          <div class="section-header">
            <el-icon><Calendar /></el-icon>
            <span>社区活动</span>
            <el-button type="primary" size="small" @click="navigateTo('/activity')" style="margin-left: auto;">
              查看更多
            </el-button>
          </div>
          <div class="activity-grid">
            <div 
              v-for="activity in recentActivities" 
              :key="activity.id"
              class="activity-card"
            >
              <div class="activity-image">
                <img :src="activity.coverImage || 'https://via.placeholder.com/300x200'" :alt="activity.title" />
              </div>
              <div class="activity-content">
                <h3>{{ activity.title }}</h3>
                <p class="activity-description">{{ truncateContent(activity.description, 80) }}</p>
                <div class="activity-meta">
                  <span class="activity-time">
                    <el-icon><Timer /></el-icon>
                    {{ formatDate(activity.startTime) }}
                  </span>
                  <span class="activity-location">
                    <el-icon><Location /></el-icon>
                    {{ activity.location }}
                  </span>
                </div>
                <div class="activity-progress">
                  <div class="progress-info">
                    <span>报名进度：{{ activity.signedUpCount || 0 }}/{{ activity.maxParticipants || 0 }}</span>
                    <span>{{ calculateProgress(activity.signedUpCount, activity.maxParticipants) }}%</span>
                  </div>
                  <el-progress 
                    :percentage="calculateProgress(activity.signedUpCount, activity.maxParticipants)" 
                    :show-text="false"
                    :stroke-width="6"
                  />
                </div>
                <div class="activity-actions">
                  <el-button type="primary" size="small">立即报名</el-button>
                  <el-button size="small" @click="viewActivityDetail(activity)">查看详情</el-button>
                </div>
              </div>
            </div>
            <div v-if="recentActivities.length === 0" class="empty-activity">
              <el-empty description="暂无即将开始的活动" />
            </div>
          </div>
        </div>

        <!-- 智慧驾驶舱入口 -->
        <div class="section">
          <div class="dashboard-entry">
            <h2>智慧驾驶舱</h2>
            <p>查看社区综合数据和风险统计</p>
            <el-button type="primary" size="large" class="dashboard-button" @click="navigateTo('/smart-dashboard')">
              <el-icon><DataAnalysis /></el-icon>
              进入智慧驾驶舱
            </el-button>
          </div>
        </div>
      </template>

      <!-- 医生端内容 -->
      <template v-else-if="user.type === 'doctor'">
        <!-- 功能磁贴 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>健康管理</span>
          </div>
          <div class="feature-grid">
            <!-- 健康档案 -->
            <div class="feature-tile" @click="navigateTo('/health')">
              <div class="feature-icon">
                <el-icon :size="40"><Files /></el-icon>
              </div>
              <div class="feature-content">
                <h3>健康档案</h3>
                <p class="feature-brief">查看居民健康档案</p>
              </div>
            </div>
            
            <!-- 预约管理 -->
            <div class="feature-tile" @click="navigateTo('/appointment')">
              <div class="feature-icon">
                <el-icon :size="40"><DocumentCopy /></el-icon>
              </div>
              <div class="feature-content">
                <h3>预约管理</h3>
                <p class="feature-brief">管理体检和疫苗预约</p>
              </div>
            </div>
            
            <!-- 慢病提醒 -->
            <div class="feature-tile" @click="navigateTo('/health-reminder-publish')">
              <div class="feature-icon">
                <el-icon :size="40"><Bell /></el-icon>
              </div>
              <div class="feature-content">
                <h3>慢病提醒</h3>
                <p class="feature-brief">发布慢病提醒信息</p>
              </div>
            </div>
            
            <!-- 健康宣教 -->
            <div class="feature-tile" @click="navigateTo('/health-education-publish')">
              <div class="feature-icon">
                <el-icon :size="40"><Reading /></el-icon>
              </div>
              <div class="feature-content">
                <h3>健康宣教</h3>
                <p class="feature-brief">发布健康宣教内容</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 公告通知 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Bell /></el-icon>
            <span>公告通知</span>
            <el-button type="primary" size="small" @click="navigateTo('/notice-board')" style="margin-left: auto;">
              查看更多
            </el-button>
          </div>
          <div class="notice-list">
            <div 
              v-for="notice in recentNotices" 
              :key="notice.id"
              class="notice-item"
              @click="navigateTo(`/notice-board/${notice.id}`)"
            >
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <span class="notice-date">{{ formatDate(notice.createdAt) }}</span>
              </div>
            </div>
            <div v-if="recentNotices.length === 0" class="empty-notice">
              <el-empty description="暂无公告" />
            </div>
          </div>
        </div>
      </template>

      <!-- 管理员端内容 -->
      <template v-else-if="user.type === 'admin'">
        <!-- 数据统计概览 -->
        <div class="section">
          <div class="section-header">
            <el-icon><DataLine /></el-icon>
            <span>数据统计概览</span>
          </div>
          <div class="todo-grid">
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><User /></el-icon>
              </div>
              <div class="todo-content">
                <h3>用户总数</h3>
                <p class="todo-count">{{ adminStats.totalUsers || 0 }}</p>
              </div>
            </div>
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><DocumentCopy /></el-icon>
              </div>
              <div class="todo-content">
                <h3>报修总数</h3>
                <p class="todo-count">{{ adminStats.totalRepairs || 0 }}</p>
              </div>
            </div>
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><Calendar /></el-icon>
              </div>
              <div class="todo-content">
                <h3>活动总数</h3>
                <p class="todo-count">{{ adminStats.totalActivities || 0 }}</p>
              </div>
            </div>
            <div class="todo-card">
              <div class="todo-icon">
                <el-icon :size="40"><Box /></el-icon>
              </div>
              <div class="todo-content">
                <h3>快递总数</h3>
                <p class="todo-count">{{ adminStats.totalExpress || 0 }}</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 快速入口 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Grid /></el-icon>
            <span>系统管理</span>
          </div>
          <div class="feature-grid">
            <!-- 用户管理 -->
            <div class="feature-tile" @click="navigateTo('/user-management')">
              <div class="feature-icon">
                <el-icon :size="40"><User /></el-icon>
              </div>
              <div class="feature-content">
                <h3>用户管理</h3>
                <p class="feature-brief">管理系统所有用户</p>
              </div>
            </div>
            
            <!-- 权限管理 -->
            <div class="feature-tile" @click="navigateTo('/role-management')">
              <div class="feature-icon">
                <el-icon :size="40"><Lock /></el-icon>
              </div>
              <div class="feature-content">
                <h3>权限管理</h3>
                <p class="feature-brief">配置角色和权限</p>
              </div>
            </div>
            
            <!-- 操作日志 -->
            <div class="feature-tile" @click="navigateTo('/operation-log')">
              <div class="feature-icon">
                <el-icon :size="40"><Document /></el-icon>
              </div>
              <div class="feature-content">
                <h3>操作日志</h3>
                <p class="feature-brief">查看系统操作记录</p>
              </div>
            </div>
            
            <!-- 智慧驾驶舱 -->
            <div class="feature-tile" @click="navigateTo('/smart-dashboard')">
              <div class="feature-icon">
                <el-icon :size="40"><DataAnalysis /></el-icon>
              </div>
              <div class="feature-content">
                <h3>智慧驾驶舱</h3>
                <p class="feature-brief">查看社区综合数据</p>
              </div>
            </div>
          </div>
        </div>

        <!-- 最新公告 -->
        <div class="section">
          <div class="section-header">
            <el-icon><Bell /></el-icon>
            <span>最新公告</span>
          </div>
          <div class="notice-list">
            <div 
              v-for="notice in recentNotices" 
              :key="notice.id"
              class="notice-item"
              @click="navigateTo('/notice')"
            >
              <div class="notice-title">{{ notice.title }}</div>
              <div class="notice-meta">
                <span class="notice-date">{{ formatDate(notice.createdAt) }}</span>
              </div>
            </div>
            <div v-if="recentNotices.length === 0" class="empty-notice">
              <el-empty description="暂无公告" />
            </div>
          </div>
        </div>
      </template>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { useRouter } from 'vue-router';
import axios from 'axios';
import {
  User,
  Box,
  DocumentCopy,
  Bell,
  Calendar,
  Stamp,
  DataAnalysis,
  Timer,
  Location,
  Grid,
  House,
  Files,
  Reading,
  Lock,
  Document,
  DataLine
} from '@element-plus/icons-vue';

const router = useRouter();
const user = ref({});
const recentNotices = ref([]);
const expressStats = ref({ pending: 0, completed: 0 });
const recentRepairs = ref([]);
const todayActivities = ref([]);
const recentActivities = ref([]);
const propertyStats = ref({ pendingCertifications: 0, pendingRepairs: 0 });
const adminStats = ref({ totalUsers: 0, totalRepairs: 0, totalActivities: 0, totalExpress: 0 });

// 导航到指定路径
const navigateTo = (path) => {
  router.push(path);
};

// 获取用户角色类型
const getUserRoleType = (type) => {
  switch (type) {
    case 'resident': return 'success';
    case 'property': return 'primary';
    case 'doctor': return 'info';
    case 'admin': return 'warning';
    default: return 'default';
  }
};

// 获取用户角色文本
const getUserRoleText = (type) => {
  switch (type) {
    case 'resident': return '社区居民';
    case 'property': return '物业管理员';
    case 'doctor': return '社区医生';
    case 'admin': return '系统管理员';
    default: return '未知角色';
  }
};

// 格式化日期
const formatDate = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleDateString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit'
  });
};

// 格式化时间
const formatTime = (dateString) => {
  if (!dateString) return '';
  const date = new Date(dateString);
  return date.toLocaleTimeString('zh-CN', {
    hour: '2-digit',
    minute: '2-digit'
  });
};

// 截断公告内容
const truncateContent = (content, maxLength = 100) => {
  if (!content) return '';
  return content.length > maxLength ? content.substring(0, maxLength) + '...' : content;
};

// 获取报修状态文本
const getRepairStatusText = (status) => {
  switch (status) {
    case 'pending': return '待处理';
    case 'processing': return '处理中';
    case 'completed': return '已完成';
    case 'cancelled': return '已取消';
    default: return '未知状态';
  }
};

// 获取报修进度
const getRepairProgress = (status) => {
  switch (status) {
    case 'pending': return 0;
    case 'processing': return 50;
    case 'completed': return 100;
    case 'cancelled': return 0;
    default: return 0;
  }
};

// 获取报修进度状态
const getRepairProgressStatus = (status) => {
  switch (status) {
    case 'pending': return '';
    case 'processing': return '';
    case 'completed': return 'success';
    case 'cancelled': return 'exception';
    default: return '';
  }
};

// 跳转到活动详情
const viewActivityDetail = (activity) => {
  router.push(`/activity/${activity.id}`);
};

// 计算进度百分比
const calculateProgress = (signedUpCount, maxParticipants) => {
  if (!maxParticipants || maxParticipants === 0) return 0;
  const progress = ((signedUpCount || 0) / maxParticipants) * 100;
  return Math.min(Math.round(progress), 100);
};

// 检查是否可以报名（简化版，始终返回 true）
const canJoinActivity = (activity) => {
  return true;
};

// 加载用户信息
const loadUserInfo = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    user.value = JSON.parse(userInfo);
  }
};

// 加载公告列表
const loadNotices = async () => {
  try {
    // 使用不需要权限的接口获取公告
    const response = await axios.get('/api/notice');
    // 获取最新的 3 条公告
    recentNotices.value = response.data
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 3);
  } catch (error) {
    console.error('加载公告失败:', error);
  }
};

// 加载居民状态数据
const loadResidentStats = async () => {
  // 只有居民用户且已登录才加载
  if (!user.value.userId || user.value.type !== 'resident') return;
  
  try {
    // 加载快递数据
    const expressResponse = await axios.get('/api/express');
    const expressList = expressResponse.data;
    expressStats.value = {
      pending: expressList.filter(item => item.status === 'in').length,
      completed: expressList.filter(item => item.status === 'picked').length
    };
  } catch (error) {
    console.error('加载居民状态数据失败:', error);
  }
};

// 加载最近的报修记录
const loadRecentRepairs = async () => {
  // 只有居民用户且已登录才加载
  if (!user.value.userId || user.value.type !== 'resident') return;
  
  try {
    const response = await axios.get('/api/repair');
    const repairList = response.data;
    // 获取最新的 3 条报修记录
    recentRepairs.value = repairList
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt))
      .slice(0, 3);
  } catch (error) {
    console.error('加载报修记录失败:', error);
  }
};

// 加载今日活动
const loadTodayActivities = async () => {
  try {
    // 使用不需要权限的接口
    const response = await axios.get('/api/activity/list');
    const activityList = response.data;
    const today = new Date().toISOString().split('T')[0];
    // 获取今日的活动
    todayActivities.value = activityList.filter(item => {
      const activityDate = new Date(item.startTime).toISOString().split('T')[0];
      return activityDate === today;
    });
  } catch (error) {
    console.error('加载活动失败:', error);
  }
};

// 加载最近的活动（只显示未过期的活动）
const loadRecentActivities = async () => {
  try {
    // 使用不需要权限的接口
    const response = await axios.get('/api/activity/list');
    const activityList = response.data;
    const now = new Date();
    
    // 过滤出未过期的活动（结束时间在未来）
    recentActivities.value = activityList
      .filter(activity => {
        const endTime = new Date(activity.endTime);
        return endTime > now; // 只显示未过期的活动
      })
      .sort((a, b) => new Date(b.createdAt) - new Date(a.createdAt)) // 按创建时间倒序
      .slice(0, 4); // 只显示最新 4 个
  } catch (error) {
    console.error('加载活动失败:', error);
  }
};

// 加载物业状态数据
const loadPropertyStats = async () => {
  if (user.value.type !== 'property') return;
  
  try {
    const response = await axios.get('/api/property/todo-stats');
    propertyStats.value = {
      pendingCertifications: response.data.pendingCertifications || 0,
      pendingRepairs: response.data.pendingRepairs || 0,
      pendingComplaints: response.data.pendingComplaints || 0,
      pendingRisks: response.data.pendingRisks || 0
    };
  } catch (error) {
    console.error('加载物业状态数据失败:', error);
    // 使用默认值 0
    propertyStats.value = {
      pendingCertifications: 0,
      pendingRepairs: 0,
      pendingComplaints: 0,
      pendingRisks: 0
    };
  }
};

// 加载管理员统计数据
const loadAdminStats = async () => {
  if (user.value.type !== 'admin') return;
  
  try {
    const [usersResponse, repairsResponse, activitiesResponse, expressResponse] = await Promise.all([
      axios.get('/api/admin/users/page', { params: { page: 1, size: 1000 } }).catch(() => ({ data: { records: [] } })),
      axios.get('/api/repair/all').catch(() => ({ data: [] })),
      axios.get('/api/activity/list').catch(() => ({ data: [] })),
      axios.get('/api/express').catch(() => ({ data: [] }))
    ]);
    
    adminStats.value = {
      totalUsers: usersResponse.data.records?.length || 0,
      totalRepairs: repairsResponse.data.length || 0,
      totalActivities: activitiesResponse.data.length || 0,
      totalExpress: expressResponse.data.length || 0
    };
  } catch (error) {
    console.error('加载管理员统计数据失败:', error);
    adminStats.value = { totalUsers: 0, totalRepairs: 0, totalActivities: 0, totalExpress: 0 };
  }
};

// 初始化数据
onMounted(async () => {
  loadUserInfo();
  await loadNotices();
  await loadResidentStats();
  await loadRecentRepairs();
  await loadTodayActivities();
  await loadRecentActivities();
  await loadPropertyStats();
  await loadAdminStats();
});
</script>

<style scoped>
.home-container {
  min-height: 100vh;
  background: linear-gradient(135deg, #f5f7fa 0%, #e4eaf1 100%);
  padding: 24px;
}

/* 英雄区域 */
.hero-section {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border-radius: 16px;
  padding: 32px;
  margin-bottom: 32px;
  display: flex;
  justify-content: space-between;
  align-items: center;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
  color: white;
}

.hero-content {
  flex: 1;
}

.hero-content h1 {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.hero-info {
  display: flex;
  align-items: center;
  gap: 16px;
}

.hero-subtitle {
  font-size: 16px;
  opacity: 0.9;
}

.hero-avatar {
  flex-shrink: 0;
}

/* 主要内容 */
.main-content {
  display: flex;
  flex-direction: column;
  gap: 32px;
}

.section {
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.2);
}

.section-header {
  display: flex;
  align-items: center;
  font-weight: bold;
  font-size: 18px;
  color: #303133;
  margin-bottom: 24px;
}

.section-header el-icon {
  margin-right: 12px;
  color: #409EFF;
  font-size: 20px;
}

/* 功能磁贴网格 */
.feature-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(240px, 1fr));
  gap: 20px;
}

.feature-tile {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 32px 24px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
  cursor: pointer;
  position: relative;
  display: flex;
  align-items: center;
  gap: 20px;
  overflow: hidden;
}

.feature-tile:hover {
  transform: translateY(-4px) scale(1.02);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
  background: rgba(255, 255, 255, 1);
}

.feature-icon {
  flex-shrink: 0;
  color: #409EFF;
  transition: all 0.3s ease;
}

.feature-tile:hover .feature-icon {
  transform: scale(1.1);
}

.feature-content {
  flex: 1;
}

.feature-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
}

.feature-brief {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.4;
}

.feature-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: #F56C6C;
  color: white;
  font-size: 12px;
  font-weight: bold;
  padding: 4px 8px;
  border-radius: 10px;
  min-width: 20px;
  text-align: center;
  box-shadow: 0 2px 8px rgba(245, 108, 108, 0.3);
}

/* 快递卡片 */
.express-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.express-info {
  flex: 1;
}

.express-info h3 {
  margin: 0 0 16px 0;
  font-size: 20px;
  color: #303133;
}

.express-count {
  margin: 0 0 24px 0;
  font-size: 48px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
}

.express-icon {
  flex-shrink: 0;
  color: #409EFF;
}

/* 报修进度 */
.repair-progress {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.repair-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.repair-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 16px;
}

.repair-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  flex: 1;
  margin-right: 16px;
}

.repair-status {
  font-size: 14px;
  padding: 4px 12px;
  border-radius: 12px;
  background: #f0f9eb;
  color: #67C23A;
  white-space: nowrap;
}

.repair-progress-bar {
  margin-bottom: 12px;
}

.repair-date {
  font-size: 12px;
  color: #909399;
}

.empty-repair {
  padding: 64px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 活动卡片 */
.activity-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  overflow: hidden;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  flex-direction: column;
  height: 100%;
}

.activity-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.25);
  background: rgba(255, 255, 255, 1);
}

/* 活动海报 */
.activity-poster {
  position: relative;
  height: 180px;
  overflow: hidden;
  background: #f0f2f5;
}

.poster-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: all 0.3s ease;
}

.activity-card:hover .poster-img {
  transform: scale(1.05);
}

.poster-placeholder {
  width: 100%;
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #f0f2f5;
}

.placeholder-img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

/* 活动信息 */
.activity-info {
  padding: 20px;
  flex: 1;
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.activity-title {
  margin: 0;
  font-size: 18px;
  font-weight: bold;
  color: #303133;
  line-height: 1.4;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.activity-desc {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.5;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

/* 活动元数据 */
.activity-meta {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.meta-item {
  font-size: 13px;
  color: #909399;
  display: flex;
  align-items: center;
  gap: 8px;
}

.meta-item el-icon {
  font-size: 14px;
}

/* 活动进度 */
.activity-progress {
  margin-top: 8px;
}

.progress-info {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 8px;
  font-size: 13px;
  color: #606266;
}

/* 活动操作 */
.activity-actions {
  display: flex;
  gap: 12px;
  margin-top: 8px;
}

.activity-actions .el-button {
  flex: 1;
  transition: all 0.3s ease;
}

.activity-actions .el-button:hover:not(:disabled) {
  transform: translateY(-2px);
}

.empty-activity {
  padding: 64px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 公告列表 */
.notice-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.notice-item {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 20px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  transition: all 0.3s ease;
  border: 1px solid rgba(255, 255, 255, 0.3);
}

.notice-item:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  background: rgba(255, 255, 255, 1);
}

.notice-header {
  display: flex;
  justify-content: space-between;
  align-items: flex-start;
  margin-bottom: 12px;
}

.notice-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: bold;
  color: #303133;
  flex: 1;
  margin-right: 12px;
}

.notice-date {
  font-size: 12px;
  color: #909399;
  white-space: nowrap;
}

.notice-content p {
  margin: 0;
  font-size: 14px;
  color: #606266;
  line-height: 1.6;
  overflow: hidden;
  text-overflow: ellipsis;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
}

.empty-notice {
  padding: 64px 0;
  display: flex;
  justify-content: center;
  align-items: center;
}

/* 物业端待办网格 */
.todo-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(320px, 1fr));
  gap: 24px;
}

.todo-card {
  background: rgba(255, 255, 255, 0.9);
  backdrop-filter: blur(10px);
  border-radius: 12px;
  padding: 32px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.08);
  border: 1px solid rgba(255, 255, 255, 0.3);
  display: flex;
  align-items: center;
  gap: 24px;
  transition: all 0.3s ease;
}

.todo-card:hover {
  transform: translateY(-4px);
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.2);
  background: rgba(255, 255, 255, 1);
}

.todo-icon {
  flex-shrink: 0;
  color: #409EFF;
}

.todo-content {
  flex: 1;
}

.todo-content h3 {
  margin: 0 0 8px 0;
  font-size: 18px;
  color: #303133;
}

.todo-count {
  margin: 0 0 20px 0;
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  line-height: 1;
}

/* 数据驾驶舱入口 */
.dashboard-entry {
  background: linear-gradient(135deg, #409EFF 0%, #337ecc 100%);
  border-radius: 16px;
  padding: 64px;
  box-shadow: 0 8px 24px rgba(64, 158, 255, 0.3);
  text-align: center;
  color: white;
  transition: all 0.3s ease;
}

.dashboard-entry:hover {
  transform: translateY(-4px);
  box-shadow: 0 12px 32px rgba(64, 158, 255, 0.4);
}

.dashboard-entry h2 {
  margin: 0 0 16px 0;
  font-size: 32px;
  font-weight: bold;
  text-shadow: 0 2px 4px rgba(0, 0, 0, 0.2);
}

.dashboard-entry p {
  margin: 0 0 32px 0;
  font-size: 18px;
  opacity: 0.9;
}

.dashboard-button {
  font-size: 18px;
  padding: 16px 48px;
  transition: all 0.3s ease;
}

.dashboard-button:hover {
  transform: translateY(-2px) scale(1.05);
}

/* 响应式调整 */
@media (max-width: 768px) {
  .home-container {
    padding: 16px;
  }

  .hero-section {
    flex-direction: column;
    align-items: flex-start;
    padding: 24px;
    gap: 24px;
  }

  .hero-content h1 {
    font-size: 28px;
  }

  .feature-grid {
    grid-template-columns: repeat(auto-fill, minmax(180px, 1fr));
  }

  .feature-tile {
    padding: 24px 16px;
    flex-direction: column;
    text-align: center;
    gap: 16px;
  }

  .express-card {
    flex-direction: column;
    text-align: center;
    gap: 24px;
  }

  .activity-info {
    padding: 16px;
  }

  .activity-poster {
    height: 150px;
  }

  .todo-grid {
    grid-template-columns: 1fr;
  }

  .dashboard-entry {
    padding: 48px 24px;
  }

  .dashboard-entry h2 {
    font-size: 24px;
  }
}

@media (max-width: 480px) {
  .hero-content h1 {
    font-size: 24px;
  }

  .hero-info {
    flex-direction: column;
    align-items: flex-start;
    gap: 8px;
  }

  .section {
    padding: 16px;
  }

  .feature-grid {
    grid-template-columns: 1fr;
  }

  .feature-tile {
    padding: 20px 16px;
  }

  .express-card {
    padding: 24px;
  }

  .express-count {
    font-size: 36px;
  }

  .repair-item {
    padding: 16px;
  }

  .activity-card {
    margin-bottom: 20px;
  }

  .activity-poster {
    height: 120px;
  }

  .activity-info {
    padding: 16px;
  }

  .activity-actions {
    flex-direction: column;
  }

  .todo-card {
    padding: 24px;
  }

  .dashboard-entry {
    padding: 32px 16px;
  }

  .dashboard-entry h2 {
    font-size: 24px;
  }

  .dashboard-button {
    width: 100%;
  }
}
</style>