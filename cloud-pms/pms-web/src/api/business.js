import request, { BASE_API } from '@/utils/request'
import { getToken } from '@/utils/auth'

/* ==================== 项目 ==================== */
export const projectApi = {
  list: (params) => request({ url: '/project/list', method: 'get', params }),
  my: () => request({ url: '/project/my', method: 'get' }),
  get: (projectId) => request({ url: `/project/${projectId}`, method: 'get' }),
  add: (data) => request({ url: '/project', method: 'post', data }),
  edit: (data) => request({ url: '/project', method: 'put', data }),
  remove: (projectIds) => request({ url: `/project/${projectIds}`, method: 'delete' }),
  changeStatus: (data) => request({ url: '/project/changeStatus', method: 'put', data }),
  recalc: (projectId) => request({ url: `/project/${projectId}/recalc`, method: 'put' }),
  milestones: (projectId) => request({ url: `/project/${projectId}/milestones`, method: 'get' }),
  saveMilestone: (data) => request({ url: '/project/milestone', method: 'post', data }),
  removeMilestone: (id) => request({ url: `/project/milestone/${id}`, method: 'delete' })
}

/* ==================== 任务 ==================== */
export const taskApi = {
  list: (params) => request({ url: '/task/list', method: 'get', params }),
  my: () => request({ url: '/task/my', method: 'get' }),
  tree: (projectId) => request({ url: `/task/tree/${projectId}`, method: 'get' }),
  board: (projectId) => request({ url: `/task/board/${projectId}`, method: 'get' }),
  get: (taskId) => request({ url: `/task/${taskId}`, method: 'get' }),
  add: (data) => request({ url: '/task', method: 'post', data }),
  edit: (data) => request({ url: '/task', method: 'put', data }),
  remove: (taskIds) => request({ url: `/task/${taskIds}`, method: 'delete' }),
  changeStatus: (data) => request({ url: '/task/changeStatus', method: 'put', data }),
  assign: (data) => request({ url: '/task/assign', method: 'put', data }),
  comments: (taskId) => request({ url: `/task/${taskId}/comments`, method: 'get' }),
  addComment: (data) => request({ url: '/task/comment', method: 'post', data }),
  removeComment: (id) => request({ url: `/task/comment/${id}`, method: 'delete' })
}

/* ==================== 工时 ==================== */
export const worklogApi = {
  list: (params) => request({ url: '/worklog/list', method: 'get', params }),
  my: (params) => request({ url: '/worklog/my', method: 'get', params }),
  add: (data) => request({ url: '/worklog', method: 'post', data }),
  edit: (data) => request({ url: '/worklog', method: 'put', data }),
  remove: (logIds) => request({ url: `/worklog/${logIds}`, method: 'delete' }),
  audit: (data) => request({ url: '/worklog/audit', method: 'put', data })
}

/* ==================== 文件 ==================== */
export const fileApi = {
  upload: (formData) =>
    request({
      url: '/file/upload',
      method: 'post',
      data: formData,
      headers: { 'Content-Type': 'multipart/form-data' }
    }),
  list: (params) => request({ url: '/file/list', method: 'get', params }),
  check: (md5) => request({ url: '/file/check', method: 'get', params: { md5 } }),
  remove: (attachId) => request({ url: `/file/${attachId}`, method: 'delete' }),
  downloadUrl: (attachId) => `${BASE_API}/file/download/${attachId}`
}

/* ==================== 消息 ==================== */
export const messageApi = {
  list: (params) => request({ url: '/message/list', method: 'get', params }),
  unreadCount: () => request({ url: '/message/unread/count', method: 'get' }),
  unreadTypes: () => request({ url: '/message/unread/types', method: 'get' }),
  read: (messageIds) => request({ url: '/message/read', method: 'put', data: { messageIds } }),
  readAll: () => request({ url: '/message/readAll', method: 'put' }),
  remove: (messageIds) => request({ url: `/message/${messageIds}`, method: 'delete' })
}

/* ==================== 报表 ==================== */
export const reportApi = {
  overview: () => request({ url: '/report/overview', method: 'get' }),
  projectStatus: () => request({ url: '/report/project/status', method: 'get' }),
  taskStatus: () => request({ url: '/report/task/status', method: 'get' }),
  taskPriority: () => request({ url: '/report/task/priority', method: 'get' }),
  worklogTrend: (days) => request({ url: '/report/worklog/trend', method: 'get', params: { days } }),
  worklogByProject: () => request({ url: '/report/worklog/project', method: 'get' }),
  performance: (limit) => request({ url: '/report/worklog/performance', method: 'get', params: { limit } }),
  projectProgress: (limit) => request({ url: '/report/project/progress', method: 'get', params: { limit } }),
  burndown: (projectId) => request({ url: `/report/burndown/${projectId}`, method: 'get' }),
  exportWorkLogUrl: (projectId) =>
    `${BASE_API}/report/worklog/export${projectId ? `?projectId=${projectId}` : ''}&token=${getToken()}`.replace(
      '&token',
      projectId ? '&token' : '?token'
    )
}
