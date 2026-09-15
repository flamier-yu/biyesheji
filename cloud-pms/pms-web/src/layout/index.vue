<template>
  <el-container class="app-wrapper">
    <!-- 左侧菜单 -->
    <el-aside :width="isCollapse ? '64px' : '210px'" class="sidebar">
      <div class="logo">
        <el-icon :size="22"><Grid /></el-icon>
        <span v-show="!isCollapse" class="logo-text">云协同 PMS</span>
      </div>
      <el-scrollbar>
        <el-menu
          :default-active="activeMenu"
          :collapse="isCollapse"
          :collapse-transition="false"
          unique-opened
          router
        >
          <template v-for="menu in visibleMenus" :key="menu.path">
            <!-- 有子菜单：目录 -->
            <el-sub-menu v-if="menu.children && menu.children.length" :index="menu.path">
              <template #title>
                <el-icon><component :is="menu.meta?.icon || 'Menu'" /></el-icon>
                <span>{{ menu.meta?.title }}</span>
              </template>
              <el-menu-item
                v-for="child in menu.children"
                :key="child.path"
                :index="child.path"
              >
                <el-icon><component :is="child.meta?.icon || 'Document'" /></el-icon>
                <span>{{ child.meta?.title }}</span>
              </el-menu-item>
            </el-sub-menu>

            <!-- 叶子菜单 -->
            <el-menu-item v-else :index="menu.path">
              <el-icon><component :is="menu.meta?.icon || 'Document'" /></el-icon>
              <span>{{ menu.meta?.title }}</span>
            </el-menu-item>
          </template>
        </el-menu>
      </el-scrollbar>
    </el-aside>

    <el-container>
      <!-- 顶部栏 -->
      <el-header class="navbar">
        <div class="navbar-left">
          <el-icon class="collapse-btn" @click="isCollapse = !isCollapse">
            <Fold v-if="!isCollapse" />
            <Expand v-else />
          </el-icon>
          <el-breadcrumb separator="/">
            <el-breadcrumb-item v-for="(item, idx) in breadcrumbs" :key="idx">
              {{ item }}
            </el-breadcrumb-item>
          </el-breadcrumb>
        </div>

        <div class="navbar-right">
          <el-tag size="small" type="info" class="dept-tag">
            {{ userStore.roles.join(' / ') || '—' }}
          </el-tag>
          <el-dropdown @command="handleCommand">
            <span class="user-info">
              <el-avatar :size="30" class="avatar">{{ userStore.avatarText }}</el-avatar>
              <span class="nickname">{{ userStore.nickName }}</span>
              <el-icon><ArrowDown /></el-icon>
            </span>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="dashboard">工作台</el-dropdown-item>
                <el-dropdown-item command="logout" divided>退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </el-header>

      <!-- 内容区 -->
      <el-main class="app-main">
        <router-view v-slot="{ Component }">
          <transition name="fade-transform" mode="out-in">
            <component :is="Component" />
          </transition>
        </router-view>
      </el-main>
    </el-container>
  </el-container>
</template>

<script setup>
import { computed, ref } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessageBox } from 'element-plus'
import { useUserStore } from '@/store/user'
import { resetRouterState } from '@/router'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()

const isCollapse = ref(false)

/** 过滤掉没有子菜单的空目录，避免出现点不动的菜单项 */
const visibleMenus = computed(() =>
  (userStore.menus || []).filter((m) => {
    if (m.hidden) return false
    if (m.component === 'Layout') {
      return m.children && m.children.length > 0
    }
    return true
  })
)

const activeMenu = computed(() => route.path)

const breadcrumbs = computed(() => {
  const items = []
  const meta = route.meta || {}
  for (const menu of userStore.menus || []) {
    if (menu.path === route.path) {
      items.push(menu.meta?.title)
      break
    }
    if (menu.children) {
      const child = menu.children.find((c) => c.path === route.path)
      if (child) {
        items.push(menu.meta?.title, child.meta?.title)
        break
      }
    }
  }
  if (items.length === 0 && meta.title) {
    items.push(meta.title)
  }
  return items.filter(Boolean)
})

async function handleCommand(command) {
  if (command === 'dashboard') {
    router.push('/dashboard')
    return
  }
  if (command === 'logout') {
    try {
      await ElMessageBox.confirm('确定要退出登录吗？', '提示', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      })
    } catch (e) {
      return
    }
    await userStore.logout()
    resetRouterState()
    router.push('/login')
  }
}
</script>

<style scoped lang="scss">
.app-wrapper {
  height: 100%;
}

.sidebar {
  background-color: var(--sidebar-bg);
  transition: width 0.28s;
  overflow: hidden;

  .logo {
    height: 56px;
    display: flex;
    align-items: center;
    justify-content: center;
    gap: 8px;
    color: #fff;
    background-color: var(--sidebar-bg-active);
    font-weight: 600;
    letter-spacing: 1px;

    .logo-text {
      font-size: 15px;
      white-space: nowrap;
    }
  }

  :deep(.el-menu) {
    border-right: none;
    --el-menu-bg-color: var(--sidebar-bg);
    --el-menu-text-color: var(--sidebar-text);
    --el-menu-active-color: #ffffff;
    --el-menu-hover-bg-color: var(--sidebar-bg-active);
  }

  :deep(.el-menu-item.is-active) {
    background-color: var(--el-color-primary);
    color: #fff;
  }
}

.navbar {
  height: 56px;
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff;
  box-shadow: 0 1px 4px rgba(0, 21, 41, 0.08);

  .navbar-left {
    display: flex;
    align-items: center;
    gap: 14px;

    .collapse-btn {
      font-size: 20px;
      cursor: pointer;
      color: #5a5e66;

      &:hover {
        color: #409eff;
      }
    }
  }

  .navbar-right {
    display: flex;
    align-items: center;
    gap: 14px;

    .dept-tag {
      margin-right: 4px;
    }

    .user-info {
      display: flex;
      align-items: center;
      gap: 8px;
      cursor: pointer;
      outline: none;

      .avatar {
        background: #409eff;
        color: #fff;
        font-size: 13px;
      }

      .nickname {
        font-size: 14px;
        color: #303133;
      }
    }
  }
}

.app-main {
  padding: 0;
  background-color: #f0f2f5;
  overflow-y: auto;
}

.fade-transform-enter-active,
.fade-transform-leave-active {
  transition: all 0.25s;
}

.fade-transform-enter-from {
  opacity: 0;
  transform: translateX(-12px);
}

.fade-transform-leave-to {
  opacity: 0;
  transform: translateX(12px);
}
</style>
