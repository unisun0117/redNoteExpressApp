import type { RouteRecordRaw } from 'vue-router'

/**
 * 客户管理模块路由
 */
export const customerRoutes: RouteRecordRaw = {
  path: 'customer',
  redirect: '/customer/customer-account',
  meta: { title: '客户管理', icon: 'User' },
  children: [
    {
      path: 'customer-account',
      name: 'CustomerAccount',
      component: () => import('@/views/system/customer-account/index.vue'),
      meta: { title: '登录账号', requiresAuth: true },
    },
    {
      path: 'customer-archive',
      name: 'CustomerArchive',
      component: () => import('@/views/customer/customer-archive/index.vue'),
      meta: { title: '客户档案', requiresAuth: true },
    },
    {
      path: 'customer-archive/:id',
      name: 'CustomerArchiveDetail',
      component: () => import('@/views/customer/customer-archive/detail.vue'),
      meta: { title: '客户档案详情', requiresAuth: true },
    },
    // 后续客户管理子模块在此追加
  ],
}
