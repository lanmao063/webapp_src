<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <div class="auth-title">
        <el-icon :size="36" color="var(--color-primary)"><Box /></el-icon>
        <h2>莱鸟</h2>
      </div>
      <el-tabs v-model="activeTab" class="login-tabs" @tab-change="handleTabChange">
        <el-tab-pane label="用户登录" name="login">
          <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
            <el-form-item label="用户名" prop="username">
              <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
            </el-form-item>
            <el-form-item label="密码" prop="password">
              <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password clearable />
            </el-form-item>
            <el-form-item label="角色" prop="role">
              <el-select v-model="loginForm.role" placeholder="请选择角色" class="w-full">
                <el-option label="普通用户" value="REGULAR" />
                <el-option label="快递员" value="COURIER" />
                <el-option label="驿站管理员" value="MANAGER" />
              </el-select>
            </el-form-item>
            <el-form-item>
              <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleLogin" :loading="loading" class="w-full">登录</el-button>
            </el-form-item>
          </el-form>
          <div class="login-links">
            <el-button type="primary" link @click="$router.push('/register')">注册账号</el-button>
            <el-button type="primary" link @click="$router.push('/forgot-password')">忘记密码</el-button>
          </div>
        </el-tab-pane>

        <el-tab-pane label="包裹出库" name="checkout">
          <el-form :model="checkoutForm" ref="checkoutFormRef" label-width="80px">
            <el-form-item label="快递单号">
              <el-input
                ref="trackingInputRef"
                v-model="checkoutForm.trackingNumber"
                placeholder="请输入或扫描快递单号"
                clearable
                @keyup.enter="handleCheckoutQuery"
              />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="checkoutForm.phone" placeholder="请输入收件人手机号" clearable @keyup.enter="handleCheckoutQuery" />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleCheckoutQuery" :loading="checkoutLoading" class="w-full">
                查询包裹
              </el-button>
            </el-form-item>
          </el-form>

          <!-- 包裹信息确认区 -->
          <div v-if="checkoutPackageInfo" class="package-info-card">
            <el-descriptions :column="1" border size="small">
              <el-descriptions-item label="快递单号">{{ checkoutPackageInfo.trackingNumber }}</el-descriptions-item>
              <el-descriptions-item label="包裹名称">{{ checkoutPackageInfo.packageName }}</el-descriptions-item>
              <el-descriptions-item label="取件码">
                <el-tag type="success">{{ checkoutPackageInfo.pickupCode }}</el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="柜型">
                <el-tag :type="cabinetTagType(checkoutPackageInfo.cabinetType)">
                  {{ cabinetLabel(checkoutPackageInfo.cabinetType) }}
                </el-tag>
              </el-descriptions-item>
              <el-descriptions-item label="入库时间">{{ checkoutPackageInfo.enterTime }}</el-descriptions-item>
            </el-descriptions>
            <el-button type="success" @click="handleCheckoutConfirm" :loading="checkoutLoading" class="w-full" style="margin-top: 12px;">
              确认出库
            </el-button>
          </div>

          <!-- 出库结果区 -->
          <div v-if="checkoutResult" class="checkout-result">
            <el-alert title="出库成功！" type="success" :closable="false" show-icon center />
            <div class="result-detail">
              <p>取件码：<strong>{{ checkoutResult.pickupCode }}</strong></p>
              <p v-if="checkoutResult.remainingCount > 0">
                该收件人还有 <strong>{{ checkoutResult.remainingCount }}</strong> 个包裹待取：
              </p>
              <p v-else>该收件人已无待取包裹</p>
              <div v-if="checkoutResult.remainingCount > 0" class="remaining-list">
                <div v-for="(item, idx) in checkoutResult.remainingPackages" :key="idx" class="remaining-item">
                  <el-tag type="warning">{{ item.pickupCode }}</el-tag>
                  <span class="remaining-tracking">{{ item.trackingNumber }}</span>
                </div>
              </div>
            </div>
            <p class="result-tip">信息将在 {{ countdown }} 秒后自动清除</p>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Box } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { setUserInfo, saveCredentials, getSavedCredentials, clearCredentials } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)
const checkoutFormRef = ref(null)
const trackingInputRef = ref(null)
const loading = ref(false)
const checkoutLoading = ref(false)
const activeTab = ref('login')

const loginForm = reactive({
  username: '',
  password: '',
  role: 'REGULAR',
  rememberMe: false
})

const checkoutForm = reactive({
  trackingNumber: '',
  phone: ''
})

const checkoutPackageInfo = ref(null)
const checkoutResult = ref(null)
const countdown = ref(5)
let countdownTimer = null

onMounted(() => {
  const saved = getSavedCredentials()
  if (saved) {
    loginForm.username = saved.username
    loginForm.password = saved.password
    loginForm.rememberMe = true
  }
})

onBeforeUnmount(() => {
  clearCheckoutTimer()
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    const res = await request.post('/auth/login', {
      username: loginForm.username,
      password: loginForm.password,
      role: loginForm.role
    })
    const { userId, username, role } = res.data
    setUserInfo({ userId, username, role })

    if (loginForm.rememberMe) {
      saveCredentials(loginForm.username, loginForm.password)
    } else {
      clearCredentials()
    }

    ElMessage.success('登录成功')
    router.push('/Welcome')
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}

const handleTabChange = (tabName) => {
  if (tabName === 'checkout') {
    resetCheckout()
    nextTick(() => trackingInputRef.value?.focus())
  }
}

const handleCheckoutQuery = async () => {
  if (!checkoutForm.trackingNumber.trim()) {
    ElMessage.warning('请输入快递单号')
    return
  }
  if (!checkoutForm.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  checkoutLoading.value = true
  try {
    const res = await request.get('/inbound/public-search', {
      params: {
        trackingNumber: checkoutForm.trackingNumber.trim(),
        phone: checkoutForm.phone.trim()
      }
    })
    checkoutPackageInfo.value = res.data
  } catch {
    checkoutPackageInfo.value = null
  } finally {
    checkoutLoading.value = false
  }
}

const handleCheckoutConfirm = async () => {
  checkoutLoading.value = true
  try {
    const res = await request.put('/inbound/public-checkout', {
      trackingNumber: checkoutForm.trackingNumber.trim(),
      phone: checkoutForm.phone.trim()
    })
    checkoutResult.value = res.data
    checkoutPackageInfo.value = null
    startCountdown()
  } catch {
    // error already handled by interceptor
  } finally {
    checkoutLoading.value = false
  }
}

const startCountdown = () => {
  clearCheckoutTimer()
  countdown.value = 5
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) {
      resetCheckout()
    }
  }, 1000)
}

const clearCheckoutTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const resetCheckout = () => {
  clearCheckoutTimer()
  checkoutForm.trackingNumber = ''
  checkoutForm.phone = ''
  checkoutPackageInfo.value = null
  checkoutResult.value = null
  nextTick(() => trackingInputRef.value?.focus())
}

const cabinetTagType = (type) => {
  return type === 'SMALL' ? 'success' : type === 'MEDIUM' ? 'warning' : 'danger'
}

const cabinetLabel = (type) => {
  return type === 'SMALL' ? '小件' : type === 'MEDIUM' ? '中件' : '大件'
}
</script>

<style scoped>
.login-tabs {
  margin-top: -10px;
}
.package-info-card {
  margin-top: var(--spacing-md);
  padding: 12px;
  background: var(--color-bg-hover);
  border-radius: var(--radius-md);
}
.checkout-result {
  margin-top: var(--spacing-md);
}
.result-detail {
  margin-top: 12px;
  padding: 12px;
  background: #f0f9eb;
  border-radius: var(--radius-md);
}
.result-detail p {
  margin: 6px 0;
  font-size: var(--font-size-base);
}
.remaining-list {
  margin-top: var(--spacing-sm);
}
.remaining-item {
  display: flex;
  align-items: center;
  gap: 10px;
  margin: 6px 0;
  padding: 6px 10px;
  background: #fdf6ec;
  border-radius: var(--radius-sm);
}
.remaining-tracking {
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}
.result-tip {
  text-align: center;
  font-size: var(--font-size-xs);
  color: var(--color-text-placeholder);
  margin-top: 12px;
}
</style>
