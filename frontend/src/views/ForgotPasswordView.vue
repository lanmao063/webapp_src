<template>
  <div class="auth-container">
    <el-card class="auth-card">
      <div class="auth-title">
        <el-icon :size="36" color="var(--color-primary)"><Lock /></el-icon>
        <h2>找回密码</h2>
      </div>
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
          <el-button type="primary" @click="handleSubmit" :loading="loading" class="w-full">重置密码</el-button>
        </el-form-item>
      </el-form>
      <div class="auth-links">
        <el-button type="primary" link @click="$router.push('/login')">返回登录</el-button>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { Lock } from '@element-plus/icons-vue'
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
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
</style>
