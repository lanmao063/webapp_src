<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>自动出库记录</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="关键字">
          <el-input v-model="searchForm.keyword" placeholder="单号/收件人/电话" clearable style="width: 280px;" @keyup.enter="handleSearch" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" v-loading="loading">
      <el-table :data="tableData" stripe class="w-full">
        <el-table-column prop="trackingNumber" label="快递单号" min-width="160" />
        <el-table-column prop="packageName" label="包裹名称" width="120">
          <template #default="{ row }">{{ row.packageName || '-' }}</template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column prop="receiverPhone" label="收件人电话" width="130" />
        <el-table-column prop="pickupCode" label="取件码" width="120" />
        <el-table-column prop="enterTime" label="入库时间" width="160">
          <template #default="{ row }">{{ row.enterTime || '-' }}</template>
        </el-table-column>
        <el-table-column prop="autoCheckoutTime" label="自动出库时间" width="160">
          <template #default="{ row }">{{ row.autoCheckoutTime || '-' }}</template>
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
import { reactive, ref } from 'vue'
import request from '@/utils/request'

const searchForm = reactive({ keyword: '' })
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const res = await request.get('/inbound/auto-checkout-list', {
      params: {
        keyword: searchForm.keyword,
        page: pagination.currentPage,
        size: pagination.pageSize
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {} finally { loading.value = false }
}

const handleSearch = () => { pagination.currentPage = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; handleSearch() }
fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }

</style>
