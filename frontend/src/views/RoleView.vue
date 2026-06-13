<template>
  <div class="role-container">
    <el-card>
      <template #header>
        <div class="card-header">
          <span>权限管理</span>
          <el-button type="primary" @click="handleAdd">新增角色</el-button>
        </div>
      </template>

      <el-table :data="roleList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="roleName" label="角色名称" width="150" />
        <el-table-column prop="roleCode" label="角色编码" width="150" />
        <el-table-column prop="description" label="描述" />
        <el-table-column label="权限列表" min-width="300">
          <template #default="scope">
            <el-tag 
              v-for="perm in scope.row.permissions" 
              :key="perm" 
              size="small" 
              style="margin-right: 5px; margin-bottom: 5px;"
            >
              {{ getPermissionText(perm) }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)">编辑</el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)" v-if="scope.row.id > 1">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 角色编辑对话框 -->
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑角色' : '新增角色'" width="500px">
      <el-form :model="roleForm" label-width="80px">
        <el-form-item label="角色名称">
          <el-input v-model="roleForm.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="角色编码">
          <el-input v-model="roleForm.roleCode" placeholder="请输入角色编码" :disabled="isEdit" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="roleForm.description" type="textarea" :rows="2" placeholder="请输入描述" />
        </el-form-item>
        <el-form-item label="权限">
          <el-checkbox-group v-model="roleForm.permissions">
            <el-checkbox value="user:read">用户查看</el-checkbox>
            <el-checkbox value="user:write">用户管理</el-checkbox>
            <el-checkbox value="repair:read">报修查看</el-checkbox>
            <el-checkbox value="repair:write">报修管理</el-checkbox>
            <el-checkbox value="complaint:read">投诉查看</el-checkbox>
            <el-checkbox value="complaint:write">投诉管理</el-checkbox>
            <el-checkbox value="notice:read">公告查看</el-checkbox>
            <el-checkbox value="notice:write">公告管理</el-checkbox>
            <el-checkbox value="activity:read">活动查看</el-checkbox>
            <el-checkbox value="activity:write">活动管理</el-checkbox>
            <el-checkbox value="express:read">快递查看</el-checkbox>
            <el-checkbox value="express:write">快递管理</el-checkbox>
            <el-checkbox value="health:read">健康查看</el-checkbox>
            <el-checkbox value="health:write">健康管理</el-checkbox>
            <el-checkbox value="risk:read">风险查看</el-checkbox>
            <el-checkbox value="risk:write">风险管理</el-checkbox>
            <el-checkbox value="system:read">系统查看</el-checkbox>
            <el-checkbox value="system:write">系统管理</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import { ElMessage, ElMessageBox } from 'element-plus';

const roleList = ref([]);
const dialogVisible = ref(false);
const isEdit = ref(false);
const currentRole = ref(null);

const roleForm = ref({
  roleName: '',
  roleCode: '',
  description: '',
  permissions: []
});

// 默认角色列表（模拟数据）
const defaultRoles = [
  {
    id: 1,
    roleName: '系统管理员',
    roleCode: 'ADMIN',
    description: '系统最高权限，管理所有功能',
    permissions: ['user:read', 'user:write', 'repair:read', 'repair:write', 'complaint:read', 'complaint:write', 'notice:read', 'notice:write', 'activity:read', 'activity:write', 'express:read', 'express:write', 'health:read', 'health:write', 'risk:read', 'risk:write', 'system:read', 'system:write']
  },
  {
    id: 2,
    roleName: '物业管理员',
    roleCode: 'PROPERTY',
    description: '物业管理人员，管理社区事务',
    permissions: ['repair:read', 'repair:write', 'complaint:read', 'complaint:write', 'notice:read', 'notice:write', 'activity:read', 'activity:write', 'express:read', 'express:write', 'risk:read', 'risk:write']
  },
  {
    id: 3,
    roleName: '社区医生',
    roleCode: 'DOCTOR',
    description: '社区医疗服务人员',
    permissions: ['health:read', 'health:write', 'notice:read', 'repair:read']
  },
  {
    id: 4,
    roleName: '社区居民',
    roleCode: 'RESIDENT',
    description: '社区居民用户',
    permissions: ['repair:read', 'repair:write', 'complaint:read', 'complaint:write', 'notice:read', 'activity:read', 'activity:write', 'express:read', 'express:write', 'health:read']
  },
  {
    id: 5,
    roleName: '维修师傅',
    roleCode: 'TECHNICIAN',
    description: '社区维修服务人员',
    permissions: ['repair:read', 'repair:write', 'notice:read']
  }
];

const loadRoles = () => {
  // 模拟从后端加载数据
  roleList.value = defaultRoles;
};

const handleAdd = () => {
  isEdit.value = false;
  currentRole.value = null;
  roleForm.value = {
    roleName: '',
    roleCode: '',
    description: '',
    permissions: []
  };
  dialogVisible.value = true;
};

const handleEdit = (row) => {
  isEdit.value = true;
  currentRole.value = row;
  roleForm.value = {
    roleName: row.roleName,
    roleCode: row.roleCode,
    description: row.description,
    permissions: [...row.permissions]
  };
  dialogVisible.value = true;
};

const handleDelete = (row) => {
  ElMessageBox.confirm(`确定要删除角色 "${row.roleName}" 吗？`, '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    roleList.value = roleList.value.filter(r => r.id !== row.id);
    ElMessage.success('删除成功');
  }).catch(() => {});
};

const handleSubmit = () => {
  if (!roleForm.value.roleName || !roleForm.value.roleCode) {
    ElMessage.warning('请填写角色名称和编码');
    return;
  }

  if (isEdit.value && currentRole.value) {
    // 编辑
    const index = roleList.value.findIndex(r => r.id === currentRole.value.id);
    if (index !== -1) {
      roleList.value[index] = {
        ...roleList.value[index],
        roleName: roleForm.value.roleName,
        description: roleForm.value.description,
        permissions: [...roleForm.value.permissions]
      };
    }
    ElMessage.success('更新成功');
  } else {
    // 新增
    const newRole = {
      id: Math.max(...roleList.value.map(r => r.id), 0) + 1,
      roleName: roleForm.value.roleName,
      roleCode: roleForm.value.roleCode,
      description: roleForm.value.description,
      permissions: [...roleForm.value.permissions]
    };
    roleList.value.push(newRole);
    ElMessage.success('新增成功');
  }

  dialogVisible.value = false;
};

// 权限名称映射
const permissionMap = {
  'user:read': '用户查看',
  'user:write': '用户管理',
  'repair:read': '报修查看',
  'repair:write': '报修管理',
  'complaint:read': '投诉查看',
  'complaint:write': '投诉管理',
  'notice:read': '公告查看',
  'notice:write': '公告管理',
  'activity:read': '活动查看',
  'activity:write': '活动管理',
  'express:read': '快递查看',
  'express:write': '快递管理',
  'health:read': '健康查看',
  'health:write': '健康管理',
  'risk:read': '风险查看',
  'risk:write': '风险管理',
  'system:read': '系统查看',
  'system:write': '系统管理'
};

const getPermissionText = (perm) => {
  return permissionMap[perm] || perm;
};

onMounted(() => {
  loadRoles();
});
</script>

<style scoped>
.role-container {
  padding: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
