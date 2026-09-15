<template>
  <div class="app-container">
    <!-- 查询条件 -->
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="账号">
          <el-input
            v-model="query.username"
            placeholder="请输入账号"
            clearable
            style="width: 150px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input
            v-model="query.nickName"
            placeholder="请输入昵称"
            clearable
            style="width: 150px"
            @keyup.enter="handleQuery"
          />
        </el-form-item>
        <el-form-item label="部门">
          <el-tree-select
            v-model="query.deptId"
            :data="deptTree"
            :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
            check-strictly
            clearable
            placeholder="请选择部门"
            style="width: 180px"
          />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 110px">
            <el-option label="正常" value="0" />
            <el-option label="停用" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="handleQuery">
            <el-icon><Search /></el-icon>搜索
          </el-button>
          <el-button @click="resetQuery">
            <el-icon><Refresh /></el-icon>重置
          </el-button>
        </el-form-item>
      </el-form>
    </div>

    <!-- 列表 -->
    <div class="table-card">
      <div class="table-toolbar">
        <el-button
          type="primary"
          v-permission="['system:user:add']"
          @click="openDialog()"
        >
          <el-icon><Plus /></el-icon>新增用户
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="userId" label="ID" width="70" align="center" />
        <el-table-column prop="username" label="登录账号" width="130" show-overflow-tooltip />
        <el-table-column prop="nickName" label="用户昵称" width="120" show-overflow-tooltip />
        <el-table-column prop="deptName" label="所属部门" width="150" show-overflow-tooltip />
        <el-table-column prop="phonenumber" label="手机号" width="130" />
        <el-table-column label="性别" width="70" align="center">
          <template #default="{ row }">
            {{ { 0: '男', 1: '女', 2: '未知' }[row.sex] || '未知' }}
          </template>
        </el-table-column>
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-switch
              v-model="row.status"
              active-value="0"
              inactive-value="1"
              @change="handleStatusChange(row)"
            />
          </template>
        </el-table-column>
        <el-table-column prop="createTime" label="创建时间" width="160" />
        <el-table-column label="操作" width="230" fixed="right">
          <template #default="{ row }">
            <el-button
              link
              type="primary"
              v-permission="['system:user:edit']"
              @click="openDialog(row)"
            >
              修改
            </el-button>
            <el-button
              link
              type="warning"
              v-permission="['system:user:resetPwd']"
              @click="handleResetPwd(row)"
            >
              重置密码
            </el-button>
            <el-button
              link
              type="danger"
              v-permission="['system:user:remove']"
              @click="handleDelete(row)"
            >
              删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-wrap">
        <el-pagination
          v-model:current-page="query.pageNum"
          v-model:page-size="query.pageSize"
          :page-sizes="[5, 10, 20, 50]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          @size-change="loadList"
          @current-change="loadList"
        />
      </div>
    </div>

    <!-- 新增/修改 -->
    <el-dialog v-model="dialogVisible" :title="dialogTitle" width="640px" destroy-on-close>
      <el-form ref="formRef" :model="form" :rules="rules" label-width="90px">
        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="登录账号" prop="username">
              <el-input v-model="form.username" placeholder="请输入登录账号" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="用户昵称" prop="nickName">
              <el-input v-model="form.nickName" placeholder="请输入用户昵称" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="所属部门" prop="deptId">
              <el-tree-select
                v-model="form.deptId"
                :data="deptTree"
                :props="{ label: 'deptName', value: 'deptId', children: 'children' }"
                check-strictly
                placeholder="请选择部门"
                style="width: 100%"
              />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="手机号" prop="phonenumber">
              <el-input v-model="form.phonenumber" placeholder="请输入手机号" />
            </el-form-item>
          </el-col>
        </el-row>

        <el-row :gutter="16">
          <el-col :span="12">
            <el-form-item label="邮箱" prop="email">
              <el-input v-model="form.email" placeholder="请输入邮箱" />
            </el-form-item>
          </el-col>
          <el-col :span="12">
            <el-form-item label="性别">
              <el-radio-group v-model="form.sex">
                <el-radio value="0">男</el-radio>
                <el-radio value="1">女</el-radio>
              </el-radio-group>
            </el-form-item>
          </el-col>
        </el-row>

        <el-form-item label="分配角色">
          <el-select v-model="form.roleIds" multiple placeholder="请选择角色" style="width: 100%">
            <el-option
              v-for="r in roles"
              :key="r.roleId"
              :label="r.roleName"
              :value="r.roleId"
            />
          </el-select>
        </el-form-item>

        <el-form-item v-if="!form.userId" label="初始密码" prop="password">
          <el-input v-model="form.password" type="password" show-password placeholder="留空则使用默认密码 123456" />
        </el-form-item>

        <el-form-item label="状态">
          <el-radio-group v-model="form.status">
            <el-radio value="0">正常</el-radio>
            <el-radio value="1">停用</el-radio>
          </el-radio-group>
        </el-form-item>

        <el-form-item label="备注">
          <el-input v-model="form.remark" type="textarea" :rows="2" placeholder="请输入备注" />
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
import { deptApi, roleApi, userApi } from '@/api/system'

const loading = ref(false)
const submitting = ref(false)
const list = ref([])
const total = ref(0)
const deptTree = ref([])
const roles = ref([])

const query = reactive({
  username: '',
  nickName: '',
  deptId: null,
  status: '',
  pageNum: 1,
  pageSize: 10
})

const dialogVisible = ref(false)
const dialogTitle = ref('新增用户')
const formRef = ref(null)
const form = reactive({
  userId: null,
  username: '',
  nickName: '',
  deptId: null,
  phonenumber: '',
  email: '',
  sex: '0',
  status: '0',
  password: '',
  roleIds: [],
  remark: ''
})

const rules = {
  username: [{ required: true, message: '请输入登录账号', trigger: 'blur' }],
  nickName: [{ required: true, message: '请输入用户昵称', trigger: 'blur' }],
  deptId: [{ required: true, message: '请选择所属部门', trigger: 'change' }],
  email: [{ type: 'email', message: '邮箱格式不正确', trigger: 'blur' }]
}

async function loadList() {
  loading.value = true
  try {
    const res = await userApi.list(query)
    list.value = res.data.rows
    total.value = res.data.total
  } finally {
    loading.value = false
  }
}

async function loadDeptTree() {
  const res = await deptApi.tree()
  deptTree.value = res.data
}

async function loadRoles() {
  const res = await roleApi.all()
  roles.value = res.data
}

function handleQuery() {
  query.pageNum = 1
  loadList()
}

function resetQuery() {
  query.username = ''
  query.nickName = ''
  query.deptId = null
  query.status = ''
  handleQuery()
}

function openDialog(row) {
  dialogVisible.value = true
  if (row) {
    dialogTitle.value = '修改用户'
    Object.assign(form, {
      userId: row.userId,
      username: row.username,
      nickName: row.nickName,
      deptId: row.deptId,
      phonenumber: row.phonenumber,
      email: row.email,
      sex: row.sex || '0',
      status: row.status,
      password: '',
      roleIds: [],
      remark: row.remark || ''
    })
    userApi.get(row.userId).then((res) => {
      form.roleIds = res.data.roleIds || []
    })
  } else {
    dialogTitle.value = '新增用户'
    Object.assign(form, {
      userId: null,
      username: '',
      nickName: '',
      deptId: null,
      phonenumber: '',
      email: '',
      sex: '0',
      status: '0',
      password: '',
      roleIds: [],
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
    if (form.userId) {
      await userApi.edit(form)
      ElMessage.success('修改成功')
    } else {
      await userApi.add(form)
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
    await ElMessageBox.confirm(`确认要${text}用户「${row.nickName}」吗？`, '提示', {
      type: 'warning'
    })
    await userApi.changeStatus({ userId: row.userId, status: row.status })
    ElMessage.success(`${text}成功`)
  } catch (e) {
    // 取消时回滚开关状态
    row.status = row.status === '0' ? '1' : '0'
  }
}

async function handleResetPwd(row) {
  try {
    const { value } = await ElMessageBox.prompt(
      `请输入「${row.nickName}」的新密码`,
      '重置密码',
      {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        inputPattern: /^.{5,20}$/,
        inputErrorMessage: '密码长度为 5-20 位'
      }
    )
    await userApi.resetPwd({ userId: row.userId, password: value })
    ElMessage.success('密码重置成功')
  } catch (e) {
    // 取消
  }
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm(`确认删除用户「${row.nickName}」吗？`, '警告', {
      type: 'warning'
    })
  } catch (e) {
    return
  }
  await userApi.remove(row.userId)
  ElMessage.success('删除成功')
  loadList()
}

onMounted(() => {
  loadList()
  loadDeptTree()
  loadRoles()
})
</script>
