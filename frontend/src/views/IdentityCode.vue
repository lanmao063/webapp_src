<template>
  <div>
    <el-breadcrumb separator="/">
      <el-breadcrumb-item :to="{ path: '/Welcome' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>身份码</el-breadcrumb-item>
    </el-breadcrumb>

    <el-card class="identity-card" v-loading="loading">
      <template #header>
        <span class="card-title">我的身份码</span>
      </template>

      <div v-if="userInfo" class="identity-content">
        <el-descriptions :column="1" border size="small" class="desc-section">
          <el-descriptions-item label="姓名">{{ userInfo.realName || '-' }}</el-descriptions-item>
          <el-descriptions-item label="手机号">{{ userInfo.phone || '-' }}</el-descriptions-item>
        </el-descriptions>

        <div v-if="userInfo.phone" class="barcode-wrap">
          <svg ref="barcodeRef"></svg>
          <p class="phone-display">{{ userInfo.phone }}</p>
          <p class="hint-text">出库时向驿站工作人员出示此码即可</p>
        </div>
        <el-empty v-else description="未绑定手机号" />
      </div>

      <el-empty v-if="!loading && !userInfo" description="暂无数据" />
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import request from '@/utils/request'
import JsBarcode from 'jsbarcode'

const barcodeRef = ref(null)
const userInfo = ref(null)
const loading = ref(false)

onMounted(async () => {
  loading.value = true
  try {
    const res = await request.get('/auth/current')
    userInfo.value = res.data
    if (userInfo.value.phone) {
      await nextTick()
      JsBarcode(barcodeRef.value, userInfo.value.phone, {
        format: 'CODE128',
        width: 2,
        height: 80,
        displayValue: false,
        fontSize: 16,
        margin: 10
      })
    }
  } catch {
    ElMessage.error('获取用户信息失败')
  } finally {
    loading.value = false
  }
})
</script>

<style scoped>
.identity-content { padding: 0; }
.identity-card { margin-top: 20px; max-width: 420px; }
.card-title { font-size: 16px; font-weight: bold; }
.desc-section { margin-bottom: 20px; }
.barcode-wrap { text-align: center; }
.phone-display { margin-top: 8px; font-size: 16px; letter-spacing: 2px; color: #303133; }
.hint-text { font-size: 12px; color: #909399; margin-top: 8px; }
</style>
