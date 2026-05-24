<template>
  <div class="page-container">
    <el-breadcrumb separator="/" class="breadcrumb">
      <el-breadcrumb-item :to="{ path: '/' }">首页</el-breadcrumb-item>
      <el-breadcrumb-item>库存盘点</el-breadcrumb-item>
    </el-breadcrumb>

    <el-row :gutter="20" style="margin-bottom: 16px;">
      <el-col :span="6" v-for="card in cards" :key="card.label">
        <el-card shadow="hover">
          <div style="text-align:center;">
            <p style="color:#909399;font-size:14px;">{{ card.label }}</p>
            <h2 :style="{color:card.color}">{{ card.value }}</h2>
          </div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-bottom: 16px;">
      <el-form inline>
        <el-form-item label="年份">
          <el-input-number v-model="year" :min="2020" :max="2030" @change="fetchChart" />
        </el-form-item>
      </el-form>
    </el-card>

    <el-card shadow="never">
      <template #header><span>{{ year }}年 月度统计</span></template>
      <div ref="chartRef" style="height: 400px;"></div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, nextTick, watch } from 'vue'
import * as echarts from 'echarts'
import request from '@/utils/request'

const year = ref(2026)
const chartRef = ref(null)
let chartInstance = null

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
  } catch {}
}

watch(year, () => fetchChart())

onMounted(() => {
  fetchOverview()
  nextTick(() => fetchChart())
})
</script>

<style scoped>
.page-container { padding: 0; }
.breadcrumb { margin-bottom: 16px; }
</style>
