<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>包裹查询</el-breadcrumb-item>
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
        <el-table-column prop="trackingNumber" label="快递单号" min-width="160">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.trackingNumber, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column prop="senderName" label="寄件人" width="100">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.senderName, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column prop="senderPhone" label="寄件手机号" width="130">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.senderPhone, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column prop="receiverName" label="收件人" width="100">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.receiverName, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column prop="receiverPhone" label="收件人手机号" width="130">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.receiverPhone, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column prop="pickupCode" label="取件码" width="120">
          <template #default="{ row }">
            <span v-html="highlightMatch(row.pickupCode, searchForm.keyword)" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.status === 'IN_WAREHOUSE'" type="warning" size="small">在库</el-tag>
            <el-tag v-else-if="row.status === 'CHECKED_OUT'" type="success" size="small">已出库</el-tag>
            <el-tag v-else-if="row.sendStatus === 'COLLECTED'" type="primary" size="small">运输中</el-tag>
            <el-tag v-else-if="row.sendStatus === 'PAID'" type="warning" size="small">待揽收</el-tag>
            <el-tag v-else-if="row.sendStatus === 'APPROVED'" type="warning" size="small">待付款</el-tag>
            <el-tag v-else-if="row.sendStatus === 'SUBMITTED'" type="info" size="small">已提交</el-tag>
            <el-tag v-else-if="row.sendStatus === 'REJECTED'" type="danger" size="small">已驳回</el-tag>
            <span v-else class="text-secondary">运输中</span>
          </template>
        </el-table-column>
        <el-table-column prop="enterTime" label="入库时间" width="160">
          <template #default="{ row }">{{ row.enterTime || '-' }}</template>
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
import { reactive, ref, onMounted } from 'vue'
import request from '@/utils/request'

const searchForm = reactive({ keyword: '' })
const tableData = ref([])
const loading = ref(false)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const fetchData = async () => {
  loading.value = true
  try {
    const kw = searchForm.keyword.trim()
    const res = await request.get('/package/search', {
      params: {
        keyword: kw,
        page: pagination.currentPage,
        size: pagination.pageSize
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {} finally { loading.value = false }
}

const escapeHtml = (text) => {
  if (!text) return ''
  return String(text).replace(/</g, '&lt;').replace(/>/g, '&gt;')
}

const highlightMatch = (text, keyword) => {
  if (!text || !keyword) return escapeHtml(text) || '-'
  const escaped = escapeHtml(String(text))
  const escapedKeyword = keyword.replace(/[.*+?^${}()|[\]\\]/g, '\\$&')
  const regex = new RegExp(`(${escapedKeyword})`, 'gi')
  return escaped.replace(regex, '<mark class="highlight">$1</mark>')
}

onMounted(() => fetchData())

const handleSearch = () => { pagination.currentPage = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; pagination.currentPage = 1; fetchData() }
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }

:deep(.highlight) {
  background-color: #fff3cd;
  color: #856404;
  padding: 0 2px;
  border-radius: 2px;
}
</style>
