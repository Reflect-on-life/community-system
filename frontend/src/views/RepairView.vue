<template>
  <div class="repair-container">
    <h2>{{ (user.type === 'property' || user.type === 'technician') ? '报修管理' : '报修申请' }}</h2>
    
    <!-- 居民端：房产认证提示 -->
    <el-alert
      v-if="user.type !== 'property' && user.type !== 'technician' && !hasCertifiedHouse"
      title="提示"
      type="warning"
      :closable="false"
      show-icon
    >
      请先完成房产认证，才能提交报修申请
    </el-alert>
    
    <!-- 居民端：报修申请表单 -->
    <el-card v-if="user.type !== 'property' && user.type !== 'technician' && hasCertifiedHouse" class="apply-card">
      <template #header>
        <div class="card-header">
          <span>提交报修申请</span>
        </div>
      </template>
      <el-form :model="repairForm" :rules="rules" ref="repairFormRef" label-width="80px">
        <el-form-item label="房产" prop="houseId">
          <el-select v-model="repairForm.houseId" placeholder="请选择房产">
            <el-option
              v-for="house in certifiedHouses"
              :key="house.id"
              :label="`${house.buildingName} - ${house.houseNo}`"
              :value="house.id"
            />
          </el-select>
        </el-form-item>
        <el-form-item label="问题描述" prop="description">
          <el-input
            v-model="repairForm.description"
            type="textarea"
            :rows="4"
            placeholder="请详细描述问题"
          ></el-input>
        </el-form-item>
        <el-form-item label="图片上传">
          <el-upload
            class="upload-demo"
            action="#"
            :auto-upload="false"
            :on-change="handleImageChange"
            :limit="3"
            :file-list="fileList"
          >
            <el-button type="primary">点击上传</el-button>
            <template #tip>
              <div class="el-upload__tip">
                支持上传最多3张图片，用于故障描述
              </div>
            </template>
          </el-upload>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit">提交申请</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 居民端：我的报修列表 -->
    <el-card v-if="user.type !== 'property' && user.type !== 'technician' && hasCertifiedHouse" class="list-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>我的报修列表</span>
        </div>
      </template>
      <el-table :data="repairList" style="width: 100%">
        <el-table-column label="房产" width="200">
          <template #default="scope">
            {{ certifiedHouses.find(h => h.id === scope.row.houseId)?.houseNo || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" width="300" />
        <el-table-column label="维修师傅" width="180">
          <template #default="scope">
            <div v-if="scope.row.assignedUserName">
              <div>{{ scope.row.assignedUserName }}</div>
              <div style="font-size: 12px; color: #909399;">{{ formatSkills(scope.row.assignedUserSkills) }}</div>
            </div>
            <span v-else>未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="{
                'pending': 'warning',
                'assigned': 'info',
                'processing': 'primary',
                'completed': 'success',
                'cancelled': 'danger'
              }[scope.row.status]"
            >
              {{ {
                'pending': '待派单',
                'assigned': '已派单',
                'processing': '处理中',
                'completed': '已完成',
                'cancelled': '已取消'
              }[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="200">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="danger" 
              size="small" 
              @click="handleCancel(scope.row.id)"
            >
              取消报修
            </el-button>
            <el-button 
              v-else-if="scope.row.status === 'completed' && !scope.row.rating" 
              type="primary" 
              size="small" 
              @click="handleEvaluate(scope.row.id)"
            >
              评价
            </el-button>
            <el-button 
              type="info" 
              size="small" 
              @click="handleViewDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 物业端/维修师傅端：报修管理列表 -->
    <el-card v-if="user.type === 'property' || user.type === 'technician'" class="list-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>报修单管理池</span>
        </div>
      </template>
      
      <!-- 状态过滤 -->
      <div class="status-filter" style="margin-bottom: 20px;">
        <el-radio-group v-model="statusFilter" size="large">
          <el-radio-button label="all">全部</el-radio-button>
          <el-radio-button label="pending">待派单</el-radio-button>
          <el-radio-button label="assigned">已派单</el-radio-button>
          <el-radio-button label="processing">处理中</el-radio-button>
          <el-radio-button label="completed">已完成</el-radio-button>
        </el-radio-group>
      </div>
      
      <el-table :data="filteredRepairList" style="width: 100%">
        <el-table-column label="房产" width="200">
          <template #default="scope">
            {{ certifiedHouses.find(h => h.id === scope.row.houseId)?.houseNo || '' }}
          </template>
        </el-table-column>
        <el-table-column prop="description" label="问题描述" width="300" />
        <el-table-column label="维修师傅" width="180">
          <template #default="scope">
            <div v-if="scope.row.assignedUserName">
              <div>{{ scope.row.assignedUserName }}</div>
              <div style="font-size: 12px; color: #909399;">{{ formatSkills(scope.row.assignedUserSkills) }}</div>
            </div>
            <span v-else>未分配</span>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-tag
              :type="{
                'pending': 'warning',
                'assigned': 'info',
                'processing': 'primary',
                'completed': 'success',
                'cancelled': 'danger'
              }[scope.row.status]"
            >
              {{ {
                'pending': '待派单',
                'assigned': '已派单',
                'processing': '处理中',
                'completed': '已完成',
                'cancelled': '已取消'
              }[scope.row.status] }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="申请时间" width="180">
          <template #default="scope">
            {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="280">
          <template #default="scope">
            <!-- 待派单 (pending)：显示'派单'按钮 -->
            <el-button 
              v-if="scope.row.status === 'pending'" 
              type="primary" 
              size="small" 
              @click="handleDispatch(scope.row.id)"
            >
              派单
            </el-button>
            <!-- 已派单 (assigned)：维修师傅可'接单'，物业可'取消' -->
            <template v-else-if="scope.row.status === 'assigned'">
              <!-- 维修师傅可以接单 -->
              <el-button 
                v-if="user.type === 'technician'" 
                type="success" 
                size="small" 
                @click="handleAccept(scope.row.id)"
              >
                接单
              </el-button>
              <!-- 物业可以取消 -->
              <el-button 
                v-if="user.type === 'property'" 
                type="danger" 
                size="small" 
                @click="handleCancelByProperty(scope.row)"
              >
                取消
              </el-button>
            </template>
            <!-- 处理中 (processing)：显示'确认完结'按钮 -->
            <template v-else-if="scope.row.status === 'processing'">
              <!-- 只有维修师傅可以确认完结 -->
              <el-button 
                v-if="user.type === 'technician'" 
                type="success" 
                size="small" 
                @click="handleComplete(scope.row.id)"
              >
                确认完结
              </el-button>
            </template>
            <!-- 已完成 (completed)：只显示'查看详情'按钮 -->
            <!-- 已取消 (cancelled)：只显示'查看详情'按钮 -->
            <el-button 
              type="info" 
              size="small" 
              @click="handleViewDetail(scope.row)"
            >
              查看详情
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
    
    <!-- 派单弹窗 -->
    <el-dialog
      v-model="dispatchDialogVisible"
      title="派单给维修师傅"
      width="30%"
    >
      <el-form :model="dispatchForm" :rules="dispatchRules" ref="dispatchFormRef" label-width="100px">
        <el-form-item label="维修师傅" prop="assignedTo">
          <el-select v-model="dispatchForm.assignedTo" placeholder="请选择维修师傅">
            <el-option
              v-for="technician in technicianList"
              :key="technician.id"
              :label="`${technician.name}（${formatSkills(technician.skills)}）`"
              :value="technician.id"
            />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dispatchDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="submitDispatch">确认派单</el-button>
        </span>
      </template>
    </el-dialog>
    
    <!-- 报修详情弹窗 -->
    <el-dialog
      v-model="dialogVisible"
      :title="(user.type === 'property' || user.type === 'technician') ? '报修处理' : '报修详情'"
      width="60%"
    >
      <div v-if="selectedRepair">
        <h3>{{ selectedRepair.description }}</h3>
        <p>房产：{{ certifiedHouses.find(h => h.id === selectedRepair.houseId)?.houseNo || '' }}</p>
        <p>状态：
          <el-tag
            :type="{
              'pending': 'warning',
              'assigned': 'info',
              'processing': 'primary',
              'completed': 'success',
              'cancelled': 'danger'
            }[selectedRepair.status]"
          >
            {{ {
              'pending': '待派单',
              'assigned': '已派单',
              'processing': '处理中',
              'completed': '已完成',
              'cancelled': '已取消'
            }[selectedRepair.status] }}
          </el-tag>
        </p>
        <p v-if="selectedRepair.assignedUserName">
          维修师傅：{{ selectedRepair.assignedUserName }}（{{ formatSkills(selectedRepair.assignedUserSkills) }}）
        </p>
        <p v-else>维修师傅：未分配</p>
        <p>申请时间：{{ new Date(selectedRepair.createdAt || Date.now()).toLocaleString() }}</p>
        <p v-if="selectedRepair.assignedAt">派单时间：{{ new Date(selectedRepair.assignedAt).toLocaleString() }}</p>
        <p v-if="selectedRepair.startedAt">开始时间：{{ new Date(selectedRepair.startedAt).toLocaleString() }}</p>
        <p v-if="selectedRepair.completedAt">完成时间：{{ new Date(selectedRepair.completedAt).toLocaleString() }}</p>
        
        <!-- 维修进度公告区域 -->
        <div v-if="repairNotices.length > 0" class="repair-notices">
          <h4 style="margin-top: 20px;">维修进度公告</h4>
          <el-timeline>
            <el-timeline-item
              v-for="notice in repairNotices"
              :key="notice.id"
              :type="getNoticeType(notice.title)"
              size="large"
              :timestamp="new Date(notice.createdAt).toLocaleString()"
            >
              <div class="notice-content">
                <div class="notice-title">{{ notice.title }}</div>
                <div class="notice-text">{{ notice.content }}</div>
                <div class="notice-publisher">发布人：{{ notice.publisher }}</div>
                <router-link 
                  v-if="notice.id" 
                  :to="`/notice/${notice.id}`" 
                  class="notice-link"
                >
                  查看公告详情
                </router-link>
              </div>
            </el-timeline-item>
          </el-timeline>
        </div>
        
        <!-- 评价表单 -->
        <el-form 
          v-if="user.type !== 'property' && user.type !== 'technician' && selectedRepair.status === 'completed' && !selectedRepair.rating && !hasRatedViaNotice" 
          :model="evaluationForm" 
          :rules="evaluationRules" 
          ref="evaluationFormRef" 
          style="margin-top: 20px;"
        >
          <h4>服务评价</h4>
          <el-form-item label="满意度" prop="rating">
            <el-rate v-model="evaluationForm.rating" :max="5" />
          </el-form-item>
          <el-form-item label="评价内容" prop="comment">
            <el-input v-model="evaluationForm.comment" type="textarea" :rows="3" placeholder="请输入您的评价" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="submitEvaluation">提交评价</el-button>
          </el-form-item>
        </el-form>
        
        <!-- 已评价信息 -->
        <div v-if="selectedRepair.rating || hasRatedViaNotice" class="rating-info" style="margin-top: 20px;">
          <h4>已评价</h4>
          <el-rate v-model="selectedRepair.rating" disabled />
          <p v-if="selectedRepair.comment">评价内容：{{ selectedRepair.comment }}</p>
          <p v-if="hasRatedViaNotice && !selectedRepair.comment" style="color: #409eff;">您已在公告通知中完成评价</p>
        </div>
      </div>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">关闭</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const user = JSON.parse(localStorage.getItem('user') || '{}');
const repairForm = ref({
  houseId: '',
  description: ''
});

const rules = {
  houseId: [
    { required: true, message: '请选择房产', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请描述问题', trigger: 'blur' },
    { min: 5, message: '描述至少5个字符', trigger: 'blur' }
  ]
};

const repairFormRef = ref(null);
const houseList = ref([]);
const userHouseList = ref([]);
const repairList = ref([]);
const fileList = ref([]);
const dialogVisible = ref(false);
const dispatchDialogVisible = ref(false);
const selectedRepair = ref(null);
const evaluationForm = ref({
  repairId: '',
  rating: 0,
  comment: ''
});

const evaluationRules = {
  rating: [
    { required: true, message: '请选择满意度', trigger: 'change' }
  ],
  comment: [
    { required: true, message: '请输入评价内容', trigger: 'blur' },
    { min: 5, message: '评价内容至少 5 个字符', trigger: 'blur' }
  ]
};

const evaluationFormRef = ref(null);
const hasRatedViaNotice = ref(false);

// 物业端：状态过滤
const statusFilter = ref('all');

// 物业端：派单表单
const dispatchForm = ref({
  assignedTo: ''
});

const dispatchRules = {
  assignedTo: [
    { required: true, message: '请选择维修师傅', trigger: 'change' }
  ]
};

const dispatchFormRef = ref(null);

// 物业端：维修师傅列表
const technicianList = ref([]);

// 维修进度公告列表
const repairNotices = ref([]);

// 物业端：根据状态过滤报修列表
const filteredRepairList = computed(() => {
  if (statusFilter.value === 'all') {
    return repairList.value;
  } else {
    return repairList.value.filter(repair => repair.status === statusFilter.value);
  }
});

// 计算用户是否有已认证的房产
const hasCertifiedHouse = computed(() => {
  return userHouseList.value.some(house => house.certified === 1);
});

// 计算用户已认证的房产列表
const certifiedHouses = computed(() => {
  return userHouseList.value.filter(house => house.certified === 1);
});

// 获取公告类型
const getNoticeType = (title) => {
  if (title.includes('派单')) return 'warning';
  if (title.includes('接单')) return 'success';
  if (title.includes('维修中')) return 'info';
  if (title.includes('完成') || title.includes('竣工')) return 'success';
  if (title.includes('取消')) return 'danger';
  return 'default';
};

// 加载用户的房产列表
const loadUserHouses = async () => {
  if (!user.userId) return;
  try {
    // 模拟用户房产列表数据
    userHouseList.value = [
      {
        id: 1,
        buildingId: 1,
        buildingName: '1号楼',
        houseId: 1,
        houseNo: '101室',
        relation: 'owner',
        certified: 1
      }
    ];
    console.log('加载用户房产列表成功，响应数据:', userHouseList.value);
  } catch (error) {
    ElMessage.error('加载用户房产列表失败');
    console.error('加载用户房产列表失败，错误信息:', error);
  }
};

// 加载维修师傅列表
const loadTechnicians = async () => {
  try {
    const response = await axios.get('/api/user/type/property');
    // 过滤出有维修技能的用户（skills 数组不为空）
    technicianList.value = response.data.filter(u => u.skills && u.skills.length > 0);
    console.log('加载维修师傅列表成功，响应数据:', technicianList.value);
  } catch (error) {
    ElMessage.error('加载维修师傅列表失败');
    console.error('加载维修师傅列表失败，错误信息:', error);
  }
};

// 加载用户的报修列表
const loadUserRepairs = async () => {
  try {
    const response = await axios.get('/api/repair');
    repairList.value = response.data;
    console.log('加载用户报修列表成功，响应数据:', repairList.value);
  } catch (error) {
    ElMessage.error('加载报修列表失败');
    console.error('加载报修列表失败，错误信息:', error);
  }
};

// 加载报修相关的公告
const loadRepairNotices = async (repairId) => {
  try {
    const response = await axios.get(`/api/notice/repair/${repairId}`);
    repairNotices.value = response.data;
    console.log('加载报修公告成功，响应数据:', repairNotices.value);
  } catch (error) {
    console.error('加载报修公告失败，错误信息:', error);
  }
};

// 处理图片上传
const handleImageChange = (file, fileList) => {
  console.log('图片上传:', file, fileList);
};

// 提交报修申请
const handleSubmit = async () => {
  await repairFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        const formData = { ...repairForm.value };
        delete formData.userId;
        
        await axios.post('/api/repair', formData);
        ElMessage.success('提交报修申请成功');
        repairFormRef.value.resetFields();
        fileList.value = [];
        await loadUserRepairs();
      } catch (error) {
        ElMessage.error('提交报修申请失败');
        console.error('提交报修申请失败，错误信息:', error);
      }
    }
  });
};

// 取消报修
const handleCancel = async (id) => {
  try {
    await axios.put(`/api/repair/cancel/${id}`);
    ElMessage.success('取消报修成功');
    await loadUserRepairs();
  } catch (error) {
    ElMessage.error('取消报修失败');
    console.error('取消报修失败，错误信息:', error);
  }
};

// 查看详情
const handleViewDetail = async (repair) => {
  selectedRepair.value = repair;
  dialogVisible.value = true;
  hasRatedViaNotice.value = false;
  await loadRepairNotices(repair.id);
  if (repair.status === 'completed') {
    await checkRepairRatedStatus(repair.id);
  }
};

// 评价
const handleEvaluate = (id) => {
  selectedRepair.value = repairList.value.find(item => item.id === id);
  evaluationForm.value.repairId = id;
  hasRatedViaNotice.value = false;
  dialogVisible.value = true;
  loadRepairNotices(id);
  if (selectedRepair.value.status === 'completed') {
    checkRepairRatedStatus(id);
  }
};

// 检查报修是否已评价
const checkRepairRatedStatus = async (repairId) => {
  try {
    const response = await axios.get(`/api/repair/${repairId}`);
    if (response.data && response.data.rating) {
      selectedRepair.value.rating = response.data.rating;
      selectedRepair.value.comment = response.data.comment;
      hasRatedViaNotice.value = true;
      
      // 同步更新 repairList 中的状态，让表格中的"评价"按钮也隐藏
      const index = repairList.value.findIndex(r => r.id === repairId);
      if (index !== -1) {
        repairList.value[index].rating = response.data.rating;
        repairList.value[index].comment = response.data.comment;
      }
    } else {
      hasRatedViaNotice.value = false;
    }
  } catch (error) {
    console.error('检查评价状态失败:', error);
    hasRatedViaNotice.value = false;
  }
};

// 提交评价
const submitEvaluation = async () => {
  await evaluationFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post(`/api/repair/rate/${evaluationForm.value.repairId}`, {
          rating: evaluationForm.value.rating,
          comment: evaluationForm.value.comment
        });
        ElMessage.success('提交评价成功');
        dialogVisible.value = false;
        await loadUserRepairs();
      } catch (error) {
        ElMessage.error('提交评价失败');
        console.error('提交评价失败，错误信息:', error);
      }
    }
  });
};

// 物业端：派单维修师傅
const handleDispatch = (id) => {
  selectedRepair.value = repairList.value.find(item => item.id === id);
  dispatchForm.value.assignedTo = '';
  dispatchDialogVisible.value = true;
};

// 提交派单
const submitDispatch = async () => {
  await dispatchFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.post(`/api/repair/dispatch/${selectedRepair.value.id}`, {
          assignedTo: dispatchForm.value.assignedTo
        });
        ElMessage.success('派单成功');
        dispatchDialogVisible.value = false;
        await loadUserRepairs();
      } catch (error) {
        ElMessage.error('派单失败');
        console.error('派单失败，错误信息:', error);
      }
    }
  });
};

// 维修师傅接单
const handleAccept = async (id) => {
  try {
    await axios.post(`/api/repair/accept/${id}`);
    ElMessage.success('接单成功');
    await loadUserRepairs();
  } catch (error) {
    ElMessage.error('接单失败');
    console.error('接单失败，错误信息:', error);
  }
};

// 物业端取消工单
const handleCancelByProperty = async (repair) => {
  try {
    await axios.put(`/api/repair/cancel-by-property/${repair.id}`, {});
    ElMessage.success('取消成功');
    await loadUserRepairs();
  } catch (error) {
    ElMessage.error('取消失败');
    console.error('取消失败，错误信息:', error);
  }
};

// 物业端：确认完结
const handleComplete = async (id) => {
  try {
    await axios.post(`/api/repair/complete/${id}`);
    ElMessage.success('确认完结成功');
    await loadUserRepairs();
  } catch (error) {
    ElMessage.error('确认完结失败');
    console.error('确认完结失败，错误信息:', error);
  }
};

// 技能类型映射
const skillMap = {
  'WATER': '水暖',
  'ELECTRIC': '电路',
  'AC': '空调',
  'ELEVATOR': '电梯',
  'GAS': '燃气'
};

// 将技能对象数组转换为中文字符串
const formatSkills = (skills) => {
  if (!skills || skills.length === 0) return '无技能';
  return skills.map(skill => skill.name || skill).join('、');
};

// 初始化数据
onMounted(async () => {
  await loadUserHouses();
  if (user.type === 'property') {
    await loadTechnicians();
    await loadUserRepairs();
  } else if (hasCertifiedHouse.value) {
    await loadUserRepairs();
  }
});
</script>

<style scoped>
.repair-container {
  padding: 20px;
}

.apply-card {
  margin-bottom: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.list-card {
  margin-top: 20px;
}

.upload-demo {
  margin-bottom: 20px;
}

.repair-notices {
  margin-top: 20px;
  padding: 20px;
  background-color: #f5f7fa;
  border-radius: 4px;
}

.notice-content {
  padding: 10px;
}

.notice-title {
  font-weight: bold;
  margin-bottom: 5px;
}

.notice-text {
  margin-bottom: 5px;
  color: #606266;
}

.notice-publisher {
  font-size: 12px;
  color: #909399;
  margin-bottom: 5px;
}

.notice-link {
  font-size: 12px;
  color: #409eff;
  text-decoration: none;
}

.notice-link:hover {
  text-decoration: underline;
}

.rating-info {
  padding: 20px;
  background-color: #f0f9ff;
  border-radius: 4px;
  border: 1px solid #409eff;
}

/* 适老模式样式 */
.elder-mode :deep(.el-form-item__label) {
  width: 120px !important;
  font-size: 18px !important;
  font-weight: 600 !important;
  color: #000000 !important;
}

.elder-mode :deep(.el-form-item__content) {
  margin-left: 120px !important;
  font-size: 18px !important;
}

.elder-mode :deep(.el-input__inner) {
  font-size: 18px !important;
  min-height: 48px !important;
  border: 1px solid #000000 !important;
}

.elder-mode :deep(.el-textarea__inner) {
  font-size: 18px !important;
  min-height: 120px !important;
  border: 1px solid #000000 !important;
}

.elder-mode :deep(.el-select .el-input__inner) {
  font-size: 18px !important;
  min-height: 48px !important;
}

.elder-mode :deep(.el-button) {
  font-size: 18px !important;
  padding: 12px 24px !important;
}

.elder-mode :deep(.el-upload__tip) {
  font-size: 16px !important;
  color: #000000 !important;
}
</style>
