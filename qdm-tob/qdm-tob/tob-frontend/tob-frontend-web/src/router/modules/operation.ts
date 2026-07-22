import type { RouteRecordRaw } from 'vue-router'

/**
 * 运营管理模块路由
 */
export const operationRoutes: RouteRecordRaw = {
  path: 'operation',
  redirect: '/operation/sales-region',
  meta: { title: '运营管理', icon: 'Setting' },
  children: [
    {
      path: 'sales-region',
      name: 'SalesRegion',
      component: () => import('@/views/operation/sales-region/index.vue'),
      meta: { title: '销售大区', requiresAuth: true },
    },
    {
      path: 'salesman',
      name: 'Salesman',
      component: () => import('@/views/operation/salesman/index.vue'),
      meta: { title: '销售员管理', requiresAuth: true },
    },
    {
      path: 'warehouse',
      name: 'Warehouse',
      component: () => import('@/views/operation/warehouse/index.vue'),
      meta: { title: '仓库管理', requiresAuth: true },
    },
    // 后续运营管理子模块在此追加
  ],
}
