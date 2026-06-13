<template>
  <div class="house-container">
    <!-- 居民界面 (ROLE_RESIDENT) -->
    <template v-if="userRole === 'resident'">
      <h2>我的房产</h2>
      
      <!-- 房产认证申请表单 -->
      <el-card class="apply-card">
        <template #header>
          <div class="card-header">
            <span>提交房产认证申请</span>
          </div>
        </template>
        <el-form :model="applyForm" :rules="rules" ref="applyFormRef" label-width="80px">
          <el-form-item label="楼栋" prop="buildingId">
            <el-select v-model="applyForm.buildingId" placeholder="请选择楼栋" @change="handleBuildingChange">
              <el-option
                v-for="building in buildingList"
                :key="building.id"
                :label="building.name"
                :value="building.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="房号" prop="houseId">
            <el-select v-model="applyForm.houseId" placeholder="请选择房号">
              <el-option
                v-for="house in houseList"
                :key="house.id"
                :label="house.houseNo"
                :value="house.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="关系" prop="relation">
            <el-select v-model="applyForm.relation" placeholder="请选择与房产的关系">
              <el-option label="业主" value="owner" />
              <el-option label="租户" value="tenant" />
              <el-option label="家属" value="family" />
            </el-select>
          </el-form-item>
          <el-form-item label="合同文件">
            <el-upload
              class="contract-uploader"
              action="/api/file/upload/contract"
              :headers="uploadHeaders"
              :show-file-list="true"
              :on-success="handleContractUpload"
              :on-error="handleContractUploadError"
              :before-upload="beforeContractUpload"
              accept="image/jpeg,image/png,image/jpg,application/pdf"
              :limit="1"
            >
              <el-button type="primary" size="small">
                <el-icon><Upload /></el-icon> 选择合同文件
              </el-button>
              <template #tip>
                <div class="el-upload__tip">
                  支持 JPG、PNG、PDF 格式，文件大小不超过 5MB
                </div>
              </template>
            </el-upload>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleApply" :loading="submitting">提交申请</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 我的房产列表 -->
      <el-card class="list-card" style="margin-top: 20px;">
        <template #header>
          <div class="card-header">
            <span>我的房产列表</span>
          </div>
        </template>
        <el-table :data="userHouseList" style="width: 100%">
          <el-table-column label="楼栋" width="180">
            <template #default="scope">
              {{ buildingList.find(b => b.id === scope.row.houseId)?.name || '' }}
            </template>
          </el-table-column>
          <el-table-column label="房号" width="180">
            <template #default="scope">
              {{ houseList.find(h => h.id === scope.row.houseId)?.houseNo || '' }}
            </template>
          </el-table-column>
          <el-table-column label="关系" width="180">
            <template #default="scope">
              {{ getRelationText(scope.row.relation) }}
            </template>
          </el-table-column>
          <el-table-column prop="certified" label="认证状态" width="180">
            <template #default="scope">
              <el-tag :type="scope.row.certified === 1 ? 'success' : 'warning'">
                {{ scope.row.certified === 1 ? '已认证' : '待审核' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="申请时间">
            <template #default="scope">
              {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </template>
    
    <!-- 物业和管理员界面 (ROLE_PROPERTY / ROLE_ADMIN) -->
    <template v-else-if="userRole === 'property' || userRole === 'admin'">
      <h2>社区房产档案管理</h2>
      
      <!-- 统计卡片 -->
      <el-row :gutter="20" style="margin-bottom: 20px;">
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>总房屋数</span>
              </div>
            </template>
            <div class="statistic-value">{{ statistics.totalHouses }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>已认证数</span>
              </div>
            </template>
            <div class="statistic-value">{{ statistics.certifiedHouses }}</div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <template #header>
              <div class="card-header">
                <span>空置数</span>
              </div>
            </template>
            <div class="statistic-value">{{ statistics.vacantHouses }}</div>
          </el-card>
        </el-col>
      </el-row>
      
      <!-- 搜索栏 -->
      <el-card class="search-card" style="margin-bottom: 20px;">
        <template #header>
          <div class="card-header">
            <span>房产搜索</span>
          </div>
        </template>
        <el-form :inline="true" :model="searchForm" class="search-form">
          <el-form-item label="楼栋">
            <el-select v-model="searchForm.buildingId" placeholder="请选择楼栋" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option
                v-for="building in buildingList"
                :key="building.id"
                :label="building.name"
                :value="building.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="房号">
            <el-input v-model="searchForm.houseNo" placeholder="请输入房号" style="width: 120px;" />
          </el-form-item>
          <el-form-item label="业主姓名">
            <el-input v-model="searchForm.ownerName" placeholder="请输入业主姓名" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleSearch">查询</el-button>
            <el-button @click="resetSearch">重置</el-button>
          </el-form-item>
        </el-form>
      </el-card>
      
      <!-- 管理操作按钮 -->
      <div class="action-buttons" style="margin-bottom: 20px;">
        <el-button type="primary" @click="openAddDialog">新增房产</el-button>
      </div>
      
      <!-- 房产底账表格 -->
      <el-card class="list-card">
        <template #header>
          <div class="card-header">
            <span>全社区房产概览</span>
          </div>
        </template>
        <el-table :data="communityHouseList" style="width: 100%">
          <el-table-column label="楼栋名称" width="180">
            <template #default="scope">
              {{ buildingList.find(b => b.id === scope.row.buildingId)?.name || '' }}
            </template>
          </el-table-column>
          <el-table-column label="房号" width="180">
            <template #default="scope">
              {{ scope.row.houseNo }}
            </template>
          </el-table-column>
          <el-table-column label="业主姓名" width="180">
            <template #default="scope">
              {{ scope.row.ownerName || '无' }}
            </template>
          </el-table-column>
          <el-table-column label="联系电话" width="180">
            <template #default="scope">
              {{ scope.row.ownerPhone || '无' }}
            </template>
          </el-table-column>
          <el-table-column label="认证状态" width="180">
            <template #default="scope">
              <el-tag :type="scope.row.certified === 1 ? 'success' : 'warning'">
                {{ scope.row.certified === 1 ? '已认证' : '未认证' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="房屋状态" width="180">
            <template #default="scope">
              <el-tag :type="scope.row.status === 1 ? 'success' : 'info'">
                {{ scope.row.status === 1 ? '已入住' : '空置' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="200">
            <template #default="scope">
              <el-button type="primary" size="small" @click="openEditDialog(scope.row)">编辑</el-button>
              <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
      
      <!-- 新增/编辑房产弹窗 -->
      <el-dialog
        v-model="dialogVisible"
        :title="dialogType === 'add' ? '新增房产' : '编辑房产'"
        width="500px"
      >
        <el-form :model="dialogForm" :rules="dialogRules" ref="dialogFormRef" label-width="80px">
          <el-form-item label="楼栋" prop="buildingId">
            <el-select v-model="dialogForm.buildingId" placeholder="请选择楼栋">
              <el-option
                v-for="building in buildingList"
                :key="building.id"
                :label="building.name"
                :value="building.id"
              />
            </el-select>
          </el-form-item>
          <el-form-item label="房号" prop="houseNo">
            <el-input v-model="dialogForm.houseNo" placeholder="请输入房号" />
          </el-form-item>
          <el-form-item label="业主姓名" prop="ownerName">
            <el-input v-model="dialogForm.ownerName" placeholder="请输入业主姓名" />
          </el-form-item>
          <el-form-item label="联系电话" prop="ownerPhone">
            <el-input v-model="dialogForm.ownerPhone" placeholder="请输入联系电话" />
          </el-form-item>
          <el-form-item label="房屋状态" prop="status">
            <el-select v-model="dialogForm.status" placeholder="请选择房屋状态">
              <el-option label="已入住" value="1" />
              <el-option label="空置" value="0" />
            </el-select>
          </el-form-item>
        </el-form>
        <template #footer>
          <span class="dialog-footer">
            <el-button @click="dialogVisible = false">取消</el-button>
            <el-button type="primary" @click="handleDialogSubmit">确定</el-button>
          </span>
        </template>
      </el-dialog>
    </template>
    
    <!-- 其他角色 -->
    <template v-else>
      <el-empty description="无权限访问此页面" />
    </template>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';
import { Upload } from '@element-plus/icons-vue';

const user = JSON.parse(localStorage.getItem('user') || '{}');
const userRole = ref('');

const applyForm = ref({
  buildingId: '',
  houseId: '',
  relation: ''
});

const rules = {
  buildingId: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  houseId: [
    { required: true, message: '请选择房号', trigger: 'change' }
  ],
  relation: [
    { required: true, message: '请选择与房产的关系', trigger: 'change' }
  ]
};

const applyFormRef = ref(null);
const buildingList = ref([]);
const houseList = ref([]);
const userHouseList = ref([]);
const submitting = ref(false);
const contractImageUrl = ref('');

const uploadHeaders = {
  Authorization: `Bearer ${localStorage.getItem('token') || ''}`
};

const statistics = ref({
  totalHouses: 0,
  certifiedHouses: 0,
  vacantHouses: 0
});

const searchForm = ref({
  buildingId: '',
  houseNo: '',
  ownerName: ''
});

const communityHouseList = ref([]);
const dialogVisible = ref(false);
const dialogType = ref('add');
const dialogForm = ref({
  buildingId: '',
  houseNo: '',
  ownerName: '',
  ownerPhone: '',
  status: '1'
});

const dialogFormRef = ref(null);
const dialogRules = {
  buildingId: [
    { required: true, message: '请选择楼栋', trigger: 'change' }
  ],
  houseNo: [
    { required: true, message: '请输入房号', trigger: 'blur' }
  ],
  ownerName: [
    {
      validator: (rule, value, callback) => {
        if (dialogForm.value.status === '1' && !value) {
          callback(new Error('请输入业主姓名'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  ownerPhone: [
    {
      validator: (rule, value, callback) => {
        if (dialogForm.value.status === '1' && !value) {
          callback(new Error('请输入联系电话'));
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ],
  status: [
    { required: true, message: '请选择房屋状态', trigger: 'change' }
  ]
};

// 获取用户角色
const getUserRole = () => {
  const userInfo = localStorage.getItem('user');
  if (userInfo) {
    const user = JSON.parse(userInfo);
    return user.type;
  }
  return '';
};

// 关系类型转换
const getRelationText = (relation) => {
  const map = {
    'owner': '业主',
    'tenant': '租户',
    'family': '家属'
  };
  return map[relation] || relation;
};

// 加载楼栋列表
const loadBuildings = async () => {
  try {
    console.log('开始加载楼栋列表');
    const response = await axios.get('/api/resident-house/buildings');
    buildingList.value = response.data;
    console.log('加载楼栋列表成功，响应数据:', buildingList.value);
  } catch (error) {
    ElMessage.error('加载楼栋列表失败');
    console.error('加载楼栋列表失败，错误信息:', error);
  }
};

// 加载所有房屋列表
const loadAllHouses = async () => {
  try {
    console.log('开始加载所有房屋列表');
    const response = await axios.get('/api/resident-house/houses/all');
    houseList.value = response.data;
    console.log('加载所有房屋列表成功，响应数据:', houseList.value);
  } catch (error) {
    ElMessage.error('加载房屋列表失败');
    console.error('加载房屋列表失败，错误信息:', error);
  }
};

// 根据楼栋加载房号列表
const loadHouses = async (buildingId) => {
  if (!buildingId) {
    houseList.value = [];
    return;
  }
  try {
    console.log('开始加载房号列表，buildingId:', buildingId);
    const response = await axios.get(`/api/resident-house/houses/${buildingId}`);
    houseList.value = response.data;
    console.log('加载房号列表成功，响应数据:', houseList.value);
  } catch (error) {
    ElMessage.error('加载房号列表失败');
    console.error('加载房号列表失败，错误信息:', error);
  }
};

// 加载用户的房产列表
const loadUserHouses = async () => {
  if (!user.userId) return;
  try {
    console.log('开始加载用户房产列表，userId:', user.userId);
    const response = await axios.get(`/api/resident-house/user/${user.userId}`);
    userHouseList.value = response.data;
    console.log('加载用户房产列表成功，响应数据:', userHouseList.value);
  } catch (error) {
    ElMessage.error('加载用户房产列表失败');
    console.error('加载用户房产列表失败，错误信息:', error);
  }
};

// 加载社区房产列表（物业端）
const loadCommunityHouses = async () => {
  try {
    console.log('开始加载社区房产列表');
    const response = await axios.get('/api/resident-house/all');
    communityHouseList.value = response.data;
    console.log('加载社区房产列表成功，响应数据:', communityHouseList.value);
    
    // 更新统计数据
    updateStatistics();
  } catch (error) {
    ElMessage.error('加载社区房产列表失败');
    console.error('加载社区房产列表失败，错误信息:', error);
  }
};

// 更新统计数据
const updateStatistics = () => {
  const total = communityHouseList.value.length;
  const certified = communityHouseList.value.filter(item => item.certified === 1).length;
  // 空置房屋：status=0 或 无业主信息
  const vacant = communityHouseList.value.filter(item => item.status === 0 || !item.ownerName || item.ownerName === '未知').length;
  
  statistics.value = {
    totalHouses: total,
    certifiedHouses: certified,
    vacantHouses: vacant
  };
};

// 监听楼栋选择变化，加载对应房号
const handleBuildingChange = (buildingId) => {
  loadHouses(buildingId);
};

const beforeContractUpload = (file) => {
  const isImage = file.type === 'image/jpeg' || file.type === 'image/png' || file.type === 'image/jpg';
  const isPdf = file.type === 'application/pdf';
  if (!isImage && !isPdf) {
    ElMessage.error('只能上传 JPG、PNG 或 PDF 格式的文件!');
    return false;
  }
  const isLt5M = file.size / 1024 / 1024 < 5;
  if (!isLt5M) {
    ElMessage.error('文件大小不能超过 5MB!');
    return false;
  }
  return true;
};

const handleContractUpload = (response) => {
  if (response.code === 200) {
    contractImageUrl.value = response.data;
    ElMessage.success('合同文件上传成功');
  } else {
    ElMessage.error(response.message || '上传失败');
  }
};

const handleContractUploadError = () => {
  ElMessage.error('合同文件上传失败，请重试');
};

// 提交认证申请
const handleApply = async () => {
  await applyFormRef.value.validate(async (valid) => {
    if (valid) {
      submitting.value = true;
      try {
        const submitData = {
          buildingId: applyForm.value.buildingId,
          houseId: applyForm.value.houseId,
          relation: applyForm.value.relation,
          contractImageUrl: contractImageUrl.value || ''
        };
        console.log('提交房产认证申请，请求数据:', submitData);
        await axios.post('/api/resident-house/apply', submitData);
        ElMessage.success('提交认证申请成功');
        applyFormRef.value.resetFields();
        contractImageUrl.value = '';
        await loadUserHouses();
      } catch (error) {
        console.error('提交认证申请失败，错误信息:', error);
        if (error.response && error.response.status === 403) {
          ElMessage.error('权限不足，请确保您已登录');
        } else if (error.response && error.response.status === 401) {
          ElMessage.error('未登录，请先登录');
        } else {
          ElMessage.error('提交认证申请失败，请重试');
        }
      } finally {
        submitting.value = false;
      }
    }
  });
};

// 处理搜索（物业端）
const handleSearch = async () => {
  try {
    console.log('开始搜索房产，搜索条件:', searchForm.value);
    // 这里可以根据搜索条件调用后端接口
    // 暂时使用本地过滤
    let filteredList = [...communityHouseList.value];
    
    if (searchForm.value.buildingId) {
      filteredList = filteredList.filter(item => item.buildingId === searchForm.value.buildingId);
    }
    
    if (searchForm.value.houseNo) {
      filteredList = filteredList.filter(item => item.houseNo.includes(searchForm.value.houseNo));
    }
    
    if (searchForm.value.ownerName) {
      filteredList = filteredList.filter(item => item.ownerName && item.ownerName.includes(searchForm.value.ownerName));
    }
    
    communityHouseList.value = filteredList;
    console.log('搜索完成，结果数量:', filteredList.length);
  } catch (error) {
    ElMessage.error('搜索失败');
    console.error('搜索失败，错误信息:', error);
  }
};

// 重置搜索（物业端）
const resetSearch = () => {
  searchForm.value = {
    buildingId: '',
    houseNo: '',
    ownerName: ''
  };
  loadCommunityHouses();
};

// 打开新增弹窗（物业端）
const openAddDialog = () => {
  dialogType.value = 'add';
  dialogForm.value = {
    buildingId: '',
    houseNo: '',
    ownerName: '',
    ownerPhone: '',
    status: '1'
  };
  dialogVisible.value = true;
};

// 打开编辑弹窗（物业端）
const openEditDialog = (row) => {
  dialogType.value = 'edit';
  dialogForm.value = {
    id: row.id,
    buildingId: row.buildingId,
    houseNo: row.houseNo,
    ownerName: row.ownerName || '',
    ownerPhone: row.ownerPhone || '',
    status: row.status.toString()
  };
  dialogVisible.value = true;
};

// 处理弹窗提交（物业端）
const handleDialogSubmit = async () => {
  await dialogFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        console.log('提交房产信息，请求数据:', dialogForm.value);
        
        if (dialogType.value === 'add') {
          // 新增房产
          await axios.post('/api/resident-house', dialogForm.value);
          ElMessage.success('新增房产成功');
        } else {
          // 编辑房产
          await axios.put(`/api/resident-house/${dialogForm.value.id}`, dialogForm.value);
          ElMessage.success('编辑房产成功');
        }
        
        dialogVisible.value = false;
        await loadCommunityHouses();
      } catch (error) {
        ElMessage.error('提交失败，请重试');
        console.error('提交失败，错误信息:', error);
      }
    }
  });
};

// 处理删除（物业端）
const handleDelete = async (id) => {
  ElMessageBox.confirm('确定要删除这个房产吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(async () => {
    try {
      console.log('删除房产，id:', id);
      await axios.delete(`/api/resident-house/${id}`);
      ElMessage.success('删除房产成功');
      await loadCommunityHouses();
    } catch (error) {
      ElMessage.error('删除失败，请重试');
      console.error('删除失败，错误信息:', error);
    }
  }).catch(() => {
    // 取消操作
  });
};

// 初始化数据
onMounted(async () => {
  console.log('开始初始化数据');
  console.log('用户信息:', user);
  
  // 获取用户角色
  userRole.value = getUserRole();
  console.log('用户角色:', userRole.value);
  
  await loadBuildings();
  
  if (userRole.value === 'resident') {
    // 居民端
    await loadAllHouses();
    await loadUserHouses();
  } else if (userRole.value === 'property' || userRole.value === 'admin') {
    // 物业和管理员端
    await loadCommunityHouses();
  }
  
  console.log('初始化数据完成');
});
</script>

<style scoped>
.house-container {
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

.search-card {
  margin-bottom: 20px;
}

.statistic-value {
  font-size: 32px;
  font-weight: bold;
  color: #409EFF;
  text-align: center;
  margin-top: 10px;
}

.action-buttons {
  margin-bottom: 20px;
}

.search-form {
  display: flex;
  align-items: center;
  gap: 10px;
}

.dialog-footer {
  width: 100%;
  display: flex;
  justify-content: flex-end;
}

.contract-uploader {
  display: flex;
  flex-direction: column;
}

.el-upload__tip {
  margin-top: 8px;
  color: #909399;
  font-size: 12px;
}
</style>
