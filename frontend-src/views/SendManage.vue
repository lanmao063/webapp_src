<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>寄件审批</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="table-card" shadow="never">
      <template #header><span>待审核寄件申请</span></template>
      <el-table :data="tableData" stripe border style="width: 100%;">
        <el-table-column prop="id" label="编号" width="80" />
        <el-table-column prop="trackingNumber" label="快递单号" width="180" />
        <el-table-column prop="packageName" label="包裹名称" width="120" />
        <el-table-column prop="weight" label="重量(kg)" width="100" />
        <el-table-column prop="fee" label="费用" width="80">
          <template #default="{ row }">￥{{ row.fee }}</template>
        </el-table-column>
        <el-table-column prop="senderName" label="寄件人" width="100" />
        <el-table-column prop="senderPhone" label="寄件人电话" width="130" />
        <el-table-column prop="receiverName" label="收件人" width="100" />
        <el-table-column label="取件方式" width="150">
          <template #default="{ row }">
            {{ row.pickupMethod === 'DOOR_PICKUP' ? '上门取件' : '自行寄出' }}
          </template>
        </el-table-column>
        <el-table-column prop="appointmentTime" label="预约时间" width="130">
          <template #default="{ row }">{{ row.appointmentTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button type="success" size="small" link @click="openApprove(row)">同意</el-button>
            <el-button type="danger" size="small" link @click="handleReject(row)">拒绝</el-button>
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

    <el-dialog v-model="dialogVisible" title="审核寄件申请" width="500px">
      <el-form :model="approveForm" label-width="110px">
        <el-form-item label="包裹名称"><span>{{ approveForm.packageName }}</span></el-form-item>
        <el-form-item label="重量(kg)">
          <el-input-number v-model="approveForm.weight" :min="0" :precision="3" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="费用">
          <el-input-number v-model="approveForm.fee" :min="0" :precision="2" style="width: 100%;" />
        </el-form-item>
        <el-form-item label="取件方式">
          <el-radio-group v-model="approveForm.pickupMethod">
            <el-radio value="SELF_DROP">自行寄出</el-radio>
            <el-radio value="DOOR_PICKUP">上门取件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预约时间" v-if="approveForm.pickupMethod === 'DOOR_PICKUP'">
          <el-select v-model="approveForm.appointmentTime" placeholder="选择时间段" style="width: 100%;">
            <el-option v-for="t in timeSlots" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleApprove" :loading="loading">确认同意</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import request from '@/utils/request'

const tableData = ref([])
const pagination = reactive({ currentPage: 1, pageSize: 10, total: 0 })
const dialogVisible = ref(false)
const loading = ref(false)
const timeSlots = ['08:00-09:00','09:00-10:00','10:00-11:00','11:00-12:00','12:00-13:00','13:00-14:00','14:00-15:00','15:00-16:00','16:00-17:00','17:00-18:00']

const approveForm = reactive({
  id: null, packageName: '', weight: null, fee: null,
  pickupMethod: 'SELF_DROP', appointmentTime: ''
})

const fetchData = async () => {
  try {
    const res = await request.get('/send-package/pending-list', {
      params: { page: pagination.currentPage, size: pagination.pageSize }
    })
    tableData.value = res.data.records
    pagination.total = res.data.total
  } catch {}
}

const openApprove = (row) => {
  approveForm.id = row.id
  approveForm.packageName = row.packageName
  approveForm.weight = row.weight
  approveForm.fee = row.fee
  approveForm.pickupMethod = row.pickupMethod || 'SELF_DROP'
  approveForm.appointmentTime = row.appointmentTime || ''
  dialogVisible.value = true
}

const handleApprove = async () => {
  loading.value = true
  try {
    await request.put(`/send-package/${approveForm.id}/approve`, {
      weight: approveForm.weight,
      fee: approveForm.fee,
      pickupMethod: approveForm.pickupMethod,
      appointmentTime: approveForm.pickupMethod === 'DOOR_PICKUP' ? approveForm.appointmentTime : null
    })
    ElMessage.success('审核通过')
    dialogVisible.value = false
    fetchData()
  } catch {} finally { loading.value = false }
}

const handleReject = async (row) => {
  try {
    await ElMessageBox.confirm(`确定拒绝该寄件申请？`, '拒绝确认', { type: 'warning' })
  } catch { return }
  try {
    await request.put(`/send-package/${row.id}/reject`)
    ElMessage.success('已拒绝')
    fetchData()
  } catch {}
}

fetchData()
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.pagination-wrapper { display: flex; justify-content: flex-end; margin-top: 16px; }
</style>
