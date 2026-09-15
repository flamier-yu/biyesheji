<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="岗位编码">
          <el-input v-model="query.postCode" placeholder="请输入岗位编码" clearable style="width: 150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="岗位名称">
          <el-input v-model="query.postName" placeholder="请输入岗位名称" clearable style="width: 150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['system:post:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增岗位
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="postId" label="ID" width="70" align="center" />
        <el-table-column prop="postCode" label="岗位编码" width="140" />
        <el-table-column prop="postName" label="岗位名称" width="160" />
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['system:post:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['system:post:remove']" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="520px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="岗位编码" prop="postCode">
          <el-input v-model="form.postCode" placeholder="如 dev / qa" />
        </el-form-item>
        <el-form-item label="岗位名称" prop="postName">
          <el-input v-model="form.postName" placeholder="请输入岗位名称" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="orderNum">
          <el-input-number v-model="form.orderNum" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
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
import { postApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const dialogVisible = ref(false)
const dialogTitle = ref('新增岗位')
const formRef = ref(null)

const query = reactive({ postCode: '', postName: '', pageNum: 1, pageSize: 10 })
const form = reactive({
  postId: null,
  postCode: '',
  postName: '',
  orderNum: 0,
  status: '0',
  remark: ''
})

const rules = {
  postCode: [{ required: true, message: '请输入岗位编码', trigger: 'blur' }],
  postName: [{ required: true, message: '请输入岗位名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await postApi.list(query)
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
  query.postCode = ''
  query.postName = ''
  handleQuery()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改岗位'
    Object.assign(form, { ...row })
  } else {
    dialogTitle.value = '新增岗位'
    Object.assign(form, { postId: null, postCode: '', postName: '', orderNum: 0, status: '0', remark: '' })
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
    if (form.postId) {
      await postApi.edit(form)
      ElMessage.success('修改成功')
    } else {
      await postApi.add(form)
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
    await ElMessageBox.confirm(`确认删除岗位「${row.postName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await postApi.remove(row.postId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>
