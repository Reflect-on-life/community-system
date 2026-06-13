<template>
  <div class="notice-container">
    <h2>公告发布</h2>
    
    <!-- 公告发布表单 -->
    <el-card class="publish-card">
      <template #header>
        <div class="card-header">
          <span>发布新公告</span>
        </div>
      </template>
      <el-form :model="noticeForm" :rules="rules" ref="noticeFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="noticeForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="noticeForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="发布人" prop="publisher">
          <el-input v-model="noticeForm.publisher" placeholder="请输入发布人" />
        </el-form-item>
        <el-form-item label="推送对象" prop="targetType">
          <el-checkbox-group v-model="noticeForm.targetType">
            <el-checkbox label="all">全体用户</el-checkbox>
            <el-checkbox label="resident">居民</el-checkbox>
            <el-checkbox label="doctor">医生</el-checkbox>
            <el-checkbox label="property">物业</el-checkbox>
            <el-checkbox label="admin">管理员</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handlePublish">发布公告</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    
    <!-- 公告列表 -->
    <el-card class="list-card" style="margin-top: 20px;">
      <template #header>
        <div class="card-header">
          <span>公告列表</span>
        </div>
      </template>
      <el-table :data="noticeList" style="width: 100%">
        <el-table-column prop="title" label="标题" width="200" />
        <el-table-column prop="content" label="内容" />
        <el-table-column prop="publisher" label="发布人" width="150" />
        <el-table-column label="发布时间" width="200">
          <template #default="scope">
            {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="scope">
            <el-button type="primary" size="small" @click="handleEdit(scope.row)" style="margin-right: 8px">
              编辑
            </el-button>
            <el-button type="danger" size="small" @click="handleDelete(scope.row)">
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <!-- 编辑公告对话框 -->
    <el-dialog
      v-model="editDialogVisible"
      title="编辑公告"
      width="600px"
    >
      <el-form :model="editForm" :rules="rules" ref="editFormRef" label-width="80px">
        <el-form-item label="标题" prop="title">
          <el-input v-model="editForm.title" placeholder="请输入公告标题" />
        </el-form-item>
        <el-form-item label="内容" prop="content">
          <el-input
            v-model="editForm.content"
            type="textarea"
            :rows="4"
            placeholder="请输入公告内容"
          />
        </el-form-item>
        <el-form-item label="发布人" prop="publisher">
          <el-input v-model="editForm.publisher" placeholder="请输入发布人" />
        </el-form-item>
        <el-form-item label="推送对象" prop="targetType">
          <el-checkbox-group v-model="editForm.targetType">
            <el-checkbox label="all">全体用户</el-checkbox>
            <el-checkbox label="resident">居民</el-checkbox>
            <el-checkbox label="doctor">医生</el-checkbox>
            <el-checkbox label="property">物业</el-checkbox>
            <el-checkbox label="admin">管理员</el-checkbox>
          </el-checkbox-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="editDialogVisible = false">取消</el-button>
          <el-button type="primary" @click="handleUpdate">确定</el-button>
        </span>
      </template>
    </el-dialog>

    <!-- 删除确认对话框 -->
    <el-dialog
      v-model="deleteDialogVisible"
      title="确认删除"
      width="400px"
    >
      <span>确定要删除这条公告吗？</span>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="deleteDialogVisible = false">取消</el-button>
          <el-button type="danger" @click="confirmDelete">确定</el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const user = JSON.parse(localStorage.getItem('user') || '{}');

const noticeForm = ref({
  title: '',
  content: '',
  publisher: user.name || '',
  targetType: ['all']
});

const rules = {
  title: [
    { required: true, message: '请输入公告标题', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入公告内容', trigger: 'blur' }
  ],
  publisher: [
    { required: true, message: '请输入发布人', trigger: 'blur' }
  ]
};

const noticeFormRef = ref(null);
const noticeList = ref([]);

// 编辑相关状态
const editDialogVisible = ref(false);
const editFormRef = ref(null);
const editForm = ref({
  title: '',
  content: '',
  publisher: '',
  targetType: ['all']
});
const currentNoticeId = ref(null);

// 删除相关状态
const deleteDialogVisible = ref(false);
const currentDeleteId = ref(null);

// 加载公告列表
const loadNoticeList = async () => {
  try {
    const response = await axios.get('/api/notice/auth');
    noticeList.value = response.data;
  } catch (error) {
    ElMessage.error('加载公告列表失败');
    console.error(error);
  }
};

// 发布公告
const handlePublish = async () => {
  await noticeFormRef.value.validate(async (valid) => {
    if (valid) {
      console.log('推送对象:', noticeForm.value.targetType);
      try {
        await axios.post('/api/notice', {
          title: noticeForm.value.title,
          content: noticeForm.value.content,
          targetType: noticeForm.value.targetType.join(','),
          createdBy: user.userId || 1
        });
        ElMessage.success('发布公告成功');
        noticeFormRef.value.resetFields();
        await loadNoticeList();
      } catch (error) {
        ElMessage.error('发布公告失败');
        console.error(error);
      }
    }
  });
};

// 编辑公告
const handleEdit = (row) => {
  currentNoticeId.value = row.id;
  editForm.value = {
    title: row.title || '',
    content: row.content || '',
    publisher: row.publisher || '',
    targetType: row.targetType ? row.targetType.split(',') : ['all']
  };
  editDialogVisible.value = true;
};

// 更新公告
const handleUpdate = async () => {
  await editFormRef.value.validate(async (valid) => {
    if (valid) {
      try {
        await axios.put(`/api/notice/${currentNoticeId.value}`, {
          title: editForm.value.title,
          content: editForm.value.content,
          targetType: editForm.value.targetType.join(',')
        });
        ElMessage.success('更新公告成功');
        editDialogVisible.value = false;
        await loadNoticeList();
      } catch (error) {
        ElMessage.error('更新公告失败');
        console.error(error);
      }
    }
  });
};

// 删除公告
const handleDelete = (row) => {
  currentDeleteId.value = row.id;
  deleteDialogVisible.value = true;
};

// 确认删除
const confirmDelete = async () => {
  try {
    await axios.delete(`/api/notice/${currentDeleteId.value}`);
    ElMessage.success('删除公告成功');
    deleteDialogVisible.value = false;
    await loadNoticeList();
  } catch (error) {
    ElMessage.error('删除公告失败');
    console.error(error);
  }
};

// 初始化数据
onMounted(async () => {
  await loadNoticeList();
});
</script>

<style scoped>
.notice-container {
  padding: 20px;
}

.publish-card {
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
</style>