<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="参数名称">
          <el-input v-model="query.configName" placeholder="请输入参数名称" clearable style="width: 170px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="参数键名">
          <el-input v-model="query.configKey" placeholder="请输入参数键名" clearable style="width: 200px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['system:config:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增参数
        </el-button>
        <el-button type="warning" v-permission="['system:config:edit']" @click="handleRefreshCache">
          <el-icon><Refresh /></el-icon>刷新缓存
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="configId" label="ID" width="70" align="center" />
        <el-table-column prop="configName" label="参数名称" width="180" />
        <el-table-column prop="configKey" label="参数键名" width="230" />
        <el-table-column prop="configValue" label="参数键值" width="150" />
        <el-table-column label="系统内置" width="100" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.configType === 'Y' ? 'warning' : 'info'">
              {{ row.configType === 'Y' ? '是' : '否' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['system:config:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['system:config:remove']" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="560px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="参数名称" prop="configName">
          <el-input v-model="form.configName" placeholder="请输入参数名称" />
        </el-form-item>
        <el-form-item label="参数键名" prop="configKey">
          <el-input v-model="form.configKey" placeholder="如 sys.user.initPassword" />
        </el-form-item>
        <el-form-item label="参数键值" prop="configValue">
          <el-input v-model="form.configValue" placeholder="请输入参数键值" />
        </el-form-item>
        <el-form-item label="系统内置">
          <el-radio-group v-model="form.configType">
            <el-radio value="Y">是</el-radio>
            <el-radio value="N">否</el-radio>
          </el-radio-group>
        </el-form-item>
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
import { configApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增参数')
const formRef = ref(null)

const query = reactive({ configName: '', configKey: '', pageNum: 1, pageSize: 10 })
const form = reactive({
  configId: null, configName: '', configKey: '', configValue: '', configType: 'N', remark: ''
})

const rules = {
  configName: [{ required: true, message: '请输入参数名称', trigger: 'blur' }],
  configKey: [{ required: true, message: '请输入参数键名', trigger: 'blur' }],
  configValue: [{ required: true, message: '请输入参数键值', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await configApi.list(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  query.configName = ''
  query.configKey = ''
  handleQuery()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改参数'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '新增参数'
    Object.assign(form, { configId: null, configName: '', configKey: '', configValue: '', configType: 'N', remark: '' })
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
    if (form.configId) {
      await configApi.edit(form)
      ElMessage.success('修改成功（缓存已刷新）')
    } else {
      await configApi.add(form)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除参数「${row.configName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await configApi.remove(row.configId)
  ElMessage.success('删除成功')
  loadList()
}

async function handleRefreshCache() {
  await configApi.refreshCache()
  ElMessage.success('参数缓存已刷新')
}

onMounted(loadList)
</script>
