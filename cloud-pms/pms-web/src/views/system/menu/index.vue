<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['system:menu:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增菜单
        </el-button>
        <el-button @click="loadList"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>

      <el-table
        :data="list"
        v-loading="loading"
        row-key="menuId"
        :tree-props="{ children: 'children' }"
        border
      >
        <el-table-column prop="menuName" label="菜单名称" min-width="170" />
        <el-table-column label="图标" width="70" align="center">
          <template #default="{ row }">
            <el-icon v-if="row.icon && row.icon !== '#'"><component :is="row.icon" /></el-icon>
            <span v-else>—</span>
          </template>
        </el-table-column>
        <el-table-column label="类型" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.menuType)">{{ typeText(row.menuType) }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="orderNum" label="排序" width="70" align="center" />
        <el-table-column prop="perms" label="权限标识" width="180" show-overflow-tooltip />
        <el-table-column prop="path" label="路由地址" width="150" show-overflow-tooltip />
        <el-table-column prop="component" label="组件路径" width="180" show-overflow-tooltip />
        <el-table-column label="状态" width="80" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['system:menu:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['system:menu:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="100px">
        <el-form-item label="上级菜单" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ label: 'menuName', value: 'menuId', children: 'children' }"
            check-strictly
            placeholder="请选择上级菜单"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="菜单类型" prop="menuType">
          <el-radio-group v-model="form.menuType">
            <el-radio value="M">目录</el-radio>
            <el-radio value="C">菜单</el-radio>
            <el-radio value="F">按钮</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="菜单名称" prop="menuName">
              <el-input v-model="form.menuName" placeholder="请输入菜单名称" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16" v-if="form.menuType !== 'F'">
          <el-col :span="12">
            <el-form-item label="路由地址" prop="path">
              <el-input v-model="form.path" placeholder="如 /system/user" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="菜单图标">
              <el-input v-model="form.icon" placeholder="Element Plus 图标名" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="组件路径" v-if="form.menuType === 'C'">
          <el-input v-model="form.component" placeholder="如 system/user/index" />
        </el-form-item>

        <el-form-item label="权限标识" v-if="form.menuType !== 'M'">
          <el-input v-model="form.perms" placeholder="如 system:user:list" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
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
import { menuApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const rawTree = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增菜单')
const formRef = ref(null)

const form = reactive({
  menuId: null,
  parentId: 0,
  menuName: '',
  menuType: 'C',
  orderNum: 0,
  path: '',
  component: '',
  perms: '',
  icon: '',
  status: '0'
})

const rules = {
  parentId: [{ required: true, message: '请选择上级菜单', trigger: 'change' }],
  menuName: [{ required: true, message: '请输入菜单名称', trigger: 'blur' }],
  menuType: [{ required: true, message: '请选择菜单类型', trigger: 'change' }],
  orderNum: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }]
}

const treeOptions = ref([{ menuId: 0, menuName: '主目录', children: [] }])

function typeText(t) {
  return { M: '目录', C: '菜单', F: '按钮' }[t] || '—'
}

function typeTag(t) {
  return { M: 'warning', C: 'primary', F: 'info' }[t] || 'info'
}

async function loadList() {
  loading.value = true
  try {
    const res = await menuApi.list()
    list.value = res.data
    rawTree.value = res.data
    treeOptions.value = [{ menuId: 0, menuName: '主目录', children: res.data }]
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改菜单'
    Object.assign(form, {
      menuId: row.menuId,
      parentId: row.parentId,
      menuName: row.menuName,
      menuType: row.menuType,
      orderNum: row.orderNum,
      path: row.path,
      component: row.component,
      perms: row.perms,
      icon: row.icon,
      status: row.status
    })
  } else {
    dialogTitle.value = '新增菜单'
    Object.assign(form, {
      menuId: null,
      parentId: 0,
      menuName: '',
      menuType: 'C',
      orderNum: 0,
      path: '',
      component: '',
      perms: '',
      icon: '',
      status: '0'
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
    if (form.menuId) {
      await menuApi.edit(form)
      ElMessage.success('修改成功')
    } else {
      await menuApi.add(form)
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
    await ElMessageBox.confirm(`确认删除菜单「${row.menuName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await menuApi.remove(row.menuId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>
