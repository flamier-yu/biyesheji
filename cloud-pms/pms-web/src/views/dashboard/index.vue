<template>
  <div class="app-container">
    <el-card class="welcome-card" shadow="never">
      <div class="welcome">
        <el-avatar :size="54" class="avatar">{{ userStore.avatarText }}</el-avatar>
        <div class="info">
          <h3>{{ greeting }}，{{ userStore.nickName }}</h3>
          <p>
            角色：<el-tag size="small" effect="plain">{{ userStore.roles.join(' / ') || '—' }}</el-tag>
            　权限标识数：<strong>{{ userStore.permissions.length }}</strong>
          </p>
        </div>
      </div>
    </el-card>

    <el-row :gutter="14" class="stat-row">
      <el-col :span="4" v-for="card in statCards" :key="card.label">
        <el-card shadow="never" class="stat-card">
          <div class="stat-label">{{ card.label }}</div>
          <div class="stat-value" :style="{ color: card.color }">{{ card.value }}</div>
        </el-card>
      </el-col>
    </el-row>

    <el-row :gutter="14">
      <el-col :span="14">
        <el-card shadow="never">
          <template #header>工时趋势（最近 14 天）</template>
          <div ref="trendRef" class="chart"></div>
        </el-card>
      </el-col>
      <el-col :span="10">
        <el-card shadow="never">
          <template #header>任务状态分布</template>
          <div ref="taskRef" class="chart"></div>
        </el-card>
      </el-col>
    </el-row>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>
        <div class="card-head">
          <span>我负责的任务</span>
          <el-button size="small" @click="$router.push('/project/task')">查看全部</el-button>
        </div>
      </template>
      <el-table :data="myTasks" v-loading="loading" border stripe>
        <el-table-column prop="taskName" label="任务" min-width="200" show-overflow-tooltip />
        <el-table-column prop="projectName" label="项目" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="taskStatusTag(row.status)">{{ taskStatusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="160">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column label="截止日期" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.overdue ? '#f56c6c' : '' }">{{ row.planEnd || '—' }}</span>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !myTasks.length" description="暂无待办任务" :image-size="70" />
    </el-card>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { reportApi, taskApi } from '@/api/business'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const loading = ref(false)
const overview = ref({})
const myTasks = ref([])
const trendRef = ref(null)
const taskRef = ref(null)
let charts = []

const greeting = computed(() => {
  const h = new Date().getHours()
  if (h < 6) return '夜深了'
  if (h < 12) return '早上好'
  if (h < 14) return '中午好'
  if (h < 18) return '下午好'
  return '晚上好'
})

const statCards = computed(() => [
  { label: '项目总数', value: overview.value.projectTotal ?? 0, color: '#409eff' },
  { label: '进行中', value: overview.value.projectRunning ?? 0, color: '#e6a23c' },
  { label: '任务总数', value: overview.value.taskTotal ?? 0, color: '#67c23a' },
  { label: '我的任务', value: myTasks.value.length, color: '#409eff' },
  { label: '逾期任务', value: overview.value.taskOverdue ?? 0, color: '#f56c6c' },
  { label: '累计工时', value: overview.value.totalHours ?? 0, color: '#909399' }
])

const taskStatusMap = { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已挂起' }
const taskStatusTagMap = { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }

function taskStatusText(s) {
  return taskStatusMap[s] || '—'
}

function taskStatusTag(s) {
  return taskStatusTagMap[s] || 'info'
}

function initChart(el) {
  if (!el) return null
  const c = echarts.init(el)
  charts.push(c)
  return c
}

async function load() {
  loading.value = true
  try {
    const [ov, trend, tasks] = await Promise.all([
      reportApi.overview(),
      reportApi.worklogTrend(14),
      taskApi.my()
    ])
    overview.value = ov.data || {}
    myTasks.value = (tasks.data || []).slice(0, 8)

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
          type: 'line',
          smooth: true,
          areaStyle: { opacity: 0.18 },
          itemStyle: { color: '#409eff' },
          data: (trend.data || []).map((d) => Number(d.hours))
        }
      ]
    })

    initChart(taskRef.value)?.setOption({
      tooltip: { trigger: 'item' },
      legend: { bottom: 0 },
      series: [
        {
          type: 'pie',
          radius: ['40%', '65%'],
          center: ['50%', '42%'],
          data: [
            { name: '待开始', value: myTasks.value.filter((t) => t.status === '0').length },
            { name: '进行中', value: myTasks.value.filter((t) => t.status === '1').length },
            { name: '已完成', value: myTasks.value.filter((t) => t.status === '2').length },
            { name: '已挂起', value: myTasks.value.filter((t) => t.status === '3').length }
          ].filter((d) => d.value > 0)
        }
      ]
    })
  } finally {
    loading.value = false
  }
}

function handleResize() {
  charts.forEach((c) => c.resize())
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  charts.forEach((c) => c.dispose())
  charts = []
})
</script>

<style scoped lang="scss">
.welcome-card {
  margin-bottom: 14px;
  border: none;

  .welcome {
    display: flex;
    align-items: center;
    gap: 16px;

    .avatar {
      background: #409eff;
      color: #fff;
      font-size: 20px;
    }

    .info {
      h3 {
        margin: 0 0 8px;
        font-size: 17px;
        color: #303133;
      }

      p {
        margin: 0;
        font-size: 13px;
        color: #909399;
      }
    }
  }
}

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
    font-size: 22px;
    font-weight: 600;
  }
}

.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 280px;
  width: 100%;
}
</style>
