<template>
  <div class="login-container">
    <el-card class="login-card">
      <h2 class="login-title">快递驿站管理系统</h2>
      <el-form :model="loginForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="loginForm.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="loginForm.password" type="password" placeholder="请输入密码" show-password clearable />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="loginForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="普通用户" value="REGULAR" />
            <el-option label="快递员" value="COURIER" />
            <el-option label="驿站管理员" value="MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-checkbox v-model="loginForm.rememberMe">记住密码</el-checkbox>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleLogin" :loading="loading" style="width: 100%;">登录</el-button>
        </el-form-item>
      </el-form>
      <div class="login-links">
        <el-button type="primary" link @click="$router.push('/register')">注册账号</el-button>
        <el-button type="primary" link @click="$router.push('/forgot-password')">忘记密码</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { setToken, setUserInfo } from '@/utils/auth'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const loginForm = reactive({
  username: '',
  password: '',
  role: 'REGULAR',
  rememberMe: false
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
      role: loginForm.role,
      rememberMe: loginForm.rememberMe
    })
    const { token, userId, username, role } = res.data
    setToken(token, loginForm.rememberMe)
    setUserInfo({ userId, username, role }, loginForm.rememberMe)
    ElMessage.success('登录成功')
    router.push('/Welcome')
  } catch {
    // error already handled by interceptor
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.login-card {
  width: 420px;
  padding: 20px;
}
.login-title {
  text-align: center;
  margin-bottom: 30px;
  color: #409eff;
}
.login-links {
  display: flex;
  justify-content: space-between;
}
</style>
