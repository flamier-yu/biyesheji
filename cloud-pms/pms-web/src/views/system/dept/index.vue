<template>
  <div class="app-container">
    <div class="table-card">
      <div class="table-toolbar">
        <el-button type="primary" v-permission="['system:dept:add']" @click="openDialog()">
          <el-icon><Plus /></el-icon>新增部门
        </el-button>
        <el-button @click="loadList"><el-icon><Refresh /></el-icon>刷新</el-button>
      </div>

      <el-table
        :data="list"
        v-loading="loading"
        row-key="deptId"
        :tree-props="{ children: 'children' }"
        border
        default-expand-all
      >
        <el-table-column prop="deptName" label="部门名称" min-width="200" />
        <el-table-column prop="deptId" label="ID" width="80" align="center" />
        <el-table-column prop="orderNum" label="排序" width="80" align="center" />
        <el-table-column prop="leader" label="负责人" width="110" />
        <el-table-column prop="phone" label="联系电话" width="140" />
        <el-table-column prop="email" label="邮箱" width="190" show-overflow-tooltip />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag :type="row.status === '0' ? 'success' : 'danger'" size="small">
              {{ row.status === '0' ? '正常' : '停用' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" v-permission="['system:dept:edit']" @click="openDialog(row)">修改</el-button>
            <el-button link type="danger" v-permission="['system:dept:remove']" @click="handleDelete(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="600px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-form-item label="上级部门" prop="parentId">
          <el-tree-select
            v-model="form.parentId"
            :data="treeOptions"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            placeholder="请选择上级部门"
            style="width: 100%"
          />
        </el-form-item>
        <el-form-item label="部门名称" prop="deptName">
          <el-input v-model="form.deptName" placeholder="请输入部门名称" />
        </el-form-item>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="显示顺序" prop="orderNum">
              <el-input-number v-model="form.orderNum" :min="0" :max="999" style="width: 100%" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="负责人">
              <el-input v-model="form.leader" placeholder="请输入负责人" />
            </el-form-item>
          </el-col>
        </el-row>
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="联系电话">
              <el-input v-model="form.phonenumber" placeholder="请输入联系电话" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="邮箱">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
        </el-row>
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
import { computed, onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { deptApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const dialogVisible = ref(false)
const dialogTitle = ref('新增部门')
const formRef = ref(null)

const form = reactive({
  deptId: null,
  parentId: 100,
  deptName: '',
  orderNum: 0,
  leader: '',
  phonenumber: '',
  email: '',
  status: '0'
})

const rules = {
  parentId: [{ required: true, message: '请选择上级部门', trigger: 'change' }],
  deptName: [{ required: true, message: '请输入部门名称', trigger: 'blur' }],
  orderNum: [{ required: true, message: '请输入显示顺序', trigger: 'blur' }]
}

/** 新增时上级部门不能选自己 */
const treeOptions = computed(() => list.value)

async function loadList() {
  loading.value = true
  try {
    const res = await deptApi.tree()
    list.value = res.data
  } finally {
    loading.value = false
  }
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改部门'
    Object.assign(form, {
      deptId: row.deptId,
      parentId: row.parentId,
      deptName: row.deptName,
      orderNum: row.orderNum,
      leader: row.leader,
      phonenumber: row.phone,
      email: row.email,
      status: row.status
    })
  } else {
    dialogTitle.value = '新增部门'
    Object.assign(form, {
      deptId: null,
      parentId: 100,
      deptName: '',
      orderNum: 0,
      leader: '',
      phonenumber: '',
      email: '',
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
    if (form.deptId) {
      await deptApi.edit(form)
      ElMessage.success('修改成功')
    } else {
      await deptApi.add(form)
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
    await ElMessageBox.confirm(`确认删除部门「${row.deptName}」吗？`, '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await deptApi.remove(row.deptId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(loadList)
</script>
