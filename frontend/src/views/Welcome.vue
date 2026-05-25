<template>
  <div class="welcome-container" v-loading="loading">
    <!-- 欢迎横幅 -->
    <el-card class="welcome-banner" shadow="never">
      <div class="banner-content">
        <el-icon :size="40" color="var(--color-primary)"><HomeFilled /></el-icon>
        <div class="banner-text">
          <h2>{{ greeting }}</h2>
          <p class="banner-subtitle">欢迎使用莱鸟驿站管理系统</p>
        </div>
      </div>
    </el-card>

    <!-- 提示卡片（欠款/待审批/待揽收） -->
    <el-card v-if="hintCard.show" shadow="hover" class="hint-card" @click="$router.push(hintCard.link)">
      <div class="hint-content">
        <span class="hint-text">{{ hintCard.text }}</span>
        <el-button type="warning" size="small" @click.stop="$router.push(hintCard.link)">{{ hintCard.btnText }}</el-button>
      </div>
    </el-card>

    <!-- REGULAR: 我的取件码 -->
    <template v-if="role === 'REGULAR'">
      <h2 class="section-title">我的取件码</h2>
      <el-table :data="pickupList" stripe class="welcome-table" v-if="pickupList.length > 0">
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
              <span v-if="row.cabinetType === 'LARGE'">大重件</span>
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
            <span v-else class="text-secondary">-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="100">
          <template #default="{ row }">
            <el-button type="warning" size="small" link :disabled="!!row.proxyPhone" @click="openProxyDialog(row)">
              {{ row.proxyPhone ? '已设置' : '代取' }}
            </el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && pickupList.length === 0" description="暂无待取包裹" />
    </template>

    <!-- COURIER: 统计卡片 -->
    <template v-if="role === 'COURIER'">
      <h2 class="section-title">工作台</h2>
      <div class="stat-grid" style="max-width: 500px;">
        <el-card shadow="hover" class="stat-card">
          <p class="stat-card-label">今日揽收</p>
          <p class="stat-card-value" style="color: var(--color-primary);">{{ stats.todayDeliveries }}</p>
        </el-card>
        <el-card shadow="hover" class="stat-card">
          <p class="stat-card-label">今日入库</p>
          <p class="stat-card-value" style="color: var(--color-success);">{{ stats.todayWarehoused }}</p>
        </el-card>
      </div>
    </template>

    <!-- MANAGER: 管理概览 -->
    <template v-if="role === 'MANAGER'">
      <h2 class="section-title">数据概览</h2>
      <div class="stat-grid" style="max-width: 900px;">
        <el-card v-for="card in cards" :key="card.label" shadow="hover" class="stat-card">
          <p class="stat-card-label">{{ card.label }}</p>
          <p class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</p>
        </el-card>
      </div>
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
import { ref, reactive, computed, onMounted } from 'vue'
import { ElMessage } from 'element-plus'
import { HomeFilled } from '@element-plus/icons-vue'
import { getUserInfo } from '@/utils/auth'
import request from '@/utils/request'

const user = getUserInfo()
const role = ref(user?.role || '')
const loading = ref(true)

const pickupList = ref([])
const unpaidCount = ref(0)
const pendingApprovalCount = ref(0)
const pendingCollectCount = ref(0)
const stats = reactive({ todayDeliveries: 0, todayWarehoused: 0 })

const cards = reactive([
  { label: '在库包裹', value: 0, color: '#e6a23c' },
  { label: '已出库', value: 0, color: '#67c23a' },
  { label: '已揽收', value: 0, color: '#409eff' },
  { label: '未处理异常', value: 0, color: '#f56c6c' }
])

const roleNameMap = { REGULAR: '用户', COURIER: '快递员', MANAGER: '管理员' }
const greeting = computed(() => {
  const name = roleNameMap[role.value] || '用户'
  const hour = new Date().getHours()
  const prefix = hour < 12 ? '早上好' : hour < 18 ? '下午好' : '晚上好'
  return `${prefix}，${name}`
})

const hintCard = computed(() => {
  if (role.value === 'REGULAR' && unpaidCount.value > 0) {
    return { show: true, text: `您有 ${unpaidCount.value} 笔待付款订单`, btnText: '去付款', link: '/MyUnpaid' }
  }
  if (role.value === 'COURIER' && pendingCollectCount.value > 0) {
    return { show: true, text: `您有 ${pendingCollectCount.value} 笔待揽收订单`, btnText: '去揽收', link: '/PickupQuery' }
  }
  if (role.value === 'MANAGER' && pendingApprovalCount.value > 0) {
    return { show: true, text: `有 ${pendingApprovalCount.value} 笔寄件申请待审批`, btnText: '去审批', link: '/SendManage' }
  }
  return { show: false, text: '', btnText: '', link: '' }
})

const proxyDialogVisible = ref(false)
const proxyLoading = ref(false)
const proxyFormRef = ref(null)
const proxyForm = reactive({ id: null, packageName: '', pickupCode: '', proxyPhone: '' })
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
  try { await proxyFormRef.value.validate() } catch { return }
  proxyLoading.value = true
  try {
    await request.put(`/inbound/${proxyForm.id}/authorize-proxy`, { proxyPhone: proxyForm.proxyPhone })
    ElMessage.success('代取人设置成功')
    proxyDialogVisible.value = false
    fetchPickupCodes()
  } catch {} finally { proxyLoading.value = false }
}

onMounted(async () => {
  loading.value = true
  try {
    if (role.value === 'REGULAR') {
      fetchPickupCodes()
      const res = await request.get('/send-package/my-unpaid', { params: { page: 1, size: 1 } })
      unpaidCount.value = res.data.total || 0
    } else if (role.value === 'COURIER') {
      const [overview, paid] = await Promise.all([
        request.get('/statistics/courier-overview'),
        request.get('/send-package/paid-list', { params: { page: 1, size: 1 } })
      ])
      Object.assign(stats, overview.data)
      pendingCollectCount.value = paid.data.total || 0
    } else if (role.value === 'MANAGER') {
      const [overview, pending] = await Promise.all([
        request.get('/statistics/overview'),
        request.get('/send-package/pending-list', { params: { page: 1, size: 1 } })
      ])
      cards[0].value = overview.data.totalInWarehouse
      cards[1].value = overview.data.totalPickedUp
      cards[2].value = overview.data.totalCollected
      cards[3].value = overview.data.unresolvedErrors
      pendingApprovalCount.value = pending.data.total || 0
    }
  } catch {} finally {
    loading.value = false
  }
})
</script>

<style scoped>
.welcome-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  height: 100%;
  padding-top: 24px;
}
.section-title {
  font-size: 22px;
  margin: 20px 0 16px;
  color: var(--color-text-primary);
}
.welcome-banner {
  width: 100%;
  max-width: 900px;
  margin-bottom: var(--spacing-md);
}
.banner-content {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: var(--spacing-sm) 0;
}
.banner-text h2 {
  font-size: 20px;
  color: var(--color-text-primary);
  margin: 0 0 4px 0;
}
.banner-subtitle {
  margin: 0;
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
.hint-card {
  width: 100%;
  max-width: 900px;
  margin-bottom: var(--spacing-md);
  cursor: pointer;
}
.hint-card:hover {
  border-color: var(--color-primary);
}
.hint-content {
  display: flex;
  align-items: center;
  justify-content: space-between;
}
.hint-text {
  font-size: 15px;
}

.welcome-table {
  width: 100%;
  max-width: 950px;
  margin-top: var(--spacing-sm);
}
</style>
