<template>
  <div class="welcome-container">
    <!-- REGULAR: 普通用户 - 我的取件码 -->
    <template v-if="role === 'REGULAR'">
      <el-row :gutter="20" style="width:100%;max-width:950px;margin-bottom:20px;" v-if="unpaidCount > 0">
        <el-col :span="24">
          <el-card shadow="hover" class="hint-card" @click="$router.push('/MyUnpaid')">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <span style="font-size:15px;">您有 <strong style="color:#e6a23c;">{{ unpaidCount }}</strong> 笔待付款订单</span>
              <el-button type="warning" size="small" @click.stop="$router.push('/MyUnpaid')">去付款</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <h1>我的取件码</h1>
      <el-table :data="pickupList" stripe border style="width: 100%; max-width: 950px; margin-top: 20px;">
        <el-table-column prop="pickupCode" label="取件码" width="150">
          <template #default="{ row }">
            <el-tag type="success" size="large">{{ row.pickupCode }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="柜型" width="100">
          <template #default="{ row }">
            <el-tag
              :type="row.cabinetType === 'LARGE' ? 'danger' : row.cabinetType === 'MEDIUM' ? 'warning' : 'success'"
              size="small"
              effect="dark"
            >
              <strong v-if="row.cabinetType === 'LARGE'">大重件</strong>
              <span v-else-if="row.cabinetType === 'MEDIUM'">中件</span>
              <span v-else>小件</span>
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="packageName" label="包裹类型" width="150" />
        <el-table-column prop="trackingNumber" label="快递单号" width="200" />
        <el-table-column prop="enterTime" label="入库时间" width="180">
          <template #default="{ row }">{{ row.enterTime || '-' }}</template>
        </el-table-column>
        <el-table-column label="代取人" width="140">
          <template #default="{ row }">
            <el-tag v-if="row.proxyPhone" type="info" size="small">{{ row.proxyPhone }}</el-tag>
            <span v-else style="color: #909399;">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button
              type="warning"
              size="small"
              link
              @click="openProxyDialog(row)"
              :disabled="!!row.proxyPhone"
            >
              {{ row.proxyPhone ? '已设置' : '代取' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <p v-if="pickupList.length === 0" style="color:#909399;margin-top:40px;">暂无待取包裹</p>
    </template>

    <!-- COURIER: 快递员 - 统计卡片 -->
    <template v-if="role === 'COURIER'">
      <el-row :gutter="20" style="width:100%;max-width:600px;margin-bottom:20px;" v-if="pendingCollectCount > 0">
        <el-col :span="24">
          <el-card shadow="hover" class="hint-card" @click="$router.push('/PickupQuery')">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <span style="font-size:15px;">您有 <strong style="color:#e6a23c;">{{ pendingCollectCount }}</strong> 笔待揽收订单</span>
              <el-button type="warning" size="small" @click.stop="$router.push('/PickupQuery')">去揽收</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <h1>工作台</h1>
      <el-row :gutter="20" style="margin-top: 20px; width: 100%; max-width: 600px;">
        <el-col :span="12">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">今日揽收</p>
              <h2 style="color:#409eff;">{{ stats.todayDeliveries }}</h2>
            </div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">今日入库</p>
              <h2 style="color:#67c23a;">{{ stats.todayWarehoused }}</h2>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- MANAGER: 驿站管理员 - 概览卡片 -->
    <template v-if="role === 'MANAGER'">
      <el-row :gutter="20" style="width:100%;max-width:900px;margin-bottom:20px;" v-if="pendingApprovalCount > 0">
        <el-col :span="24">
          <el-card shadow="hover" class="hint-card" @click="$router.push('/SendManage')">
            <div style="display:flex;align-items:center;justify-content:space-between;">
              <span style="font-size:15px;">有 <strong style="color:#e6a23c;">{{ pendingApprovalCount }}</strong> 笔寄件申请待审批</span>
              <el-button type="warning" size="small" @click.stop="$router.push('/SendManage')">去审批</el-button>
            </div>
          </el-card>
        </el-col>
      </el-row>
      <h1>数据概览</h1>
      <el-row :gutter="20" style="margin-top: 20px; width: 100%; max-width: 900px;">
        <el-col :span="6" v-for="card in cards" :key="card.label">
          <el-card shadow="hover">
            <div style="text-align:center;">
              <p style="color:#909399;font-size:14px;">{{ card.label }}</p>
              <h2 :style="{color:card.color}">{{ card.value }}</h2>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </template>

    <!-- 代取人设置对话框 -->
    <el-dialog v-model="proxyDialogVisible" title="设置代取人" width="450px">
      <el-form :model="proxyForm" :rules="proxyRules" ref="proxyFormRef" label-width="110px">
        <el-form-item label="包裹名称">
          <span>{{ proxyForm.packageName }}</span>
        </el-form-item>
        <el-form-item label="取件码">
          <el-tag type="success">{{ proxyForm.pickupCode }}</el-tag>
        </el-form-item>
        <el-form-item label="代取人手机号" prop="proxyPhone">
          <el-input v-model="proxyForm.proxyPhone" placeholder="请输入代取人的手机号码" maxlength="11" clearable />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="proxyDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleProxySubmit" :loading="proxyLoading">确认设置</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { getUserInfo } from '@/utils/auth'
import request from '@/utils/request'

const user = getUserInfo()
const role = ref(user?.role || '')

const pickupList = ref([])
const unpaidCount = ref(0)
const pendingApprovalCount = ref(0)
const pendingCollectCount = ref(0)
const stats = reactive({ todayDeliveries: 0, todayWarehoused: 0 })

const cards = reactive([
  { label: '在库包裹', value: 0, color: '#e6a23c' },
  { label: '已出库', value: 0, color: '#67c23a' },
  { label: '总包裹', value: 0, color: '#409eff' },
  { label: '未处理异常', value: 0, color: '#f56c6c' }
])

const proxyDialogVisible = ref(false)
const proxyLoading = ref(false)
const proxyFormRef = ref(null)
const proxyForm = reactive({
  id: null,
  packageName: '',
  pickupCode: '',
  proxyPhone: ''
})
const proxyRules = {
  proxyPhone: [
    { required: true, message: '请输入代取人手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const fetchPickupCodes = () => {
  request.get('/inbound/my-pickup-codes').then(res => {
    pickupList.value = res.data || []
  }).catch(() => {})
}

const openProxyDialog = (row) => {
  proxyForm.id = row.id
  proxyForm.packageName = row.packageName
  proxyForm.pickupCode = row.pickupCode
  proxyForm.proxyPhone = ''
  proxyDialogVisible.value = true
}

const handleProxySubmit = async () => {
  if (!proxyFormRef.value) return
  try {
    await proxyFormRef.value.validate()
  } catch {
    return
  }
  proxyLoading.value = true
  try {
    await request.put(`/inbound/${proxyForm.id}/authorize-proxy`, {
      proxyPhone: proxyForm.proxyPhone
    })
    ElMessage.success('代取人设置成功')
    proxyDialogVisible.value = false
    fetchPickupCodes()
  } catch {} finally { proxyLoading.value = false }
}

onMounted(() => {
  if (role.value === 'REGULAR') {
    fetchPickupCodes()
    request.get('/send-package/my-unpaid', { params: { page: 1, size: 1 } }).then(res => {
      unpaidCount.value = res.data.total || 0
    }).catch(() => {})
  } else if (role.value === 'COURIER') {
    request.get('/statistics/courier-overview').then(res => {
      Object.assign(stats, res.data)
    }).catch(() => {})
    request.get('/send-package/paid-list', { params: { page: 1, size: 1 } }).then(res => {
      pendingCollectCount.value = res.data.total || 0
    }).catch(() => {})
  } else if (role.value === 'MANAGER') {
    request.get('/statistics/overview').then(res => {
      cards[0].value = res.data.totalInWarehouse
      cards[1].value = res.data.totalPickedUp
      cards[2].value = res.data.totalParcels
      cards[3].value = res.data.unresolvedErrors
    }).catch(() => {})
    request.get('/send-package/pending-list', { params: { page: 1, size: 1 } }).then(res => {
      pendingApprovalCount.value = res.data.total || 0
    }).catch(() => {})
  }
})
</script>

<style scoped>
.welcome-container {
  display: flex;
  flex-direction: column;
  justify-content: flex-start;
  align-items: center;
  height: 100%;
  padding-top: 40px;
  color: #606266;
}
.welcome-container h1 {
  font-size: 28px;
  margin-bottom: 16px;
}
.hint-card { cursor: pointer; }
.hint-card:hover { border-color: #409eff; }
</style>
