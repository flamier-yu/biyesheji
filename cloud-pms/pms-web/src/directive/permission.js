import { useUserStore } from '@/store/user'

/**
 * 判断当前用户是否拥有指定权限
 * @param {string|string[]} value 权限标识，数组时任一命中即可
 */
export function checkPermission(value) {
  const userStore = useUserStore()
  const permissions = userStore.permissions || []
  if (permissions.includes('*:*:*')) {
    return true
  }
  if (Array.isArray(value)) {
    return value.some((p) => permissions.includes(p))
  }
  return permissions.includes(value)
}

/**
 * v-permission 指令：无权限时移除元素
 * 用法：<el-button v-permission="['system:user:add']">新增</el-button>
 */
export default {
  mounted(el, binding) {
    if (!checkPermission(binding.value)) {
      el.parentNode && el.parentNode.removeChild(el)
    }
  }
}
