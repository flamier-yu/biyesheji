import { createRouter, createWebHashHistory } from 'vue-router'
import NProgress from 'nprogress'
import 'nprogress/nprogress.css'
import { getToken } from '@/utils/auth'
import { useUserStore } from '@/store/user'
import Layout from '@/layout/index.vue'

NProgress.configure({ showSpinner: false })

/** 静态路由：无需权限即可访问 */
export const constantRoutes = [
  {
    path: '/login',
    component: () => import('@/views/login/index.vue'),
    hidden: true
  },
  {
    path: '/404',
    component: () => import('@/views/error/404.vue'),
    hidden: true
  }
]

/** 所有页面组件（Vite 需要显式声明 glob 才能动态解析） */
const viewModules = import.meta.glob('../views/**/*.vue')

function resolveComponent(component) {
  if (!component) {
    return () => import('@/views/error/404.vue')
  }
  const key = `../views/${component}.vue`
  return viewModules[key] || (() => import('@/views/error/404.vue'))
}

/**
 * 把后端返回的菜单树转换为 vue-router 路由
 * @param {Array} menus 菜单数组
 * @param {boolean} isTop 是否顶层。只有顶层非 Layout 的叶子菜单才需要额外包一层布局，
 *                        子菜单直接挂页面组件，否则会出现 Layout 嵌套导致侧边栏/顶栏重复渲染
 */
function buildRoutes(menus, isTop = true) {
  const result = []
  if (!Array.isArray(menus)) {
    return result
  }
  menus.forEach((menu) => {
    const children = buildRoutes(menu.children || [], false)

    if (menu.component === 'Layout') {
      // 目录：使用布局组件承载子菜单
      result.push({
        path: menu.path,
        name: menu.name,
        component: Layout,
        redirect: children.length ? children[0].path : undefined,
        meta: menu.meta,
        hidden: menu.hidden,
        children
      })
    } else if (isTop) {
      // 顶层叶子菜单：外层包一层布局，内层空路径渲染实际页面
      result.push({
        path: menu.path,
        component: Layout,
        hidden: menu.hidden,
        children: [
          {
            path: '',
            name: menu.name,
            component: resolveComponent(menu.component),
            meta: menu.meta
          }
        ]
      })
    } else {
      // 子菜单：直接指向页面组件（不再包 Layout）
      result.push({
        path: menu.path,
        name: menu.name,
        component: resolveComponent(menu.component),
        meta: menu.meta,
        hidden: menu.hidden
      })
    }
  })
  return result
}

/**
 * 从构建好的路由表中找到第一个可访问的叶子路径。
 * <p>
 * 用于把根路径 / 重定向到当前用户真正有权限的第一个页面，
 * 避免某些角色未被授予「工作台」菜单时，访问根路径又落到 404。
 */
function findFirstLeafPath(routes) {
  for (const route of routes) {
    if (route.hidden) {
      continue
    }
    if (route.children && route.children.length) {
      const childPath = findFirstLeafPath(route.children)
      if (childPath) {
        return childPath
      }
    }
    if (route.path && route.path !== '/') {
      return route.path
    }
  }
  return null
}

const router = createRouter({
  history: createWebHashHistory(),
  routes: constantRoutes,
  scrollBehavior: () => ({ top: 0 })
})

/** 动态路由是否已加载 */
let dynamicRoutesLoaded = false

router.beforeEach(async (to, from, next) => {
  NProgress.start()
  const token = getToken()

  if (!token) {
    if (to.path === '/login') {
      next()
    } else {
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  if (to.path === '/login') {
    next('/')
    return
  }

  const userStore = useUserStore()

  if (!dynamicRoutesLoaded) {
    try {
      await userStore.loadUserInfo()
      const menus = await userStore.loadMenus()
      const dynamicRoutes = buildRoutes(menus)
      dynamicRoutes.forEach((route) => router.addRoute(route))

      // 根路径重定向到当前用户第一个可访问的菜单，避免访问 / 时落空到 404
      const homePath = findFirstLeafPath(dynamicRoutes) || '/dashboard'
      router.addRoute({ path: '/', redirect: homePath })

      // 兜底 404，必须在动态路由之后添加
      router.addRoute({ path: '/:pathMatch(.*)*', redirect: '/404' })
      dynamicRoutesLoaded = true
      // 重新进入目标路由，让新注册的路由生效
      next({ ...to, replace: true })
    } catch (e) {
      userStore.reset()
      dynamicRoutesLoaded = false
      next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    }
    return
  }

  next()
})

router.afterEach(() => {
  NProgress.done()
})

/** 退出登录时重置路由状态 */
export function resetRouterState() {
  dynamicRoutesLoaded = false
}

export default router
