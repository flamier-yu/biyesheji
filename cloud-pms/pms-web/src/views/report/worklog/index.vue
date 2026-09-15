<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>工时统计</span>
          <div>
            <el-select v-model="projectId" clearable placeholder="全部项目" style="width: 220px; margin-right: 10px">
              <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
            </el-select>
            <el-button type="primary" v-permission="['report:worklog:export']" @click="handleExport">
              <el-icon><Download /></el-icon>导出 Excel
            </el-button>
          </div>
        </div>
      </template>

      <el-row :gutter="16">
        <el-col :span="24">
          <div ref="trendRef" class="chart"></div>
        </el-col>
      </el-row>
    </el-card>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>各项目工时占比</template>
          <div ref="pieRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>工时明细</template>
          <el-table :data="logList" v-loading="loading" border stripe height="300">
            <el-table-column prop="workDate" label="日期" width="115" />
            <el-table-column prop="nickName" label="填报人" width="90" />
            <el-table-column prop="projectName" label="项目" show-overflow-tooltip />
            <el-table-column prop="hours" label="工时" width="70" align="center" />
          </el-table>
        </el-card>
      </el-col>
    </el-row>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { projectApi, reportApi, worklogApi } from '@/api/business'
import { BASE_API } from '@/utils/request'
import { getToken } from '@/utils/auth'

const loading = ref(false)
const projects = ref([])
const projectId = ref(null)
const logList = ref([])

const trendRef = ref(null)
const pieRef = ref(null)
let charts = []

function initChart(el) {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

async function load() {
  loading.value = true
  try {
    const [trend, byProject, logs] = await Promise.all([
      reportApi.worklogTrend(30),
      reportApi.worklogByProject(),
      worklogApi.list({ projectId: projectId.value, status: '1', pageNum: 1, pageSize: 20 })
    ])

    logList.value = logs.data.rows || []

    await nextTick()

    initChart(trendRef.value)?.setOption({
      tooltip: { trigger: 'axis' },
      grid: { left: 45, right: 20, top: 30, bottom: 50 },
      xAxis: {
        type: 'category',
        data: (trend.data || []).map((d) => String(d.workDate).slice(5)),
        axisLabel: { rotate: 45 }
      },
      yAxis: { type: 'value', name: '小时' },
      series: [
        {
          type: 'bar',
          barMaxWidth: 22,
          itemStyle: { color: '#409eff' },
          data: (trend.data || []).map((d) => Number(d.hours))
        }
      ]
    })

    initChart(pieRef.value)?.setOption({
      tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 小时 ({d}%)' },
      legend: { bottom: 0, type: 'scroll' },
      series: [
        {
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '42%'],
          data: (byProject.data || []).map((d) => ({
            name: d.projectName || '未命名项目',
            value: Number(d.hours)
          }))
        }
      ]
    })
  } finally {
    loading.value = false
  }
}

function handleExport() {
  const url = `${BASE_API}/report/worklog/export?token=${getToken()}${
    projectId.value ? `&projectId=${projectId.value}` : ''
  }`
  window.open(url, '_blank')
}

function handleResize() {
  charts.forEach((c) => c.resize())
}

onMounted(async () => {
  const res = await projectApi.my()
  projects.value = res.data
  await load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
  charts = []
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 320px;
  width: 100%;
}
</style>
