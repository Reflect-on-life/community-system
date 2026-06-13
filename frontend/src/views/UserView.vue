<template>
  <div class="user-container">
    <h2>用户管理</h2>
    
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="用户类型">
            <el-select v-model="filterForm.type" placeholder="请选择用户类型" style="width: 120px;">
              <el-option label="全部" value="" />
              <el-option label="居民" value="resident" />
              <el-option label="物业" value="property" />
              <el-option label="医生" value="doctor" />
              <el-option label="管理员" value="admin" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="handleAdd">新增用户</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="userList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="name" label="姓名" width="180" />
        <el-table-column prop="phone" label="手机号" width="180" />
        <el-table-column prop="type" label="用户类型" width="120">
          <template #default="scope">
            <el-tag :type="getTypeTagType(scope.row.type)">{{ getTypeLabel(scope.row.type) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="status" label="状态" width="120">
          <template #default="scope">
            <el-switch 
              v-model="scope.row.status" 
              active-text="启用" 
              inactive-text="禁用"
              @change="handleStatusChange(scope.row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="创建时间" width="200">
          <template #default="scope">
            {{ new Date(scope.row.createdAt).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row.id)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      
      <!-- 分页 -->
      <div class="pagination-container">
        <el-pagination
          v-model:current-page="pagination.current"
          v-model:page-size="pagination.size"
          :page-sizes="[10, 20, 50, 100]"
          layout="total, sizes, prev, pager, next, jumper"
          :total="pagination.total"
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
        />
      </div>
    </el-card>
    
    <!-- 新增/编辑对话框 -->
    <el-dialog
      v-model="dialogVisible"
      :title="dialogTitle"
      width="500px"
    >
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="姓名" prop="name">
          <el-input v-model="form.name" placeholder="请输入姓名" />
        </el-form-item>
        <el-form-item label="手机号" prop="phone">
          <div class="input-with-counter">
            <el-input v-model="form.phone" placeholder="请输入手机号" />
            <span class="input-counter">{{ form.phone.length }}/11</span>
          </div>
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <div class="input-with-counter">
            <el-input v-model="form.password" type="password" placeholder="请输入密码" />
            <span class="input-counter">{{ form.password.length }}/6-20</span>
          </div>
        </el-form-item>
        <el-form-item label="用户类型" prop="type">
          <el-select v-model="form.type" placeholder="请选择用户类型">
            <el-option label="居民" value="resident" />
            <el-option label="物业" value="property" />
            <el-option label="医生" value="doctor" />
            <el-option label="管理员" value="admin" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-switch v-model="form.status" active-text="启用" inactive-text="禁用" />
        </el-form-item>
        <el-form-item label="性别">
          <el-select v-model="form.gender" placeholder="请选择性别">
            <el-option label="男" value="1" />
            <el-option label="女" value="0" />
          </el-select>
        </el-form-item>
        <el-form-item label="出生日期">
          <el-date-picker v-model="form.birthday" type="date" placeholder="选择日期" />
        </el-form-item>
        <el-form-item label="身份证号" prop="idCard">
          <div class="input-with-counter">
            <el-input v-model="form.idCard" placeholder="请输入身份证号" />
            <span class="input-counter">{{ form.idCard.length }}/18</span>
          </div>
        </el-form-item>
        <el-form-item label="标签">
          <el-input v-model="form.tags" placeholder="请输入标签，多个用逗号分隔" />
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="dialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleSubmit">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed } from 'vue';
import axios from 'axios';
import { ElMessage, ElMessageBox } from 'element-plus';

// 筛选表单
const filterForm = reactive({
  type: ''
});

// 分页数据
const pagination = reactive({
  current: 1,
  size: 10,
  total: 0
});

// 用户列表
const userList = ref([]);

// 对话框状态
const dialogVisible = ref(false);
const dialogTitle = ref('');

// 表单引用
const formRef = ref(null);

// 表单数据
const form = reactive({
  id: '',
  name: '',
  phone: '',
  password: '',
  type: 'resident',
  status: 1,
  gender: '',
  birthday: '',
  idCard: '',
  tags: ''
});

// 表单验证规则
const rules = {
  name: [
    { required: true, message: '请输入姓名', trigger: 'blur' },
    { min: 2, max: 50, message: '姓名长度在2-50个字符之间', trigger: 'blur' }
  ],
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '请输入正确的手机号格式', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度在6-20个字符之间', trigger: 'blur' }
  ],
  type: [
    { required: true, message: '请选择用户类型', trigger: 'change' }
  ],
  idCard: [
    {
      validator: (rule, value, callback) => {
        if (value) {
          const idCardRegex = /^[1-9]\d{5}(18|19|20)\d{2}(0[1-9]|1[0-2])(0[1-9]|[12]\d|3[01])\d{3}[\dXx]$/;
          if (!idCardRegex.test(value)) {
            callback(new Error('请输入正确的身份证号格式'));
          } else {
            callback();
          }
        } else {
          callback();
        }
      },
      trigger: 'blur'
    }
  ]
};

// 用户类型标签颜色
const getTypeTagType = (type) => {
  const typeMap = {
    resident: 'primary',
    property: 'success',
    doctor: 'warning',
    admin: 'danger'
  };
  return typeMap[type] || 'info';
};

// 用户类型显示文本
const getTypeLabel = (type) => {
  const typeMap = {
    resident: '居民',
    property: '物业',
    doctor: '医生',
    admin: '管理员'
  };
  return typeMap[type] || type;
};

// 加载用户列表
const loadUserList = async () => {
  try {
    console.log('开始加载用户列表');
    console.log('请求参数:', {
      page: pagination.current,
      size: pagination.size,
      type: filterForm.type
    });
    const url = '/api/admin/users/page';
    console.log('请求URL:', url);
    
    const response = await axios.get(url, {
      params: {
        page: pagination.current,
        size: pagination.size,
        type: filterForm.type
      }
    });
    
    console.log('请求成功，响应数据:', response.data);
    // 转换status为布尔值
    userList.value = response.data.records.map(user => ({
      ...user,
      status: !!user.status
    }));
    pagination.total = response.data.total;
  } catch (error) {
    console.error('加载用户列表失败:', error);
    console.error('错误详情:', {
      status: error.response?.status,
      statusText: error.response?.statusText,
      url: error.config?.url,
      method: error.config?.method
    });
    ElMessage.error('加载用户列表失败');
  }
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  loadUserList();
};

// 新增用户
const handleAdd = () => {
  dialogTitle.value = '新增用户';
  // 重置表单
  Object.assign(form, {
    id: '',
    name: '',
    phone: '',
    password: '',
    type: 'resident',
    status: 1,
    gender: '',
    birthday: '',
    idCard: '',
    tags: ''
  });
  dialogVisible.value = true;
};

// 编辑用户
const handleEdit = (user) => {
  dialogTitle.value = '编辑用户';
  // 填充表单
  Object.assign(form, {
    id: user.id,
    name: user.name,
    phone: user.phone,
    type: user.type,
    status: user.status,
    gender: user.gender,
    birthday: user.birthday,
    idCard: user.idCard,
    tags: user.tags
  });
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  try {
    // 表单验证
    await formRef.value.validate();
    
    const formData = {
      ...form,
      status: form.status ? 1 : 0
    };
    
    if (form.id) {
      // 编辑
      await axios.put(`/api/admin/users/${form.id}`, formData);
      ElMessage.success('编辑用户成功');
    } else {
      // 新增
      await axios.post('/api/admin/users', formData);
      ElMessage.success('新增用户成功');
    }
    dialogVisible.value = false;
    loadUserList();
  } catch (error) {
    if (error.name === 'Error') {
      // 表单验证失败
      return;
    }
    ElMessage.error('操作失败');
    console.error(error);
  }
};

// 删除用户
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个用户吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    });
    
    await axios.delete(`/api/admin/users/${id}`);
    ElMessage.success('删除用户成功');
    loadUserList();
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

// 状态变更
const handleStatusChange = async (user) => {
  try {
    await axios.put(`/api/admin/users/${user.id}`, {
      id: user.id,
      status: user.status ? 1 : 0
    });
    ElMessage.success('状态更新成功');
  } catch (error) {
    // 恢复原状态
    user.status = !user.status;
    ElMessage.error('状态更新失败');
    console.error(error);
  }
};

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.size = size;
  loadUserList();
};

// 当前页变更
const handleCurrentChange = (current) => {
  pagination.current = current;
  loadUserList();
};

// 初始化加载
onMounted(() => {
  loadUserList();
});
</script>

<style scoped>
.user-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
  justify-content: flex-start;
  align-items: center;
}

.table-card {
  margin-bottom: 20px;
}

.pagination-container {
  margin-top: 20px;
  display: flex;
  justify-content: flex-end;
}

.dialog-footer {
  text-align: right;
}

.input-with-counter {
  position: relative;
  width: 100%;
}

.input-with-counter .el-input__wrapper {
  padding-right: 60px !important;
}

.input-counter {
  position: absolute;
  right: 10px;
  top: 50%;
  transform: translateY(-50%);
  font-size: 12px;
  color: #909399;
  pointer-events: none;
  background: transparent;
  padding: 0;
  border-radius: 0;
}
</style>
