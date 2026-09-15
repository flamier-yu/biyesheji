<template>
  <div class="app-container">
    <el-card shadow="never">
      <template #header>
        <div class="card-head">
          <div>
            <span>消息中心</span>
            <el-tag v-if="unreadCount > 0" type="danger" size="small" style="margin-left: 8px">
              {{ unreadCount }} 条未读
            </el-tag>
            <el-tag :type="wsConnected ? 'success' : 'info'" size="small" style="margin-left: 8px">
              实时推送 {{ wsConnected ? '已连接' : '未连接' }}
            </el-tag>
          </div>
          <div>
            <el-button size="small" @click="handleReadAll" :disabled="!unreadCount">全部已读</el-button>
            <el-button size="small" type="primary" @click="load"><el-icon><Refresh /></el-icon>刷新</el-button>
          </div>
        </div>
      </template>

      <el-form :model="query" inline>
        <el-form-item label="类型">
          <el-select v-model="query.msgType" clearable placeholder="全部" style="width: 140px" @change="handleQuery">
            <el-option v-for="(t, k) in msgTypeMap" :key="k" :label="t" :value="k" />
          </el-select>
        </el-form-item>
        <el-form-item label="状态">
          <el-select v-model="query.isRead" clearable placeholder="全部" style="width: 130px" @change="handleQuery">
            <el-option label="未读" value="0" />
            <el-option label="已读" value="1" />
          </el-select>
        </el-form-item>
        <el-form-item label="标题">
          <el-input v-model="query.title" placeholder="请输入标题" clearable style="width: 180px" @keyup.enter="handleQuery" />
        </el-form-item>
      </el-form>

      <el-table :data="list" v-loading="loading" border stripe>
        <el-table-column width="60" align="center">
          <template #default="{ row }">
            <el-badge v-if="row.isRead === '0'" is-dot type="danger" />
          </template>
        </el-table-column>
        <el-table-column label="类型" width="110" align="center">
          <template #default="{ row }">
            <el-tag size="small" :type="typeTag(row.msgType)">{{ msgTypeMap[row.msgType] || '系统' }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="title" label="标题" width="180" show-overflow-tooltip />
        <el-table-column prop="content" label="内容" min-width="300" show-overflow-tooltip />
        <el-table-column prop="senderName" label="发送人" width="110" />
        <el-table-column prop="createTime" label="时间" width="160" />
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="{ row }">
            <el-button link type="primary" :disabled="row.isRead === '1'" @click="handleRead(row)">标记已读</el-button>
            <el-button link type="danger" v-permission="['message:message:remove']" @click="handleDelete(row)">删除</el-button>
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
          @size-change="load"
          @current-change="load"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { onMounted, onUnmounted, reactive, ref } from 'vue'
import { ElMessage, ElMessageBox, ElNotification } from 'element-plus'
import { messageApi } from '@/api/business'
import { useUserStore } from '@/store/user'

const userStore = useUserStore()

const msgTypeMap = {
  0: '系统消息',
  1: '任务通知',
  2: '项目通知',
  3: '工时通知',
  4: '逾期预警'
}

const loading = ref(false)
const list = ref([])
const total = ref(0)
const unreadCount = ref(0)
const wsConnected = ref(false)
const query = reactive({ msgType: '', isRead: '', title: '', pageNum: 1, pageSize: 10 })

let socket = null
let heartbeatTimer = null
let reconnectTimer = null

function typeTag(t) {
  return { 0: 'info', 1: 'primary', 2: 'success', 3: 'warning', 4: 'danger' }[t] || 'info'
}

async function load() {
  loading.value = true
  try {
    const res = await messageApi.list(query)
    list.value = res.data.rows
    total.value = res.data.total
    const c = await messageApi.unreadCount()
    unreadCount.value = c.data || 0
  } finally {
    loading.value = false
  }
}

function handleQuery() {
  query.pageNum = 1
  load()
}

async function handleRead(row) {
  await messageApi.read([row.messageId])
  ElMessage.success('已标记为已读')
  load()
}

async function handleReadAll() {
  await messageApi.readAll()
  ElMessage.success('全部已读')
  load()
}

async function handleDelete(row) {
  try {
    await ElMessageBox.confirm('确认删除该消息吗？', '警告', { type: 'warning' })
  } catch (e) {
    return
  }
  await messageApi.remove(row.messageId)
  ElMessage.success('删除成功')
  load()
}

/* ==================== WebSocket ==================== */
function connectWs() {
  const userId = userStore.userId
  if (!userId) return
  const protocol = location.protocol === 'https:' ? 'wss' : 'ws'
  const host = location.host
  // 开发环境经 Vite 代理转发到后端
  const url = `${protocol}://${host}/ws/message/${userId}`

  try {
    socket = new WebSocket(url)
  } catch (e) {
    return
  }

  socket.onopen = () => {
    wsConnected.value = true
    clearInterval(heartbeatTimer)
    heartbeatTimer = setInterval(() => {
      if (socket && socket.readyState === WebSocket.OPEN) socket.send('ping')
    }, 25000)
  }

  socket.onmessage = (evt) => {
    if (evt.data === 'pong') return
    try {
      const msg = JSON.parse(evt.data)
      unreadCount.value += 1
      ElNotification({
        title: msg.title || '新消息',
        message: msg.content || '',
        type: msgType(msg.msgType),
        duration: 6000
      })
      // 当前页是第一页且无筛选时自动刷新
      if (query.pageNum === 1 && !query.isRead && !query.msgType) load()
    } catch (e) {
      // 忽略非 JSON 消息
    }
  }

  socket.onclose = () => {
    wsConnected.value = false
    clearInterval(heartbeatTimer)
    // 5 秒后自动重连
    clearTimeout(reconnectTimer)
    reconnectTimer = setTimeout(connectWs, 5000)
  }

  socket.onerror = () => {
    wsConnected.value = false
  }
}

function msgType(t) {
  return { 0: 'info', 1: 'success', 2: 'success', 3: 'warning', 4: 'error' }[t] || 'info'
}

onMounted(async () => {
  await load()
  connectWs()
})

onUnmounted(() => {
  clearInterval(heartbeatTimer)
  clearTimeout(reconnectTimer)
  if (socket) {
    socket.onclose = null
    socket.close()
  }
})
</script>

<style scoped>
.card-head {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
</style>
