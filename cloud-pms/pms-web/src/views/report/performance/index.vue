<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <span>成员绩效排行（按已审批通过工时统计）</span>
          <el-button @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
        </div>
      </template>

      <div ref="chartRef" class="chart" v-loading="loading"></div>
    </el-card>

    <el-card shadow="never" style="margin-top: 14px">
      <template #header>绩效明细</template>
      <el-table :data="list" border stripe>
        <el-table-column label="排名" width="80" align="center">
          <template #default="{ $index }">
            <el-tag v-if="$index < 3" :type="['danger', 'warning', 'success'][$index]" size="small">
              NO.{{ $index + 1 }}
            </el-tag>
            <span v-else>{{ $index + 1 }}</span>
          </template>
        </el-table-column>
        <el-table-column prop="nickName" label="成员" width="140" />
        <el-table-column prop="hours" label="累计工时" width="130" align="center">
          <template #default="{ row }">{{ row.hours }} h</template>
        </el-table-column>
        <el-table-column label="占比" min-width="240">
          <template #default="{ row }">
            <el-progress :percentage="percent(row.hours)" :stroke-width="14" />
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !list.length" description="暂无工时数据" />
    </el-card>
  </div>
</template>

<script setup>
import { nextTick, onMounted, onUnmounted, ref } from 'vue'
import * as echarts from 'echarts'
import { reportApi } from '@/api/business'

const loading = ref(false)
const list = ref([])
const chartRef = ref(null)
let chart = null

function percent(hours) {
  const total = list.value.reduce((s, r) => s + Number(r.hours || 0), 0)
  if (!total) return 0
  return Math.round((Number(hours) / total) * 100)
}

async function load() {
  loading.value = true
  try {
    const res = await reportApi.performance(10)
    list.value = res.data || []
    await nextTick()
    if (!chart && chartRef.value) {
      chart = echarts.init(chartRef.value)
    }
    chart?.setOption({
      tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
      grid: { left: 50, right: 30, top: 30, bottom: 60 },
      xAxis: {
        type: 'category',
        data: list.value.map((d) => d.nickName),
        axisLabel: { rotate: 30 }
      },
      yAxis: { type: 'value', name: '小时' },
      series: [
        {
          type: 'bar',
          barMaxWidth: 34,
          itemStyle: {
            color: '#409eff',
            borderRadius: [4, 4, 0, 0]
          },
          label: { show: true, position: 'top', formatter: '{c} h' },
          data: list.value.map((d) => Number(d.hours))
        }
      ]
    })
  } finally {
    loading.value = false
  }
}

function handleResize() {
  chart?.resize()
}

onMounted(() => {
  load()
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
  chart?.dispose()
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.chart {
  height: 340px;
  width: 100%;
}
</style>
