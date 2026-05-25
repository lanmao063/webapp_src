<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>包裹入库</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never" v-loading="loading">
      <template #header><span>包裹入库</span></template>
      <el-form :model="form" label-width="110px" @keyup.enter="handleEntry">
        <el-form-item label="快递单号">
          <el-input v-model="form.trackingNumber" ref="inputRef" placeholder="扫描/输入快递单号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleEntry" :loading="loading">确认入库</el-button>
        </el-form-item>
      </el-form>

      <div v-if="pickupCode" class="result-card">
        <el-alert title="入库成功！" type="success" :closable="false" show-icon center>
          <template #default>
            <div style="margin-top:8px;">
              <p>取件码</p>
              <h2 style="color:#67c23a;font-size:32px;letter-spacing:4px;">{{ pickupCode }}</h2>
            </div>
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

const handleEntry = async () => {
  if (!form.trackingNumber) { ElMessage.warning('请输入快递单号'); return }
  loading.value = true
  try {
    const res = await request.put(`/inbound/${form.trackingNumber}/warehouse-entry`)
    // if the backend returns a pickup code, display it
    pickupCode.value = res.data || '已生成'
    ElMessage.success('入库成功')
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
