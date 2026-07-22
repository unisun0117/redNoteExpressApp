import type { RouteRecordRaw } from 'vue-router'

export const systemRoutes: RouteRecordRaw[] = [
  {
    path: '/system/user',
    name: 'SystemUser',
    component: () => import('@/views/system/user/index.vue'),
    meta: { title: '用户管理', requiresAuth: true },
  },
  {
    path: '/system/menu',
    name: 'SystemMenu',
    component: () => import('@/views/system/menu/index.vue'),
    meta: { title: '菜单管理', requiresAuth: true },
  },
  {
    path: '/system/role',
    name: 'SystemRole',
    component: () => import('@/views/system/role/index.vue'),
    meta: { title: '角色管理', requiresAuth: true },
  },
]
