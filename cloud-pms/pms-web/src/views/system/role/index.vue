<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="角色名称">
          <el-input v-model="query.roleName" placeholder="请输入角色名称" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="权限字符">
          <el-input v-model="query.roleKey" placeholder="请输入权限字符" clearable style="width: 160px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery"><el-icon><Search /></el-icon>搜索</el-button>
          <el-button @click="resetQuery"><el-icon><Refresh /></el-icon>重置</el-button>
        </el-form-item>
      </el-form>
    </div>

    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['system:role:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增角色
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="roleId" label="ID" width="70" align="center" />
        <el-table-column prop="roleName" label="角色名称" width="140" />
        <el-table-column prop="roleKey" label="权限字符" width="140" />
        <el-table-column prop="roleSort" label="排序" width="70" align="center" />
        <el-table-column label="数据范围" width="140">
          <template #default="{ row }">
            {{ dataScopeText(row.dataScope) }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch v-model="row.status" active-value="0" inactive-value="1" @change="handleStatusChange(row)" />
          </template>
        </el-table-column>
        <el-table-column prop="remark" label="备注" show-overflow-tooltip />
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="160" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['system:role:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['system:role:remove']" @click="handleDelete(row)">删除</el-button>
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

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="620px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="角色名称" prop="roleName">
          <el-input v-model="form.roleName" placeholder="请输入角色名称" />
        </el-form-item>
        <el-form-item label="权限字符" prop="roleKey">
          <el-input v-model="form.roleKey" placeholder="如 pm / member" />
        </el-form-item>
        <el-form-item label="显示顺序" prop="roleSort">
          <el-input-number v-model="form.roleSort" :min="0" :max="999" />
        </el-form-item>
        <el-form-item label="数据范围">
          <el-select v-model="form.dataScope" style="width: 100%">
            <el-option label="全部数据" value="1" />
            <el-option label="本部门数据" value="2" />
            <el-option label="本部门及以下" value="3" />
            <el-option label="仅本人数据" value="4" />
          </el-select>
        </el-form-item>
        <el-form-item label="菜单权限">
          <div class="menu-tree-box">
            <el-tree
              ref="menuTreeRef"
              :data="menuTree"
              :props="{ label: 'menuName', children: 'children' }"
              node-key="menuId"
              show-checkbox
              default-expand-all
            />
          </div>
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
import { nextTick, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { menuApi, roleApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const menuTree = ref([])
const menuTreeRef = ref(null)

const query = reactive({ roleName: '', roleKey: '', pageNum: 1, pageSize: 10 })
const dialogVisible = ref(false)
const dialogTitle = ref('新增角色')
const formRef = ref(null)
const form = reactive({
  roleId: null,
  roleName: '',
  roleKey: '',
  roleSort: 0,
  dataScope: '1',
  status: '0',
  remark: ''
})

const rules = {
  roleName: [{ required: true, message: '请输入角色名称', trigger: 'blur' }],
  roleKey: [{ required: true, message: '请输入权限字符', trigger: 'blur' }]
}

function dataScopeText(v) {
  return { 1: '全部数据', 2: '本部门数据', 3: '本部门及以下', 4: '仅本人数据' }[v] || '—'
}

async function loadList() {
  loading.value = true
  try {
    const res = await roleApi.list(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadMenuTree() {
  const res = await menuApi.treeSelect()
  menuTree.value = res.data
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  query.roleName = ''
  query.roleKey = ''
  handleQuery()
}

async function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改角色'
    Object.assign(form, {
      roleId: row.roleId,
      roleName: row.roleName,
      roleKey: row.roleKey,
      roleSort: row.roleSort,
      dataScope: row.dataScope || '1',
      status: row.status,
      remark: row.remark || ''
    })
    const res = await roleApi.get(row.roleId)
    const checked = res.data.menuIds || []
    await nextTick()
    menuTreeRef.value?.setCheckedKeys(checked, false)
  } else {
    dialogTitle.value = '新增角色'
    Object.assign(form, {
      roleId: null,
      roleName: '',
      roleKey: '',
      roleSort: 0,
      dataScope: '1',
      status: '0',
      remark: ''
    })
    await nextTick()
    menuTreeRef.value?.setCheckedKeys([], false)
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
    const menuIds = [
      ...(menuTreeRef.value?.getCheckedKeys() || []),
      ...(menuTreeRef.value?.getHalfCheckedKeys() || [])
    ]
    const payload = { ...form, menuIds }
    if (form.roleId) {
      await roleApi.edit(payload)
      ElMessage.success('修改成功')
    } else {
      await roleApi.add(payload)
      ElMessage.success('新增成功')
    }
    dialogVisible.value = false
    loadList()
  } finally {
    submitting.value = false
  }
}

async function handleStatusChange(row) {
  const text = row.status === '0' ? '启用' : '停用'
  try {
    await ElMessageBox.confirm(`确认要${text}角色「${row.roleName}」吗？`, '提示', { type: 'warning' })
    await roleApi.changeStatus({ roleId: row.roleId, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch (e) {
    row.status = row.status === '0' ? '1' : '0'
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除角色「${row.roleName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await roleApi.remove(row.roleId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => {
  loadList()
  loadMenuTree()
})
</script>

<style scoped>
.menu-tree-box {
  width: 100%;
  max-height: 260px;
  overflow-y: auto;
  border: 1px solid #dcdfe6;
  border-radius: 4px;
  padding: 8px;
}
</style>
