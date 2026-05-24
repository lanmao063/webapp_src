<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>快递入库</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never">
      <template #header><span>快递入库</span></template>
      <el-form :model="form" label-width="110px" @keyup.enter="handleDeliver">
        <el-form-item label="快递单号">
          <el-input v-model="form.trackingNumber" ref="inputRef" placeholder="扫描/输入快递单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleDeliver" :loading="loading">确认入库</el-button>
        </el-form-item>
      </el-form>

      <div v-if="pickupCode" class="result-card">
        <el-alert :title="'入库成功！取件码：' + pickupCode" type="success" :closable="false" show-icon center>
          <template #default>
            <h2 style="color:#67c23a;font-size:32px;letter-spacing:4px;">{{ pickupCode }}</h2>
          </template>
        </el-alert>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const form = reactive({ trackingNumber: '' })
const loading = ref(false)
const inputRef = ref(null)
const pickupCode = ref('')

onMounted(() => {
  inputRef.value?.focus()
})

const handleDeliver = async () => {
  if (!form.trackingNumber) { ElMessage.warning('请输入快递单号'); return }
  loading.value = true
  try {
    const res = await request.get(`/send-package/${form.trackingNumber}`)
    const sp = res.data
    if (sp.status !== 'DELIVERING') {
      ElMessage.warning('该订单状态不是派送中，无法入库')
      return
    }
    const deliverRes = await request.put(`/send-package/${sp.id}/deliver`)
    pickupCode.value = deliverRes.data
    form.trackingNumber = ''
    inputRef.value?.focus()
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.result-card { margin-top: 20px; text-align: center; }
</style>
