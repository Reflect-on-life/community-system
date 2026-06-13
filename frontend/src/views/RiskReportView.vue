<template>
  <div class="risk_report-container">
    <h2>风险上报</h2>
    
    <el-alert
      title="AI 智能识别"
      type="info"
      description="上传图片后，系统会自动识别风险类型并生成事件标题，您也可以手动修改"
      :closable="false"
      style="margin-bottom: 20px"
    />
    
    <el-card class="report-card">
      <h3>上报风险事件</h3>
      <el-form :model="reportForm" :rules="formRules" label-width="100px" ref="reportFormRef">
        <el-form-item label="事件标题" prop="title">
          <el-input v-model="reportForm.title" placeholder="AI 将自动生成标题，您也可以手动输入" />
          <div class="form-item-hint" v-if="reportForm.aiResult === 'AI 识别成功'">
            <el-icon><SuccessFilled /></el-icon>
            <span>AI 已自动生成事件标题，如有需要您可以修改</span>
          </div>
        </el-form-item>
        
        <el-form-item label="发生位置" prop="location">
          <el-select v-model="reportForm.location" placeholder="请选择风险发生位置" style="width: 100%">
            <el-option label="花园区域" value="花园区域" />
            <el-option label="停车场" value="停车场" />
            <el-option label="楼道" value="楼道" />
            <el-option label="小区大门" value="小区大门" />
            <el-option label="健身区" value="健身区" />
            <el-option label="儿童游乐区" value="儿童游乐区" />
            <el-option label="游泳池" value="游泳池" />
            <el-option label="地下车库" value="地下车库" />
            <el-option label="后门" value="后门" />
          </el-select>
        </el-form-item>
        
        <el-form-item label="上传图片" prop="imageUrl">
          <el-upload
            class="image-uploader"
            :show-file-list="false"
            :http-request="customUpload"
            :before-upload="beforeImageUpload"
            accept="image/*"
          >
            <div class="image-uploader-content" v-if="!reportForm.imageUrl">
              <el-icon class="image-uploader-icon"><Plus /></el-icon>
              <div class="image-uploader-text">点击上传图片</div>
            </div>
          </el-upload>
          <div class="image-preview" v-if="reportForm.imageUrl">
            <img :src="reportForm.imageUrl" alt="风险图片" />
            <div class="image-preview-actions">
              <el-button type="danger" size="small" @click="removeImage">删除</el-button>
            </div>
          </div>
        </el-form-item>
        
        <!-- AI 识别结果展示区域 -->
        <el-form-item label="AI 识别结果" v-if="reportForm.aiResult">
          <el-alert
            :title="reportForm.aiResult"
            :type="getAlertType(reportForm.aiRiskLevel)"
            :closable="false"
            show-icon
          >
            <template #default>
              <div class="ai-result-content">
                <!-- 风险类型选择 -->
                <div class="risk-type-selector">
                  <span class="selector-label">风险类型：</span>
                  <el-select v-model="reportForm.riskType" placeholder="请选择风险类型" style="width: 200px;">
                    <el-option label="老人摔倒" value="FALL_DOWN" />
                    <el-option label="路面积水" value="WATER_LOGGING" />
                    <el-option label="设施损坏" value="FACILITY_DAMAGE" />
                    <el-option label="电动车违规充电" value="EV_CHARGING" />
                    <el-option label="火灾风险" value="FIRE_RISK" />
                    <el-option label="垃圾堆积" value="GARBAGE_PILE" />
                    <el-option label="非法张贴" value="ILLEGAL_POSTING" />
                    <el-option label="儿童安全风险" value="CHILD_SAFETY" />
                    <el-option label="宠物安全风险" value="PET_SAFETY" />
                    <el-option label="施工维修风险" value="CONSTRUCTION_RISK" />
                    <el-option label="其他" value="OTHER" />
                  </el-select>
                  <el-alert
                    v-if="reportForm.aiRiskType === '未知风险'"
                    type="warning"
                    show-icon
                    :closable="false"
                    style="margin-top: 10px; font-size: 13px;"
                    title="AI 识别不确定，请手动选择风险类型"
                  />
                </div>
                
                <!-- 风险等级展示 -->
                <p class="risk-level-display">
                  <span class="selector-label">风险等级：</span>
                  <el-tag :type="getLevelTagType(reportForm.aiRiskLevel)">{{ getRiskLevelText(reportForm.aiRiskLevel) }}</el-tag>
                </p>
                
                <!-- AI 生成描述 -->
                <el-form-item label="AI 生成描述" prop="description" style="margin-top: 15px;">
                  <el-input
                    v-model="reportForm.description"
                    type="textarea"
                    rows="4"
                    placeholder="AI 正在生成描述..."
                  />
                </el-form-item>
                <p class="ai-hint">您可以修改 AI 生成的描述内容</p>
              </div>
            </template>
          </el-alert>
        </el-form-item>
        
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="submitting">
            提交上报
          </el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue';
import { Plus, SuccessFilled } from '@element-plus/icons-vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';

// 风险类型选项（使用中文作为 value）
const RISK_TYPE_OPTIONS = [
  { value: '老人摔倒', label: '老人摔倒' },
  { value: '路面积水', label: '路面积水' },
  { value: '设施损坏', label: '设施损坏' },
  { value: '电动车违规充电', label: '电动车违规充电' },
  { value: '火灾风险', label: '火灾风险' },
  { value: '垃圾堆积', label: '垃圾堆积' },
  { value: '非法张贴', label: '非法张贴' },
  { value: '儿童安全风险', label: '儿童安全风险' },
  { value: '宠物安全风险', label: '宠物安全风险' },
  { value: '施工维修风险', label: '施工维修风险' },
  { value: '其他', label: '其他' }
];

// 英文风险类型到中文的映射（用于 AI 识别结果转换）
const EN_TO_CN_RISK_TYPE_MAP = {
  'FALL_DOWN': '老人摔倒',
  'WATER_LOGGING': '路面积水',
  'FACILITY_DAMAGE': '设施损坏',
  'EV_CHARGING': '电动车违规充电',
  'FIRE_RISK': '火灾风险',
  'GARBAGE_PILE': '垃圾堆积',
  'ILLEGAL_POSTING': '非法张贴',
  'CHILD_SAFETY': '儿童安全风险',
  'PET_SAFETY': '宠物安全风险',
  'CONSTRUCTION_RISK': '施工维修风险',
  'OTHER': '其他',
  'elder_fall': '老人摔倒',
  'road_water': '路面积水',
  'facility_damage': '设施损坏',
  'ebike_charging': '电动车违规充电',
  'fire_risk': '火灾风险'
};

const reportForm = reactive({
  title: '',
  location: '',
  description: '',
  riskType: '',
  imageUrl: '',
  aiResult: '',
  aiRiskType: '',
  aiRiskLevel: ''
});

// 表单验证规则
const formRules = reactive({
  title: [
    { required: true, message: '请输入事件标题', trigger: 'blur' },
    { min: 3, max: 50, message: '长度在 3 到 50 个字符', trigger: 'blur' }
  ],
  location: [
    { required: true, message: '请选择风险发生位置', trigger: 'change' }
  ],
  riskType: [
    { required: true, message: '请选择风险类型', trigger: 'change' }
  ],
  imageUrl: [
    { required: true, message: '请上传风险图片', trigger: 'change' }
  ],
  description: [
    { required: true, message: '请输入事件描述', trigger: 'blur' },
    { min: 10, message: '描述至少需要 10 个字符', trigger: 'blur' }
  ]
});

// 图片上传前验证
const beforeImageUpload = (file) => {
  const isImage = file.type.startsWith('image/');
  const isLt2M = file.size / 1024 / 1024 < 2;

  if (!isImage) {
    ElMessage.error('只能上传图片文件！');
  }
  if (!isLt2M) {
    ElMessage.error('图片大小不能超过 2MB！');
  }
  return isImage && isLt2M;
};

// 自定义图片上传
const customUpload = async (options) => {
  const { file } = options;
  
  try {
    // 上传前先清空之前的 AI 识别结果
    reportForm.aiResult = '';
    reportForm.aiRiskType = '';
    reportForm.aiRiskLevel = '';
    reportForm.riskType = '';
    reportForm.description = '';
    
    const formData = new FormData();
    formData.append('file', file);
    
    const response = await axios.post('/api/file/upload', formData, {
      headers: {
        'Content-Type': 'multipart/form-data'
      }
    });
    
    if (response.data.code === 200 || response.data.success) {
      reportForm.imageUrl = response.data.data || response.data.url;
      ElMessage.success('图片上传成功');
      
      // 调用 AI 识别接口
      try {
        // 直接传递相对路径和标题给后端，后端会使用标题 + 图片联合识别
        const aiResponse = await axios.post('/api/risk/recognize', {
          imageUrl: reportForm.imageUrl,  // 直接传递相对路径 /upload/images/xxx.png
          riskTitle: reportForm.title     // 传递用户输入的标题
        });
        
        if (aiResponse.data) {
          const result = aiResponse.data;
          // 保存 AI 识别结果
          reportForm.aiRiskType = result.riskType;
          reportForm.aiRiskLevel = result.riskLevel;
          reportForm.aiResult = 'AI 识别成功';
          
          // 自动填充 AI 识别的风险类型 (使用映射关系，将英文转换为中文)
          if (result.riskType !== '未知风险') {
            // 如果是英文类型，转换为中文
            reportForm.riskType = EN_TO_CN_RISK_TYPE_MAP[result.riskType] || result.riskType;
            
            // AI 识别成功后，自动生成事件标题
            reportForm.title = reportForm.riskType;
          } else {
            reportForm.riskType = ''; // 未知风险时清空，让用户手动选择
          }
          
          // 根据 AI 识别结果自动生成描述
          reportForm.description = generateAiDescription(result);
          
          ElMessage.success('AI 识别完成，已自动生成标题和描述');
        }
      } catch (error) {
        console.error('AI 识别失败:', error);
        ElMessage.warning('AI 识别失败，请手动填写信息');
        reportForm.aiResult = 'AI 识别失败';
      }
    } else {
      ElMessage.error(response.data.message || '图片上传失败');
    }
  } catch (error) {
    console.error('上传失败:', error);
    ElMessage.error('图片上传失败：' + (error.response?.data?.message || error.message));
  }
};

// 删除图片
const removeImage = () => {
  reportForm.imageUrl = '';
  reportForm.aiResult = '';
  reportForm.aiRiskType = '';
  reportForm.aiRiskLevel = '';
  ElMessage.info('图片已删除');
};

// 获取风险类型文本
const getRiskTypeText = (type) => {
  const option = RISK_TYPE_OPTIONS.find(opt => opt.value === type);
  return option ? option.label : '未知风险';
};

// 根据 AI 识别结果生成描述
const generateAiDescription = (aiResult) => {
  const riskTypeText = aiResult.riskType; // 直接使用后端返回的中文风险类型
  const riskLevelText = getRiskLevelText(aiResult.riskLevel);
  const locationText = reportForm.location || '未知位置';
  
  // 如果后端已经生成了描述，直接使用
  if (aiResult.description) {
    return `${riskTypeText}（${locationText}）：${aiResult.description}`;
  }
  
  // 否则使用预设模板
  const descriptions = {
    '老人摔倒': `监测到有人摔倒在${locationText}，风险等级：${riskLevelText}。建议立即前往查看并提供必要帮助，确保人员安全。`,
    '路面积水': `监测到${locationText}有积水情况，风险等级：${riskLevelText}。建议及时清理积水，防止行人滑倒或车辆受损。`,
    '设施损坏': `监测到${locationText}公共设施损坏，风险等级：${riskLevelText}。建议尽快安排维修，消除安全隐患。`,
    '电动车安全风险': `监测到${locationText}有电动车违规充电行为，风险等级：${riskLevelText}。建议及时制止并进行安全教育。`,
    '火灾风险': `监测到${locationText}有火灾风险，风险等级：${riskLevelText}。请立即处理，消除火灾隐患！`,
    '垃圾堆积': `监测到${locationText}有垃圾堆积，风险等级：${riskLevelText}。请及时清理，维护环境卫生。`,
    '非法张贴': `监测到${locationText}有非法张贴物，风险等级：${riskLevelText}。请及时清理，维护小区环境。`,
    '儿童安全风险': `监测到${locationText}有儿童活动，风险等级：${riskLevelText}。请关注儿童安全，防止意外发生。`,
    '宠物安全风险': `监测到${locationText}有宠物活动，风险等级：${riskLevelText}。请注意宠物管理和卫生问题。`,
    '施工维修风险': `监测到${locationText}有施工维修活动，风险等级：${riskLevelText}。请注意施工安全和噪音影响。`,
    '未知风险': `在${locationText}发现异常情况，风险等级：${riskLevelText}。建议前往查看具体情况并采取相应措施。`
  };
  
  return descriptions[riskTypeText] || descriptions['未知风险'];
};

// 获取风险等级文本
const getRiskLevelText = (level) => {
  switch (level) {
    case 'HIGH':
      return '高风险';
    case 'MEDIUM':
      return '中风险';
    case 'LOW':
      return '低风险';
    default:
      return '未知风险';
  }
};

// 获取警告框类型
const getAlertType = (level) => {
  switch (level) {
    case 'HIGH':
      return 'error';
    case 'MEDIUM':
      return 'warning';
    case 'LOW':
      return 'info';
    default:
      return 'info';
  }
};

// 获取等级标签类型
const getLevelTagType = (level) => {
  switch (level) {
    case 'HIGH':
      return 'danger';
    case 'MEDIUM':
      return 'warning';
    case 'LOW':
      return 'success';
    default:
      return 'info';
  }
};

const submitting = ref(false);

// 提交表单
const handleSubmit = async () => {
  submitting.value = true;
  try {
    await axios.post('/api/risk/report', reportForm);
    ElMessage.success('风险上报成功');
    handleReset();
  } catch (error) {
    console.error('上报失败:', error);
    ElMessage.error('风险上报失败，请稍后重试');
  } finally {
    submitting.value = false;
  }
};

// 重置表单
const handleReset = () => {
  reportForm.title = '';
  reportForm.location = '';
  reportForm.description = '';
  reportForm.riskType = '';
  reportForm.imageUrl = '';
  reportForm.aiResult = '';
  reportForm.aiRiskType = '';
  reportForm.aiRiskLevel = '';
};
</script>

<style scoped>
.risk-report-container {
  padding: 20px;
}

.report-card {
  max-width: 800px;
  margin: 0 auto;
}

.report-card h3 {
  margin: 0 0 20px 0;
  font-size: 18px;
  color: #303133;
}

.image-uploader {
  width: 100%;
  max-width: 400px;
}

.image-uploader .el-upload {
  border: 1px dashed #d9d9d9;
  border-radius: 6px;
  cursor: pointer;
  position: relative;
  overflow: hidden;
  width: 100%;
}

.image-uploader .el-upload:hover {
  border-color: #409EFF;
}

.image-uploader-content {
  padding: 30px 20px;
  text-align: center;
}

.image-uploader-icon {
  font-size: 28px;
  color: #8c939d;
  margin-bottom: 8px;
}

.image-uploader-text {
  font-size: 14px;
  color: #8c939d;
}

.image-preview {
  margin-top: 10px;
  width: 100%;
  max-width: 400px;
  height: 300px;
  overflow: hidden;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  position: relative;
}

.image-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.image-preview-actions {
  position: absolute;
  bottom: 10px;
  right: 10px;
  background: rgba(0, 0, 0, 0.6);
  padding: 5px 10px;
  border-radius: 4px;
}

.el-form-item {
  margin-bottom: 22px;
}

.el-button {
  margin-right: 10px;
}

.ai-result-content {
  padding: 5px 0;
}

.ai-result-content p {
  margin: 8px 0;
  font-size: 14px;
}

.ai-result-content strong {
  color: #409EFF;
  font-size: 15px;
}

.ai-hint {
  color: #909399;
  font-size: 13px;
  margin-top: 12px;
  font-style: italic;
}

/* 风险类型选择器样式 */
.risk-type-selector {
  margin-bottom: 15px;
}

.selector-label {
  font-weight: 600;
  color: #606266;
  font-size: 14px;
  margin-right: 10px;
}

.risk-level-display {
  margin: 10px 0;
  display: flex;
  align-items: center;
}

.el-form-item[label="AI 识别结果"] {
  align-items: flex-start;
}

/* 优化 AI 识别结果中的表单项样式 */
.ai-result-content :deep(.el-form-item) {
  margin-bottom: 10px;
}

.ai-result-content :deep(.el-form-item__label) {
  font-size: 14px;
  color: #606266;
}

/* 表单项提示文字样式 */
.form-item-hint {
  display: flex;
  align-items: center;
  gap: 6px;
  font-size: 13px;
  color: #67C23A;
  margin-top: 8px;
}

.form-item-hint .el-icon {
  font-size: 16px;
}
</style>