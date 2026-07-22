import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { useUserStore } from '@/store/user'
import { loginByTicket } from '@/api/auth'

/**
 * 路由懒加载辅助函数
 */
function lazyView(viewPath: string) {
  return () => import(`@/views/${viewPath}.vue`)
}

const TICKET_RETRY_KEY = '__ticket_retry_count'

/**
 * 基础路由
 */
const baseRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: lazyView('Home'),
    meta: { title: '首页', requiresAuth: true },
  },
  {
    path: '/login',
    name: 'Login',
    component: lazyView('Login'),
    meta: { title: '登录', requiresAuth: false },
  },
]

/**
 * 业务模块路由插槽
 */
export const businessRoutes: RouteRecordRaw[] = []

const routes: RouteRecordRaw[] = [
  ...baseRoutes,
  ...businessRoutes,
  {
    path: '/:pathMatch(.*)*',
    redirect: '/',
  },
]

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes,
  scrollBehavior() {
    return { top: 0 }
  },
})

// ===== 全局路由守卫 =====
router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  document.title = (to.meta.title as string) || '企业微信H5'

  // ----- 1. 检查 URL 中的 CAS Ticket -----
  const ticket = (to.query.ticket as string) || null

  if (ticket) {
    try {
      const data = await loginByTicket(ticket)

      // 登录成功 → 清除重试计数 → 保存 Token → 跳转目标路由
      sessionStorage.removeItem(TICKET_RETRY_KEY)
      useUserStore().setToken(data.access_token)
      if (data.real_name) {
        useUserStore().setUserInfo({ realName: data.real_name, mobile: data.mobile })
      }

      next({ path: to.path, query: {}, replace: true })
      return
    } catch (err: unknown) {
      // 403 无账号 → 直接跳登录页提示，不重试
      const axiosErr = err as { response?: { status?: number } }
      if (axiosErr?.response?.status === 403) {
        sessionStorage.removeItem(TICKET_RETRY_KEY)
        next('/login?error=no_account')
        return
      }

      // Ticket 换 Token 失败 → 计数防死循环
      const retryCount = Number(sessionStorage.getItem(TICKET_RETRY_KEY) || '0') + 1
      sessionStorage.setItem(TICKET_RETRY_KEY, String(retryCount))

      if (retryCount >= 3) {
        console.error('CAS Ticket 连续验证失败 3 次，停止自动重试')
        sessionStorage.removeItem(TICKET_RETRY_KEY)
        next('/login?error=ticket_failed')
        return
      }

      console.warn(`CAS Ticket 验证失败，第 ${retryCount} 次重试...`)
      setTimeout(() => {
        window.location.replace(to.fullPath)
      }, 500)
      return
    }
  }

  // ----- 2. 无 Ticket → 常规 Token 校验 -----
  const token = sessionStorage.getItem('token')

  // 已登录用户访问登录页 → 重定向到首页
  if (token && to.path === '/login') {
    next('/')
    return
  }

  // 需要认证但无 Token → 重定向到登录页，携带原始路径
  if (to.meta.requiresAuth !== false && !token) {
    next(`/login?redirect=${encodeURIComponent(to.fullPath)}`)
    return
  }

  next()
})

export default router
