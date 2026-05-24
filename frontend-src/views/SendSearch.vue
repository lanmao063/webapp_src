<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>寄件查询</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="search-card" shadow="never">
      <el-form :model="searchForm" inline>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="单号/寄件人/收件人/电话" clearable style="width: 250px;" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" stripe border style="width: 100%;">
        <el-table-column prop="trackingNumber" label="快递单号" min-width="160" />
        <el-table-column prop="packageName" label="包裹名称" width="120" />
        <el-table-column prop="senderName" label="寄件人" width="100" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column prop="fee" label="费用(元)" width="100" />
        <el-table-column label="付款" width="80">
          <template #default="{ row }">
            <el-tag :type="row.isPaid ? 'success' : 'danger'" size="small">
              {{ row.isPaid ? '已付' : '未付' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">{{ statusLabel(row.status) }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
        <el-pagination
          v-model:current-page="pagination.currentPage" v-model:page-size="pagination.pageSize"
          :page-sizes="[10, 20, 50]" :total="pagination.total"
          layout="total, sizes, prev, pager, next, jumper" background
          @size-change="fetchData" @current-change="fetchData"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import request from '@/utils/request'

const searchForm = reactive({ keyword: '' })
const tableData = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const statusMap = { PENDING: '待处理', IN_TRANSIT: '运输中', DELIVERED: '已送达', RETURNED: '已退回' }
const statusLabel = (s) => statusMap[s] || s

const fetchData = async () => {
  try {
    const res = await request.get('/send-package/search', {
      params: { keyword: searchForm.keyword, page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

const handleSearch = () => { pagination.currentPage = 1; fetchData() }
fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
