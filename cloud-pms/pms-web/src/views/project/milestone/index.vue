<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <el-select v-model="currentProjectId" placeholder="请选择项目" style="width: 260px; margin-right: 12px" @change="loadMilestones">
          <el-option v-for="p in projects" :key="p.projectId" :label="p.projectName" :value="p.projectId" />
        </el-select>
        <el-button type="primary" v-permission="['project:milestone:add']" :disabled="!currentProjectId" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增里程碑
        </el-button>
      </div>

      <el-table :data="milestones" v-loading="loading" border stripe>
        <el-table-column prop="orderNum" label="顺序" width="70" align="center" />
        <el-table-column prop="milestoneName" label="里程碑名称" min-width="200" />
        <el-table-column prop="planDate" label="计划完成" width="120" />
        <el-table-column prop="actualDate" label="实际完成" width="120" />
        <el-table-column label="状态" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="statusTag(row.status)">{{ statusText(row.status) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['project:milestone:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['project:milestone:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <el-empty v-if="!loading && !milestones.length" description="暂无里程碑" />
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="里程碑名称" prop="milestoneName">
          <el-input v-model="form.milestoneName" placeholder="如 需求评审完成" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="计划完成">
              <el-date-picker v-model="form.planDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="实际完成">
              <el-date-picker v-model="form.actualDate" type="date" value-format="YYYY-MM-DD" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="状态">
              <el-select v-model="form.status" style="width: 100%">
                <el-option label="未开始" value="0" />
                <el-option label="进行中" value="1" />
                <el-option label="已完成" value="2" />
                <el-option label="已延期" value="3" />
              </el-select>
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取 消</el-button>
        <el-button type="primary" :loading="submitting" @click="handleSubmit">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { projectApi } from '@/api/business'

const loading = ref(false)
const submitting = ref(false)
const projects = ref([])
const currentProjectId = ref(null)
const milestones = ref([])

const dialogVisible = ref(false)
const dialogTitle = ref('新增里程碑')
const formRef = ref(null)
const form = reactive({
  milestoneId: null,
  projectId: null,
  milestoneName: '',
  planDate: '',
  actualDate: '',
  status: '0',
  orderNum: 0,
  remark: ''
})

const rules = {
  milestoneName: [{ required: true, message: '请输入里程碑名称', trigger: 'blur' }]
}

function statusText(s) {
  return { 0: '未开始', 1: '进行中', 2: '已完成', 3: '已延期' }[s] || '—'
}

function statusTag(s) {
  return { 0: 'info', 1: 'primary', 2: 'success', 3: 'danger' }[s] || 'info'
}

async function loadProjects() {
  const res = await projectApi.my()
  projects.value = res.data
  if (projects.value.length && !currentProjectId.value) {
    currentProjectId.value = projects.value[0].projectId
  }
}

async function loadMilestones() {
  if (!currentProjectId.value) return
  loading.value = true
  try {
    const res = await projectApi.milestones(currentProjectId.value)
    milestones.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改里程碑'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '新增里程碑'
    Object.assign(form, {
      milestoneId: null,
      projectId: currentProjectId.value,
      milestoneName: '',
      planDate: '',
      actualDate: '',
      status: '0',
      orderNum: milestones.value.length + 1,
      remark: ''
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
    form.projectId = currentProjectId.value
    await projectApi.saveMilestone(form)
    ElMessage.success('保存成功')
    dialogVisible.value = false
    loadMilestones()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除里程碑「${row.milestoneName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await projectApi.removeMilestone(row.milestoneId)
  ElMessage.success('删除成功')
  loadMilestones()
}

onMounted(async () => {
  await loadProjects()
  await loadMilestones()
})
</script>
