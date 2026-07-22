import { createSSRApp } from 'vue'
import { createPinia } from 'pinia'
import uviewPlus from 'uview-plus'
import App from './App.vue'
import { tools, initRouter } from '@/router/index'

// ==================================================================
//  全局登录守卫：拦截所有页面跳转，未登录时重定向到登录页
//  白名单页面跳过校验（登录/注册页自身不能拦截，否则死循环）
// ==================================================================
function setupLoginGuard() {
  // 不需要登录的页面白名单
  const publicPages = ['/pages/login/index', '/pages/register/index']

  function isPublic(url: string): boolean {
    return publicPages.some((p) => url.startsWith(p))
  }

  function requireLogin(url: string, next: () => void) {
    if (isPublic(url)) {
      next()
      return
    }

    const token = uni.getStorageSync('token')
    if (token) {
      next()
    } else {
      uni.reLaunch({ url: '/pages/login/index' })
    }
  }

  // 拦截 navigateTo
  uni.addInterceptor('navigateTo', {
    invoke(args) {
      requireLogin(args.url, () => {})
    },
  })

  // 拦截 switchTab（首页/我的/购物车/分类等 TabBar 页）
  uni.addInterceptor('switchTab', {
    invoke(args) {
      requireLogin(args.url, () => {})
    },
  })

  // 拦截 redirectTo
  uni.addInterceptor('redirectTo', {
    invoke(args) {
      requireLogin(args.url, () => {})
    },
  })

  // 拦截 reLaunch
  uni.addInterceptor('reLaunch', {
    invoke(args) {
      requireLogin(args.url, () => {})
    },
  })
}

// 注意：SSR 环境下 createSSRApp 创建应用实例
// 此处使用 createSSRApp 确保与 uni-app 服务端渲染兼容
export function createApp() {
  const app = createSSRApp(App)

  // 注册 Pinia 状态管理
  const pinia = createPinia()
  app.use(pinia)

  // 注册 uview-plus UI 组件库
  app.use(uviewPlus, () => {
    return {
      options: {
        // 修改config对象的属性
        config: {
          // 只加载一次字体图标
          loadFontOnce: true,
        },
      },
    }
  })

  // 注册全局登录守卫
  // setupLoginGuard()
  initRouter()

  // 挂载全局工具类到 Vue 上，供 template 模版使用
  app.config.globalProperties.$tools = tools

  return {
    app,
    pinia,
  }
}
