<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>包裹查询</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="关键字">
          <el-autocomplete
            v-model="searchForm.keyword"
            :fetch-suggestions="querySearch"
            placeholder="编号/名称/收件人/电话"
            clearable
            style="width: 280px;"
            @select="handleSelect"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="searchForm.status" placeholder="全部" clearable style="width: 150px;">
            <el-option label="在库" value="IN_WAREHOUSE" />
            <el-option label="已取件" value="PICKED_UP" />
            <el-option label="异常" value="ERROR" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSearch">查询</el-button>
          <el-button @click="handleReset">重置</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never">
      <el-table :data="tableData" stripe border style="width: 100%;">
        <el-table-column prop="id" label="包裹编号" min-width="140" />
        <el-table-column prop="packageName" label="名称" width="120" />
        <el-table-column prop="senderName" label="寄件人" width="100" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column label="体积(cm³)" width="110">
          <template #default="{ row }">
            <el-tag :type="row.volume < 12000 ? 'success' : row.volume > 125000 ? 'danger' : 'warning'" size="small">
              {{ row.volume < 12000 ? '小件' : row.volume > 125000 ? '大件' : '中件' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100">
          <template #default="{ row }">
            <el-tag :type="statusTag(row.status)" size="small">{{ statusLabel(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="enterTime" label="入库时间" width="160" />
        <el-table-column prop="outTime" label="取件时间" width="160">
          <template #default="{ row }">{{ row.outTime || '-' }}</template>
        </el-table-column>
      </el-table>
      <div class="pagination-wrapper">
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

const searchForm = reactive({ keyword: '', status: '' })
const tableData = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })

const statusMap = { IN_WAREHOUSE: '在库', PICKED_UP: '已取件', ERROR: '异常' }
const statusTagMap = { IN_WAREHOUSE: 'warning', PICKED_UP: 'success', ERROR: 'danger' }
const statusLabel = (s) => statusMap[s] || s
const statusTag = (s) => statusTagMap[s] || 'info'

let searchTimeout = null

const querySearch = (queryString, cb) => {
  if (!queryString) { cb([]); return }
  if (searchTimeout) clearTimeout(searchTimeout)
  searchTimeout = setTimeout(async () => {
    try {
      const res = await request.get('/parcel/search', {
        params: { keyword: queryString, page: 1, size: 10 }
      })
      const suggestions = (res.data.records || []).map(item => ({
        value: item.id,
        label: item.id + ' - ' + item.packageName + ' (' + statusLabel(item.status) + ')'
      }))
      cb(suggestions)
    } catch { cb([]) }
  }, 300)
}

const handleSelect = (item) => {
  searchForm.keyword = item.value
  handleSearch()
}

const fetchData = async () => {
  try {
    const res = await request.get('/parcel/search', {
      params: {
        keyword: searchForm.keyword,
        status: searchForm.status,
        page: pagination.currentPage,
        size: pagination.pageSize
      }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

const handleSearch = () => { pagination.currentPage = 1; fetchData() }
const handleReset = () => { searchForm.keyword = ''; searchForm.status = ''; handleSearch() }
fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
