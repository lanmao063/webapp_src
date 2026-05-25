<template>
  <div class="page-container" v-loading="loading">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>取件</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never">
      <template #header><span>取件确认</span></template>
      <el-form :model="form" label-width="100px" @keyup.enter="handleScan">
        <el-form-item label="快递单号">
          <el-input v-model="form.trackingNumber" ref="inputRef" placeholder="扫描/输入快递单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleScan" :loading="scanning">查询包裹</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" v-if="pkgInfo" class="result-card">
      <template #header><span>包裹信息</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="快递单号">{{ pkgInfo.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="包裹名称">{{ pkgInfo.packageName || '-' }}</el-descriptions-item>
        <el-descriptions-item label="收件人电话">{{ pkgInfo.receiverPhone }}</el-descriptions-item>
        <el-descriptions-item label="取件码">{{ pkgInfo.pickupCode || '-' }}</el-descriptions-item>
        <el-descriptions-item label="柜型">
          <el-tag :type="cabinetTag(pkgInfo.cabinetType)" size="small">{{ cabinetLabel(pkgInfo.cabinetType) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag type="warning" size="small">在库</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ pkgInfo.enterTime || '-' }}</el-descriptions-item>
      </el-descriptions>
      <div class="pickup-action">
        <el-button type="success" size="large" @click="handlePickup" :loading="loading">
          确认出库
        </el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const form = reactive({ trackingNumber: '' })
const pkgInfo = ref(null)
const scanning = ref(false)
const loading = ref(false)
const inputRef = ref(null)

onMounted(() => {
  inputRef.value?.focus()
})

const cabinetLabel = (type) => {
  if (type === 'SMALL') return '小件柜'
  if (type === 'LARGE') return '大件柜'
  if (type === 'MEDIUM') return '中件柜'
  return '-'
}
const cabinetTag = (type) => {
  if (type === 'SMALL') return 'success'
  if (type === 'LARGE') return 'danger'
  if (type === 'MEDIUM') return 'warning'
  return 'info'
}

const handleScan = async () => {
  if (!form.trackingNumber) { ElMessage.warning('请输入快递单号'); return }
  scanning.value = true
  try {
    const res = await request.get('/inbound/search', { params: { trackingNumber: form.trackingNumber } })
    pkgInfo.value = res.data
    if (!pkgInfo.value) {
      ElMessage.warning('未找到该包裹，请检查单号是否正确')
    }
  } catch {
    pkgInfo.value = null
  } finally { scanning.value = false }
}

const handlePickup = async () => {
  if (!pkgInfo.value) return

  if (!pkgInfo.value.phoneMatch) {
    try {
      await ElMessageBox.confirm(
        `该包裹收件人手机号与您的手机号不一致，这可能不是您的包裹。确定要继续取件吗？`,
        '手机号不匹配',
        { confirmButtonText: '继续取件', cancelButtonText: '取消', type: 'warning' }
      )
    } catch {
      return
    }
  }

  loading.value = true
  try {
    await request.put(`/inbound/${pkgInfo.value.trackingNumber}/checkout`)
    ElMessage.success('出库成功')
    pkgInfo.value = null
    form.trackingNumber = ''
    inputRef.value?.focus()
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.result-card { margin-top: 16px; }
.pickup-action { margin-top: 16px; text-align: center; }
</style>
