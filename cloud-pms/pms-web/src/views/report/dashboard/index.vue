<template>
  <div class="app-container">
    <!-- 指标卡 -->
    <el-row :gutter="14" class="stat-row">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <!-- 图表区 -->
    <el-row :gutter="14">
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>项目状态分布</template>
          <div ref="projectStatusRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="12">
        <el-card shadow="never">
          <template #header>任务状态分布</template>
          <div ref="taskStatusRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14" style="margin-top: 14px">
      <el-col :span="16">
        <el-card shadow="never">
          <template #header>工时趋势（最近 14 天）</template>
          <div ref="trendRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="8">
        <el-card shadow="never">
          <template #header>各项目工时占比</template>
          <div ref="worklogPieRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>项目进度一览</template>
      <el-table :data="projectProgress" border stripe>
        <el-table-column prop="projectName" label="项目名称" min-width="200" />
        <el-table-column label="优先级" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="priorityTag(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="任务" width="110" align="center">
          <template #default="{ row }">{{ row.doneCount || 0 }} / {{ row.taskCount || 0 }}</template>
        </el-table-column>
        <el-table-column label="进度" min-width="220">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="14" />
          </template>
        </el-table-column>
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || '—' }}
            </el-tag>
          </template>
        </el-table-column>
      </el-table>
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api/business'

const statusMap = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '进行中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已暂停', type: 'info' },
  4: { text: '已终止', type: 'danger' }
}

const projectStatusText = { 0: '待审批', 1: '进行中', 2: '已完成', 3: '已暂停', 4: '已终止' }
const taskStatusText = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已挂起' }

const overview = ref({})
const projectProgress = ref([])

const projectStatusRef = ref(null)
const taskStatusRef = ref(null)
const trendRef = ref(null)
const worklogPieRef = ref(null)
let charts = []

const statCards = computed(() => [
  { label: '项目总数', value: overview.value.projectTotal ?? 0, color: '#409eff' },
  { label: '进行中', value: overview.value.projectRunning ?? 0, color: '#e6a23c' },
  { label: '任务总数', value: overview.value.taskTotal ?? 0, color: '#67c23a' },
  { label: '已完成任务', value: overview.value.taskDone ?? 0, color: '#67c23a' },
  { label: '逾期任务', value: overview.value.taskOverdue ?? 0, color: '#f56c6c' },
  { label: '累计工时', value: overview.value.totalHours ?? 0, color: '#909399' }
])

function priorityText(p) {
  return { 1: '高', 2: '中', 3: '低' }[p] || '—'
}

function priorityTag(p) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[p] || 'info'
}

function initChart(el) {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

async function loadAll() {
  const [ov, ps, ts, trend, byProject, progress] = await Promise.all([
    reportApi.overview(),
    reportApi.projectStatus(),
    reportApi.taskStatus(),
    reportApi.worklogTrend(14),
    reportApi.worklogByProject(),
    reportApi.projectProgress(8)
  ])

  overview.value = ov.data || {}
  projectProgress.value = progress.data || []

  await nextTick()

  // 项目状态饼图
  const psChart = initChart(projectStatusRef.value)
  psChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: ['42%', '68%'],
        center: ['50%', '45%'],
        data: (ps.data || []).map((d) => ({
          name: projectStatusText[d.name] || d.name,
          value: Number(d.value)
        })),
        label: { formatter: '{b}\n{c}' }
      }
    ]
  })

  // 任务状态饼图
  const tsChart = initChart(taskStatusRef.value)
  tsChart?.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: 0 },
    series: [
      {
        type: 'pie',
        radius: '62%',
        center: ['50%', '45%'],
        data: (ts.data || []).map((d) => ({
          name: taskStatusText[d.name] || d.name,
          value: Number(d.value)
        }))
      }
    ]
  })

  // 工时趋势折线
  const trendChart = initChart(trendRef.value)
  trendChart?.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 45, right: 20, top: 30, bottom: 40 },
    xAxis: {
      type: 'category',
      data: (trend.data || []).map((d) => String(d.workDate).slice(5)),
      axisLabel: { rotate: 45 }
    },
    yAxis: { type: 'value', name: '小时' },
    series: [
      {
        type: 'line',
        smooth: true,
        areaStyle: { opacity: 0.18 },
        itemStyle: { color: '#409eff' },
        data: (trend.data || []).map((d) => Number(d.hours))
      }
    ]
  })

  // 项目工时占比
  const pieChart = initChart(worklogPieRef.value)
  pieChart?.setOption({
    tooltip: { trigger: 'item', formatter: '{b}<br/>{c} 小时 ({d}%)' },
    legend: { bottom: 0, type: 'scroll' },
    series: [
      {
        type: 'pie',
        radius: ['38%', '62%'],
        center: ['50%', '42%'],
        data: (byProject.data || []).map((d) => ({
          name: d.projectName || '未命名项目',
          value: Number(d.hours)
        }))
      }
    ]
  })
}

function handleResize() {
  charts.forEach((c) => c.resize())
}

onMounted(async () => {
  await loadAll()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
  charts = []
})
</script>

<style scoped lang="scss">
.stat-row {
  margin-bottom: 14px;
}

.stat-card {
  border: none;
  text-align: center;

  .stat-label {
    font-size: 13px;
    color: #909399;
    margin-bottom: 6px;
  }

  .stat-value {
    font-size: 24px;
    font-weight: 600;
  }
}

.chart {
  height: 300px;
  width: 100%;
}
</style>
