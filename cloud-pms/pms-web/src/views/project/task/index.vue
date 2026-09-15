<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form inline>
        <el-form-item label="项目">
          <el-select v-model="currentProjectId" placeholder="请选择项目" style="width: 240px" @change="reload">
            <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="视图">
          <el-radio-group v-model="viewMode">
            <el-radio-button value="list">列表</el-radio-button>
            <el-radio-button value="board">看板</el-radio-button>
            <el-radio-button value="gantt">甘特图</el-radio-button>
          </el-radio-group>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" v-permission="['project:task:add']" :disabled="!currentProjectId" @click="openDialog()">
            <el-icon><Plus /></el-icon>新建任务
          </el-button>
          <el-button @click="reload"><el-icon><Refresh /></el-icon>刷新</el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表视图 -->
    <div v-show="viewMode === 'list'" class="table-card">
      <el-table
        :data="taskTree"
        v-loading="loading"
        row-key="taskId"
        :tree-props="{ children: 'children' }"
        border
      >
        <el-table-column prop="taskName" label="任务名称" min-width="200" show-overflow-tooltip>
          <template #default="{ row }">
            <el-icon v-if="row.overdue" color="#f56c6c" style="margin-right: 4px"><Warning /></el-icon>
            <el-link type="primary" @click="openDetail(row)">{{ row.taskName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="assigneeName" label="负责人" width="100" />
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="priorityTag(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="150">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column prop="planStart" label="计划开始" width="110" />
        <el-table-column label="计划结束" width="130">
          <template #default="{ row }">
            <span :style="{ color: row.overdue ? '#f56c6c' : '' }">{{ row.planEnd || '—' }}</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['project:task:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="success" v-permission="['project:task:edit']" @click="openStatusDialog(row)">状态</el-button>
            <el-button link type="danger" v-permission="['project:task:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !taskTree.length" description="暂无任务" />
    </div>

    <!-- 看板视图 -->
    <div v-show="viewMode === 'board'" class="board-wrap" v-loading="loading">
      <div v-for="col in boardColumns" :key="col.status" class="board-column">
        <div class="board-head">
          <span>{{ col.title }}</span>
          <el-tag size="small" :type="col.type">{{ col.items.length }}</el-tag>
        </div>
        <div class="board-body">
          <el-card v-for="task in col.items" :key="task.taskId" shadow="hover" class="board-card" @click="openDetail(task)">
            <div class="card-title">
              <el-icon v-if="task.overdue" color="#f56c6c"><Warning /></el-icon>
              {{ task.taskName }}
            </div>
            <div class="card-meta">
              <el-tag size="small" :type="priorityTag(task.priority)">{{ priorityText(task.priority) }}</el-tag>
              <span class="assignee">{{ task.assigneeName || '未指派' }}</span>
            </div>
            <el-progress :percentage="task.progress || 0" :stroke-width="6" :show-text="false" />
            <div class="card-date">{{ task.planEnd || '未设定截止' }}</div>
          </el-card>
          <el-empty v-if="!col.items.length" description="暂无" :image-size="50" />
        </div>
      </div>
    </div>

    <!-- 甘特图 -->
    <div v-show="viewMode === 'gantt'" class="table-card">
      <div ref="ganttRef" class="gantt-chart" v-loading="loading"></div>
      <el-empty v-if="!loading && !flatTasks.length" description="暂无任务数据" />
    </div>

    <!-- 新建/修改任务 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="680px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="95px">
        <el-form-item label="任务名称" prop="taskName">
          <el-input v-model="form.taskName" placeholder="请输入任务名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="上级任务">
              <el-select v-model="form.parentId" clearable placeholder="不选则为顶级任务" style="width: 100%">
                <el-option v-for="t in flatTasks" :key="t.taskId" :label="t.taskName" :value="t.taskId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属里程碑">
              <el-select v-model="form.milestoneId" clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="m in milestones" :key="m.milestoneId" :label="m.milestoneName" :value="m.milestoneId" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-select v-model="form.assigneeId" filterable clearable placeholder="请选择" style="width: 100%">
                <el-option v-for="u in users" :key="u.userId" :label="u.nickName" :value="u.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划开始">
              <el-date-picker v-model="form.planStart" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="计划结束">
              <el-date-picker v-model="form.planEnd" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="预估工时">
              <el-input-number v-model="form.estimateHours" :min="0" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="待开始" value="0" />
                <el-option label="进行中" value="1" />
                <el-option label="已完成" value="2" />
                <el-option label="已挂起" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="任务描述">
          <el-input v-model="form.description" type="textarea" :rows="3" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 状态变更 -->
    <el-dialog v-model="statusVisible" title="变更任务状态" width="420px">
      <el-form label-width="80px">
        <el-form-item label="任务">
          <span>{{ currentRow?.taskName }}</span>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="targetStatus" style="width: 100%">
            <el-option label="待开始" value="0" />
            <el-option label="进行中" value="1" />
            <el-option label="已完成" value="2" />
            <el-option label="已挂起" value="3" />
          </el-select>
        </el-form-item>
        <el-form-item label="进度" v-if="targetStatus === '1'">
          <el-slider v-model="targetProgress" :min="0" :max="100" show-input />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStatus">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 任务详情 + 评论 -->
    <el-drawer v-model="detailVisible" :title="detail.taskName" size="600px">
      <el-descriptions :column="2" border size="small">
        <el-descriptions-item label="负责人">{{ detail.assigneeName || '未指派' }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusTag(detail.status)">{{ statusText(detail.status) }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划开始">{{ detail.planStart || '—' }}</el-descriptions-item>
        <el-descriptions-item label="计划结束">{{ detail.planEnd || '—' }}</el-descriptions-item>
        <el-descriptions-item label="预估工时">{{ detail.estimateHours || 0 }} h</el-descriptions-item>
        <el-descriptions-item label="进度">{{ detail.progress || 0 }}%</el-descriptions-item>
        <el-descriptions-item label="任务描述" :span="2">{{ detail.description || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">评论（{{ comments.length }}）</el-divider>
      <div class="comment-box">
        <el-input v-model="commentText" type="textarea" :rows="2" placeholder="输入评论内容..." />
        <div class="comment-actions">
          <el-button type="primary" size="small" @click="submitComment">发表评论</el-button>
        </div>
      </div>
      <el-timeline style="margin-top: 16px">
        <el-timeline-item v-for="c in comments" :key="c.commentId" :timestamp="c.createTime" placement="top">
          <div class="comment-item">
            <strong>{{ c.nickName }}</strong>
            <p>{{ c.content }}</p>
          </div>
        </el-timeline-item>
        <el-empty v-if="!comments.length" description="暂无评论" :image-size="60" />
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup>
import { computed, nextTick, onMounted, reactive, ref, watch } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import * as echarts from 'echarts'
import { projectApi, taskApi } from '@/api/business'
import { userApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const projects = ref([])
const currentProjectId = ref(null)
const taskTree = ref([])
const flatTasks = ref([])
const milestones = ref([])
const users = ref([])
const viewMode = ref('list')
const ganttRef = ref(null)
let ganttChart = null

const dialogVisible = ref(false)
const dialogTitle = ref('新建任务')
const formRef = ref(null)
const form = reactive({
  taskId: null,
  projectId: null,
  parentId: null,
  milestoneId: null,
  taskName: '',
  assigneeId: null,
  priority: '2',
  status: '0',
  planStart: '',
  planEnd: '',
  estimateHours: 0,
  description: ''
})

const rules = {
  taskName: [{ required: true, message: '请输入任务名称', trigger: 'blur' }]
}

const statusVisible = ref(false)
const targetStatus = ref('1')
const targetProgress = ref(50)
const currentRow = ref(null)

const detailVisible = ref(false)
const detail = ref({})
const comments = ref([])
const commentText = ref('')

const boardColumns = computed(() => [
  { status: '0', title: '待开始', type: 'info', items: taskTree.value.filter((t) => t.status === '0') },
  { status: '1', title: '进行中', type: 'primary', items: taskTree.value.filter((t) => t.status === '1') },
  { status: '2', title: '已完成', type: 'success', items: taskTree.value.filter((t) => t.status === '2') },
  { status: '3', title: '已挂起', type: 'warning', items: taskTree.value.filter((t) => t.status === '3') }
])

function statusText(s) {
  return { 0: '待开始', 1: '进行中', 2: '已完成', 3: '已挂起' }[s] || '—'
}

function statusTag(s) {
  return { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning' }[s] || 'info'
}

function priorityText(p) {
  return { 1: '高', 2: '中', 3: '低' }[p] || '—'
}

function priorityTag(p) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[p] || 'info'
}

async function loadProjects() {
  const res = await projectApi.my()
  projects.value = res.data
  if (projects.value.length && !currentProjectId.value) {
    currentProjectId.value = projects.value[0].projectId
  }
}

async function loadUsers() {
  const res = await userApi.list({ pageNum: 1, pageSize: 200 })
  users.value = res.data.rows
}

function flatten(tree, acc = []) {
  tree.forEach((t) => {
    acc.push(t)
    if (t.children && t.children.length) flatten(t.children, acc)
  })
  return acc
}

async function loadTasks() {
  if (!currentProjectId.value) return
  loading.value = true
  try {
    const res = await taskApi.tree(currentProjectId.value)
    taskTree.value = res.data
    flatTasks.value = flatten(res.data)
    const ms = await projectApi.milestones(currentProjectId.value)
    milestones.value = ms.data
    if (viewMode.value === 'gantt') {
      await nextTick()
      renderGantt()
    }
  } finally {
    loading.value = false
  }
}

function reload() {
  loadTasks()
}

/* ==================== 甘特图 ==================== */
function renderGantt() {
  if (!ganttRef.value) return
  if (!ganttChart) {
    ganttChart = echarts.init(ganttRef.value)
  }
  const tasks = flatTasks.value.filter((t) => t.planStart && t.planEnd)
  if (!tasks.length) {
    ganttChart.clear()
    return
  }
  const names = tasks.map((t) => t.taskName)
  const minDate = new Date(Math.min(...tasks.map((t) => new Date(t.planStart).getTime())))
  const maxDate = new Date(Math.max(...tasks.map((t) => new Date(t.planEnd).getTime())))

  const data = tasks.map((t, idx) => ({
    value: [idx, new Date(t.planStart).getTime(), new Date(t.planEnd).getTime(), t.progress || 0],
    itemStyle: { color: t.status === '2' ? '#67c23a' : t.status === '1' ? '#409eff' : '#909399' }
  }))

  ganttChart.setOption({
    tooltip: {
      formatter: (p) => {
        const t = tasks[p.value[0]]
        return `${t.taskName}<br/>${t.planStart} ~ ${t.planEnd}<br/>进度：${t.progress || 0}%`
      }
    },
    grid: { left: 140, right: 40, top: 20, bottom: 40 },
    xAxis: {
      type: 'time',
      min: minDate.getTime(),
      max: maxDate.getTime(),
      axisLabel: { formatter: '{MM}-{dd}' }
    },
    yAxis: {
      type: 'category',
      data: names,
      axisLabel: { width: 130, overflow: 'truncate' }
    },
    series: [
      {
        type: 'custom',
        renderItem: (params, api) => {
          const categoryIndex = api.value(0)
          const start = api.coord([api.value(1), categoryIndex])
          const end = api.coord([api.value(2), categoryIndex])
          const height = api.size([0, 1])[1] * 0.5
          const rect = echarts.graphic.clipRectByRect(
            { x: start[0], y: start[1] - height / 2, width: Math.max(end[0] - start[0], 2), height },
            { x: params.coordSys.x, y: params.coordSys.y, width: params.coordSys.width, height: params.coordSys.height }
          )
          return (
            rect && {
              type: 'rect',
              transition: ['shape'],
              shape: rect,
              style: api.style()
            }
          )
        },
        encode: { x: [1, 2], y: 0 },
        data
      }
    ]
  })
  ganttChart.resize()
}

watch(viewMode, async (val) => {
  if (val === 'gantt') {
    await nextTick()
    renderGantt()
  }
})

/* ==================== 增删改 ==================== */
function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改任务'
    Object.assign(form, {
      taskId: row.taskId,
      projectId: row.projectId,
      parentId: row.parentId === 0 ? null : row.parentId,
      milestoneId: row.milestoneId,
      taskName: row.taskName,
      assigneeId: row.assigneeId,
      priority: row.priority || '2',
      status: row.status || '0',
      planStart: row.planStart,
      planEnd: row.planEnd,
      estimateHours: row.estimateHours || 0,
      description: row.description
    })
  } else {
    dialogTitle.value = '新建任务'
    Object.assign(form, {
      taskId: null,
      projectId: currentProjectId.value,
      parentId: null,
      milestoneId: null,
      taskName: '',
      assigneeId: null,
      priority: '2',
      status: '0',
      planStart: '',
      planEnd: '',
      estimateHours: 0,
      description: ''
    })
  }
}

async function handleSubmit() {
  if (!formRef.value) return
  try {
    await formRef.value.validate()
  } catch (e) {
    return
  }
  submitting.value = true
  try {
    const payload = { ...form, projectId: currentProjectId.value }
    if (form.taskId) {
      await taskApi.edit(payload)
      ElMessage.success('修改成功')
    } else {
      await taskApi.add(payload)
      ElMessage.success('新建成功')
    }
    dialogVisible.value = false
    loadTasks()
  } finally {
    submitting.value = false
  }
}

function openStatusDialog(row) {
  currentRow.value = row
  targetStatus.value = row.status
  targetProgress.value = row.progress || 0
  statusVisible.value = true
}

async function submitStatus() {
  await taskApi.changeStatus({
    taskId: currentRow.value.taskId,
    status: targetStatus.value,
    progress: targetStatus.value === '1' ? targetProgress.value : null
  })
  ElMessage.success('状态已更新')
  statusVisible.value = false
  loadTasks()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除任务「${row.taskName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await taskApi.remove(row.taskId)
  ElMessage.success('删除成功')
  loadTasks()
}

/* ==================== 详情与评论 ==================== */
async function openDetail(row) {
  const res = await taskApi.get(row.taskId)
  detail.value = res.data
  const cs = await taskApi.comments(row.taskId)
  comments.value = cs.data || []
  commentText.value = ''
  detailVisible.value = true
}

async function submitComment() {
  if (!commentText.value.trim()) {
    ElMessage.warning('请输入评论内容')
    return
  }
  await taskApi.addComment({ taskId: detail.value.taskId, content: commentText.value })
  ElMessage.success('评论成功')
  commentText.value = ''
  const cs = await taskApi.comments(detail.value.taskId)
  comments.value = cs.data || []
}

onMounted(async () => {
  await loadProjects()
  await loadUsers()
  await loadTasks()
  window.addEventListener('resize', () => ganttChart && ganttChart.resize())
})
</script>

<style scoped lang="scss">
.board-wrap {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 14px;
}

.board-column {
  background: #f5f7fa;
  border-radius: 8px;
  padding: 12px;
  min-height: 420px;
}

.board-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-weight: 600;
  color: #303133;
  margin-bottom: 10px;
  padding-bottom: 8px;
  border-bottom: 1px solid #e4e7ed;
}

.board-body {
  display: flex;
  flex-direction: column;
  gap: 10px;
}

.board-card {
  cursor: pointer;

  .card-title {
    font-size: 13px;
    font-weight: 500;
    margin-bottom: 8px;
    display: flex;
    align-items: center;
    gap: 4px;
  }

  .card-meta {
    display: flex;
    justify-content: space-between;
    align-items: center;
    margin-bottom: 8px;

    .assignee {
      font-size: 12px;
      color: #909399;
    }
  }

  .card-date {
    margin-top: 8px;
    font-size: 12px;
    color: #a8abb2;
  }
}

.gantt-chart {
  height: 520px;
  width: 100%;
}

.comment-actions {
  text-align: right;
  margin-top: 8px;
}

.comment-item {
  strong {
    color: #409eff;
    font-size: 13px;
  }

  p {
    margin: 6px 0 0;
    color: #606266;
    font-size: 13px;
  }
}
</style>
