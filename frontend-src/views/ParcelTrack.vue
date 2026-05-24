<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>查件</el-breadcrumb-item>
    </el-breadcrumb>
    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="包裹编号">
          <el-input v-model="searchForm.id" placeholder="请输入包裹编号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>
    <el-card class="result-card" shadow="never" v-if="parcel">
      <template #header><span>包裹详情</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="包裹编号">{{ parcel.id }}</el-descriptions-item>
        <el-descriptions-item label="包裹名称">{{ parcel.packageName }}</el-descriptions-item>
        <el-descriptions-item label="寄件人">{{ parcel.senderName }}</el-descriptions-item>
        <el-descriptions-item label="收件人">{{ parcel.receiverName }}</el-descriptions-item>
        <el-descriptions-item label="重量(kg)">{{ parcel.weight }}</el-descriptions-item>
        <el-descriptions-item label="体积(cm³)">{{ parcel.volume }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag :type="parcel.status === 'IN_WAREHOUSE' ? 'warning' : 'success'" size="small">
            {{ parcel.status === 'IN_WAREHOUSE' ? '在库' : '已取件' }}
          </el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="入库时间">{{ parcel.enterTime }}</el-descriptions-item>
        <el-descriptions-item label="取件时间" v-if="parcel.outTime">{{ parcel.outTime }}</el-descriptions-item>
        <el-descriptions-item label="备注">{{ parcel.notes || '-' }}</el-descriptions-item>
      </el-descriptions>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const searchForm = reactive({ id: '' })
const parcel = ref(null)

const handleSearch = async () => {
  if (!searchForm.id) {
    ElMessage.warning('请输入包裹编号')
    return
  }
  try {
    const res = await request.get(`/parcel/${searchForm.id}`)
    parcel.value = res.data
  } catch {
    parcel.value = null
  }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }
</style>
