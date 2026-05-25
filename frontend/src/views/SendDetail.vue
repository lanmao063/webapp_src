<template>
  <div class="page-container" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>寄件详情</el-breadcrumb-item>
    </el-breadcrumb>

    <div v-if="order">
      <el-card shadow="never" class="info-card">
        <template #header><span>包裹信息</span></template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="快递单号">{{ order.trackingNumber }}</el-descriptions-item>
          <el-descriptions-item label="包裹名称">{{ order.packageName }}</el-descriptions-item>
          <el-descriptions-item label="重量">{{ order.weight }} kg</el-descriptions-item>
          <el-descriptions-item label="费用">￥{{ order.fee }}</el-descriptions-item>
          <el-descriptions-item label="寄件人">{{ order.senderName }}</el-descriptions-item>
          <el-descriptions-item label="寄件人电话">{{ order.senderPhone }}</el-descriptions-item>
          <el-descriptions-item label="寄件人地址" :span="2">{{ order.senderAddress }}</el-descriptions-item>
          <el-descriptions-item label="收件人">{{ order.receiverName }}</el-descriptions-item>
          <el-descriptions-item label="收件人电话">{{ order.receiverPhone }}</el-descriptions-item>
          <el-descriptions-item label="收件人地址" :span="2">{{ order.receiverAddress }}</el-descriptions-item>
          <el-descriptions-item v-if="order.notes" label="备注" :span="2">{{ order.notes }}</el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="info-card">
        <template #header><span>订单信息</span></template>
        <el-descriptions :column="2" border size="small">
          <el-descriptions-item label="取件方式">
            <el-tag :type="order.pickupMethod === 'DOOR_PICKUP' ? 'warning' : 'info'" size="small">
              {{ order.pickupMethod === 'DOOR_PICKUP' ? '上门取件' : '自行寄出' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="预约时间">{{ order.appointmentTime || '-' }}</el-descriptions-item>
          <el-descriptions-item label="付款状态">
            <el-tag :type="order.isPaid ? 'success' : 'info'" size="small">
              {{ order.isPaid ? '已付款' : '未付款' }}
            </el-tag>
          </el-descriptions-item>
          <el-descriptions-item label="当前状态">
            <el-tag :type="statusTagType(order.status)" size="small">{{ statusLabel(order.status) }}</el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </el-card>

      <el-card shadow="never" class="info-card">
        <template #header><span>状态时间线</span></template>
        <div class="timeline-wrapper">
          <div v-for="(node, idx) in timeline" :key="idx" class="timeline-node" :class="{ active: node.active, last: idx === timeline.length - 1 }">
            <div class="timeline-dot">
              <el-icon v-if="node.active" color="#67c23a"><CircleCheck /></el-icon>
              <div v-else class="dot-empty"></div>
            </div>
            <div class="timeline-line" v-if="idx < timeline.length - 1" :class="{ filled: node.active }"></div>
            <div class="timeline-content">
              <span class="timeline-label" :class="node.active ? 'text-primary' : 'text-muted'">{{ node.label }}</span>
              <span class="timeline-time" v-if="node.time">{{ node.time }}</span>
              <span class="timeline-time text-muted" v-else>未到达</span>
            </div>
          </div>
        </div>
      </el-card>
    </div>

    <el-empty v-if="!loading && !order" description="暂无数据" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { ElMessage } from 'element-plus'
import { CircleCheck } from '@element-plus/icons-vue'
import request from '@/utils/request'

const route = useRoute()
const order = ref(null)
const loading = ref(false)

const statusLabelMap = {
  SUBMITTED: '已提交',
  APPROVED: '已审核',
  PAID: '已付款',
  COLLECTED: '已揽收',
  REJECTED: '已驳回'
}
const statusLabel = (s) => statusLabelMap[s] || s

const statusTagType = (s) => {
  if (s === 'COLLECTED') return 'success'
  if (s === 'PAID' || s === 'APPROVED') return 'warning'
  if (s === 'REJECTED') return 'danger'
  return 'info'
}

const statusOrder = ['SUBMITTED', 'APPROVED', 'PAID', 'COLLECTED']

const timeline = computed(() => {
  if (!order.value) return []
  const sp = order.value
  const nodes = [
    { key: 'SUBMITTED', label: '提交寄件', time: sp.createdAt, active: true },
    { key: 'APPROVED', label: '管理员审核通过', time: sp.approvedAt, active: statusOrder.indexOf(sp.status) >= 1 && sp.status !== 'REJECTED' },
    { key: 'PAID', label: '用户付款', time: sp.paidAt, active: statusOrder.indexOf(sp.status) >= 2 && sp.status !== 'REJECTED' },
    { key: 'COLLECTED', label: '快递员揽收', time: sp.collectedAt, active: sp.status === 'COLLECTED' }
  ]
  if (sp.status === 'REJECTED') {
    nodes.splice(1, 3, { key: 'REJECTED', label: '审核驳回', time: sp.updatedAt, active: true })
  }
  return nodes
})

onMounted(async () => {
  const id = route.query.id
  if (!id) {
    ElMessage.error('缺少订单编号')
    return
  }
  loading.value = true
  try {
    const res = await request.get(`/send-package/${id}`)
    order.value = res.data
  } catch {
    ElMessage.error('获取订单详情失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.info-card { margin-bottom: 16px; }
.timeline-wrapper {
  display: flex;
  align-items: flex-start;
  padding: 20px 0;
  overflow-x: auto;
}
.timeline-node {
  display: flex;
  flex-direction: column;
  align-items: center;
  min-width: 120px;
  flex: 1;
}
.timeline-dot {
  width: 24px;
  height: 24px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 8px;
  z-index: 1;
}
.dot-empty {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  background: #dcdfe6;
}
.timeline-line {
  position: absolute;
  top: 32px;
  left: 50%;
  width: 100%;
  height: 2px;
  background: #ebeef5;
}
.timeline-line.filled {
  background: #67c23a;
}
.timeline-content {
  text-align: center;
  margin-top: 4px;
}
.timeline-label {
  font-size: 13px;
  display: block;
}
.timeline-time {
  font-size: 12px;
  color: #909399;
  display: block;
  margin-top: 4px;
}
.timeline-wrapper {
  position: relative;
}
.timeline-node {
  position: relative;
}
.timeline-node .timeline-line {
  position: absolute;
  top: 12px;
  left: 60px;
  right: -60px;
  height: 2px;
  background: #ebeef5;
  z-index: 0;
}
.timeline-node .timeline-line.filled {
  background: #67c23a;
}
.text-primary { color: #303133; }
.text-muted { color: #c0c4cc; }
</style>
