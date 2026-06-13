<template>
  <div class="alarm-history">
    <!-- 页面标题 -->
    <div class="page-header">
      <h1 class="page-title">
        <el-icon><Bell /></el-icon>
        告警历史记录
      </h1>
      <el-button type="primary" @click="handleRefresh">
        <el-icon><Refresh /></el-icon>
        刷新
      </el-button>
    </div>

    <!-- 筛选栏 -->
    <div class="filter-section">
      <el-form :inline="true" :model="filterForm" class="filter-form">
        <!-- 告警类型筛选 -->
        <el-form-item label="告警类型">
          <el-select v-model="filterForm.alarmType" placeholder="全部" clearable style="width: 150px">
            <el-option label="火灾" value="火灾" />
            <el-option label="人员摔倒" value="人员摔倒" />
            <el-option label="非法闯入" value="非法闯入" />
            <el-option label="打架" value="打架" />
            <el-option label="发传单" value="发传单" />
            <el-option label="垃圾堆积" value="垃圾堆积" />
            <el-option label="正常场景" value="正常场景" />
          </el-select>
        </el-form-item>

        <!-- 时间范围筛选 -->
        <el-form-item label="时间范围">
          <el-date-picker
            v-model="filterForm.dateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 240px"
          />
        </el-form-item>

        <!-- 查询按钮 -->
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">
            <el-icon><RefreshLeft /></el-icon>
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 数据表格 -->
    <div class="table-section">
      <el-table
        :data="tableData"
        v-loading="loading"
        border
        stripe
        style="width: 100%"
        :header-cell-style="{ background: '#f5f7fa', color: '#606266' }"
      >
        <!-- 序号 -->
        <el-table-column
          type="index"
          label="序号"
          width="60"
          align="center"
        />

        <!-- 告警类型 -->
        <el-table-column
          label="告警类型"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="getAlarmTypeTag(row.alarmType)" size="normal">
              {{ row.alarmType }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 置信度 -->
        <el-table-column
          label="置信度"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <span>{{ (row.confidence * 100).toFixed(1) }}%</span>
          </template>
        </el-table-column>

        <!-- 告警时间 -->
        <el-table-column
          prop="alarmTime"
          label="告警时间"
          width="180"
          align="center"
          :formatter="formatAlarmTime"
        />

        <!-- 截图 -->
        <el-table-column
          label="截图"
          width="120"
          align="center"
        >
          <template #default="{ row }">
            <el-image
              v-if="row.imagePath"
              :src="`http://localhost:8080${row.imagePath}`"
              fit="cover"
              style="width: 60px; height: 60px; border-radius: 4px; cursor: pointer"
              :preview-src-list="[`http://localhost:8080${row.imagePath}`]"
              :hide-on-click-modal="true"
            >
              <template #placeholder>
                <div style="width: 60px; height: 60px; background: #f5f7fa; display: flex; align-items: center; justify-content: center;">
                  <el-icon><Picture /></el-icon>
                </div>
              </template>
            </el-image>
            <span v-else style="color: #909399">无截图</span>
          </template>
        </el-table-column>

        <!-- 位置 -->
        <el-table-column
          prop="location"
          label="位置"
          min-width="150"
          show-overflow-tooltip
        />

        <!-- 状态 -->
        <el-table-column
          label="状态"
          width="100"
          align="center"
        >
          <template #default="{ row }">
            <el-tag :type="row.isRead === 1 ? 'success' : 'warning'" size="normal">
              {{ row.isRead === 1 ? '已读' : '未读' }}
            </el-tag>
          </template>
        </el-table-column>

        <!-- 操作 -->
        <el-table-column
          label="操作"
          width="180"
          align="center"
          fixed="right"
        >
          <template #default="{ row }">
            <el-button
              v-if="row.isRead === 0"
              type="primary"
              link
              size="small"
              @click="handleMarkAsRead(row)"
            >
              标为已读
            </el-button>
            <span v-else style="color: #909399; font-size: 12px">已处理</span>
            <el-button
              type="danger"
              link
              size="small"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- 底部分页 -->
    <div class="pagination-section">
      <el-pagination
        v-model:current-page="pagination.page"
        v-model:page-size="pagination.size"
        :page-sizes="[10, 20, 50]"
        :total="pagination.total"
        layout="total, sizes, prev, pager, next, jumper"
        @size-change="handleSizeChange"
        @current-change="handlePageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import {
  Bell,
  Refresh,
  Search,
  RefreshLeft,
  Picture
} from '@element-plus/icons-vue'
import axios from 'axios'

// ========== 响应式数据 ==========
const loading = ref(false)
const tableData = ref([])
const filterForm = reactive({
  alarmType: '',
  dateRange: []
})

const pagination = reactive({
  page: 1,
  size: 10,
  total: 0
})

// ========== 方法 ==========

// 加载告警列表
const loadAlarmList = async () => {
  loading.value = true
  
  try {
    const params = {
      page: pagination.page,
      size: pagination.size
    }

    // 添加告警类型筛选
    if (filterForm.alarmType) {
      params.alarmType = filterForm.alarmType
    }

    // 添加时间范围筛选
    if (filterForm.dateRange && filterForm.dateRange.length === 2) {
      params.startTime = filterForm.dateRange[0] + ' 00:00:00'
      params.endTime = filterForm.dateRange[1] + ' 23:59:59'
    }

    const response = await axios.get('http://localhost:8080/api/alarm/list', { params })

    if (response.data && response.data.records) {
      // 统一字段名（risk_event 和 alarm_record 字段名不同）
      tableData.value = response.data.records.map(item => ({
        id: item.id,
        alarmType: item.riskType || item.alarmType || '未知',
        confidence: item.aiConfidence !== undefined ? item.aiConfidence : (item.confidence || 0),
        imagePath: item.imageUrl || item.imagePath || '',
        location: item.location || '未知位置',
        alarmTime: item.createTime || item.alarmTime,
        isRead: item.isRead !== undefined ? item.isRead : 0,
        description: item.description || ''
      }))
      pagination.total = response.data.total || 0
    }
  } catch (error) {
    console.error('加载告警列表失败:', error)
    ElMessage.error('加载失败：' + error.message)
    tableData.value = []
    pagination.total = 0
  } finally {
    loading.value = false
  }
}

// 处理查询
const handleQuery = () => {
  pagination.page = 1
  loadAlarmList()
  ElMessage.success('查询成功')
}

// 处理重置
const handleReset = () => {
  filterForm.alarmType = ''
  filterForm.dateRange = []
  pagination.page = 1
  loadAlarmList()
  ElMessage.success('重置成功')
}

// 处理刷新
const handleRefresh = () => {
  loadAlarmList()
  ElMessage.success('刷新成功')
}

// 处理页码变化
const handlePageChange = (page) => {
  pagination.page = page
  loadAlarmList()
}

// 处理每页条数变化
const handleSizeChange = (size) => {
  pagination.size = size
  pagination.page = 1
  loadAlarmList()
}

// 标记为已读
const handleMarkAsRead = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要将此告警标记为已读吗？',
      '提示',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.post(`http://localhost:8080/api/alarm/read/${row.id}`)
    
    // 更新本地状态
    row.isRead = 1
    
    ElMessage.success('标记成功')
    
    // 重新加载列表
    loadAlarmList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('标记已读失败:', error)
      ElMessage.error('操作失败')
    }
  }
}

// 删除告警记录
const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(
      '确定要删除这条告警记录吗？删除后无法恢复。',
      '删除确认',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )

    await axios.delete(`http://localhost:8080/api/alarm/${row.id}`)
    
    ElMessage.success('删除成功')
    
    // 重新加载列表
    loadAlarmList()
  } catch (error) {
    if (error !== 'cancel') {
      console.error('删除告警失败:', error)
      ElMessage.error('删除失败')
    }
  }
}

// 获取告警类型标签
const getAlarmTypeTag = (type) => {
  switch (type) {
    case '火灾':
      return 'danger'
    case '打架':
      return 'danger'
    case '非法闯入':
      return 'danger'
    case '人员摔倒':
      return 'warning'
    case '垃圾堆积':
      return 'warning'
    case '发传单':
      return 'info'
    default:
      return 'success'
  }
}

// 格式化告警时间
const formatAlarmTime = (row) => {
  if (!row.alarmTime) return ''
  const date = new Date(row.alarmTime)
  return date.toLocaleString('zh-CN', {
    year: 'numeric',
    month: '2-digit',
    day: '2-digit',
    hour: '2-digit',
    minute: '2-digit',
    second: '2-digit'
  })
}

// ========== 生命周期 ==========
onMounted(() => {
  loadAlarmList()
})
</script>

<style scoped>
.alarm-history {
  min-height: 100vh;
  background: #f5f7fa;
  padding: 20px;
}

/* 页面标题 */
.page-header {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.page-title {
  font-size: 20px;
  font-weight: 600;
  color: #303133;
  display: flex;
  align-items: center;
  gap: 8px;
  margin: 0;
}

/* 筛选栏 */
.filter-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-bottom: 20px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

/* 表格区域 */
.table-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 分页区域 */
.pagination-section {
  background: #fff;
  padding: 20px;
  border-radius: 8px;
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.05);
}

/* 表格样式优化 */
:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-table th) {
  font-weight: 600;
}

:deep(.el-table__row:hover) {
  background-color: #f5f7fa;
}

/* 图片悬停效果 */
:deep(.el-image) {
  transition: transform 0.3s;
}

:deep(.el-image:hover) {
  transform: scale(1.1);
}

/* 分页样式 */
:deep(.el-pagination) {
  padding: 10px 0;
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled).active) {
  background-color: #409eff;
}

:deep(.el-pagination.is-background .el-pager li:not(.disabled):hover) {
  color: #409eff;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .alarm-history {
    padding: 10px;
  }

  .page-header {
    flex-direction: column;
    gap: 10px;
    align-items: flex-start;
  }

  .filter-form {
    flex-direction: column;
  }

  .filter-form :deep(.el-form-item) {
    width: 100%;
  }

  .pagination-section {
    justify-content: center;
  }
}
</style>
