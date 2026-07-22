import { createRouter, createWebHistory, type RouteRecordRaw } from 'vue-router'
import { getToken, setToken, setRealName, setMobile } from '@/utils/auth'
import { loginByTicket } from '@/api/auth'

import Layout from '@/layouts/DefaultLayout.vue'

// 动态导入所有模块路由（新增模块文件即可自动注册，无需手动 import）
const moduleRoutes: RouteRecordRaw[] = Object.values(
  import.meta.glob('./modules/*.ts', { eager: true }),
).flatMap((mod) =>
  Object.values(mod as Record<string, RouteRecordRaw | RouteRecordRaw[]>).flatMap((v) =>
    Array.isArray(v) ? v : [v],
  ),
)

// 公共路由
const publicRoutes: RouteRecordRaw[] = [
  {
    path: '/login',
    name: 'Login',
    component: () => import('@/views/login/index.vue'),
    meta: { title: '登录', requiresAuth: false },
  },
  {
    path: '/redirect',
    component: Layout,
    children: [
      {
        path: '/redirect/:path(.*)',
        component: () => import('@/views/error/redirect.vue'),
      },
    ],
  },
  {
    path: '/:pathMatch(.*)*', // 兜底路由
    name: 'NotFound',
    component: () => import('@/views/error/404.vue'),
    meta: { title: '404' },
  },
]

// 需要认证的主路由
const protectedRoutes: RouteRecordRaw[] = [
  {
    path: '/',
    name: 'Home',
    component: () => import('@/layouts/DefaultLayout.vue'),
    redirect: '/home',
    children: [
      {
        path: 'home',
        name: 'Dashboard',
        component: () => import('@/views/home/index.vue'),
        meta: { title: '首页', icon: 'HomeFilled' },
      },
      ...moduleRoutes,
    ],
  },
]

const router = createRouter({
  history: createWebHistory(),
  routes: [...publicRoutes, ...protectedRoutes],
  scrollBehavior: () => ({ top: 0 }),
})

router.beforeEach(async (to, _from, next) => {
  // 设置页面标题
  if (to.meta?.title) {
    document.title = `${to.meta.title} - 钱鲜达`
  }

  // 1. 检查 URL 中的 CAS Ticket（History 模式：ticket 在 query 参数中）
  const ticket = (to.query.ticket as string) || null

  if (ticket) {
    try {
      const data = await loginByTicket(ticket)
      setToken(data.token)
      if (data.real_name) setRealName(data.real_name)
      if (data.mobile) setMobile(data.mobile)

      // 登录成功 → 清除 ticket 参数，跳转目标页面
      const redirect = (to.query.redirect as string) || '/home'
      next({ path: redirect, query: {}, replace: true })
      return
    } catch {
      // ticket 无效 → 跳转登录页显示错误
      next({ path: '/login', query: { error: 'ticket_failed' }, replace: true })
      return
    }
  }

  // 2. 常规 Token 校验
  const token = getToken()

  // 已登录用户访问登录页 → 重定向到首页
  if (token && to.path === '/login') {
    next({ path: '/home', replace: true })
    return
  }

  // 需要认证但无 Token → 重定向到登录页
  if (to.meta?.requiresAuth !== false && !token) {
    next({ path: '/login', query: { redirect: to.fullPath }, replace: true })
    return
  }
  next()
})

export default router
