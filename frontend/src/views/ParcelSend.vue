<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>寄件</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card shadow="never" v-loading="loading">
      <template #header><span>寄件信息</span></template>
      <el-form :model="form" :rules="rules" ref="formRef" label-width="110px">
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="包裹类型" prop="packageName">
              <el-select v-model="form.packageName" placeholder="请选择包裹类型" class="w-full">
                <el-option v-for="pt in packageTypes" :key="pt.value" :label="pt.label" :value="pt.value" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="重量(kg)" prop="weight">
              <el-input-number v-model="form.weight" :min="0" :precision="3" class="w-full" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="寄件人" prop="senderName">
              <el-input v-model="form.senderName" placeholder="请输入寄件人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="寄件人电话" prop="senderPhone">
              <el-input v-model="form.senderPhone" placeholder="请输入寄件人电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="寄件人地址">
          <el-input v-model="form.senderAddress" placeholder="请输入寄件人地址" />
        </el-form-item>
        <el-row :gutter="20">
          <el-col :span="12">
            <el-form-item label="收件人" prop="receiverName">
              <el-input v-model="form.receiverName" placeholder="请输入收件人" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="收件人电话" prop="receiverPhone">
              <el-input v-model="form.receiverPhone" placeholder="请输入收件人电话" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="收件人地址">
          <el-input v-model="form.receiverAddress" placeholder="请输入收件人地址" />
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="form.notes" type="textarea" :rows="2" placeholder="选填，备注信息" />
        </el-form-item>

        <el-form-item label="取件方式" prop="pickupMethod">
          <el-radio-group v-model="form.pickupMethod">
            <el-radio value="SELF_DROP">到快递点自行寄出</el-radio>
            <el-radio value="DOOR_PICKUP">快递员上门取件</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="预约时间" v-if="form.pickupMethod === 'DOOR_PICKUP'" prop="appointmentTime">
          <el-select v-model="form.appointmentTime" placeholder="请选择上门时间段" class="w-250">
            <el-option v-for="t in timeSlots" :key="t" :label="t" :value="t" />
          </el-select>
        </el-form-item>

        <el-form-item label="预估费用" v-if="form.weight">
          <el-tag type="warning" size="large">￥{{ estimatedFee }}</el-tag>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleSubmit" :loading="loading">提交寄件</el-button>
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" class="mt-md">
      <template #header><span>收费表</span></template>
      <el-table :data="feeTable" class="w-full">
        <el-table-column prop="range" label="重量范围" />
        <el-table-column prop="fee" label="费用" />
      </el-table>
    </el-card>

    <el-card shadow="never" class="mt-md" v-if="result">
      <template #header><span>寄件结果</span></template>
      <el-descriptions :column="2" border>
        <el-descriptions-item label="快递单号">{{ result.trackingNumber }}</el-descriptions-item>
        <el-descriptions-item label="费用">￥{{ result.fee }}</el-descriptions-item>
      </el-descriptions>
      <el-button type="primary" class="mt-btn" @click="$router.push(`/SendDetail?id=${result.id}`)">查看详情</el-button>
    </el-card>
  </div>
</template>

<script setup>
import { reactive, ref, computed, watch } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import { getUserInfo } from '@/utils/auth'
import { calcFee, feeTable } from '@/utils/fee'

const formRef = ref(null)
const loading = ref(false)
const result = ref(null)

const timeSlots = ['08:00-09:00','09:00-10:00','10:00-11:00','11:00-12:00','12:00-13:00','13:00-14:00','14:00-15:00','15:00-16:00','16:00-17:00','17:00-18:00']

const packageTypes = [
  { value: '文件', label: '文件' },
  { value: '衣物', label: '衣物' },
  { value: '电子产品', label: '电子产品' },
  { value: '食品', label: '食品' },
  { value: '日用品', label: '日用品' },
  { value: '书籍', label: '书籍' },
  { value: '其他', label: '其他' }
]

const form = reactive({
  packageName: '', weight: null, senderName: '', senderPhone: '',
  senderAddress: '', receiverName: '', receiverPhone: '', receiverAddress: '',
  notes: '', pickupMethod: 'SELF_DROP', appointmentTime: ''
})

// auto-fill sender info from logged-in user
const user = getUserInfo()
if (user) {
  form.senderName = user.realName || ''
  form.senderPhone = user.phone || ''
}

watch(() => form.pickupMethod, (val) => {
  if (val === 'SELF_DROP') {
    form.appointmentTime = ''
    formRef.value?.clearValidate('appointmentTime')
  }
})

const estimatedFee = computed(() => calcFee(form.weight))

const rules = computed(() => ({
  packageName: [{ required: true, message: '请选择包裹类型', trigger: 'change' }],
  weight: [{ required: true, message: '请输入重量', trigger: 'blur' }],
  senderName: [{ required: true, message: '请输入寄件人', trigger: 'blur' }],
  senderPhone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  receiverName: [{ required: true, message: '请输入收件人', trigger: 'blur' }],
  receiverPhone: [{ required: true, pattern: /^1[3-9]\d{9}$/, message: '手机号格式不正确', trigger: 'blur' }],
  pickupMethod: [{ required: true, message: '请选择取件方式', trigger: 'change' }],
  appointmentTime: form.pickupMethod === 'DOOR_PICKUP'
    ? [{ required: true, message: '请选择预约时间', trigger: 'change' }]
    : []
}))

const handleSubmit = async () => {
  if (!formRef.value) return
  try { await formRef.value.validate() } catch { return }
  loading.value = true
  try {
    const res = await request.post('/send-package', {
      packageName: form.packageName,
      weight: form.weight,
      senderName: form.senderName,
      senderPhone: form.senderPhone,
      senderAddress: form.senderAddress,
      receiverName: form.receiverName,
      receiverPhone: form.receiverPhone,
      receiverAddress: form.receiverAddress,
      notes: form.notes,
      pickupMethod: form.pickupMethod,
      appointmentTime: form.pickupMethod === 'DOOR_PICKUP' ? form.appointmentTime : null
    })
    result.value = res.data
    ElMessage.success('寄件申请已提交，请等待管理员审核')
  } catch {} finally { loading.value = false }
}
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
.w-250 { width: 250px; }
.mt-btn { margin-top: 12px; }
</style>
