<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>揽收</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <template #header><span>待揽收订单（已付款）</span></template>
      <el-table :data="tableData" stripe class="w-full">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="trackingNumber" label="快递单号" width="200" />
        <el-table-column prop="packageName" label="包裹名称" width="120" />
        <el-table-column prop="weight" label="重量(kg)" width="100" />
        <el-table-column label="费用" width="80">
          <template #default="{ row }">￥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column prop="senderName" label="寄件人" width="100" />
        <el-table-column prop="senderPhone" label="寄件人电话" width="130" />
        <el-table-column prop="senderAddress" label="寄件人地址" min-width="150" />
        <el-table-column label="取件方式" width="120">
          <template #default="{ row }">
            <el-tag :type="row.pickupMethod === 'DOOR_PICKUP' ? 'warning' : 'info'" size="small">
              {{ row.pickupMethod === 'DOOR_PICKUP' ? '上门取件' : '自行寄出' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="130">
          <template #default="{ row }">{{ row.appointmentTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="$router.push(`/SendDetail?id=${row.id}`)">详情</el-button>
            <el-button type="success" size="small" @click="handleCollect(row)">揽收</el-button>
          </template>
        </el-table-column>
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
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/send-package/paid-list', {
      params: { page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {} finally { loading.value = false }
}

const handleCollect = async (row) => {
  try {
    await ElMessageBox.confirm(`确认揽收：${row.trackingNumber}？`, '揽收确认', { type: 'success' })
  } catch { return }
  try {
    await request.put(`/send-package/${row.id}/collect`)
    ElMessage.success('揽收成功')
    fetchData()
  } catch {}
}

fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.page-pagination { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
