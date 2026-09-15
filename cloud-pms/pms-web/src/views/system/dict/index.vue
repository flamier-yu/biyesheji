<template>
  <div class="app-container">
    <el-tabs v-model="activeTab" class="dict-tabs">
      <!-- 字典类型 -->
      <el-tab-pane label="字典类型" name="type">
        <div class="table-toolbar">
          <el-button type="primary" v-permission="['system:dict:add']" @click="openTypeDialog()">
            <el-icon><Plus /></el-icon>新增字典类型
          </el-button>
        </div>

        <el-table :data="typeList" v-loading="typeLoading" border stripe>
          <el-table-column prop="dictId" label="ID" width="70" align="center" />
          <el-table-column prop="dictName" label="字典名称" width="160" />
          <el-table-column prop="dictType" label="字典类型" width="200" />
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === '0' ? 'success' : 'danger'">
                {{ row.status === '0' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column prop="remark" label="备注" show-overflow-tooltip />
          <el-table-column label="操作" width="200" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" @click="viewData(row)">字典数据</el-button>
              <el-button link type="warning" v-permission="['system:dict:edit']" @click="openTypeDialog(row)">修改</el-button>
              <el-button link type="danger" v-permission="['system:dict:remove']" @click="handleTypeDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>

        <div class="pagination-wrap">
          <el-pagination
            v-model:current-page="typeQuery.pageNum"
            v-model:page-size="typeQuery.pageSize"
            :total="typeTotal"
            layout="total, prev, pager, next"
            @current-change="loadTypeList"
          />
        </div>
      </el-tab-pane>

      <!-- 字典数据 -->
      <el-tab-pane label="字典数据" name="data">
        <div class="table-toolbar">
          <el-select v-model="dataQuery.dictType" placeholder="请选择字典类型" style="width: 220px; margin-right: 10px" @change="loadDataList">
            <el-option v-for="t in typeOptions" :key="t.dictType" :label="`${t.dictName}（${t.dictType}）`" :value="t.dictType" />
          </el-select>
          <el-button type="primary" v-permission="['system:dict:add']" @click="openDataDialog()">
            <el-icon><Plus /></el-icon>新增字典数据
          </el-button>
        </div>

        <el-table :data="dataList" v-loading="dataLoading" border stripe>
          <el-table-column prop="dictCode" label="编码" width="80" align="center" />
          <el-table-column prop="dictSort" label="排序" width="80" align="center" />
          <el-table-column prop="dictLabel" label="字典标签" width="140" />
          <el-table-column prop="dictValue" label="字典键值" width="120" />
          <el-table-column prop="dictType" label="字典类型" width="170" />
          <el-table-column label="回显样式" width="110" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.listClass || 'info'">{{ row.dictLabel }}</el-tag>
            </template>
          </el-table-column>
          <el-table-column label="状态" width="90" align="center">
            <template #default="{ row }">
              <el-tag size="small" :type="row.status === '0' ? 'success' : 'danger'">
                {{ row.status === '0' ? '正常' : '停用' }}
              </el-tag>
            </template>
          </el-table-column>
          <el-table-column label="操作" width="150" fixed="right">
            <template #default="{ row }">
              <el-button link type="primary" v-permission="['system:dict:edit']" @click="openDataDialog(row)">修改</el-button>
              <el-button link type="danger" v-permission="['system:dict:remove']" @click="handleDataDelete(row)">删除</el-button>
            </template>
          </el-table-column>
        </el-table>
      </el-tab-pane>
    </el-tabs>

    <!-- 字典类型表单 -->
    <el-dialog v-model="typeDialogVisible" :title="typeDialogTitle" width="520px" destroy-on-close>
      <el-form ref="typeFormRef" :model="typeForm" :rules="typeRules" label-width="90px">
        <el-form-item label="字典名称" prop="dictName">
          <el-input v-model="typeForm.dictName" placeholder="请输入字典名称" />
        </el-form-item>
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="typeForm.dictType" placeholder="如 pm_task_status" />
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="typeForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="备注">
          <el-input v-model="typeForm.remark" type="textarea" :rows="2" />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="typeDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitType">确 定</el-button>
      </template>
    </el-dialog>

    <!-- 字典数据表单 -->
    <el-dialog v-model="dataDialogVisible" :title="dataDialogTitle" width="520px" destroy-on-close>
      <el-form ref="dataFormRef" :model="dataForm" :rules="dataRules" label-width="90px">
        <el-form-item label="字典类型" prop="dictType">
          <el-input v-model="dataForm.dictType" disabled />
        </el-form-item>
        <el-form-item label="字典标签" prop="dictLabel">
          <el-input v-model="dataForm.dictLabel" placeholder="如 进行中" />
        </el-form-item>
        <el-form-item label="字典键值" prop="dictValue">
          <el-input v-model="dataForm.dictValue" placeholder="如 1" />
        </el-form-item>
        <el-form-item label="显示排序" prop="dictSort">
          <el-input-number v-model="dataForm.dictSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="回显样式">
          <el-select v-model="dataForm.listClass" clearable placeholder="请选择" style="width: 100%">
            <el-option v-for="s in ['primary', 'success', 'info', 'warning', 'danger']" :key="s" :label="s" :value="s" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-radio-group v-model="dataForm.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dataDialogVisible = false">取 消</el-button>
        <el-button type="primary" @click="submitData">确 定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { dictApi } from '@/api/system'

const activeTab = ref('type')

const typeLoading = ref(false)
const typeList = ref([])
const typeTotal = ref(0)
const typeOptions = ref([])
const typeQuery = reactive({ pageNum: 1, pageSize: 10 })

const dataLoading = ref(false)
const dataList = ref([])
const dataQuery = reactive({ dictType: '' })

const typeDialogVisible = ref(false)
const typeDialogTitle = ref('新增字典类型')
const typeFormRef = ref(null)
const typeForm = reactive({ dictId: null, dictName: '', dictType: '', status: '0', remark: '' })
const typeRules = {
  dictName: [{ required: true, message: '请输入字典名称', trigger: 'blur' }],
  dictType: [{ required: true, message: '请输入字典类型', trigger: 'blur' }]
}

const dataDialogVisible = ref(false)
const dataDialogTitle = ref('新增字典数据')
const dataFormRef = ref(null)
const dataForm = reactive({
  dictCode: null, dictType: '', dictLabel: '', dictValue: '', dictSort: 0, listClass: 'info', status: '0'
})
const dataRules = {
  dictType: [{ required: true, message: '请选择字典类型', trigger: 'change' }],
  dictLabel: [{ required: true, message: '请输入字典标签', trigger: 'blur' }],
  dictValue: [{ required: true, message: '请输入字典键值', trigger: 'blur' }]
}

async function loadTypeList() {
  typeLoading.value = true
  try {
    const res = await dictApi.typeList(typeQuery)
    typeList.value = res.data.rows
    typeTotal.value = res.data.total
  } finally {
    typeLoading.value = false
  }
}

async function loadTypeOptions() {
  const res = await dictApi.typeAll()
  typeOptions.value = res.data
}

async function loadDataList() {
  if (!dataQuery.dictType) {
    dataList.value = []
    return
  }
  dataLoading.value = true
  try {
    const res = await dictApi.dataList({ dictType: dataQuery.dictType, pageNum: 1, pageSize: 100 })
    dataList.value = res.data.rows
  } finally {
    dataLoading.value = false
  }
}

function viewData(row) {
  activeTab.value = 'data'
  dataQuery.dictType = row.dictType
  loadDataList()
}

function openTypeDialog(row) {
  typeDialogVisible.value = true
  if (row) {
    typeDialogTitle.value = '修改字典类型'
    Object.assign(typeForm, { ...row })
  } else {
    typeDialogTitle.value = '新增字典类型'
    Object.assign(typeForm, { dictId: null, dictName: '', dictType: '', status: '0', remark: '' })
  }
}

async function submitType() {
  if (!typeFormRef.value) return
  try {
    await typeFormRef.value.validate()
  } catch (e) {
    return
  }
  if (typeForm.dictId) {
    await dictApi.editType(typeForm)
    ElMessage.success('修改成功')
  } else {
    await dictApi.addType(typeForm)
    ElMessage.success('新增成功')
  }
  typeDialogVisible.value = false
  loadTypeList()
  loadTypeOptions()
}

async function handleTypeDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典类型「${row.dictName}」及其字典数据吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await dictApi.removeType(row.dictId)
  ElMessage.success('删除成功')
  loadTypeList()
  loadTypeOptions()
}

function openDataDialog(row) {
  if (!dataQuery.dictType) {
    ElMessage.warning('请先选择字典类型')
    return
  }
  dataDialogVisible.value = true
  if (row) {
    dataDialogTitle.value = '修改字典数据'
    Object.assign(dataForm, { ...row })
  } else {
    dataDialogTitle.value = '新增字典数据'
    Object.assign(dataForm, {
      dictCode: null, dictType: dataQuery.dictType, dictLabel: '', dictValue: '', dictSort: 0, listClass: 'info', status: '0'
    })
  }
}

async function submitData() {
  if (!dataFormRef.value) return
  try {
    await dataFormRef.value.validate()
  } catch (e) {
    return
  }
  if (dataForm.dictCode) {
    await dictApi.editData(dataForm)
    ElMessage.success('修改成功')
  } else {
    await dictApi.addData(dataForm)
    ElMessage.success('新增成功')
  }
  dataDialogVisible.value = false
  loadDataList()
}

async function handleDataDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除字典数据「${row.dictLabel}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await dictApi.removeData(row.dictCode)
  ElMessage.success('删除成功')
  loadDataList()
}

onMounted(() => {
  loadTypeList()
  loadTypeOptions()
})
</script>

<style scoped>
.dict-tabs {
  background: #fff;
  padding: 16px;
  border-radius: 6px;
}
</style>
