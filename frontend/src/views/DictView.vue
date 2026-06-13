<template>
  <div class="dict-container">
    <h2>数据字典管理</h2>
    
    <!-- 筛选区域 -->
    <el-card class="filter-card">
      <div class="filter-form">
        <el-form :inline="true" :model="filterForm" class="demo-form-inline">
          <el-form-item label="字典类型">
            <el-input v-model="filterForm.type" placeholder="请输入字典类型" style="width: 200px;" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="handleQuery">查询</el-button>
          </el-form-item>
          <el-form-item>
            <el-button type="success" @click="handleAdd">新增</el-button>
          </el-form-item>
        </el-form>
      </div>
    </el-card>
    
    <!-- 表格区域 -->
    <el-card class="table-card">
      <el-table :data="dictList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80" />
        <el-table-column prop="type" label="字典类型" width="150" />
        <el-table-column prop="code" label="字典编码" width="150" />
        <el-table-column prop="value" label="字典值" width="150" />
        <el-table-column prop="description" label="描述" min-width="300" />
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
      <el-form :model="form" label-width="80px">
        <el-form-item label="字典类型">
          <el-input v-model="form.type" placeholder="请输入字典类型" />
        </el-form-item>
        <el-form-item label="字典编码">
          <el-input v-model="form.code" placeholder="请输入字典编码" />
        </el-form-item>
        <el-form-item label="字典值">
          <el-input v-model="form.value" placeholder="请输入字典值" />
        </el-form-item>
        <el-form-item label="描述">
          <el-input v-model="form.description" placeholder="请输入描述" type="textarea" />
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
import { ref, reactive, onMounted } from 'vue';
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

// 字典列表
const dictList = ref([]);

// 对话框状态
const dialogVisible = ref(false);
const dialogTitle = ref('');

// 表单数据
const form = reactive({
  id: '',
  type: '',
  code: '',
  value: '',
  description: ''
});

// 加载字典列表
const loadDictList = async () => {
  try {
    const params = {
      page: pagination.current,
      size: pagination.size
    };
    
    // 添加筛选参数
    if (filterForm.type) {
      params.type = filterForm.type;
    }
    
    const response = await axios.get('/api/admin/dict/page', {
      params
    });
    
    dictList.value = response.data.records;
    pagination.total = response.data.total;
  } catch (error) {
    ElMessage.error('加载字典列表失败');
    console.error(error);
  }
};

// 查询
const handleQuery = () => {
  pagination.current = 1;
  loadDictList();
};

// 新增字典
const handleAdd = () => {
  dialogTitle.value = '新增字典';
  // 重置表单
  Object.assign(form, {
    id: '',
    type: '',
    code: '',
    value: '',
    description: ''
  });
  dialogVisible.value = true;
};

// 编辑字典
const handleEdit = (dict) => {
  dialogTitle.value = '编辑字典';
  // 填充表单
  Object.assign(form, {
    id: dict.id,
    type: dict.type,
    code: dict.code,
    value: dict.value,
    description: dict.description
  });
  dialogVisible.value = true;
};

// 提交表单
const handleSubmit = async () => {
  try {
    if (form.id) {
      // 编辑
      await axios.put(`/api/admin/dict/${form.id}`, form);
      ElMessage.success('编辑字典成功');
    } else {
      // 新增
      await axios.post('/api/admin/dict', form);
      ElMessage.success('新增字典成功');
    }
    dialogVisible.value = false;
    loadDictList();
  } catch (error) {
    ElMessage.error('操作失败');
    console.error(error);
  }
};

// 删除字典
const handleDelete = async (id) => {
  try {
    await ElMessageBox.confirm('确定要删除这个字典项吗？', '删除确认', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'danger'
    });
    
    await axios.delete(`/api/admin/dict/${id}`);
    ElMessage.success('删除字典成功');
    loadDictList();
  } catch (error) {
    if (error.message !== 'cancel') {
      ElMessage.error('删除失败');
      console.error(error);
    }
  }
};

// 分页大小变更
const handleSizeChange = (size) => {
  pagination.size = size;
  loadDictList();
};

// 当前页变更
const handleCurrentChange = (current) => {
  pagination.current = current;
  loadDictList();
};

// 初始化加载
onMounted(() => {
  loadDictList();
});
</script>

<style scoped>
.dict-container {
  padding: 20px;
}

.filter-card {
  margin-bottom: 20px;
}

.filter-form {
  display: flex;
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
</style>