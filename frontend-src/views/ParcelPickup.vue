<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>取件</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card shadow="never">
      <template #header><span>取件确认</span></template>
      <el-form :model="form" label-width="100px" @keyup.enter="handlePickup">
        <el-form-item label="包裹编号">
          <el-input v-model="form.id" ref="inputRef" placeholder="扫描/输入包裹编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="success" @click="handlePickup" :loading="loading">确认取件</el-button>
        </el-form-item>
      </el-form>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const form = reactive({ id: '' })
const loading = ref(false)
const inputRef = ref(null)

onMounted(() => {
  inputRef.value?.focus()
})

const handlePickup = async () => {
  if (!form.id) { ElMessage.warning('请输入包裹编号'); return }
  loading.value = true
  try {
    await request.put(`/parcel/${form.id}/pickup`)
    ElMessage.success('取件成功')
    form.id = ''
    inputRef.value?.focus()
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
</style>
