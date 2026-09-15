<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="项目名称">
          <el-input v-model="query.projectName" placeholder="请输入项目名称" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="项目编号">
          <el-input v-model="query.projectCode" placeholder="请输入项目编号" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 130px">
            <el-option v-for="(t, k) in statusMap" :key="k" :label="t.text" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['project:project:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新建项目
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="projectCode" label="项目编号" width="120" />
        <el-table-column prop="projectName" label="项目名称" min-width="180" show-overflow-tooltip>
          <template #default="{ row }">
            <el-link type="primary" @click="openDetail(row)">{{ row.projectName }}</el-link>
          </template>
        </el-table-column>
        <el-table-column prop="managerName" label="项目经理" width="100" />
        <el-table-column prop="deptName" label="所属部门" width="130" show-overflow-tooltip />
        <el-table-column label="优先级" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="priorityTag(row.priority)">{{ priorityText(row.priority) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column label="进度" width="170">
          <template #default="{ row }">
            <el-progress :percentage="row.progress || 0" :stroke-width="12" />
          </template>
        </el-table-column>
        <el-table-column label="任务" width="90" align="center">
          <template #default="{ row }">
            <span>{{ row.doneTaskCount || 0 }} / {{ row.taskCount || 0 }}</span>
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusMap[row.status]?.type || 'info'">
              {{ statusMap[row.status]?.text || '—' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="endDate" label="计划结束" width="115" />
        <el-table-column label="操作" width="190" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['project:project:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="success" v-permission="['project:project:edit']" @click="openStatusDialog(row)">状态</el-button>
            <el-button link type="danger" v-permission="['project:project:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[5, 10, 20]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 新增/修改 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="700px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="95px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目编号" prop="projectCode">
              <el-input v-model="form.projectCode" placeholder="如 PJ2026004" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目名称" prop="projectName">
              <el-input v-model="form.projectName" placeholder="请输入项目名称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="项目经理" prop="managerId">
              <el-select v-model="form.managerId" filterable placeholder="请选择项目经理" style="width: 100%">
                <el-option v-for="u in users" :key="u.userId" :label="u.nickName" :value="u.userId" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
                check-strictly
                clearable
                placeholder="请选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="优先级">
              <el-select v-model="form.priority" style="width: 100%">
                <el-option label="高" value="1" />
                <el-option label="中" value="2" />
                <el-option label="低" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="项目预算">
              <el-input-number v-model="form.budget" :min="0" :precision="2" :step="10000" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="开始日期">
              <el-date-picker v-model="form.startDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="结束日期">
              <el-date-picker v-model="form.endDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="项目成员">
          <el-select v-model="form.memberIds" multiple filterable placeholder="请选择成员" style="width: 100%">
            <el-option v-for="u in users" :key="u.userId" :label="u.nickName" :value="u.userId" />
          </el-select>
        </el-form-item>

        <el-form-item label="项目描述">
          <el-input v-model="form.description" type="textarea" :rows="3" placeholder="请输入项目描述" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 状态变更 -->
    <el-dialog v-model="statusVisible" title="变更项目状态" width="420px">
      <el-form label-width="80px">
        <el-form-item label="当前状态">
          <el-tag :type="statusMap[currentRow?.status]?.type">{{ statusMap[currentRow?.status]?.text }}</el-tag>
        </el-form-item>
        <el-form-item label="目标状态">
          <el-select v-model="targetStatus" style="width: 100%">
            <el-option v-for="(t, k) in statusMap" :key="k" :label="t.text" :value="k" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="statusVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitStatus">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 项目详情 -->
    <el-drawer v-model="detailVisible" :title="detail.projectName" size="720px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="项目编号">{{ detail.projectCode }}</el-descriptions-item>
        <el-descriptions-item label="项目经理">{{ detail.managerName }}</el-descriptions-item>
        <el-descriptions-item label="所属部门">{{ detail.deptName }}</el-descriptions-item>
        <el-descriptions-item label="状态">
          <el-tag size="small" :type="statusMap[detail.status]?.type">{{ statusMap[detail.status]?.text }}</el-tag>
        </el-descriptions-item>
        <el-descriptions-item label="计划周期" :span="2">
          {{ detail.startDate }} ~ {{ detail.endDate }}
        </el-descriptions-item>
        <el-descriptions-item label="项目预算">{{ detail.budget }}</el-descriptions-item>
        <el-descriptions-item label="进度">{{ detail.progress }}%</el-descriptions-item>
        <el-descriptions-item label="项目描述" :span="2">{{ detail.description || '—' }}</el-descriptions-item>
      </el-descriptions>

      <el-divider content-position="left">项目成员（{{ detail.members?.length || 0 }}）</el-divider>
      <el-space wrap>
        <el-tag v-for="m in detail.members || []" :key="m.id" type="info" effect="plain">
          {{ m.nickName || m.username }}
        </el-tag>
      </el-space>

      <el-divider content-position="left">里程碑（{{ detail.milestones?.length || 0 }}）</el-divider>
      <el-timeline>
        <el-timeline-item
          v-for="ms in detail.milestones || []"
          :key="ms.milestoneId"
          :timestamp="ms.planDate"
          :type="ms.status === '2' ? 'success' : ms.status === '3' ? 'danger' : 'primary'"
        >
          {{ ms.milestoneName }}
          <el-tag size="small" style="margin-left: 8px">{{ milestoneText(ms.status) }}</el-tag>
        </el-timeline-item>
        <el-empty v-if="!detail.milestones || !detail.milestones.length" description="暂无里程碑" :image-size="60" />
      </el-timeline>
    </el-drawer>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/business'
import { deptApi, userApi } from '@/api/system'

const statusMap = {
  0: { text: '待审批', type: 'warning' },
  1: { text: '进行中', type: 'primary' },
  2: { text: '已完成', type: 'success' },
  3: { text: '已暂停', type: 'info' },
  4: { text: '已终止', type: 'danger' }
}

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const users = ref([])
const deptTree = ref([])

const query = reactive({ projectName: '', projectCode: '', status: '', pageNum: 1, pageSize: 10 })

const dialogVisible = ref(false)
const dialogTitle = ref('新建项目')
const formRef = ref(null)
const form = reactive({
  projectId: null,
  projectCode: '',
  projectName: '',
  managerId: null,
  deptId: null,
  priority: '2',
  budget: 0,
  startDate: '',
  endDate: '',
  memberIds: [],
  description: ''
})

const rules = {
  projectCode: [{ required: true, message: '请输入项目编号', trigger: 'blur' }],
  projectName: [{ required: true, message: '请输入项目名称', trigger: 'blur' }],
  managerId: [{ required: true, message: '请选择项目经理', trigger: 'change' }]
}

const statusVisible = ref(false)
const targetStatus = ref('1')
const currentRow = ref(null)

const detailVisible = ref(false)
const detail = ref({})

function priorityText(p) {
  return { 1: '高', 2: '中', 3: '低' }[p] || '—'
}

function priorityTag(p) {
  return { 1: 'danger', 2: 'warning', 3: 'info' }[p] || 'info'
}

function milestoneText(s) {
  return { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已延期' }[s] || '—'
}

async function loadList() {
  loading.value = true
  try {
    const res = await projectApi.list(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadUsers() {
  const res = await userApi.list({ pageNum: 1, pageSize: 200 })
  users.value = res.data.rows
}

async function loadDeptTree() {
  const res = await deptApi.tree()
  deptTree.value = res.data
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  query.projectName = ''
  query.projectCode = ''
  query.status = ''
  handleQuery()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改项目'
    Object.assign(form, {
      projectId: row.projectId,
      projectCode: row.projectCode,
      projectName: row.projectName,
      managerId: row.managerId,
      deptId: row.deptId,
      priority: row.priority || '2',
      budget: row.budget || 0,
      startDate: row.startDate,
      endDate: row.endDate,
      memberIds: [],
      description: row.description
    })
    projectApi.get(row.projectId).then((res) => {
      form.memberIds = res.data.memberIds || []
    })
  } else {
    dialogTitle.value = '新建项目'
    Object.assign(form, {
      projectId: null,
      projectCode: 'PJ' + new Date().getFullYear() + String(Date.now()).slice(-3),
      projectName: '',
      managerId: null,
      deptId: null,
      priority: '2',
      budget: 0,
      startDate: '',
      endDate: '',
      memberIds: [],
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
    if (form.projectId) {
      await projectApi.edit(form)
      ElMessage.success('修改成功')
    } else {
      await projectApi.add(form)
      ElMessage.success('创建成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

function openStatusDialog(row) {
  currentRow.value = row
  targetStatus.value = row.status
  statusVisible.value = true
}

async function submitStatus() {
  await projectApi.changeStatus({ projectId: currentRow.value.projectId, status: targetStatus.value })
  ElMessage.success('状态已更新')
  statusVisible.value = false
  loadList()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除项目「${row.projectName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await projectApi.remove(row.projectId)
  ElMessage.success('删除成功')
  loadList()
}

async function openDetail(row) {
  const res = await projectApi.get(row.projectId)
  detail.value = res.data
  detailVisible.value = true
}

onMounted(() => {
  loadList()
  loadUsers()
  loadDeptTree()
})
</script>
