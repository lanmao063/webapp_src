<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>包裹入库</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never">
      <template #header><span>入库信息</span></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="包裹名称" prop="packageName">
              <el-input v-model="form.packageName" placeholder="请输入包裹名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量(kg)" prop="weight">
              <el-input-number v-model="form.weight" :min="0" :precision="3" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="体积(cm³)" prop="volume">
              <el-input-number v-model="form.volume" :min="0" :precision="3" style="width: 100%;" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="申报价值(元)" prop="declaredValue">
              <el-input-number v-model="form.declaredValue" :min="0" :precision="2" style="width: 100%;" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="寄件人" prop="senderName">
              <el-input v-model="form.senderName" placeholder="请输入寄件人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收件人" prop="receiverName">
              <el-input v-model="form.receiverName" placeholder="请输入收件人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收件人电话">
              <el-input v-model="form.receiverPhone" placeholder="请输入收件人电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收件人地址">
              <el-input v-model="form.receiverAddress" placeholder="请输入收件人地址" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="选填" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">入库</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" style="margin-top: 16px;" v-if="result">
      <template #header><span>入库结果</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="包裹编号">{{ result.id }}</el-descriptions-item>
        <el-descriptions-item label="包裹名称">{{ result.packageName }}</el-descriptions-item>
        <el-descriptions-item label="体积(cm³)">{{ result.volume }}</el-descriptions-item>
        <el-descriptions-item label="柜型">
          <el-tag :type="cabinetTag(result.volume)" size="small">{{ cabinetLabel(result.volume) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ result.enterTime }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const form = reactive({
  packageName: '',
  weight: null,
  volume: null,
  declaredValue: null,
  senderName: '',
  receiverName: '',
  receiverPhone: '',
  receiverAddress: '',
  notes: ''
})

const rules = {
  packageName: [{ required: true, message: '请输入包裹名称', trigger: 'blur' }],
  weight: [{ required: true, message: '请输入重量', trigger: 'blur' }],
  volume: [{ required: true, message: '请输入体积', trigger: 'blur' }],
  declaredValue: [{ required: true, message: '请输入申报价值', trigger: 'blur' }],
  senderName: [{ required: true, message: '请输入寄件人', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收件人', trigger: 'blur' }]
}

const cabinetLabel = (vol) => {
  if (!vol) return '-'
  if (vol < 12000) return '小件柜'
  if (vol > 125000) return '大件柜'
  return '中件柜'
}
const cabinetTag = (vol) => {
  if (!vol) return 'info'
  if (vol < 12000) return 'success'
  if (vol > 125000) return 'danger'
  return 'warning'
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await request.post('/parcel/entry', form)
    result.value = res.data
    ElMessage.success('入库成功')
  } catch {} finally { loading.value = false }
}

const handleReset = () => {
  Object.assign(form, {
    packageName: '', weight: null, volume: null, declaredValue: null,
    senderName: '', receiverName: '', receiverPhone: '', receiverAddress: '', notes: ''
  })
  result.value = null
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
</style>
