<template>
  <div class="welcome-container">
    <!-- REGULAR: 普通用户 - 我的取件码 -->
    <template v-if="role === 'REGULAR'">
      <h1>我的取件码</h1>
      <el-table :data="pickupList" stripe border style="width: 100%; max-width: 700px; margin-top: 20px;">
        <el-table-column prop="pickupCode" label="取件码" width="150">
          <template #default="{ row }">
            <el-tag type="success" size="large">{{ row.pickupCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageName" label="包裹类型" width="150" />
        <el-table-column prop="trackingNumber" label="快递单号" width="200" />
        <el-table-column prop="createdAt" label="到达时间" width="180">
          <template #default="{ row }">{{ row.updatedAt || '-' }}</template>
        </el-table-column>
      </el-table>
      <p v-if="pickupList.length === 0" style="color:#909399;margin-top:40px;">暂无待取包裹</p>
    </template>

    <!-- COURIER: 快递员 - 统计卡片 -->
    <template v-if="role === 'COURIER'">
      <h1>工作台</h1>
      <el-row :gutter="20" style="margin-top: 20px; width: 100%; max-width: 800px;">
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">待取件</p>
              <h2 style="color:#e6a23c;">{{ stats.pendingPickups }}</h2>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">今日派送</p>
              <h2 style="color:#409eff;">{{ stats.todayDeliveries }}</h2>
            </div>
          </el-card>
        </el-col>
        <el-col :span="8">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">今日入库</p>
              <h2 style="color:#67c23a;">{{ stats.todayWarehoused }}</h2>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- MANAGER: 驿站管理员 - 概览卡片 -->
    <template v-if="role === 'MANAGER'">
      <h1>数据概览</h1>
      <el-row :gutter="20" style="margin-top: 20px; width: 100%; max-width: 900px;">
        <el-col :span="6" v-for="card in cards" :key="card.label">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">{{ card.label }}</p>
              <h2 :style="{color:card.color}">{{ card.value }}</h2>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUserInfo } from '@/utils/auth'
import request from '@/utils/request'

const user = getUserInfo()
const role = ref(user?.role || '')

const pickupList = ref([])
const stats = reactive({ pendingPickups: 0, todayDeliveries: 0, todayWarehoused: 0 })

const cards = reactive([
  { label: '在库包裹', value: 0, color: '#e6a23c' },
  { label: '已取件', value: 0, color: '#67c23a' },
  { label: '总包裹', value: 0, color: '#409eff' },
  { label: '未处理异常', value: 0, color: '#f56c6c' }
])

onMounted(() => {
  if (role.value === 'REGULAR') {
    request.get('/send-package/my-pickup-codes').then(res => {
      pickupList.value = res.data || []
    }).catch(() => {})
  } else if (role.value === 'COURIER') {
    request.get('/statistics/courier-overview').then(res => {
      Object.assign(stats, res.data)
    }).catch(() => {})
  } else if (role.value === 'MANAGER') {
    request.get('/statistics/overview').then(res => {
      cards[0].value = res.data.totalInWarehouse
      cards[1].value = res.data.totalPickedUp
      cards[2].value = res.data.totalParcels
      cards[3].value = res.data.unresolvedErrors
    }).catch(() => {})
  }
})
</script>

<style scoped>
.welcome-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100%;
  padding-top: 40px;
  color: #606266;
}
.welcome-container h1 {
  font-size: 28px;
  margin-bottom: 16px;
}
</style>
