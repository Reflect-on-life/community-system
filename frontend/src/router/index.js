import { createRouter, createWebHistory } from 'vue-router'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: () => import('../views/LoginView.vue')
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('../views/RegisterView.vue')
    },
    {
      path: '/',
      name: 'home',
      component: () => import('../views/HomeView.vue')
    },
    {
      path: '/express',
      name: 'express',
      component: () => import('../views/ExpressView.vue')
    },
    {
      path: '/house',
      name: 'house',
      component: () => import('../views/HouseView.vue')
    },
    {
      path: '/repair',
      name: 'repair',
      component: () => import('../views/RepairView.vue')
    },
    {
      path: '/health',
      name: 'health',
      component: () => import('../views/HealthView.vue')
    },
    {
      path: '/user',
      name: 'user',
      component: () => import('../views/UserView.vue')
    },
    {
      path: '/role',
      name: 'role',
      component: () => import('../views/RoleView.vue')
    },
    {
      path: '/appointment',
      name: 'appointment',
      component: () => import('../views/AppointmentView.vue')
    },
    {
      path: '/vaccine-appointment',
      name: 'vaccine-appointment',
      component: () => import('../views/VaccineAppointmentView.vue'),
      meta: { requiresDoctor: true }
    },
    {
      path: '/certification-audit',
      name: 'certification-audit',
      component: () => import('../views/CertificationAuditView.vue')
    },
    {
      path: '/notice',
      name: 'notice',
      component: () => import('../views/NoticeView.vue')
    },
    {
      path: '/notice/:id',
      name: 'notice-detail',
      component: () => import('../views/NoticeDetailView.vue')
    },
    {
      path: '/complaint-management',
      name: 'complaint-management',
      component: () => import('../views/ComplaintManagementView.vue')
    },
    {
      path: '/notice-statistics',
      name: 'notice-statistics',
      component: () => import('../views/NoticeStatisticsView.vue')
    },
    {
      path: '/satisfaction-statistics',
      name: 'satisfaction-statistics',
      component: () => import('../views/SatisfactionStatisticsView.vue')
    },
    {
      path: '/activity-check',
      name: 'activity-check',
      component: () => import('../views/ActivityCheckView.vue')
    },
    {
      path: '/activity',
      name: 'activity',
      component: () => import('../views/ActivityListView.vue')
    },
    {
      path: '/activity/detail/:id',
      name: 'activity-detail',
      component: () => import('../views/ActivityDetailView.vue')
    },
    {
      path: '/complaint',
      name: 'complaint',
      component: () => import('../views/ComplaintView.vue')
    },
    {
      path: '/activity-management',
      name: 'activity-management',
      component: () => import('../views/ActivityManagementView.vue'),
      meta: { requiresProperty: true }
    },
    {
      path: '/activity-view',
      name: 'activity-view',
      component: () => import('../views/ActivityView.vue'),
      meta: { requiresProperty: true }
    },
    {
      path: '/my-signups',
      name: 'my-signups',
      component: () => import('../views/MySignupsView.vue')
    },
    {
      path: '/notice-board',
      name: 'notice-board',
      component: () => import('../views/NoticeBoardView.vue')
    },
    {
      path: '/repair-management',
      name: 'repair-management',
      component: () => import('../views/RepairManagementView.vue')
    },
    {
      path: '/log',
      name: 'log',
      component: () => import('../views/LogView.vue')
    },
    {
      path: '/dict',
      name: 'dict',
      component: () => import('../views/DictView.vue')
    },
    {
      path: '/doctor-health-records',
      name: 'doctor-health-records',
      component: () => import('../views/DoctorHealthRecords.vue'),
      meta: { requiresDoctor: true }
    },
    {
      path: '/doctor/health-records',
      name: 'doctor-health-records-view',
      component: () => import('../views/DoctorHealthRecordsView.vue'),
      meta: { requiresDoctor: true }
    },
    {
      path: '/neighbor-messages',
      name: 'neighbor-messages',
      component: () => import('../views/NeighborMessageView.vue')
    },
    {
      path: '/neighbor-message/:id',
      name: 'neighbor-message-detail',
      component: () => import('../views/NeighborMessageDetail.vue')
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('../views/ProfileView.vue')
    },

    {
      path: '/health-reminder',
      name: 'health-reminder',
      component: () => import('../views/HealthReminderView.vue')
    },
    {
      path: '/health-education-list',
      name: 'health-education-list',
      component: () => import('../views/HealthEducationListView.vue')
    },
    {
      path: '/risk-report',
      name: 'risk-report',
      component: () => import('../views/RiskReportView.vue')
    },
    {
      path: '/risk-management',
      name: 'risk-management',
      component: () => import('../views/RiskManagementView.vue')
    },
    {
      path: '/risk-dashboard',
      name: 'risk-dashboard',
      component: () => import('../views/RiskDashboardView.vue')
    },
    {
      path: '/risk-map',
      name: 'risk-map',
      component: () => import('../views/RiskMapView.vue')
    },
    {
      path: '/elderly-care-stats',
      name: 'elderly-care-stats',
      component: () => import('../views/ElderlyCareStatsView.vue')
    },
    {
      path: '/smart-dashboard',
      name: 'smart-dashboard',
      component: () => import('../views/SmartDashboardView.vue')
    },
    {
      path: '/monitor-screen',
      name: 'monitor-screen',
      component: () => import('../views/MonitorScreenView.vue'),
      meta: { requiresProperty: true }
    },
    {
      path: '/camera-monitor',
      name: 'camera-monitor',
      component: () => import('../views/CameraMonitor.vue')
    },
    {
      path: '/alarm/history',
      name: 'alarm-history',
      component: () => import('../views/AlarmHistory.vue')
    },
    {
      path: '/doctor-workplace',
      name: 'doctor-workplace',
      component: () => import('../views/DoctorWorkplace.vue'),
      meta: { requiresDoctor: true }
    },
    {
      path: '/doctor-health-records-manage',
      name: 'doctor-health-records-manage',
      component: () => import('../views/DoctorHealthRecordsView.vue'),
      meta: { requiresDoctor: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const token = localStorage.getItem('token');
  const userStr = localStorage.getItem('user');
  
  // 公开页面：登录和注册
  const publicPaths = ['/login', '/register'];
  
  if (publicPaths.includes(to.path)) {
    // 如果已登录，跳转到首页
    if (token && userStr) {
      next('/');
    } else {
      next();
    }
  } else {
    // 检查是否有 token 和用户信息
    if (token && userStr) {
      try {
        const user = JSON.parse(userStr);
        const userType = user.type;
        
        // 检查是否需要权限的页面
        // 1. 只有 admin 类型的用户可以访问 log、user 和 dict 页面
        if (to.path === '/log' || to.path === '/user' || to.path === '/dict') {
          if (userType === 'admin') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 2. 只有 admin 或 property 类型的用户可以访问智慧驾驶舱
        else if (to.path === '/smart-dashboard') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 2. 只有 admin 或 property 类型的用户可以访问风险统计大屏
        else if (to.path === '/risk-dashboard') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 4. 只有admin或property类型的用户可以访问养老服务统计
        else if (to.path === '/elderly-care-stats') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 5. 只有 admin 或 property 类型的用户可以访问监控大屏
        else if (to.path === '/monitor-screen') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 6. 只有 admin 或 property 类型的用户可以访问活动管理
        else if (to.path === '/activity-management' || to.path === '/activity-view') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 7. 只有 admin 或 property 类型的用户可以访问公告发布
        else if (to.path === '/notice') {
          if (userType === 'admin' || userType === 'property') {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 3. 医生角色的权限限制
        else if (userType === 'doctor') {
          // 医生只能访问以下页面
          const allowedPaths = [
            '/', 
            '/appointment', 
            '/health', 
            '/doctor-health-records', 
            '/doctor/health-records',
            '/doctor-health-records-manage',
            '/health-record-manage',
            '/doctor-workplace',
            '/vaccine-appointment', 
            '/health-reminder',
            '/health-education-list', 
            '/neighbor-messages',
            '/profile',
            '/notice-board',
            '/repair',
            '/repair-management',
            '/express',
            '/house',
            '/risk-report',
            '/complaint',
            '/activity',
            '/my-signups',
            '/profile'
          ];
          // 检查是否是帖子详情页
          const isNeighborMessageDetail = to.path.startsWith('/neighbor-message/');
          // 检查是否是活动详情页
          const isActivityDetail = to.path.startsWith('/activity/detail/');
          if (allowedPaths.includes(to.path) || isNeighborMessageDetail || isActivityDetail) {
            next();
          } else {
            // 无权限，跳转到首页
            next('/');
          }
        }
        // 7. 医生入口的权限守卫（基于路由元信息）
        else if (to.matched.some(record => record.meta && record.meta.requiresDoctor)) {
          if (userType === 'doctor') {
            next();
          } else {
            next('/');
          }
        }
        // 6. 医生入口的严格 guard
        else if (to.path === '/doctor-health-records') {
          if (userType === 'doctor') {
            next();
          } else {
            next('/');
          }
        }
        // 7. 预约页面的权限检查（只允许医生和居民访问）
        else if (to.path === '/appointment') {
          if (userType === 'doctor' || userType === 'resident' || userType === 'technician') {
            next();
          } else {
            // 其他角色无权限，跳转到首页
            next('/');
          }
        }
        // 8. 其他角色的页面访问逻辑
        else {
          next();
        }
      } catch (error) {
        // 解析失败，清除无效数据并跳转到登录页
        localStorage.removeItem('token');
        localStorage.removeItem('user');
        next('/login');
      }
    } else {
      // 未登录，跳转到登录页
      next('/login');
    }
  }
});

export default router
