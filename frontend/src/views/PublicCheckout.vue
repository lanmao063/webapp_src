<template>
  <div class="checkout-page">
    <div class="checkout-bg" :style="{ backgroundImage: `url(${bgImage})` }"></div>
    <div class="checkout-content">
      <el-card class="checkout-card">
        <div class="card-title">
          <img src="@/assets/logo.png" alt="logo" class="card-logo" />
          <h2>莱鸟 · 包裹出库</h2>
        </div>
        <p class="card-subtitle">无需登录，输入快递单号和收件人手机号即可取件</p>

        <el-form :model="form" ref="formRef" label-width="80px">
          <el-form-item label="快递单号">
            <el-input
              ref="inputRef"
              v-model="form.trackingNumber"
              placeholder="请输入或扫描快递单号"
              size="large"
              clearable
              @keyup.enter="handleCheckout"
            />
          </el-form-item>
          <el-form-item label="手机号">
            <el-input ref="phoneRef" v-model="form.phone" placeholder="请输入收件人手机号" size="large" clearable @keyup.enter="handleCheckout" />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" size="large" @click="handleCheckout" :loading="loading" class="w-full">
              确认出库
            </el-button>
          </el-form-item>
        </el-form>

        <!-- 出库结果 -->
        <div v-if="result" class="checkout-result">
          <el-alert title="出库成功！" type="success" :closable="false" show-icon center />
          <div class="result-detail">
            <p>取件码：<strong>{{ result.pickupCode }}</strong></p>
            <p v-if="result.remainingCount > 0">
              该收件人还有 <strong>{{ result.remainingCount }}</strong> 个包裹待取：
            </p>
            <p v-else>该收件人已无待取包裹</p>
            <div v-if="result.remainingCount > 0" class="remaining-list">
              <div v-for="(item, idx) in result.remainingPackages" :key="idx" class="remaining-item">
                <el-tag type="warning">{{ item.pickupCode }}</el-tag>
                <span class="remaining-tracking">{{ item.trackingNumber }}</span>
              </div>
            </div>
          </div>
          <p class="result-tip">信息将在 {{ countdown }} 秒后自动清除</p>
        </div>

        <div class="checkout-links">
          <el-button type="primary" link @click="$router.push('/login')">返回登录</el-button>
        </div>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted, onBeforeUnmount, nextTick, watch } from 'vue'
import { ElMessage } from 'element-plus'
import { getRandomBackground } from '@/utils/background'
import request from '@/utils/request'

const bgImage = getRandomBackground()

const formRef = ref(null)
const inputRef = ref(null)
const phoneRef = ref(null)
const loading = ref(false)

const form = reactive({
  trackingNumber: '',
  phone: ''
})

const result = ref(null)
const countdown = ref(5)
let countdownTimer = null

const MIN_TRACKING_LEN = 20

onMounted(() => {
  nextTick(() => inputRef.value?.focus())
})

onBeforeUnmount(() => {
  clearTimer()
})

watch(() => form.trackingNumber, (val) => {
  if (val && val.trim().length >= MIN_TRACKING_LEN) {
    nextTick(() => phoneRef.value?.focus())
  }
})

watch(() => form.phone, (val) => {
  if (val && val.trim().length === 11 && form.trackingNumber.trim().length >= MIN_TRACKING_LEN) {
    handleCheckout()
  }
})

const clearTimer = () => {
  if (countdownTimer) {
    clearInterval(countdownTimer)
    countdownTimer = null
  }
}

const resetAll = () => {
  clearTimer()
  form.trackingNumber = ''
  form.phone = ''
  result.value = null
  nextTick(() => inputRef.value?.focus())
}

const handleCheckout = async () => {
  if (!form.trackingNumber.trim()) {
    ElMessage.warning('请输入快递单号')
    return
  }
  if (!form.phone.trim()) {
    ElMessage.warning('请输入手机号')
    return
  }
  loading.value = true
  try {
    const res = await request.put('/inbound/public-checkout', {
      trackingNumber: form.trackingNumber.trim(),
      phone: form.phone.trim()
    })
    result.value = res.data
    startCountdown()
  } catch {} finally {
    loading.value = false
  }
}

const startCountdown = () => {
  clearTimer()
  countdown.value = 5
  countdownTimer = setInterval(() => {
    countdown.value--
    if (countdown.value <= 0) resetAll()
  }, 1000)
}
</script>

<style scoped>
.checkout-page {
  position: relative;
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: #e8ecf1;
  overflow: hidden;
}

/* 模糊背景层 */
.checkout-bg {
  position: absolute;
  inset: 0;
  background-size: cover;
  background-position: center;
  background-repeat: no-repeat;
  filter: blur(10px);
  transform: scale(1.05);
  z-index: 0;
}
.checkout-bg::after {
  content: '';
  position: absolute;
  inset: 0;
  background: rgba(255, 255, 255, .05);
}

/* 内容层 */
.checkout-content {
  position: relative;
  z-index: 1;
}

.checkout-card {
  width: 520px;
  max-width: 92vw;
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, .12);
}

.card-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.card-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: var(--spacing-sm);
}
.card-title h2 {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.card-subtitle {
  text-align: center;
  margin-bottom: var(--spacing-lg);
  font-size: var(--font-size-sm);
  color: var(--color-text-secondary);
}

.checkout-links {
  display: flex;
  justify-content: center;
  margin-top: var(--spacing-md);
}

/* 出库结果 */
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
