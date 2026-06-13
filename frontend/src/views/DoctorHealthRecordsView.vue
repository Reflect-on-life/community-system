<template>
  <div class="doctor-health-records">
    <!-- 顶部标题 -->
    <div class="page-header">
      <h2>健康档案管理</h2>
    </div>

    <!-- 搜索区 -->
    <el-card class="search-card">
      <el-form :model="searchForm" class="search-form">
        <el-form-item label="居民姓名">
          <el-input v-model="searchForm.name" placeholder="请输入居民姓名" clearable style="width: 200px" />
        </el-form-item>
        <el-form-item label="重点人群">
          <el-select v-model="searchForm.isKeyGroup" placeholder="全部" clearable style="width: 120px">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="慢病情况">
          <el-select v-model="searchForm.hasChronicDisease" placeholder="全部" clearable style="width: 120px">
            <el-option label="是" :value="true" />
            <el-option label="否" :value="false" />
          </el-select>
        </el-form-item>
        <el-form-item label="BMI范围">
          <el-select v-model="searchForm.bmiRange" placeholder="全部" clearable style="width: 150px">
            <el-option label="<18.5 (偏瘦)" value="underweight" />
            <el-option label="18.5-24 (正常)" value="normal" />
            <el-option label="24-28 (超重)" value="overweight" />
            <el-option label=">28 (肥胖)" value="obese" />
          </el-select>
        </el-form-item>
        <el-form-item label="体检日期">
          <el-date-picker
            v-model="searchForm.checkDateRange"
            type="daterange"
            range-separator="至"
            start-placeholder="开始日期"
            end-placeholder="结束日期"
            value-format="YYYY-MM-DD"
            style="width: 260px"
          />
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

    <!-- 统计概览 -->
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

    <!-- 表格区 -->
    <el-card class="table-card">
      <el-table v-loading="loading" :data="filteredResidents" style="width: 100%">
        <el-table-column prop="name" label="姓名" min-width="100" />
        <el-table-column prop="gender" label="性别" min-width="80">
          <template #default="scope">
            {{ getGenderText(scope.row.gender) }}
          </template>
        </el-table-column>
        <el-table-column prop="age" label="年龄" min-width="80" />
        <el-table-column prop="bmi" label="BMI" min-width="100">
          <template #default="scope">
            <el-tag v-if="scope.row.bmi" :type="getBMIType(scope.row.bmi)">
              {{ scope.row.bmi }}
            </el-tag>
            <span v-else class="no-data">暂无</span>
          </template>
        </el-table-column>
        <el-table-column label="重点人群" min-width="120">
          <template #default="scope">
            <el-tag v-if="scope.row.isKeyGroup" type="primary">是</el-tag>
            <el-tag v-else type="info">否</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="chronicDisease" label="慢病情况" min-width="150">
          <template #default="scope">
            <el-tag v-if="scope.row.chronicDisease && scope.row.chronicDisease !== '无'" type="warning">
              {{ scope.row.chronicDisease }}
            </el-tag>
            <el-tag v-else type="info">无</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" min-width="280">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleHealthCheck(scope.row)">
              <el-icon><Document /></el-icon>
              录入体检
            </el-button>
            <el-button type="warning" size="small" @click="handleEditChronic(scope.row)">
              <el-icon><Edit /></el-icon>
              编辑慢病
            </el-button>
            <el-button type="success" size="small" @click="handleViewHistory(scope.row)">
              <el-icon><TrendCharts /></el-icon>
              查看历史
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <!-- 分页 -->
      <div class="pagination-container">
        <div class="pagination-left">
          <el-button type="success" @click="handleExportExcel">
            <el-icon><Download /></el-icon>
            导出Excel
          </el-button>
        </div>
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
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="慢性阻塞性肺疾病" value="慢性阻塞性肺疾病" />
            <el-option label="慢性肾病" value="慢性肾病" />
            <el-option label="骨质疏松" value="骨质疏松" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="healthCheckDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitHealthCheck">提交</el-button>
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
            <el-option label="脑卒中" value="脑卒中" />
            <el-option label="慢性阻塞性肺疾病" value="慢性阻塞性肺疾病" />
            <el-option label="慢性肾病" value="慢性肾病" />
            <el-option label="骨质疏松" value="骨质疏松" />
            <el-option label="其他" value="其他" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editChronicDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitEditChronic">保存</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 查看历史弹窗 -->
    <el-dialog
      v-model="historyDialogVisible"
      title="历史体检记录"
      width="900px"
    >
      <div class="history-header">
        <span>居民：{{ currentResident.name }}</span>
      </div>
      <el-table :data="historyData" style="width: 100%" border>
        <el-table-column label="体检日期" width="120">
          <template #default="scope">
            {{ scope.row.createdAt || scope.row.created_at || '-' }}
          </template>
        </el-table-column>
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
        <el-table-column label="慢病情况">
          <template #default="scope">
            {{ scope.row.chronicDiseaseOther || scope.row.chronicDisease || '-' }}
          </template>
        </el-table-column>
      </el-table>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="historyDialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { Search, Document, Edit, User, Star, Warning, TrendCharts, Download } from '@element-plus/icons-vue'
import axios from 'axios'
import * as XLSX from 'xlsx'

// 页面数据
const currentPage = ref(1)
const pageSize = ref(10)
const total = ref(0)
const loading = ref(false)

// 搜索表单
const searchForm = reactive({
  name: '',
  isKeyGroup: null,
  hasChronicDisease: null,
  bmiRange: null,
  checkDateRange: null
})

// 弹窗控制
const healthCheckDialogVisible = ref(false)
const editChronicDialogVisible = ref(false)
const historyDialogVisible = ref(false)

// 当前操作的居民
const currentResident = ref({})

// 历史数据
const historyData = ref([])

// 居民列表数据
const residents = ref([])

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
  chronicDisease: []
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
  return residents.value.filter(item => {
    // 姓名筛选
    if (searchForm.name && !item.name.toLowerCase().includes(searchForm.name.toLowerCase())) {
      return false
    }
    
    // 重点人群筛选
    if (searchForm.isKeyGroup !== null && item.isKeyGroup !== searchForm.isKeyGroup) {
      return false
    }
    
    // 慢病情况筛选
    if (searchForm.hasChronicDisease !== null) {
      const hasDisease = item.chronicDisease && item.chronicDisease !== '无' && item.chronicDisease !== ''
      if (hasDisease !== searchForm.hasChronicDisease) {
        return false
      }
    }
    
    // BMI范围筛选
    if (searchForm.bmiRange && item.bmi) {
      const bmi = parseFloat(item.bmi)
      if (isNaN(bmi)) return false
      
      switch (searchForm.bmiRange) {
        case 'underweight':
          if (bmi >= 18.5) return false
          break
        case 'normal':
          if (bmi < 18.5 || bmi >= 24) return false
          break
        case 'overweight':
          if (bmi < 24 || bmi >= 28) return false
          break
        case 'obese':
          if (bmi < 28) return false
          break
      }
    }
    
    // 体检日期筛选
    if (searchForm.checkDateRange && searchForm.checkDateRange.length === 2) {
      // 这里需要根据实际数据结构调整，假设item中有checkDate字段
      // 如果没有具体的体检日期字段，可以暂时跳过日期筛选
    }
    
    return true
  })
})

// 获取性别文本
const getGenderText = (gender) => {
  if (gender === '0' || gender === 0) return '女'
  if (gender === '1' || gender === 1) return '男'
  if (gender === 'male') return '男'
  if (gender === 'female') return '女'
  return '未知'
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

// 计算统计数据
const calculateStats = () => {
  const data = residents.value
  stats.total = data.length
  stats.keyGroup = data.filter(item => item.isKeyGroup).length
  stats.chronicDisease = data.filter(item => item.hasChronicDisease).length
  
  const bmiList = data.filter(item => item.bmi && !isNaN(item.bmi)).map(item => parseFloat(item.bmi))
  if (bmiList.length > 0) {
    const sum = bmiList.reduce((acc, curr) => acc + curr, 0)
    stats.avgBMI = (sum / bmiList.length).toFixed(1)
  } else {
    stats.avgBMI = 0
  }
}

// 获取BMI类型
const getBMIType = (bmi) => {
  if (!bmi) return 'info'
  if (bmi < 18.5) return 'info'
  if (bmi < 24) return 'success'
  if (bmi < 28) return 'warning'
  return 'danger'
}

// 加载居民数据
const loadResidents = async () => {
  loading.value = true
  try {
    const params = {
      name: searchForm.name || undefined,
      isKeyGroup: searchForm.isKeyGroup !== null ? searchForm.isKeyGroup : undefined,
      hasChronicDisease: searchForm.hasChronicDisease !== null ? searchForm.hasChronicDisease : undefined,
      bmiRange: searchForm.bmiRange || undefined,
      startDate: searchForm.checkDateRange?.[0] || undefined,
      endDate: searchForm.checkDateRange?.[1] || undefined,
      page: currentPage.value,
      pageSize: pageSize.value
    }
    
    const response = await axios.get('/api/doctor-residents/list', { params })
    
    if (response.data && response.data.data) {
      residents.value = response.data.data.map(item => ({
        ...item,
        isKeyGroup: isKeyGroup(item.tags),
        hasChronicDisease: item.chronicDisease && item.chronicDisease !== '无' && item.chronicDisease !== ''
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
  } finally {
    loading.value = false
  }
}

// 搜索处理
const handleSearch = () => {
  currentPage.value = 1
  loadResidents()
}

// 重置处理
const handleReset = () => {
  searchForm.name = ''
  searchForm.isKeyGroup = null
  searchForm.hasChronicDisease = null
  searchForm.bmiRange = null
  searchForm.checkDateRange = null
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

// 重点人群开关处理
const handleKeyGroupChange = async (resident) => {
  try {
    await ElMessageBox.confirm(
      `确定要${resident.isKeyGroup ? '标记为' : '取消标记'}${resident.name}为重点人群吗？`,
      '确认操作',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      }
    )
    
    // 调用API更新重点人群状态
    const tags = resident.isKeyGroup ? 'chronic_disease' : null
    const response = await axios.put(`/api/doctor-residents/tags/${resident.id}`, { tags })
    
    if (response.data && response.data.success) {
      console.log('更新重点人群状态成功:', {
        id: resident.id,
        name: resident.name,
        isKeyGroup: resident.isKeyGroup
      })
      ElMessage.success(`${resident.isKeyGroup ? '标记成功' : '取消标记成功'}`)
      resident.tags = tags
    } else {
      throw new Error('更新失败')
    }
  } catch (error) {
    if (error !== 'cancel') {
      console.error('操作失败:', error)
      ElMessage.error('操作失败，请重试')
    }
    // 恢复原状态
    resident.isKeyGroup = !resident.isKeyGroup
  }
}

// 录入体检
const handleHealthCheck = (resident) => {
  currentResident.value = { ...resident }
  healthCheckForm.height = null
  healthCheckForm.weight = null
  healthCheckForm.chronicDisease = []
  healthCheckForm.bloodPressure = ''
  healthCheckForm.bloodSugar = null
  healthCheckDialogVisible.value = true
}

// 提交体检数据
const submitHealthCheck = async () => {
  if (!healthCheckFormRef.value) return
  
  try {
    await healthCheckFormRef.value.validate()
    
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
      doctorSuggestion: '',
      notes: ''
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
    
    const chronicDiseaseStr = editChronicForm.chronicDisease.length > 0 
      ? editChronicForm.chronicDisease.join(',') 
      : ''
    
    // 这里可以调用专门的更新慢病API
    // 暂时复用体检API，实际项目中应该有专门的接口
    const data = {
      userId: currentResident.value.id,
      chronicDisease: chronicDiseaseStr
    }
    
    console.log('更新慢病信息:', data)
    
    // 模拟API调用
    await new Promise(resolve => setTimeout(resolve, 500))
    
    ElMessage.success('慢病信息更新成功')
    editChronicDialogVisible.value = false
    loadResidents()
  } catch (error) {
    console.error('表单验证失败:', error)
    ElMessage.error('操作失败，请重试')
  }
}

// 查看历史
const handleViewHistory = async (resident) => {
  currentResident.value = { ...resident }
  historyDialogVisible.value = true
  
  try {
    const response = await axios.get(`/api/doctor-residents/health-records/${resident.id}`)
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

// 导出Excel
const handleExportExcel = () => {
  const data = residents.value
  if (data.length === 0) {
    ElMessage.warning('暂无数据可导出')
    return
  }

  // 准备导出数据
  const exportData = data.map(item => ({
    '姓名': item.name,
    '性别': getGenderText(item.gender),
    '年龄': item.age,
    'BMI': item.bmi || '暂无',
    '重点人群': item.isKeyGroup ? '是' : '否',
    '慢病情况': item.chronicDisease || '无'
  }))

  // 创建工作簿和工作表
  const ws = XLSX.utils.json_to_sheet(exportData)
  const wb = XLSX.utils.book_new()
  XLSX.utils.book_append_sheet(wb, ws, '健康档案')

  // 导出文件
  XLSX.writeFile(wb, `健康档案_${new Date().getTime()}.xlsx`)
  
  ElMessage.success('导出成功')
}

// 页面加载时获取数据
onMounted(() => {
  loadResidents()
})
</script>

<style scoped>
.doctor-health-records {
  padding: 20px;
  background-color: #f5f7fa;
  min-height: 100vh;
}

.page-header {
  margin-bottom: 30px;
}

.page-header h2 {
  color: #303133;
  font-size: 24px;
  font-weight: 500;
  margin: 0;
}

.search-card {
  margin-bottom: 20px;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.stats-row {
  margin-bottom: 20px;
}

.stat-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  height: 120px;
  display: flex;
  align-items: center;
}

.stat-content {
  display: flex;
  align-items: center;
  padding: 10px;
  width: 100%;
  height: 100%;
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

.search-form {
  display: flex;
  align-items: center;
  flex-wrap: wrap;
  gap: 15px;
}

.table-card {
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.pagination-left {
  display: flex;
  gap: 10px;
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

.no-data {
  color: #909399;
  font-size: 14px;
}

.history-header {
  margin-bottom: 20px;
  padding: 10px;
  background-color: #f5f7fa;
  border-radius: 4px;
  color: #606266;
  font-size: 14px;
}
</style>