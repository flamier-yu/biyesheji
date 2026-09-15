<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="模块标题">
          <el-input v-model="query.title" placeholder="请输入模块标题" clearable style="width: 150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="操作人员">
          <el-input v-model="query.operName" placeholder="请输入操作人员" clearable style="width: 140px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="业务类型">
          <el-select v-model="query.businessType" placeholder="全部" clearable style="width: 120px">
            <el-option v-for="(t, i) in businessTypes" :key="i" :label="t" :value="i" />
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
        <el-button type="danger" v-permission="['system:operlog:remove']" @click="handleClean">
          <el-icon><Delete /></el-icon>清空日志
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="operId" label="ID" width="70" align="center" />
        <el-table-column prop="title" label="模块" width="110" />
        <el-table-column label="类型" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.businessType)">
              {{ businessTypes[row.businessType] || '其它' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="operName" label="操作人" width="100" />
        <el-table-column label="请求" width="220" show-overflow-tooltip>
          <template #default="{ row }">
            <span class="method">{{ row.requestMethod }}</span> {{ row.operUrl }}
          </template>
        </el-table-column>
        <el-table-column prop="operIp" label="IP" width="130" />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === 0 ? 'success' : 'danger'">
              {{ row.status === 0 ? '正常' : '异常' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="costTime" label="耗时" width="90" align="center">
          <template #default="{ row }">{{ row.costTime }} ms</template>
        </el-table-column>
        <el-table-column prop="operTime" label="操作时间" width="160" />
        <el-table-column label="操作" width="100" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" @click="openDetail(row)">详情</el-button>
            <el-button link type="danger" v-permission="['system:operlog:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <el-dialog v-model="detailVisible" title="操作日志详情" width="700px">
      <el-descriptions :column="2" border>
        <el-descriptions-item label="模块标题">{{ detail.title }}</el-descriptions-item>
        <el-descriptions-item label="操作人员">{{ detail.operName }}</el-descriptions-item>
        <el-descriptions-item label="请求方式">{{ detail.requestMethod }}</el-descriptions-item>
        <el-descriptions-item label="操作IP">{{ detail.operIp }}</el-descriptions-item>
        <el-descriptions-item label="请求地址" :span="2">{{ detail.operUrl }}</el-descriptions-item>
        <el-descriptions-item label="目标方法" :span="2">{{ detail.method }}</el-descriptions-item>
        <el-descriptions-item label="请求参数" :span="2">
          <pre class="log-pre">{{ detail.operParam }}</pre>
        </el-descriptions-item>
        <el-descriptions-item label="返回结果" :span="2">
          <pre class="log-pre">{{ detail.jsonResult }}</pre>
        </el-descriptions-item>
        <el-descriptions-item v-if="detail.errorMsg" label="错误信息" :span="2">
          <span style="color: #f56c6c">{{ detail.errorMsg }}</span>
        </el-descriptions-item>
        <el-descriptions-item label="操作时间">{{ detail.operTime }}</el-descriptions-item>
        <el-descriptions-item label="消耗时间">{{ detail.costTime }} ms</el-descriptions-item>
      </el-descriptions>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { operLogApi } from '@/api/system'

const businessTypes = ['其它', '新增', '修改', '删除', '导出', '导入']

const loading = ref(false)
const list = ref([])
const total = ref(0)
const detailVisible = ref(false)
const detail = ref({})

const query = reactive({ title: '', operName: '', businessType: null, pageNum: 1, pageSize: 10 })

function typeTag(t) {
  return { 0: 'info', 1: 'success', 2: 'warning', 3: 'danger', 4: 'primary', 5: 'primary' }[t] || 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await operLogApi.list(query)
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
  query.title = ''
  query.operName = ''
  query.businessType = null
  handleQuery()
}

function openDetail(row) {
  detail.value = row
  detailVisible.value = true
}

async function handleDelete(row) {
  await operLogApi.remove(row.operId)
  ElMessage.success('删除成功')
  loadList()
}

async function handleClean() {
  try {
    await ElMessageBox.confirm('确认清空全部操作日志吗？该操作不可恢复。', '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await operLogApi.clean()
  ElMessage.success('清空成功')
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.method {
  color: #409eff;
  font-weight: 600;
  font-size: 12px;
}

.log-pre {
  margin: 0;
  white-space: pre-wrap;
  word-break: break-all;
  max-height: 180px;
  overflow: auto;
  font-size: 12px;
}
</style>
