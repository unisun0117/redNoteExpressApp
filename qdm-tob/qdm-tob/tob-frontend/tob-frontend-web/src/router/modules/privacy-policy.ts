import type { RouteRecordRaw } from 'vue-router'

/**
 * 小程序隐私政策管理模块路由
 */
export const privacyPolicyRoutes: RouteRecordRaw[] = [
  {
    path: '/system/privacy-policy',
    name: 'PrivacyPolicy',
    component: () => import('@/views/system/privacy-policy/index.vue'),
    meta: { title: '小程序隐私政策', requiresAuth: true },
  },
  {
    path: '/system/privacy-policy/edit/:id?',
    name: 'PrivacyPolicyEdit',
    component: () => import('@/views/system/privacy-policy/edit.vue'),
    meta: { title: '编辑隐私政策', requiresAuth: true },
  },
  {
    path: '/system/privacy-policy/detail/:id',
    name: 'PrivacyPolicyDetail',
    component: () => import('@/views/system/privacy-policy/detail.vue'),
    meta: { title: '隐私政策详情', requiresAuth: true },
  },
]
