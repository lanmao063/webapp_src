<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>我的异常记录</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <template #header><span>我的异常报告</span></template>
      <el-table :data="tableData" stripe class="w-full">
        <el-table-column prop="trackingNumber" label="快递单号" min-width="160" />
        <el-table-column prop="errorType" label="异常类型" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="row.status === 'UNRESOLVED' ? 'danger' : 'success'" size="small">
              {{ row.status === 'UNRESOLVED' ? '未处理' : '已处理' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="处理情况" min-width="220">
          <template #default="{ row }">
            <template v-if="row.status === 'RESOLVED'">
              <div>处理人：{{ row.handlerName || '-' }}</div>
              <div>结果：{{ row.handleResult || '-' }}</div>
              <div class="text-secondary" style="font-size:12px;">{{ row.handleTime || '-' }}</div>
            </template>
            <span v-else class="text-secondary">等待处理</span>
          </template>
        </el-table-column>
        <el-table-column prop="createdAt" label="报告时间" width="160" />
      </el-table>
      <el-empty v-if="!loading && tableData.length === 0" description="暂无数据" />
      <div class="page-pagination">
        <el-pagination
          v-model:current-page="pagination.currentPage"
          v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper"
          background
          @size-change="fetchData"
          @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/error-parcel/my-list', {
      params: { page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {} finally { loading.value = false }
}

fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
