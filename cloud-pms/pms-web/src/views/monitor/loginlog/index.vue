<template>
  <div class="app-container">
    <div class="search-bar">
      <el-form :model="query" inline>
        <el-form-item label="登录账号">
          <el-input v-model="query.username" placeholder="请输入账号" clearable style="width: 150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="登录IP">
          <el-input v-model="query.ipaddr" placeholder="请输入IP" clearable style="width: 150px" @keyup.enter="handleQuery" />
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.status" placeholder="全部" clearable style="width: 120px">
            <el-option label="成功" value="0" />
            <el-option label="失败" value="1" />
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
        <el-button type="danger" v-permission="['system:loginlog:remove']" @click="handleClean">
          <el-icon><Delete /></el-icon>清空日志
        </el-button>
      </div>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column prop="infoId" label="ID" width="80" align="center" />
        <el-table-column prop="username" label="登录账号" width="130" />
        <el-table-column prop="ipaddr" label="登录IP" width="140" />
        <el-table-column prop="browser" label="浏览器" width="110" />
        <el-table-column prop="os" label="操作系统" width="110" />
        <el-table-column label="状态" width="90" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="row.status === '0' ? 'success' : 'danger'">
              {{ row.status === '0' ? '成功' : '失败' }}
            </el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="msg" label="提示消息" show-overflow-tooltip />
        <el-table-column prop="loginTime" label="登录时间" width="160" />
        <el-table-column label="操作" width="90" fixed="right">
          <template #default="{ row }">
            <el-button link type="danger" v-permission="['system:loginlog:remove']" @click="handleDelete(row)">删除</el-button>
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
  </div>
</template>

<script setup>
import { onMounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { loginLogApi } from '@/api/system'

const loading = ref(false)
const list = ref([])
const total = ref(0)

const query = reactive({ username: '', ipaddr: '', status: '', pageNum: 1, pageSize: 10 })

async function loadList() {
  loading.value = true
  try {
    const res = await loginLogApi.list(query)
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
  query.username = ''
  query.ipaddr = ''
  query.status = ''
  handleQuery()
}

async function handleDelete(row) {
  await loginLogApi.remove(row.infoId)
  ElMessage.success('删除成功')
  loadList()
}

async function handleClean() {
  try {
    await ElMessageBox.confirm('确认清空全部登录日志吗？该操作不可恢复。', '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await loginLogApi.clean()
  ElMessage.success('清空成功')
  loadList()
}

onMounted(loadList)
</script>
