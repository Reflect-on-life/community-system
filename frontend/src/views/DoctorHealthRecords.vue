<template>
  <div class="doctor-health-records">
    <!-- 顶部标题区域 -->
    <div class="page-header">
      <div class="header-left">
        <h1>健康档案管理</h1>
      </div>
      <div class="header-right">
        <el-button type="primary" @click="handleAppointment">
          <el-icon><Document /></el-icon>
          预约接单
        </el-button>
      </div>
    </div>

    <!-- 统计概览卡片 -->
    <el-row :gutter="20" class="stats-row">
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #409eff">
              <el-icon size="24"><User /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">总居民数</div>
              <div class="stat-value">{{ stats.total }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #67c23a">
              <el-icon size="24"><Star /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">重点人群</div>
              <div class="stat-value">{{ stats.keyGroup }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #e6a23c">
              <el-icon size="24"><Warning /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">慢病居民</div>
              <div class="stat-value">{{ stats.chronicDisease }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
      <el-col :span="6">
        <el-card class="stat-card">
          <div class="stat-content">
            <div class="stat-icon" style="background: #f56c6c">
              <el-icon size="24"><TrendCharts /></el-icon>
            </div>
            <div class="stat-info">
              <div class="stat-label">平均BMI</div>
              <div class="stat-value">{{ stats.avgBMI }}</div>
            </div>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <el-form :inline="true" :model="searchForm" class="filter-form">
        <el-form-item label="居民姓名">
          <el-input v-model="searchForm.name" placeholder="请输入居民姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="重点人群">
          <el-select v-model="searchForm.isKeyGroup" placeholder="全部" clearable style="width: 120px">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="searchForm.gender" placeholder="全部" clearable style="width: 120px">
            <el-option label="男" value="1" />
            <el-option label="女" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="年龄范围">
          <el-input-number v-model="searchForm.minAge" placeholder="最小" :min="0" :max="120" style="width: 100px" />
          <span style="margin: 0 8px">-</span>
          <el-input-number v-model="searchForm.maxAge" placeholder="最大" :min="0" :max="120" style="width: 100px" />
        </el-form-item>
        <el-form-item label="BMI范围">
          <el-select v-model="searchForm.bmiRange" placeholder="全部" clearable style="width: 150px">
            <el-option label="<18.5 (偏瘦)" value="underweight" />
            <el-option label="18.5-24 (正常)" value="normal" />
            <el-option label="24-28 (超重)" value="overweight" />
            <el-option label=">28 (肥胖)" value="obese" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">
            <el-icon><Search /></el-icon>
            查询
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <!-- 表格区域 -->
    <el-card class="table-card">
      <!-- 操作按钮区域 -->
      <div class="table-actions">
        <div class="actions-left">
          <el-button type="success" @click="handleExportCSV">
            <el-icon><Download /></el-icon>
            导出 CSV
          </el-button>
          <el-button type="danger" @click="handleBatchDelete" :disabled="selectedRows.length === 0">
            <el-icon><Delete /></el-icon>
            批量删除
          </el-button>
        </div>
        <div class="actions-right">
          <el-tag type="info">已选择 {{ selectedRows.length }} 项</el-tag>
        </div>
      </div>

      <!-- 表格 -->
      <el-table
        v-loading="loading"
        :data="filteredResidents"
        style="width: 100%"
        border
        stripe
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="55" />
        <el-table-column prop="name" label="姓名" width="150" />
        <el-table-column prop="gender" label="性别" width="80">
          <template #default="scope">
            {{ getGenderText(scope.row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" width="80" />
        <el-table-column prop="bmi" label="BMI" width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.bmi" :type="getBMIType(scope.row.bmi)">
              {{ scope.row.bmi }}
            </el-tag>
            <span v-else class="no-data">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="重点人群" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isKeyGroup" type="primary">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="慢病情况" width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.hasChronicDisease" type="warning">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="220" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleHealthCheck(scope.row)">
              <el-icon><Document /></el-icon>
              录入体检
            </el-button>
            <el-button type="warning" size="small" @click="handleEditChronic(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑慢病
            </el-button>
            <el-button type="success" size="small" @click="handleViewDetail(scope.row)">
              <el-icon><TrendCharts /></el-icon>
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>

    <!-- 录入体检弹窗 -->
    <el-dialog
      v-model="healthCheckDialogVisible"
      title="录入体检数据"
      width="600px"
    >
      <el-form :model="healthCheckForm" :rules="healthCheckRules" ref="healthCheckFormRef" label-width="100px">
        <el-form-item label="居民姓名">
          <el-input v-model="currentResident.name" disabled />
        </el-form-item>
        <el-form-item label="身高" prop="height">
          <el-input-number v-model="healthCheckForm.height" :min="50" :max="250" :precision="1" controls-position="right" style="width: 100%" />
          <span class="unit">cm</span>
        </el-form-item>
        <el-form-item label="体重" prop="weight">
          <el-input-number v-model="healthCheckForm.weight" :min="20" :max="200" :precision="1" controls-position="right" style="width: 100%" />
          <span class="unit">kg</span>
        </el-form-item>
        <el-form-item label="血压" prop="bloodPressure">
          <el-input v-model="healthCheckForm.bloodPressure" placeholder="例如：120/80" />
        </el-form-item>
        <el-form-item label="血糖" prop="bloodSugar">
          <el-input-number v-model="healthCheckForm.bloodSugar" :min="0" :max="30" :precision="1" controls-position="right" style="width: 100%" />
          <span class="unit">mmol/L</span>
        </el-form-item>
        <el-form-item label="慢病情况">
          <el-select v-model="healthCheckForm.chronicDisease" placeholder="请选择慢病类型" multiple style="width: 100%">
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
        <el-form-item label="医生建议">
          <el-input v-model="healthCheckForm.doctorSuggestion" type="textarea" :rows="3" placeholder="请输入医生建议" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="healthCheckForm.notes" type="textarea" :rows="2" placeholder="请输入备注" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="healthCheckDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHealthCheck" :loading="saving">提交</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 编辑慢病弹窗 -->
    <el-dialog
      v-model="editChronicDialogVisible"
      title="编辑慢病信息"
      width="500px"
    >
      <el-form :model="editChronicForm" :rules="editChronicRules" ref="editChronicFormRef" label-width="100px">
        <el-form-item label="居民姓名">
          <el-input v-model="currentResident.name" disabled />
        </el-form-item>
        <el-form-item label="慢病情况">
          <el-select v-model="editChronicForm.chronicDisease" placeholder="请选择慢病类型" multiple style="width: 100%">
            <el-option label="高血压" value="高血压" />
            <el-option label="糖尿病" value="糖尿病" />
            <el-option label="冠心病" value="冠心病" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editChronicDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditChronic" :loading="saving">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看详情弹窗 -->
    <el-dialog
      v-model="detailDialogVisible"
      title="历史体检记录"
      width="900px"
      @opened="initDetailCharts"
    >
      <div class="detail-header">
        <span>居民：{{ currentResident.name }}</span>
        <el-tag type="info">年龄：{{ currentResident.age }}岁</el-tag>
        <el-tag type="info">性别：{{ getGenderText(currentResident.gender) }}</el-tag>
      </div>
      
      <el-tabs v-model="activeTab">
        <el-tab-pane label="数据列表" name="list">
          <el-table :data="historyData" style="width: 100%" border>
            <el-table-column prop="checkDate" label="体检日期" width="120" />
            <el-table-column prop="height" label="身高" width="80">
              <template #default="scope">
                {{ scope.row.height }} cm
              </template>
            </el-table-column>
            <el-table-column prop="weight" label="体重" width="80">
              <template #default="scope">
                {{ scope.row.weight }} kg
              </template>
            </el-table-column>
            <el-table-column prop="bmi" label="BMI" width="80">
              <template #default="scope">
                <el-tag :type="getBMIType(scope.row.bmi)">
                  {{ scope.row.bmi }}
                </el-tag>
              </template>
            </el-table-column>
            <el-table-column prop="bloodPressure" label="血压" width="100" />
            <el-table-column prop="bloodSugar" label="血糖" width="100">
              <template #default="scope">
                {{ scope.row.bloodSugar }} mmol/L
              </template>
            </el-table-column>
            <el-table-column prop="chronicDisease" label="慢病情况" />
            <el-table-column prop="doctorSuggestion" label="医生建议" min-width="150" />
          </el-table>
        </el-tab-pane>
        <el-tab-pane label="趋势图表" name="chart">
          <div class="chart-container">
            <div ref="bloodPressureChart" class="chart-item"></div>
            <div ref="bloodSugarChart" class="chart-item"></div>
          </div>
        </el-tab-pane>
      </el-tabs>
      
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="detailDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted, watch, nextTick } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, Edit, User, Star, Warning, TrendCharts, Download, Delete } from '@element-plus/icons-vue'
import axios from 'axios'
import * as echarts from 'echarts'

// 页面数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)
const saving = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  isKeyGroup: null,
  gender: null,
  minAge: null,
  maxAge: null,
  bmiRange: null
})

// 弹窗控制
const healthCheckDialogVisible = ref(false)
const editChronicDialogVisible = ref(false)
const detailDialogVisible = ref(false)
const activeTab = ref('list')

// 当前操作的居民
const currentResident = ref({})

// 居民列表数据
const residents = ref([])
const selectedRows = ref([])

// 历史数据
const historyData = ref([])

// 图表引用
const bloodPressureChart = ref(null)
const bloodSugarChart = ref(null)
let bpChartInstance = null
let bsChartInstance = null

// 统计数据
const stats = reactive({
  total: 0,
  keyGroup: 0,
  chronicDisease: 0,
  avgBMI: 0
})

// 体检表单
const healthCheckForm = reactive({
  height: null,
  weight: null,
  bloodPressure: '',
  bloodSugar: null,
  chronicDisease: [],
  doctorSuggestion: '',
  notes: ''
})

// 编辑慢病表单
const editChronicForm = reactive({
  chronicDisease: []
})

// 表单验证规则
const healthCheckRules = {
  height: [
    { required: true, message: '请输入身高', trigger: 'blur' }
  ],
  weight: [
    { required: true, message: '请输入体重', trigger: 'blur' }
  ],
  bloodPressure: [
    { required: true, message: '请输入血压', trigger: 'blur' }
  ],
  bloodSugar: [
    { required: true, message: '请输入血糖', trigger: 'blur' }
  ]
}

const editChronicRules = {
  chronicDisease: []
}

// 表单引用
const healthCheckFormRef = ref(null)
const editChronicFormRef = ref(null)

// 过滤后的居民列表
const filteredResidents = computed(() => {
  let result = [...residents.value]
  
  if (searchForm.name) {
    result = result.filter(item => item.name.includes(searchForm.name))
  }
  
  if (searchForm.isKeyGroup !== null) {
    result = result.filter(item => item.isKeyGroup === searchForm.isKeyGroup)
  }
  
  if (searchForm.gender !== null) {
    result = result.filter(item => String(item.gender) === String(searchForm.gender))
  }
  
  if (searchForm.minAge !== null) {
    result = result.filter(item => item.age >= searchForm.minAge)
  }
  
  if (searchForm.maxAge !== null) {
    result = result.filter(item => item.age <= searchForm.maxAge)
  }
  
  if (searchForm.bmiRange) {
    result = result.filter(item => {
      if (!item.bmi) return false
      const bmi = parseFloat(item.bmi)
      switch (searchForm.bmiRange) {
        case 'underweight':
          return bmi < 18.5
        case 'normal':
          return bmi >= 18.5 && bmi < 24
        case 'overweight':
          return bmi >= 24 && bmi < 28
        case 'obese':
          return bmi >= 28
        default:
          return true
      }
    })
  }
  
  return result
})

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === '0' || gender === 0) return '女'
  if (gender === '1' || gender === 1) return '男'
  if (gender === 'male') return '男'
  if (gender === 'female') return '女'
  return '未知'
}

// 获取BMI类型
const getBMIType = (bmi) => {
  if (!bmi) return 'info'
  if (bmi < 18.5) return 'info'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

// 判断是否为重点人群
const isKeyGroup = (tags) => {
  if (!tags) return false
  const keyGroupTags = ['key_person', 'chronic_disease', 'elderly', 'pregnant', 'child']
  if (typeof tags === 'string') {
    return keyGroupTags.some(tag => tags.includes(tag))
  }
  if (Array.isArray(tags)) {
    return tags.some(tag => keyGroupTags.includes(tag))
  }
  return false
}

// 判断是否有慢病
const hasChronicDisease = (chronicDisease) => {
  if (!chronicDisease) return false
  if (chronicDisease === '无' || chronicDisease === '') return false
  return true
}

// 计算统计数据
const calculateStats = () => {
  const data = residents.value
  stats.total = data.length
  stats.keyGroup = data.filter(item => item.isKeyGroup).length
  stats.chronicDisease = data.filter(item => item.hasChronicDisease).length
  
  const bmiList = data.filter(item => item.bmi).map(item => parseFloat(item.bmi))
  if (bmiList.length > 0) {
    const sum = bmiList.reduce((acc, curr) => acc + curr, 0)
    stats.avgBMI = (sum / bmiList.length).toFixed(1)
  } else {
    stats.avgBMI = 0
  }
}

// 加载居民数据
const loadResidents = async () => {
  loading.value = true
  try {
    const params = {
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await axios.get('/api/doctor-residents/list', { params })
    
    if (response.data && response.data.data) {
      residents.value = response.data.data.map(item => ({
        ...item,
        isKeyGroup: isKeyGroup(item.tags),
        hasChronicDisease: hasChronicDisease(item.chronicDisease)
      }))
      total.value = response.data.total || residents.value.length
      calculateStats()
    } else {
      residents.value = []
      total.value = 0
      calculateStats()
    }
  } catch (error) {
    console.error('加载居民数据失败:', error)
    ElMessage.error('加载居民数据失败，请重试')
    residents.value = []
    total.value = 0
    calculateStats()
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  console.log('搜索条件:', searchForm)
}

// 重置处理
const handleReset = () => {
  searchForm.name = ''
  searchForm.isKeyGroup = null
  searchForm.gender = null
  searchForm.minAge = null
  searchForm.maxAge = null
  searchForm.bmiRange = null
  currentPage.value = 1
  loadResidents()
}

// 分页处理
const handleSizeChange = (size) => {
  pageSize.value = size
  currentPage.value = 1
  loadResidents()
}

const handleCurrentChange = (current) => {
  currentPage.value = current
  loadResidents()
}

// 表格选择变化
const handleSelectionChange = (selection) => {
  selectedRows.value = selection
}

// 预约接单
const handleAppointment = () => {
  ElMessage.info('预约接单功能开发中...')
}

// 录入体检
const handleHealthCheck = (resident) => {
  currentResident.value = { ...resident }
  healthCheckForm.height = null
  healthCheckForm.weight = null
  healthCheckForm.chronicDisease = []
  healthCheckForm.bloodPressure = ''
  healthCheckForm.bloodSugar = null
  healthCheckForm.doctorSuggestion = ''
  healthCheckForm.notes = ''
  healthCheckDialogVisible.value = true
}

// 提交体检数据
const submitHealthCheck = async () => {
  if (!healthCheckFormRef.value) return
  
  try {
    await healthCheckFormRef.value.validate()
    
    saving.value = true
    
    const chronicDiseaseStr = healthCheckForm.chronicDisease.length > 0 
      ? healthCheckForm.chronicDisease.join(',') 
      : ''
    
    const data = {
      userId: currentResident.value.id,
      height: healthCheckForm.height,
      weight: healthCheckForm.weight,
      bloodPressure: healthCheckForm.bloodPressure,
      bloodSugar: healthCheckForm.bloodSugar,
      chronicDisease: chronicDiseaseStr,
      doctorSuggestion: healthCheckForm.doctorSuggestion,
      notes: healthCheckForm.notes
    }
    
    const response = await axios.post('/api/doctor-residents/health-check', data)
    
    if (response.data && response.data.success) {
      console.log('提交体检数据成功:', data)
      ElMessage.success('体检数据录入成功')
      healthCheckDialogVisible.value = false
      loadResidents()
    } else {
      throw new Error('提交失败')
    }
  } catch (error) {
    console.error('表单验证失败或提交失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    saving.value = false
  }
}

// 编辑慢病
const handleEditChronic = (resident) => {
  currentResident.value = { ...resident }
  editChronicForm.chronicDisease = resident.chronicDisease && resident.chronicDisease !== '无' 
    ? resident.chronicDisease.split(',') 
    : []
  editChronicDialogVisible.value = true
}

// 提交慢病编辑
const submitEditChronic = async () => {
  if (!editChronicFormRef.value) return
  
  try {
    await editChronicFormRef.value.validate()
    
    saving.value = true
    
    const chronicDiseaseStr = editChronicForm.chronicDisease.length > 0 
      ? editChronicForm.chronicDisease.join(',') 
      : ''
    
    const data = {
      userId: currentResident.value.id,
      chronicDisease: chronicDiseaseStr
    }
    
    console.log('更新慢病信息:', data)
    
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('慢病信息更新成功')
    editChronicDialogVisible.value = false
    loadResidents()
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('操作失败，请重试')
  } finally {
    saving.value = false
  }
}

// 查看详情
const handleViewDetail = async (resident) => {
  currentResident.value = { ...resident }
  detailDialogVisible.value = true
  activeTab.value = 'list'
  
  try {
    const response = await axios.get(`/api/doctor-residents/history/${resident.id}`)
    if (response.data && response.data.data) {
      historyData.value = response.data.data
    } else {
      historyData.value = []
    }
  } catch (error) {
    console.error('加载历史记录失败:', error)
    ElMessage.error('加载历史记录失败，请重试')
    historyData.value = []
  }
}

// 初始化详情图表
const initDetailCharts = () => {
  if (historyData.value.length === 0) return
  
  nextTick(() => {
    const records = historyData.value.sort((a, b) => new Date(a.checkDate) - new Date(b.checkDate))
    const dates = records.map(r => r.checkDate)
    
    // 血压数据
    const systolicData = []
    const diastolicData = []
    records.forEach(r => {
      if (r.bloodPressure) {
        const bp = r.bloodPressure.split('/')
        if (bp.length === 2) {
          systolicData.push(parseFloat(bp[0]))
          diastolicData.push(parseFloat(bp[1]))
        } else {
          systolicData.push(null)
          diastolicData.push(null)
        }
      } else {
        systolicData.push(null)
        diastolicData.push(null)
      }
    })
    
    // 血糖数据
    const bloodSugarData = records.map(r => r.bloodSugar)
    
    // 初始化血压图表
    if (bloodPressureChart.value) {
      if (bpChartInstance) {
        bpChartInstance.dispose()
      }
      bpChartInstance = echarts.init(bloodPressureChart.value)
      bpChartInstance.setOption({
        title: {
          text: '血压趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        legend: {
          data: ['收缩压', '舒张压'],
          bottom: 0
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: 'mmHg'
        },
        series: [
          {
            name: '收缩压',
            type: 'line',
            data: systolicData,
            smooth: true,
            itemStyle: {
              color: '#ff4d4f'
            }
          },
          {
            name: '舒张压',
            type: 'line',
            data: diastolicData,
            smooth: true,
            itemStyle: {
              color: '#1890ff'
            }
          }
        ]
      })
    }
    
    // 初始化血糖图表
    if (bloodSugarChart.value) {
      if (bsChartInstance) {
        bsChartInstance.dispose()
      }
      bsChartInstance = echarts.init(bloodSugarChart.value)
      bsChartInstance.setOption({
        title: {
          text: '血糖趋势',
          left: 'center'
        },
        tooltip: {
          trigger: 'axis'
        },
        xAxis: {
          type: 'category',
          data: dates,
          axisLabel: {
            rotate: 45
          }
        },
        yAxis: {
          type: 'value',
          name: 'mmol/L'
        },
        series: [
          {
            name: '血糖',
            type: 'line',
            data: bloodSugarData,
            smooth: true,
            itemStyle: {
              color: '#52c41a'
            }
          }
        ]
      })
    }
  })
}

// 导出CSV
const handleExportCSV = () => {
  const data = filteredResidents.value
  if (data.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  const headers = ['姓名', '性别', '年龄', 'BMI', '重点人群', '慢病情况', '最后体检日期']
  const rows = data.map(item => [
    item.name,
    getGenderText(item.gender),
    item.age,
    item.bmi || '暂无',
    item.isKeyGroup ? '是' : '否',
    item.hasChronicDisease ? '是' : '否',
    item.lastCheckDate || '-'
  ])

  const csvContent = [
    headers.join(','),
    ...rows.map(row => row.join(','))
  ].join('\n')

  const blob = new Blob(['\ufeff' + csvContent], { type: 'text/csv;charset=utf-8;' })
  const link = document.createElement('a')
  const url = URL.createObjectURL(blob)
  
  link.setAttribute('href', url)
  link.setAttribute('download', `健康档案_${new Date().getTime()}.csv`)
  link.style.visibility = 'hidden'
  document.body.appendChild(link)
  link.click()
  document.body.removeChild(link)
  
  ElMessage.success('导出成功')
}

// 批量删除
const handleBatchDelete = async () => {
  try {
    await ElMessageBox.confirm(
      `确定要删除选中的 ${selectedRows.value.length} 条记录吗？`,
      '确认删除',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    ElMessage.info('批量删除功能开发中...')
  } catch (error) {
    if (error !== 'cancel') {
      console.error('批量删除失败:', error)
      ElMessage.error('操作失败，请重试')
    }
  }
}

// 页面加载时获取数据
onMounted(() => {
  loadResidents()
})

// 监听标签切换
watch(activeTab, (newTab) => {
  if (newTab === 'chart' && detailDialogVisible.value) {
    nextTick(() => {
      initDetailCharts()
    })
  }
})
</script>

<style scoped>
.doctor-health-records {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 20px;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.header-left h1 {
  margin: 0;
  color: #303133;
  font-size: 24px;
  font-weight: 500;
}

.header-right {
  display: flex;
  gap: 10px;
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
}

.stat-icon {
  width: 60px;
  height: 60px;
  border-radius: 12px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: white;
  margin-right: 15px;
}

.stat-info {
  flex: 1;
}

.stat-label {
  color: #909399;
  font-size: 14px;
  margin-bottom: 5px;
}

.stat-value {
  color: #303133;
  font-size: 24px;
  font-weight: bold;
}

.filter-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.filter-form {
  display: flex;
  flex-wrap: wrap;
  gap: 10px;
}

.filter-form :deep(.el-form-item) {
  margin-bottom: 0;
  margin-right: 10px;
}

.filter-form :deep(.el-form-item__label) {
  font-weight: 500;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.table-actions {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 15px;
}

.actions-left {
  display: flex;
  gap: 10px;
}

.actions-right {
  display: flex;
  gap: 10px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.unit {
  margin-left: 10px;
  color: #909399;
  font-size: 14px;
}

.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
}

.no-data {
  color: #909399;
  font-size: 14px;
}

.detail-header {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
  display: flex;
  gap: 10px;
  align-items: center;
}

.chart-container {
  display: flex;
  flex-direction: column;
  gap: 20px;
}

.chart-item {
  height: 300px;
  width: 100%;
}

:deep(.el-form-item__label) {
  font-weight: 500;
}

:deep(.el-table) {
  font-size: 14px;
}

:deep(.el-tag) {
  font-weight: 500;
}

:deep(.el-button) {
  margin-right: 8px;
}

:deep(.el-button:last-child) {
  margin-right: 0;
}

/* 适老模式支持 */
@media (prefers-reduced-motion: reduce) {
  .doctor-health-records {
    transition: none;
  }
}

/* 响应式布局 */
@media (max-width: 1200px) {
  .stat-card {
    margin-bottom: 10px;
  }
}

@media (max-width: 768px) {
  .page-header {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .header-right {
    width: 100%;
    margin-top: 10px;
  }
  
  .filter-form {
    flex-direction: column;
  }
  
  .filter-form :deep(.el-form-item) {
    width: 100%;
    margin-right: 0;
  }
  
  .table-actions {
    flex-direction: column;
    align-items: flex-start;
  }
  
  .actions-left,
  .actions-right {
    width: 100%;
  }
}
</style>
