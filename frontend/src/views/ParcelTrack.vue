<template>
  <div class="page-container" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>查件</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="快递单号">
          <el-input v-model="searchForm.trackingNumber" placeholder="请输入快递单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="result-card" shadow="never" v-if="parcel">
      <template #header><span>包裹详情</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="快递单号">{{ parcel.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="包裹名称">{{ parcel.packageName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="寄件人">{{ parcel.senderName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="寄件人电话">{{ parcel.senderPhone || '-' }}</el-descriptions-item>
        <el-descriptions-item label="寄件人地址">{{ parcel.senderAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ parcel.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="收件人电话">{{ parcel.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="收件人地址">{{ parcel.receiverAddress || '-' }}</el-descriptions-item>
        <el-descriptions-item label="重量(kg)">{{ parcel.weight }}</el-descriptions-item>
        <el-descriptions-item label="体积(cm³)">{{ parcel.volume }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag v-if="parcel.inboundStatus === 'IN_WAREHOUSE'" type="warning" size="small">在库</el-tag>
          <el-tag v-else-if="parcel.inboundStatus === 'CHECKED_OUT'" type="success" size="small">已出库</el-tag>
          <el-tag v-else-if="parcel.sendStatus === 'COLLECTED'" type="primary" size="small">运输中</el-tag>
          <el-tag v-else-if="parcel.sendStatus === 'PAID'" type="warning" size="small">待揽收</el-tag>
          <el-tag v-else-if="parcel.sendStatus === 'APPROVED'" type="warning" size="small">待付款</el-tag>
          <el-tag v-else-if="parcel.sendStatus === 'SUBMITTED'" type="info" size="small">已提交</el-tag>
          <el-tag v-else-if="parcel.sendStatus === 'REJECTED'" type="danger" size="small">已驳回</el-tag>
          <span v-else class="text-secondary">运输中</span>
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ parcel.enterTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="取件时间">{{ parcel.outTime || '-' }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ parcel.notes || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>

    <el-empty v-if="searched && !loading && !parcel" description="未找到该包裹" />
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchForm = reactive({ trackingNumber: '' })
const parcel = ref(null)
const loading = ref(false)
const searched = ref(false)

const handleSearch = async () => {
  if (!searchForm.trackingNumber) {
    ElMessage.warning('请输入快递单号')
    return
  }
  loading.value = true
  try {
    const res = await request.get('/package/track', { params: { trackingNumber: searchForm.trackingNumber } })
    parcel.value = res.data
  } catch {
    parcel.value = null
  } finally {
    loading.value = false
    searched.value = true
  }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }
.text-secondary { color: #909399; }
</style>
