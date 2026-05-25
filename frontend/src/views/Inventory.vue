<template>
  <div class="page-container" v-loading="loading">
    <el-breadcrumb separator="/" class="page-breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>库存盘点</el-breadcrumb-item>
    </el-breadcrumb>

    <div class="stat-grid">
      <el-card v-for="card in cards" :key="card.label" shadow="hover" class="stat-card">
        <p class="stat-card-label">{{ card.label }}</p>
        <p class="stat-card-value" :style="{ color: card.color }">{{ card.value }}</p>
      </el-card>
    </div>

    <el-card shadow="never" class="year-card">
      <el-form inline>
        <el-form-item label="年份">
          <el-input-number v-model="year" :min="2020" :max="2030" @change="fetchChart" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never" v-loading="chartLoading">
      <template #header><span>{{ year }}年 月度统计</span></template>
      <div ref="chartRef" class="chart-container"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, onUnmounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const year = ref(new Date().getFullYear())
const chartRef = ref(null)
const loading = ref(false)
const chartLoading = ref(false)
let chartInstance = null
let resizeHandler = null

const cards = reactive([
  { label: '在库包裹', value: 0, color: '#e6a23c' },
  { label: '已取件', value: 0, color: '#67c23a' },
  { label: '总包裹', value: 0, color: '#409eff' },
  { label: '未处理异常', value: 0, color: '#f56c6c' }
])

const fetchOverview = async () => {
  try {
    const res = await request.get('/statistics/overview')
    cards[0].value = res.data.totalInWarehouse
    cards[1].value = res.data.totalPickedUp
    cards[2].value = res.data.totalParcels
    cards[3].value = res.data.unresolvedErrors
  } catch {}
}

const fetchChart = async () => {
  chartLoading.value = true
  try {
    const res = await request.get('/statistics/chart', { params: { year: year.value } })
    const { labels, enterData, pickupData } = res.data
    if (!chartInstance) {
      chartInstance = echarts.init(chartRef.value)
    }
    chartInstance.setOption({
      tooltip: { trigger: 'axis' },
      legend: { data: ['入库', '取件'] },
      xAxis: { type: 'category', data: labels },
      yAxis: { type: 'value' },
      series: [
        { name: '入库', type: 'bar', data: enterData, color: '#409eff' },
        { name: '取件', type: 'line', data: pickupData, color: '#67c23a' }
      ]
    })
  } catch {} finally {
    chartLoading.value = false
  }
}

onMounted(async () => {
  loading.value = true
  await fetchOverview()
  await nextTick()
  await fetchChart()
  loading.value = false

  resizeHandler = () => chartInstance?.resize()
  window.addEventListener('resize', resizeHandler)
})

onUnmounted(() => {
  if (chartInstance) {
    chartInstance.dispose()
    chartInstance = null
  }
  if (resizeHandler) {
    window.removeEventListener('resize', resizeHandler)
  }
})
</script>

<style scoped>
.chart-container {
  height: 400px;
}
.year-card {
  margin-bottom: var(--spacing-md);
}
</style>
