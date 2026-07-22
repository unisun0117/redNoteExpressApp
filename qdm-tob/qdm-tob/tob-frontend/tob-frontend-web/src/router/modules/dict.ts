import type { RouteRecordRaw } from 'vue-router'

export const dictRoutes: RouteRecordRaw[] = [
  {
    path: '/system/dict',
    name: 'DictManagement',
    component: () => import('@/views/system/dict/index.vue'),
    meta: { title: '字典管理', requiresAuth: true },
  },
]
