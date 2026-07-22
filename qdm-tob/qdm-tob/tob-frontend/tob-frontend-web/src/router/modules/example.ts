/**
 * 业务模块路由示例文件
 *
 * 使用方式：
 * 1. 在 src/router/modules/ 下新建模块路由文件（如 user.ts、order.ts）
 * 2. 在该文件中导出 RouteRecordRaw[] 数组
 * 3. 在 src/router/index.ts 中通过 import 引入并调用 router.addRoute() 动态注册
 *
 * 示例代码：
 *
 * import type { RouteRecordRaw } from 'vue-router'
 *
 * export const exampleRoutes: RouteRecordRaw[] = [
 *   {
 *     path: '/example',
 *     name: 'Example',
 *     component: () => import('@/views/example/index.vue'),
 *     meta: { title: '示例模块', requiresAuth: true },
 *   },
 * ]
 */

export {}
