import type { RouteRecordRaw } from 'vue-router'

/**
 * 公告管理模块路由（归属运营管理）
 */
export const announcementRoutes: RouteRecordRaw[] = [
  {
    path: '/operation/announcement',
    name: 'Announcement',
    component: () => import('@/views/operation/announcement/index.vue'),
    meta: { title: '公告管理', requiresAuth: true },
  },
]
