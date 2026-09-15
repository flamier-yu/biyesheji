import request from '@/utils/request'

/* ==================== 用户 ==================== */
export const userApi = {
  list: (params) => request({ url: '/system/user/list', method: 'get', params }),
  get: (userId) => request({ url: `/system/user/${userId}`, method: 'get' }),
  add: (data) => request({ url: '/system/user', method: 'post', data }),
  edit: (data) => request({ url: '/system/user', method: 'put', data }),
  remove: (userIds) => request({ url: `/system/user/${userIds}`, method: 'delete' }),
  resetPwd: (data) => request({ url: '/system/user/resetPwd', method: 'put', data }),
  changeStatus: (data) => request({ url: '/system/user/changeStatus', method: 'put', data })
}

/* ==================== 角色 ==================== */
export const roleApi = {
  list: (params) => request({ url: '/system/role/list', method: 'get', params }),
  all: () => request({ url: '/system/role/all', method: 'get' }),
  get: (roleId) => request({ url: `/system/role/${roleId}`, method: 'get' }),
  add: (data) => request({ url: '/system/role', method: 'post', data }),
  edit: (data) => request({ url: '/system/role', method: 'put', data }),
  remove: (roleIds) => request({ url: `/system/role/${roleIds}`, method: 'delete' }),
  changeStatus: (data) => request({ url: '/system/role/changeStatus', method: 'put', data })
}

/* ==================== 菜单 ==================== */
export const menuApi = {
  list: (params) => request({ url: '/system/menu/list', method: 'get', params }),
  treeSelect: () => request({ url: '/system/menu/treeselect', method: 'get' }),
  get: (menuId) => request({ url: `/system/menu/${menuId}`, method: 'get' }),
  add: (data) => request({ url: '/system/menu', method: 'post', data }),
  edit: (data) => request({ url: '/system/menu', method: 'put', data }),
  remove: (menuId) => request({ url: `/system/menu/${menuId}`, method: 'delete' })
}

/* ==================== 部门 ==================== */
export const deptApi = {
  list: (params) => request({ url: '/system/dept/list', method: 'get', params }),
  tree: () => request({ url: '/system/dept/tree', method: 'get' }),
  get: (deptId) => request({ url: `/system/dept/${deptId}`, method: 'get' }),
  add: (data) => request({ url: '/system/dept', method: 'post', data }),
  edit: (data) => request({ url: '/system/dept', method: 'put', data }),
  remove: (deptId) => request({ url: `/system/dept/${deptId}`, method: 'delete' })
}

/* ==================== 岗位 ==================== */
export const postApi = {
  list: (params) => request({ url: '/system/post/list', method: 'get', params }),
  all: () => request({ url: '/system/post/all', method: 'get' }),
  get: (postId) => request({ url: `/system/post/${postId}`, method: 'get' }),
  add: (data) => request({ url: '/system/post', method: 'post', data }),
  edit: (data) => request({ url: '/system/post', method: 'put', data }),
  remove: (postIds) => request({ url: `/system/post/${postIds}`, method: 'delete' })
}

/* ==================== 字典 ==================== */
export const dictApi = {
  typeList: (params) => request({ url: '/system/dict/type/list', method: 'get', params }),
  typeAll: () => request({ url: '/system/dict/type/all', method: 'get' }),
  addType: (data) => request({ url: '/system/dict/type', method: 'post', data }),
  editType: (data) => request({ url: '/system/dict/type', method: 'put', data }),
  removeType: (dictIds) => request({ url: `/system/dict/type/${dictIds}`, method: 'delete' }),
  dataByType: (dictType) => request({ url: `/system/dict/data/type/${dictType}`, method: 'get' }),
  dataList: (params) => request({ url: '/system/dict/data/list', method: 'get', params }),
  addData: (data) => request({ url: '/system/dict/data', method: 'post', data }),
  editData: (data) => request({ url: '/system/dict/data', method: 'put', data }),
  removeData: (dictCodes) => request({ url: `/system/dict/data/${dictCodes}`, method: 'delete' })
}

/* ==================== 参数配置 ==================== */
export const configApi = {
  list: (params) => request({ url: '/system/config/list', method: 'get', params }),
  get: (configId) => request({ url: `/system/config/${configId}`, method: 'get' }),
  add: (data) => request({ url: '/system/config', method: 'post', data }),
  edit: (data) => request({ url: '/system/config', method: 'put', data }),
  remove: (configIds) => request({ url: `/system/config/${configIds}`, method: 'delete' }),
  refreshCache: () => request({ url: '/system/config/refreshCache', method: 'delete' })
}

/* ==================== 日志 ==================== */
export const operLogApi = {
  list: (params) => request({ url: '/system/operlog/list', method: 'get', params }),
  remove: (operIds) => request({ url: `/system/operlog/${operIds}`, method: 'delete' }),
  clean: () => request({ url: '/system/operlog/clean', method: 'delete' })
}

export const loginLogApi = {
  list: (params) => request({ url: '/system/loginlog/list', method: 'get', params }),
  remove: (infoIds) => request({ url: `/system/loginlog/${infoIds}`, method: 'delete' }),
  clean: () => request({ url: '/system/loginlog/clean', method: 'delete' })
}
