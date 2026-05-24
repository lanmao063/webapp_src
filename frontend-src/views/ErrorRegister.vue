<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>异常登记</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never" style="max-width: 550px;">
      <template #header><span>登记包裹异常</span></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="包裹编号" prop="parcelId">
          <el-input v-model="form.parcelId" placeholder="请输入包裹编号" />
        </el-form-item>
        <el-form-item label="异常类型" prop="errorType">
          <el-select v-model="form.errorType" placeholder="请选择异常类型" style="width: 100%;">
            <el-option label="包裹破损" value="DAMAGED" />
            <el-option label="包裹丢失" value="LOST" />
            <el-option label="地址错误" value="WRONG_ADDRESS" />
            <el-option label="其他" value="OTHER" />
          </el-select>
        </el-form-item>
        <el-form-item label="详细描述" prop="description">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请描述异常情况" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交登记</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const formRef = ref(null)
const loading = ref(false)
const form = reactive({ parcelId: '', errorType: '', description: '' })

const rules = {
  parcelId: [{ required: true, message: '请输入包裹编号', trigger: 'blur' }],
  errorType: [{ required: true, message: '请选择异常类型', trigger: 'change' }],
  description: [{ required: true, message: '请输入描述', trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    await request.post('/error-parcel', form)
    ElMessage.success('异常登记成功')
    Object.assign(form, { parcelId: '', errorType: '', description: '' })
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
</style>
