<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>异常处理</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="table-card" shadow="never">
      <template #header><span>待处理异常列表</span></template>
      <el-table :data="tableData" stripe border style="width: 100%;">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="parcelId" label="包裹编号" min-width="140" />
        <el-table-column prop="errorType" label="异常类型" width="120" />
        <el-table-column prop="description" label="描述" min-width="200" />
        <el-table-column prop="reportedBy" label="报告人" width="100" />
        <el-table-column label="操作" width="120">
          <template #default="{ row }">
            <el-button type="primary" size="small" link @click="openHandleDialog(row)">处理</el-button>
          </template>
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

    <el-dialog v-model="dialogVisible" title="处理异常" width="500px">
      <el-form :model="handleForm" label-width="100px">
        <el-form-item label="包裹编号"><span>{{ handleForm.parcelId }}</span></el-form-item>
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

const tableData = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const loading = ref(false)
const handleForm = reactive({ id: null, parcelId: '', errorType: '', handleResult: '' })

const fetchData = async () => {
  try {
    const res = await request.get('/error-parcel/list', {
      params: { status: 'UNRESOLVED', page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

const openHandleDialog = (row) => {
  handleForm.id = row.id
  handleForm.parcelId = row.parcelId
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
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
