<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>异常查询</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="search-card" shadow="never">
      <el-form inline>
        <el-form-item label="状态">
          <el-select v-model="statusFilter" placeholder="全部" clearable @change="fetchData" style="width: 150px;">
            <el-option label="未处理" value="UNRESOLVED" />
            <el-option label="已处理" value="RESOLVED" />
          </el-select>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card class="table-card" shadow="never" v-loading="tableLoading">
      <el-table :data="tableData" stripe class="w-full">
        <el-table-column prop="id" label="编号" width="80" />
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
        <el-table-column prop="reportedBy" label="报告人" width="100" />
        <el-table-column prop="reporterPhone" label="报告人手机号" width="130" />
        <el-table-column prop="createdAt" label="创建时间" width="160" />
        <el-table-column label="操作" width="140" fixed="right">
          <template #default="{ row }">
            <el-button v-if="row.status === 'UNRESOLVED'" type="primary" size="small" link @click="openHandleDialog(row)">处理</el-button>
            <span v-else class="text-secondary" style="font-size: 12px;">{{ row.handlerName || '-' }} 处理</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!tableLoading && tableData.length === 0" description="暂无数据" />
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

    <el-dialog v-model="dialogVisible" title="处理异常" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="快递单号"><span>{{ handleForm.trackingNumber }}</span></el-form-item>
        <el-form-item label="异常类型"><span>{{ handleForm.errorType }}</span></el-form-item>
        <el-form-item label="处理结果" prop="handleResult">
          <el-input v-model="handleForm.handleResult" type="textarea" :rows="3" placeholder="请输入处理结果" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit" :loading="loading">确认处理</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const statusFilter = ref('')
const tableData = ref([])
const tableLoading = ref(false)
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const loading = ref(false)
const handleForm = reactive({ id: null, trackingNumber: '', errorType: '', handleResult: '' })

const fetchData = async () => {
  tableLoading.value = true
  try {
    const res = await request.get('/error-parcel/list', {
      params: { status: statusFilter.value, page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {} finally { tableLoading.value = false }
}

const openHandleDialog = (row) => {
  handleForm.id = row.id
  handleForm.trackingNumber = row.trackingNumber
  handleForm.errorType = row.errorType
  handleForm.handleResult = ''
  dialogVisible.value = true
}

const handleSubmit = async () => {
  loading.value = true
  try {
    await request.put(`/error-parcel/${handleForm.id}/handle`, { handleResult: handleForm.handleResult })
    ElMessage.success('处理成功')
    dialogVisible.value = false
    fetchData()
  } catch {} finally { loading.value = false }
}

fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.search-card { margin-bottom: 16px; }

</style>
