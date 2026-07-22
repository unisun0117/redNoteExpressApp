import type { RouteRecordRaw } from 'vue-router'

export const orderRoutes: RouteRecordRaw = {
  path: 'order',
  redirect: '/order/account',
  meta: { title: '订单管理', icon: 'ShoppingCart' },
  children: [
    {
      path: 'account',
      name: 'OrderAccount',
      component: () => import('@/views/order/account/index.vue'),
      meta: { title: '账户管理', requiresAuth: true },
    },
    {
      path: 'transaction',
      name: 'OrderTransaction',
      component: () => import('@/views/order/transaction/index.vue'),
      meta: { title: '资金流水', requiresAuth: true },
    },
  ],
}
