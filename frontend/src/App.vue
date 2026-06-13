<template>
  <div id="app">
    <!-- 登录和注册页面不使用布局 -->
    <template v-if="$route.path === '/login' || $route.path === '/register'">
      <router-view />
    </template>
    <!-- 其他页面使用布局 -->
    <template v-else>
      <MainLayout />
    </template>
  </div>
</template>

<script setup>
import MainLayout from './components/MainLayout.vue'
</script>

<style>
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

body {
  font-family: Arial, sans-serif;
  background-color: var(--bg-body);
  color: var(--text-main);
  transition: background-color 0.3s ease, color 0.3s ease;
}

#app {
  min-height: 100vh;
}

/* 适老模式全局样式 */
.elder-mode {
  font-size: 18px;
}

.elder-mode body {
  font-size: 18px;
}

.elder-mode .el-card {
  border-radius: 8px;
}

.elder-mode .el-form-item__label {
  font-size: 16px;
  font-weight: bold;
  color: #303133;
}

.elder-mode .el-input__inner,
.elder-mode .el-textarea__inner {
  font-size: 16px;
  height: 40px;
  line-height: 40px;
}

.elder-mode .el-button {
  font-size: 16px;
  padding: 12px 24px;
  height: auto;
}

.elder-mode .el-dialog {
  border-radius: 8px;
}

.elder-mode .el-message {
  font-size: 16px;
  min-width: 300px;
}
</style>
