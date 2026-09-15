<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" class="worklog-tabs">
      <!-- 我的工时 -->
      <el-tab-pane label="我的工时" name="mine">
        <el-form inline>
          <el-form-item label="日期范围">
            <el-date-picker
              v-model="dateRange"
              type="daterange"
              value-format="YYYY-MM-DD"
              start-placeholder="开始日期"
              end-placeholder="结束日期"
              @change="loadMyLogs"
            />
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="openDialog()">
              <el-icon><Plus /></el-icon>填报工时
            </el-button>
          </el-form-item>
        </el-form>

        <el-table :data="myLogs" v-loading="loading" border stripe>
          <el-table-column prop="workDate" label="工作日期" width="120" />
          <el-table-column prop="projectName" label="项目" min-width="160" show-overflow-tooltip />
          <el-table-column prop="taskName" label="任务" min-width="160" show-overflow-tooltip />
          <el-table-column prop="hours" label="工时" width="80" align="center" />
          <el-table-column prop="content" label="工作内容" min-width="200" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditRemark" label="审批意见" width="140" show-overflow-tooltip />
          <el-table-column label="操作" width="140" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" :disabled="row.status === '1'" @click="openDialog(row)">修改</el-button>
              <el-button link type="danger" :disabled="row.status === '1'" @click="handleDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
        <el-empty v-if="!loading && !myLogs.length" description="暂无工时记录" />
      </el-tab-pane>

      <!-- 工时审批 -->
      <el-tab-pane label="工时审批" name="audit">
        <el-form :model="query" inline>
          <el-form-item label="项目">
            <el-select v-model="query.projectId" clearable placeholder="全部项目" style="width: 200px">
              <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
            </el-select>
          </el-form-item>
          <el-form-item label="状态">
            <el-select v-model="query.status" clearable placeholder="全部" style="width: 130px">
              <el-option label="待审批" value="0" />
              <el-option label="已通过" value="1" />
              <el-option label="已驳回" value="2" />
            </el-select>
          </el-form-item>
          <el-form-item>
            <el-button type="primary" @click="loadAuditList"><el-icon><Search /></el-icon>搜索</el-button>
          </el-form-item>
        </el-form>

        <div class="table-toolbar">
          <el-button type="success" v-permission="['project:worklog:audit']" :disabled="!selection.length" @click="handleAudit('1')">
            <el-icon><Check /></el-icon>批量通过
          </el-button>
          <el-button type="danger" v-permission="['project:worklog:audit']" :disabled="!selection.length" @click="handleAudit('2')">
            <el-icon><Close /></el-icon>批量驳回
          </el-button>
        </div>

        <el-table :data="auditList" v-loading="loading" border stripe @selection-change="(v) => (selection = v)">
          <el-table-column type="selection" width="50" :selectable="(row) => row.status === '0'" />
          <el-table-column prop="workDate" label="工作日期" width="120" />
          <el-table-column prop="nickName" label="填报人" width="100" />
          <el-table-column prop="projectName" label="项目" min-width="150" show-overflow-tooltip />
          <el-table-column prop="taskName" label="任务" min-width="150" show-overflow-tooltip />
          <el-table-column prop="hours" label="工时" width="80" align="center" />
          <el-table-column prop="content" label="工作内容" min-width="180" show-overflow-tooltip />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="auditByName" label="审批人" width="100" />
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="query.pageNum"
            v-model:page-size="query.pageSize"
            :total="total"
            layout="total, prev, pager, next"
            @current-change="loadAuditList"
          />
        </div>
      </el-tab-pane>
    </el-tabs>

    <!-- 填报工时 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="95px">
        <el-form-item label="所属项目" prop="projectId">
          <el-select v-model="form.projectId" filterable placeholder="请选择项目" style="width: 100%" @change="onProjectChange">
            <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
          </el-select>
        </el-form-item>
        <el-form-item label="关联任务">
          <el-select v-model="form.taskId" filterable clearable placeholder="可选" style="width: 100%">
            <el-option v-for="t in taskOptions" :key="t.taskId" :label="t.taskName" :value="t.taskId" />
          </el-select>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="工作日期" prop="workDate">
              <el-date-picker v-model="form.workDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="工时数" prop="hours">
              <el-input-number v-model="form.hours" :min="0.5" :max="24" :step="0.5" :precision="1" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="工作内容" prop="content">
          <el-input v-model="form.content" type="textarea" :rows="3" placeholder="请描述今天完成的工作" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">提 交</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi, taskApi, worklogApi } from '@/api/business'

const activeTab = ref('mine')
const loading = ref(false)
const submitting = ref(false)

const dateRange = ref([])
const myLogs = ref([])
const projects = ref([])
const taskOptions = ref([])

const query = reactive({ projectId: null, status: '', pageNum: 1, pageSize: 10 })
const auditList = ref([])
const total = ref(0)
const selection = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('填报工时')
const formRef = ref(null)
const form = reactive({
  logId: null,
  projectId: null,
  taskId: null,
  workDate: new Date().toISOString().slice(0, 10),
  hours: 8,
  content: ''
})

const rules = {
  projectId: [{ required: true, message: '请选择所属项目', trigger: 'change' }],
  workDate: [{ required: true, message: '请选择工作日期', trigger: 'change' }],
  hours: [{ required: true, message: '请输入工时数', trigger: 'blur' }],
  content: [{ required: true, message: '请输入工作内容', trigger: 'blur' }]
}

function statusText(s) {
  return { 0: '待审批', 1: '已通过', 2: '已驳回' }[s] || '—'
}

function statusTag(s) {
  return { 0: 'warning', 1: 'success', 2: 'danger' }[s] || 'info'
}

async function loadProjects() {
  const res = await projectApi.my()
  projects.value = res.data
}

async function loadMyLogs() {
  loading.value = true
  try {
    const params = {}
    if (dateRange.value && dateRange.value.length === 2) {
      params.startDate = dateRange.value[0]
      params.endDate = dateRange.value[1]
    }
    const res = await worklogApi.my(params)
    myLogs.value = res.data
  } finally {
    loading.value = false
  }
}

async function loadAuditList() {
  loading.value = true
  try {
    const res = await worklogApi.list(query)
    auditList.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function onProjectChange(projectId) {
  form.taskId = null
  taskOptions.value = []
  if (!projectId) return
  const res = await taskApi.tree(projectId)
  const flat = []
  const walk = (arr) => arr.forEach((t) => { flat.push(t); if (t.children?.length) walk(t.children) })
  walk(res.data || [])
  taskOptions.value = flat
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改工时'
    Object.assign(form, {
      logId: row.logId,
      projectId: row.projectId,
      taskId: row.taskId,
      workDate: row.workDate,
      hours: Number(row.hours),
      content: row.content
    })
    onProjectChange(row.projectId)
  } else {
    dialogTitle.value = '填报工时'
    Object.assign(form, {
      logId: null,
      projectId: null,
      taskId: null,
      workDate: new Date().toISOString().slice(0, 10),
      hours: 8,
      content: ''
    })
    taskOptions.value = []
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
    if (form.logId) {
      await worklogApi.edit(form)
      ElMessage.success('修改成功，已重新提交审批')
    } else {
      await worklogApi.add(form)
      ElMessage.success('提交成功，等待审批')
    }
    dialogVisible.value = false
    loadMyLogs()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该条工时吗？', '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await worklogApi.remove(row.logId)
  ElMessage.success('删除成功')
  loadMyLogs()
}

async function handleAudit(status) {
  const ids = selection.value.map((r) => r.logId)
  if (!ids.length) return

  let remark = ''
  if (status === '2') {
    try {
      const { value } = await ElMessageBox.prompt('请输入驳回原因', '驳回', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPlaceholder: '驳回原因（必填）'
      })
      remark = value
    } catch (e) {
      return
    }
  } else {
    try {
      await ElMessageBox.confirm(`确认通过选中的 ${ids.length} 条工时吗？`, '提示', { type: 'warning' })
    } catch (e) {
      return
    }
  }

  await worklogApi.audit({ logIds: ids, status, auditRemark: remark })
  ElMessage.success('审批完成')
  loadAuditList()
}

onMounted(() => {
  loadProjects()
  loadMyLogs()
  loadAuditList()
})
</script>

<style scoped>
.worklog-tabs {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
}
</style>
