<template>
  <div class="login-page">
    <div class="login-image-panel"></div>
    <div class="login-form-panel">
      <el-card class="auth-card">
        <div class="auth-title">
          <img src="@/assets/logo.png" alt="logo" class="auth-logo" />
          <h2>莱鸟</h2>
        </div>
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
        <div class="auth-links">
          <el-button type="primary" link @click="$router.push('/register')">注册账号</el-button>
          <el-button type="primary" link @click="$router.push('/forgot-password')">忘记密码</el-button>
        </div>

        <el-divider />
        <el-button size="large" class="checkout-btn" @click="$router.push('/public-checkout')">
          <el-icon><Van /></el-icon>
          <span>包裹出库</span>
          <span class="checkout-hint">（免登录，凭快递单号+手机号取件）</span>
        </el-button>
      </el-card>
    </div>
  </div>
</template>

<script setup>
import { reactive, ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Van } from '@element-plus/icons-vue'
import request from '@/utils/request'
import { setUserInfo, saveCredentials, getSavedCredentials, clearCredentials } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'REGULAR',
  rememberMe: false
})

onMounted(() => {
  const saved = getSavedCredentials()
  if (saved) {
    loginForm.username = saved.username
    loginForm.password = saved.password
    loginForm.rememberMe = true
  }
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }]
}

const handleLogin = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
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
  } catch {} finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  display: flex;
  height: 100vh;
  overflow: hidden;
}

/* === Left: image panel with gradient transition === */
.login-image-panel {
  flex: 0 0 50%;
  width: 50%;
  height: 100vh;
  background: url('@/assets/login.png') center/cover no-repeat;
  position: relative;
}
.login-image-panel::after {
  content: '';
  position: absolute;
  inset: 0;
  background: linear-gradient(to right, transparent 65%, #f0f2f5 100%);
}

/* === Right: form panel === */
.login-form-panel {
  flex: 0 0 50%;
  width: 50%;
  height: 100vh;
  background: #f0f2f5;
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 40px;
  overflow-y: auto;
}

.auth-card {
  width: 420px;
  max-width: 90vw;
  border-radius: var(--radius-lg);
  box-shadow: 0 8px 32px rgba(0, 0, 0, .08);
}

.auth-logo {
  width: 48px;
  height: 48px;
  object-fit: contain;
}

.auth-title {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  margin-bottom: var(--spacing-lg);
}
.auth-title h2 {
  font-size: var(--font-size-xxl);
  font-weight: 600;
  color: var(--color-text-primary);
  margin: 0;
}

.checkout-btn {
  width: 100%;
  height: 48px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  border: 1px dashed var(--color-border);
  border-radius: var(--radius-md);
  color: var(--color-text-regular);
  transition: border-color var(--transition-fast), color var(--transition-fast);
}
.checkout-btn:hover {
  border-color: var(--color-primary);
  color: var(--color-primary);
}
.checkout-hint {
  font-size: var(--font-size-xs);
  color: var(--color-text-secondary);
  font-weight: normal;
}

.auth-links {
  display: flex;
  justify-content: center;
  gap: var(--spacing-md);
  margin-top: var(--spacing-sm);
}

@media (max-width: 768px) {
  .login-image-panel {
    display: none;
  }
  .login-form-panel {
    flex: 1;
    padding: 20px;
  }
}
</style>
