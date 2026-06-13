<template>
  <div class="express-container">
    <h1>快递管理</h1>
    
    <el-card class="inbound-card" v-if="user.type === 'property' || user.type === 'admin'">
      <template #header>
        <div class="card-header">
          <span>扫码入库</span>
        </div>
      </template>
      <el-form :model="inboundForm" label-width="100px">
        <el-form-item label="快递单号">
          <el-input v-model="inboundForm.trackingNo" placeholder="请输入或扫描快递单号"></el-input>
        </el-form-item>
        <el-form-item label="手机号">
          <el-input 
            v-model="inboundForm.phone" 
            placeholder="请输入收件人手机号"
            @input="handlePhoneInput"
            maxlength="11"
          ></el-input>
        </el-form-item>
        <el-form-item label="姓名">
          <el-input 
            v-model="inboundForm.userName" 
            placeholder="自动显示"
            disabled
            :class="{ 'loading': searchingUser }"
          >
            <template #prefix>
              <el-icon v-if="searchingUser"><Loading /></el-icon>
              <el-icon v-else-if="inboundForm.userName"><User /></el-icon>
            </template>
          </el-input>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleInbound" :disabled="!inboundForm.userId">确认入库</el-button>
        </el-form-item>
      </el-form>
      <el-alert
        v-if="pickupCode"
        :title="'取件码：' + pickupCode"
        type="success"
        show-icon
      />
    </el-card>

    <el-card class="list-card">
      <template #header>
        <div class="card-header">
          <span>快递状态列表</span>
        </div>
      </template>
      <el-table :data="expressList" style="width: 100%">
        <el-table-column prop="id" label="ID" width="80"></el-table-column>
        <el-table-column prop="trackingNo" label="快递单号"></el-table-column>
        <el-table-column v-if="user.type === 'property' || user.type === 'admin'" prop="userName" label="用户"></el-table-column>
        <el-table-column prop="pickupCode" label="取件码"></el-table-column>
        <el-table-column prop="status" label="状态">
          <template #default="scope">
            <el-tag :type="getStatusType(scope.row.status)">{{ getStatusText(scope.row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="receivedAt" label="入库时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.receivedAt) }}
          </template>
        </el-table-column>
        <el-table-column prop="pickedAt" label="取件时间" width="180">
          <template #default="scope">
            {{ formatDate(scope.row.pickedAt) }}
          </template>
        </el-table-column>
        <el-table-column label="操作" width="120">
          <template #default="scope">
            <el-button 
              v-if="scope.row.status === 'in' && (user.type === 'resident' || user.type === 'property' || user.type === 'admin')" 
              type="primary" 
              size="small" 
              @click="handlePickup(scope.row.pickupCode)"
            >
              {{ user.type === 'resident' ? '确认取件' : '取件' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script>
import axios from 'axios'
import { User, Loading } from '@element-plus/icons-vue'

export default {
  name: 'ExpressView',
  components: {
    User,
    Loading
  },
  data() {
    return {
      inboundForm: {
        trackingNo: '',
        userId: '',
        phone: '',
        userName: ''
      },
      expressList: [],
      pickupCode: '',
      user: {
        type: ''
      },
      searchingUser: false,
      searchTimer: null
    }
  },
  mounted() {
    this.loadUserInfo()
    this.fetchExpressList()
  },
  methods: {
    // 加载用户信息
    loadUserInfo() {
      const userInfo = localStorage.getItem('user');
      if (userInfo) {
        this.user = JSON.parse(userInfo);
      }
    },
    // 手机号输入时搜索用户
    handlePhoneInput() {
      // 清除之前的定时器
      if (this.searchTimer) {
        clearTimeout(this.searchTimer)
      }
      
      const phone = this.inboundForm.phone.trim()
      
      // 手机号长度为 11 位时自动搜索
      if (phone.length === 11 && /^\d{11}$/.test(phone)) {
        this.searchTimer = setTimeout(() => {
          this.searchUserByPhone(phone)
        }, 500) // 500ms 防抖
      } else if (phone.length > 0 && phone.length < 11) {
        // 清空姓名和 userId
        this.inboundForm.userName = ''
        this.inboundForm.userId = ''
      }
    },
    // 根据手机号搜索用户
    async searchUserByPhone(phone) {
      this.searchingUser = true
      try {
        // 调用后端手机号查询接口
        const response = await axios.get('/api/admin/users/search/phone', {
          params: { phone }
        })
        
        const matchedUser = response.data
        
        if (matchedUser) {
          this.inboundForm.userName = matchedUser.name || matchedUser.userName || '未知用户'
          this.inboundForm.userId = matchedUser.id
          this.$message.success(`找到用户：${this.inboundForm.userName}`)
        } else {
          this.inboundForm.userName = ''
          this.inboundForm.userId = ''
          this.$message.warning('未找到该手机号的用户')
        }
      } catch (error) {
        console.error('搜索用户失败:', error)
        this.inboundForm.userName = ''
        this.inboundForm.userId = ''
        this.$message.error('搜索用户失败，请检查手机号是否正确')
      } finally {
        this.searchingUser = false
      }
    },
    async handleInbound() {
      try {
        const response = await axios.post('/api/express/inbound', this.inboundForm)
        this.pickupCode = response.data.pickupCode
        this.fetchExpressList()
        this.inboundForm = {
          trackingNo: '',
          userId: ''
        }
      } catch (error) {
        console.error('入库失败:', error)
        this.$message.error('入库失败，请重试')
      }
    },
    async handlePickup(pickupCode) {
      try {
        await axios.post(`/api/express/pickup/${pickupCode}`)
        this.$message.success('取件成功')
        this.fetchExpressList()
      } catch (error) {
        console.error('取件失败:', error)
        this.$message.error('取件失败，请重试')
      }
    },
    async fetchExpressList() {
      try {
        console.log('开始加载快递列表');
        const response = await axios.get('/api/express');
        this.expressList = response.data;
        console.log('加载快递列表成功，响应数据:', this.expressList);
      } catch (error) {
        console.error('获取快递列表失败:', error);
        this.$message.error('获取快递列表失败');
      }
    },
    getStatusType(status) {
      switch (status) {
        case 'in': return 'info'
        case 'notified': return 'warning'
        case 'picked': return 'success'
        case 'out': return 'danger'
        default: return ''
      }
    },
    getStatusText(status) {
      switch (status) {
        case 'in': return '已入库'
        case 'notified': return '已通知'
        case 'picked': return '已取件'
        case 'out': return '已出库'
        default: return status
      }
    },
    formatDate(dateStr) {
      if (!dateStr) return ''
      const date = new Date(dateStr)
      return date.toLocaleString()
    }
  }
}
</script>

<style scoped>
.express-container {
  padding: 2rem;
}

h1 {
  color: #333;
  margin-bottom: 2rem;
}

.inbound-card {
  margin-bottom: 2rem;
}

.list-card {
  margin-top: 1rem;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
