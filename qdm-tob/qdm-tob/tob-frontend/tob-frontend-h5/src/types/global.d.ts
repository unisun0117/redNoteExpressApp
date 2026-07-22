/** 全局类型扩展 */

/** Vue Router Meta 类型扩展 */
import 'vue-router'

declare module 'vue-router' {
  interface RouteMeta {
    /** 页面标题 */
    title?: string
    /** 是否需要登录鉴权 */
    requiresAuth?: boolean
    /** 页面缓存标识 */
    keepAlive?: boolean
  }
}

export {}
