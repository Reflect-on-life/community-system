<template>
  <div class="health-reminder-publish-container">
    <h2>发布慢病提醒</h2>
    
    <el-card class="form-card">
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="提醒标题" prop="title">
          <el-input v-model="form.title" placeholder="请输入提醒标题" maxlength="100" show-word-limit />
        </el-form-item>
        
        <el-form-item label="目标慢病标签" prop="targetTag">
          <el-select v-model="form.targetTag" placeholder="请选择目标慢病标签">
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
        
        <el-form-item label="提醒内容" prop="content">
          <el-input
            v-model="form.content"
            type="textarea"
            placeholder="请输入提醒内容"
            :rows="8"
            maxlength="2000"
            show-word-limit
          />
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="submitForm" :loading="submitting">
            发布提醒
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
  targetTag: '',
  content: ''
});

const formRef = ref(null);
const submitting = ref(false);

const rules = {
  title: [
    { required: true, message: '请输入提醒标题', trigger: 'blur' },
    { min: 2, max: 100, message: '标题长度在 2 到 100 个字符', trigger: 'blur' }
  ],
  targetTag: [
    { required: true, message: '请选择目标慢病标签', trigger: 'change' }
  ],
  content: [
    { required: true, message: '请输入提醒内容', trigger: 'blur' },
    { min: 10, max: 2000, message: '内容长度在 10 到 2000 个字符', trigger: 'blur' }
  ]
};

const submitForm = async () => {
  if (!formRef.value) return;
  
  try {
    await formRef.value.validate();
    submitting.value = true;
    
    await axios.post('/api/health/reminder', form.value);
    
    ElMessage.success('发布成功');
    resetForm();
  } catch (error) {
    ElMessage.error('发布失败');
    console.error('发布慢病提醒失败:', error);
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
    targetTag: '',
    content: ''
  };
};
</script>

<style scoped>
.health-reminder-publish-container {
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
