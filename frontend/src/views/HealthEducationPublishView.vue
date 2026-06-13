<template>
  <div class="health-education-publish-container">
    <h2>发布健康宣教</h2>
    
    <el-card class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="宣教标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入宣教标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="宣教内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入宣教内容"
            :rows="10"
            maxlength="5000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            发布宣教
          </el-button>
          <el-button @click="resetForm">
            重置
          </el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

const form = ref({
  title: '',
  content: ''
});

const formRef = ref(null);
const submitting = ref(false);

const rules = {
  title: [
    { required: true, message: '请输入宣教标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  content: [
    { required: true, message: '请输入宣教内容', trigger: 'blur' },
    { min: 10, max: 5000, message: '内容长度在 10 到 5000 个字符', trigger: 'blur' }
  ]
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    await axios.post('/api/health/education', form.value);
    
    ElMessage.success('发布成功');
    resetForm();
  } catch (error) {
    ElMessage.error('发布失败');
    console.error('发布健康宣教失败:', error);
  } finally {
    submitting.value = false;
  }
};

const resetForm = () => {
  if (formRef.value) {
    formRef.value.resetFields();
  }
  form.value = {
    title: '',
    content: ''
  };
};
</script>

<style scoped>
.health-education-publish-container {
  padding: 20px;
}

h2 {
  color: #333;
  margin-bottom: 20px;
}

.form-card {
  max-width: 800px;
  margin: 0 auto;
}
</style>
