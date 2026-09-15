import { defineStore } from 'pinia'
import { getInfo, getRouters, login as loginApi, logout as logoutApi } from '@/api/login'
import { getToken, removeToken, setToken } from '@/utils/auth'

export const useUserStore = defineStore('user', {
  state: () => ({
    token: getToken(),
    userId: null,
    username: '',
    nickName: '',
    avatar: '',
    deptName: '',
    roles: [],
    permissions: [],
    /** 后端返回的动态路由 */
    menus: []
  }),

  getters: {
    /** 头像文字（用于无头像时展示昵称首字） */
    avatarText: (state) => (state.nickName ? state.nickName.charAt(0) : 'U')
  },

  actions: {
    async login(form) {
      const res = await loginApi(form)
      this.token = res.data.token
      setToken(this.token)
      return res
    },

    async loadUserInfo() {
      const res = await getInfo()
      const data = res.data || {}
      this.userId = data.userId
      this.username = data.username
      this.nickName = data.nickName
      this.avatar = data.avatar
      this.deptName = data.deptName
      this.roles = data.roles || []
      this.permissions = data.permissions || []
      return data
    },

    async loadMenus() {
      const res = await getRouters()
      this.menus = res.data || []
      return this.menus
    },

    async logout() {
      try {
        await logoutApi()
      } catch (e) {
        // 令牌可能已失效，忽略
      }
      this.reset()
    },

    reset() {
      this.token = null
      this.userId = null
      this.username = ''
      this.nickName = ''
      this.avatar = ''
      this.roles = []
      this.permissions = []
      this.menus = []
      removeToken()
    }
  }
})
