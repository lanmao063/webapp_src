<template>
  <div class="forgot-container">
    <el-card class="forgot-card">
      <h2 class="forgot-title">找回密码</h2>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="form.username" placeholder="请输入用户名" clearable />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="form.phone" placeholder="请输入注册时的电话" clearable />
        </el-form-item>
        <el-form-item label="新密码" prop="newPassword">
          <el-input v-model="form.newPassword" type="password" placeholder="请输入新密码" show-password clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading" style="width: 100%;">重置密码</el-button>
        </el-form-item>
      </el-form>
      <div class="forgot-links">
        <el-button type="primary" link @click="$router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'

const router = useRouter()
const formRef = ref(null)
const loading = ref(false)

const form = reactive({
  username: '',
  phone: '',
  newPassword: ''
})

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ],
  newPassword: [{ required: true, message: '请输入新密码', trigger: 'blur' }]
}

const handleSubmit = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await request.post('/auth/forgot-password', {
      username: form.username,
      phone: form.phone,
      newPassword: form.newPassword
    })
    ElMessage.success('密码重置成功，请登录')
    router.push('/login')
  } catch {
    // already handled
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.forgot-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.forgot-card {
  width: 420px;
  padding: 20px;
}
.forgot-title {
  text-align: center;
  margin-bottom: 20px;
  color: #409eff;
}
.forgot-links {
  display: flex;
  justify-content: center;
}
</style>
