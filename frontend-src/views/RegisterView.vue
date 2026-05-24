<template>
  <div class="register-container">
    <el-card class="register-card">
      <h2 class="register-title">用户注册</h2>
      <el-form :model="registerForm" :rules="rules" ref="formRef" label-width="80px">
        <el-form-item label="用户名" prop="username">
          <el-input v-model="registerForm.username" placeholder="小于12字" maxlength="12" clearable />
        </el-form-item>
        <el-form-item label="密码" prop="password">
          <el-input v-model="registerForm.password" type="password" placeholder="6-20位密码" minlength="6" maxlength="20" show-password clearable />
        </el-form-item>
        <el-form-item label="确认密码" prop="repassword">
          <el-input v-model="registerForm.repassword" type="password" placeholder="请再次输入密码" maxlength="20" show-password clearable />
        </el-form-item>
        <el-form-item label="角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="请选择角色" style="width: 100%;">
            <el-option label="普通用户" value="REGULAR" />
            <el-option label="快递员" value="COURIER" />
            <el-option label="驿站管理员" value="MANAGER" />
          </el-select>
        </el-form-item>
        <el-form-item label="工号" prop="idNumber" v-if="registerForm.role !== 'REGULAR'">
          <el-input v-model="registerForm.idNumber" placeholder="员工填写，至少3位" clearable />
        </el-form-item>
        <el-form-item label="电话" prop="phone">
          <el-input v-model="registerForm.phone" placeholder="请输入手机号" clearable />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleRegister" :loading="loading" style="width: 100%;">注册</el-button>
        </el-form-item>
      </el-form>
      <div class="register-links">
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

const registerForm = reactive({
  username: '',
  password: '',
  repassword: '',
  role: 'REGULAR',
  idNumber: '',
  phone: ''
})

const validateRepassword = (rule, value, callback) => {
  if (value !== registerForm.password) {
    callback(new Error('两次密码不一致'))
  } else {
    callback()
  }
}

const rules = {
  username: [{ required: true, message: '请输入用户名', trigger: 'blur' }],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, max: 20, message: '密码长度为6-20位', trigger: 'blur' }
  ],
  repassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { validator: validateRepassword, trigger: 'blur' }
  ],
  role: [{ required: true, message: '请选择角色', trigger: 'change' }],
  idNumber: [{ min: 3, message: '工号至少3位', trigger: 'blur' }],
  phone: [
    { required: true, message: '请输入电话', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }
  ]
}

const handleRegister = async () => {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch {
    return
  }
  loading.value = true
  try {
    await request.post('/auth/register', {
      username: registerForm.username,
      password: registerForm.password,
      role: registerForm.role,
      idNumber: registerForm.idNumber,
      phone: registerForm.phone
    })
    ElMessage.success('注册成功，请登录')
    router.push('/login')
  } catch {
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  display: flex;
  justify-content: center;
  align-items: center;
  height: 100vh;
  background-color: #f5f7fa;
}
.register-card {
  width: 450px;
  padding: 20px;
}
.register-title {
  text-align: center;
  margin-bottom: 20px;
  color: #409eff;
}
.register-links {
  display: flex;
  justify-content: center;
}
</style>
