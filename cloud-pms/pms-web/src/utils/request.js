import axios from 'axios'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getToken, removeToken } from './auth'

/** 与 vite.config.js 中的代理前缀保持一致 */
export const BASE_API = '/dev-api'

const service = axios.create({
  baseURL: BASE_API,
  timeout: 15000
})

/* ==================== 请求拦截 ==================== */
service.interceptors.request.use(
  (config) => {
    const token = getToken()
    if (token) {
      config.headers['Authorization'] = 'Bearer ' + token
    }
    return config
  },
  (error) => Promise.reject(error)
)

/* ==================== 响应拦截 ==================== */
let isRelogining = false

service.interceptors.response.use(
  (response) => {
    const res = response.data
    // 非标准响应直接返回（如文件流）
    if (res == null || typeof res !== 'object' || res.code === undefined) {
      return response.data
    }
    if (res.code === 200) {
      return res
    }
    if (res.code === 401) {
      handleUnauthorized()
      return Promise.reject(new Error(res.msg || '登录状态已过期'))
    }
    ElMessage.error(res.msg || '操作失败')
    return Promise.reject(new Error(res.msg || '操作失败'))
  },
  (error) => {
    const msg = error.response
      ? `请求失败(${error.response.status})`
      : '网络异常，请检查后端服务是否已启动'
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

function handleUnauthorized() {
  if (isRelogining) return
  isRelogining = true
  ElMessageBox.confirm('登录状态已过期，请重新登录', '提示', {
    confirmButtonText: '重新登录',
    cancelButtonText: '取消',
    type: 'warning'
  })
    .then(() => {
      removeToken()
      location.href = '/'
    })
    .catch(() => {})
    .finally(() => {
      isRelogining = false
    })
}

export default service
