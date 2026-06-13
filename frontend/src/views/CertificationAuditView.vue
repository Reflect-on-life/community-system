<template>
  <div class="certification-audit-container">
    <h2>认证审核</h2>
    
    <!-- 标签页 -->
    <el-tabs v-model="activeTab" style="margin-top: 20px;">
      <!-- 待审核申请 -->
      <el-tab-pane label="待审核申请" name="pending">
        <el-card class="audit-card">
          <el-table :data="pendingApplications" style="width: 100%">
            <el-table-column label="申请人" min-width="100">
              <template #default="scope">
                {{ scope.row.userName || '未知用户' }}
              </template>
            </el-table-column>
            <el-table-column label="联系方式" min-width="120">
              <template #default="scope">
                {{ scope.row.phone || '未知电话' }}
              </template>
            </el-table-column>
            <el-table-column label="楼栋" min-width="80">
              <template #default="scope">
                {{ scope.row.buildingName || '' }}
              </template>
            </el-table-column>
            <el-table-column label="房号" min-width="80">
              <template #default="scope">
                {{ scope.row.houseNo || '' }}
              </template>
            </el-table-column>
            <el-table-column label="关系" min-width="80">
              <template #default="scope">
                {{ getRelationText(scope.row.relation) }}
              </template>
            </el-table-column>
            <el-table-column label="合同文件" min-width="120">
              <template #default="scope">
                <img
                  v-if="scope.row.contractImageUrl && isImage(scope.row.contractImageUrl)"
                  :src="scope.row.contractImageUrl"
                  @click="previewImage(scope.row.contractImageUrl)"
                  style="width: 60px; height: 60px; cursor: pointer; object-fit: cover; border-radius: 4px;"
                />
                <el-link v-else-if="scope.row.contractImageUrl && isPdf(scope.row.contractImageUrl)" :href="scope.row.contractImageUrl" target="_blank" type="primary">
                  查看PDF
                </el-link>
                <span v-else style="color: #999;">未上传</span>
              </template>
            </el-table-column>
            <el-table-column label="申请时间" min-width="150">
              <template #default="scope">
                {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100">
              <template #default="scope">
                <el-button type="primary" size="small" @click="approveApplication(scope.row.userId, scope.row.houseId)">
                  通过
                </el-button>
                <el-button type="danger" size="small" @click="rejectApplication(scope.row.userId, scope.row.houseId)">
                  拒绝
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
      
      <!-- 已审核记录 -->
      <el-tab-pane label="已审核记录" name="processed">
        <el-card class="audit-card">
          <el-table :data="processedApplications" style="width: 100%">
            <el-table-column label="申请人" min-width="100">
              <template #default="scope">
                {{ scope.row.userName || '未知用户' }}
              </template>
            </el-table-column>
            <el-table-column label="联系方式" min-width="120">
              <template #default="scope">
                {{ scope.row.phone || '未知电话' }}
              </template>
            </el-table-column>
            <el-table-column label="楼栋" min-width="80">
              <template #default="scope">
                {{ scope.row.buildingName || '' }}
              </template>
            </el-table-column>
            <el-table-column label="房号" min-width="80">
              <template #default="scope">
                {{ scope.row.houseNo || '' }}
              </template>
            </el-table-column>
            <el-table-column label="关系" min-width="80">
              <template #default="scope">
                {{ getRelationText(scope.row.relation) }}
              </template>
            </el-table-column>
            <el-table-column label="合同文件" min-width="120">
              <template #default="scope">
                <img
                  v-if="scope.row.contractImageUrl && isImage(scope.row.contractImageUrl)"
                  :src="scope.row.contractImageUrl"
                  @click="previewImage(scope.row.contractImageUrl)"
                  style="width: 60px; height: 60px; cursor: pointer; object-fit: cover; border-radius: 4px;"
                />
                <el-link v-else-if="scope.row.contractImageUrl && isPdf(scope.row.contractImageUrl)" :href="scope.row.contractImageUrl" target="_blank" type="primary">
                  查看PDF
                </el-link>
                <span v-else style="color: #999;">未上传</span>
              </template>
            </el-table-column>
            <el-table-column label="认证状态" min-width="80">
              <template #default="scope">
                <el-tag type="success" v-if="scope.row.certified === 1">已认证</el-tag>
                <el-tag type="danger" v-else>未认证</el-tag>
              </template>
            </el-table-column>
            <el-table-column label="申请时间" min-width="150">
              <template #default="scope">
                {{ new Date(scope.row.createdAt || Date.now()).toLocaleString() }}
              </template>
            </el-table-column>
            <el-table-column label="操作" min-width="100">
              <template #default="scope">
                <el-button type="warning" size="small" @click="revokeCertification(scope.row.userId, scope.row.houseId)" v-if="scope.row.certified === 1">
                  撤回认证
                </el-button>
              </template>
            </el-table-column>
          </el-table>
        </el-card>
      </el-tab-pane>
    </el-tabs>

    <!-- 图片预览弹窗 -->
    <el-dialog
      v-model="imagePreviewVisible"
      title="合同图片预览"
      width="80%"
      :append-to-body="true"
      destroy-on-close
      class="image-preview-dialog"
    >
      <div class="image-preview-toolbar">
        <el-button-group>
          <el-button size="small" @click="zoomOut" :disabled="imageScale <= 0.5">
            <el-icon><ZoomOut /></el-icon> 缩小
          </el-button>
          <el-button size="small" @click="resetZoom">
            {{ Math.round(imageScale * 100) }}%
          </el-button>
          <el-button size="small" @click="zoomIn" :disabled="imageScale >= 5">
            <el-icon><ZoomIn /></el-icon> 放大
          </el-button>
        </el-button-group>
        <span style="color: #909399; font-size: 12px; margin-left: 12px;">提示：滚轮缩放 / 拖拽移动</span>
      </div>
      <div 
        class="image-preview-container"
        ref="imageContainerRef"
        @wheel.prevent="handleWheel"
        @mousedown="startDrag"
        @mousemove="onDrag"
        @mouseup="endDrag"
        @mouseleave="endDrag"
      >
        <img 
          :src="previewImageUrl" 
          :style="{ 
            transform: `scale(${imageScale}) translate(${dragOffset.x}px, ${dragOffset.y}px)`,
            transformOrigin: 'center center',
            cursor: isDragging ? 'grabbing' : 'grab'
          }" 
          ref="previewImageRef"
          draggable="false"
        />
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue';
import axios from 'axios';
import { ElMessage } from 'element-plus';
import { ZoomIn, ZoomOut } from '@element-plus/icons-vue';

const activeTab = ref('pending');
const pendingApplications = ref([]);
const processedApplications = ref([]);
const imagePreviewVisible = ref(false);
const previewImageUrl = ref('');
const imageScale = ref(1);
const isDragging = ref(false);
const dragOffset = ref({ x: 0, y: 0 });
const dragStart = ref({ x: 0, y: 0 });
const imageContainerRef = ref(null);
const previewImageRef = ref(null);

const isImage = (url) => {
  if (!url) return false;
  return /\.(jpg|jpeg|png|gif|webp)$/i.test(url);
};

const isPdf = (url) => {
  if (!url) return false;
  return /\.pdf$/i.test(url);
};

const getRelationText = (relation) => {
  const map = {
    'owner': '业主',
    'tenant': '租户',
    'family': '家属'
  };
  return map[relation] || relation;
};

const previewImage = (url) => {
  previewImageUrl.value = url;
  imagePreviewVisible.value = true;
  imageScale.value = 1;
  dragOffset.value = { x: 0, y: 0 };
};

const zoomIn = () => {
  if (imageScale.value < 5) {
    imageScale.value = Math.min(5, imageScale.value + 0.25);
  }
};

const zoomOut = () => {
  if (imageScale.value > 0.5) {
    imageScale.value = Math.max(0.5, imageScale.value - 0.25);
  }
};

const resetZoom = () => {
  imageScale.value = 1;
  dragOffset.value = { x: 0, y: 0 };
};

const handleWheel = (e) => {
  if (e.deltaY < 0) {
    zoomIn();
  } else {
    zoomOut();
  }
};

const startDrag = (e) => {
  if (e.button === 0) {
    isDragging.value = true;
    dragStart.value = { x: e.clientX - dragOffset.value.x, y: e.clientY - dragOffset.value.y };
  }
};

const onDrag = (e) => {
  if (isDragging.value && imageScale.value > 1) {
    dragOffset.value = { 
      x: e.clientX - dragStart.value.x, 
      y: e.clientY - dragStart.value.y 
    };
  }
};

const endDrag = () => {
  isDragging.value = false;
};

// 加载待审核申请
const loadPendingApplications = async () => {
  try {
    const response = await axios.get('/api/resident-house/pending');
    pendingApplications.value = response.data;
  } catch (error) {
    ElMessage.error('加载待审核申请失败');
    console.error(error);
  }
};

// 加载已审核记录
const loadProcessedApplications = async () => {
  try {
    const response = await axios.get('/api/resident-house/processed');
    processedApplications.value = response.data;
  } catch (error) {
    ElMessage.error('加载已审核记录失败');
    console.error(error);
  }
};

// 通过申请
const approveApplication = async (userId, houseId) => {
  console.log('审核通过请求，userId:', userId, 'houseId:', houseId);
  try {
    await axios.put('/api/resident-house/approve', {}, {
      params: {
        userId: userId,
        houseId: houseId
      }
    });
    ElMessage.success('审核通过成功');
    // 重新加载待审核申请和已审核记录
    await loadPendingApplications();
    await loadProcessedApplications();
  } catch (error) {
    ElMessage.error('审核通过失败');
    console.error('审核通过失败，错误信息:', error);
    if (error.response) {
      console.error('响应数据:', error.response.data);
      console.error('响应状态:', error.response.status);
    }
  }
};

// 拒绝申请
const rejectApplication = async (userId, houseId) => {
  console.log('审核拒绝请求，userId:', userId, 'houseId:', houseId);
  try {
    // 这里可以实现拒绝申请的逻辑，例如删除申请记录或设置状态为拒绝
    await axios.delete('/api/resident-house/reject', {
      params: {
        userId: userId,
        houseId: houseId
      }
    });
    ElMessage.success('审核拒绝成功');
    // 重新加载待审核申请和已审核记录
    await loadPendingApplications();
    await loadProcessedApplications();
  } catch (error) {
    ElMessage.error('审核拒绝失败');
    console.error('审核拒绝失败，错误信息:', error);
    if (error.response) {
      console.error('响应数据:', error.response.data);
      console.error('响应状态:', error.response.status);
    }
  }
};

// 撤回认证
const revokeCertification = async (userId, houseId) => {
  console.log('撤回认证请求，userId:', userId, 'houseId:', houseId);
  try {
    await axios.put('/api/resident-house/revoke', {}, {
      params: {
        userId: userId,
        houseId: houseId
      }
    });
    ElMessage.success('撤回认证成功');
    // 重新加载已审核记录
    await loadProcessedApplications();
  } catch (error) {
    ElMessage.error('撤回认证失败');
    console.error('撤回认证失败，错误信息:', error);
    if (error.response) {
      console.error('响应数据:', error.response.data);
      console.error('响应状态:', error.response.status);
    }
  }
};

// 初始化数据
onMounted(async () => {
  await loadPendingApplications();
  await loadProcessedApplications();
});
</script>

<style scoped>
.certification-audit-container {
  padding: 20px;
}

.audit-card {
  margin-top: 20px;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.image-preview-toolbar {
  display: flex;
  align-items: center;
  padding-bottom: 12px;
  border-bottom: 1px solid #ebeef5;
  margin-bottom: 16px;
}

.image-preview-container {
  overflow: hidden;
  height: 65vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f7fa;
  border-radius: 4px;
  user-select: none;
}

.image-preview-container img {
  max-width: 100%;
  max-height: 100%;
  transition: transform 0.15s ease-out;
}
</style>
