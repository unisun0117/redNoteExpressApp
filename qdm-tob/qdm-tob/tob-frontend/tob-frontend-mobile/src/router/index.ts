// src/utils/router.ts
import { onLoad } from '@dcloudio/uni-app'
import { useUserStore } from '@/store/modules/user'

interface RouteMetaItem {
    requiresAuth?: boolean
    title?: string
}

const routeMeta: Record<string, RouteMetaItem> = {
    '/pages/index/index': { requiresAuth: false, title: '首页' },
}

const WHITE_LIST: string[] = ['pages/index/index', 'pages/login/index', 'pages/register/index']
const TAB_BAR_LIST: string[] = ['pages/index/index', 'pages/order/index', 'pages/user/index', 'pages/cat/index']
const LOGIN_PATH = '/pages/login/index'

export function getMeta(path: string): RouteMetaItem {
    const cleanPath = path.split('?')[0]
    return routeMeta[cleanPath] || {}
}

function normalizePath(url: string): string {
    let path = url.split('?')[0]
    if (path.startsWith('/')) path = path.substring(1)
    return path
}

function isWhiteListed(url: string): boolean {
    const path = normalizePath(url)
    if (!path || path === '/') return true
    return WHITE_LIST.some(item => (item.startsWith('/') ? item.substring(1) : item) === path)
}

function isTabBar(url: string): boolean {
    const path = normalizePath(url)
    return TAB_BAR_LIST.some(item => (item.startsWith('/') ? item.substring(1) : item) === path)
}

// 核心鉴权检查
function checkAuth(targetUrl: string, isInterceptor = false): boolean {
    if (isWhiteListed(targetUrl)) return true
    if (getMeta(targetUrl).requiresAuth === false) return true

    // ⚠️ 注意：如果在 main.ts 初始化时 Pinia 还没准备好，这里直接调用 useUserStore() 可能会报错。
    // 放在 checkAuth 内部延迟执行是安全的，因为只有发生路由跳转时才会触发这个函数。
    const userStore = useUserStore()
    if (userStore.isLoggedIn) return true

    let redirectPath = targetUrl
    if (!redirectPath.startsWith('/')) redirectPath = '/' + redirectPath
    if (redirectPath === '/' || !redirectPath) redirectPath = '/pages/index/index'
    if (redirectPath.includes(LOGIN_PATH)) return true

    const encodedUrl = encodeURIComponent(redirectPath)
    const loginUrl = `${LOGIN_PATH}?redirect=${encodedUrl}`

    console.log(isInterceptor ? '[拦截器] 未登录，重定向' : '[页面守卫] 未登录，重定向', '目标:', redirectPath)

    // #ifdef H5
    window.location.replace(window.location.origin + window.location.pathname + '#' + loginUrl)
    // #endif
    // #ifndef H5
    uni.reLaunch({ url: loginUrl })
    // #endif

    return false
}

// ==================== 🌟 核心：导出路由初始化函数 ====================
export function initRouter() {
    // #ifdef H5
    const NAV_METHODS = ['navigateTo', 'redirectTo', 'reLaunch', 'switchTab'] as const
    // #endif
    // #ifndef H5
    const NAV_METHODS = ['navigateTo', 'redirectTo'] as const
    // #endif

    NAV_METHODS.forEach(method => {
        uni.addInterceptor(method, {
            invoke(args: { url: string }) {
                return checkAuth(args.url, true)
            }
        })
    })
    console.log('[Router] 全局导航拦截器已注入')
}

// ==================== 页面级守卫 ====================
export function usePageGuard() {
    onLoad((options) => {
        const pages = getCurrentPages()
        const route = pages.length > 0 ? (pages[pages.length - 1].route || '') : ''
        let fullUrl = route
        if (options && Object.keys(options).length > 0) {
            const query = Object.entries(options).map(([k, v]) => `${k}=${v}`).join('&')
            fullUrl += '?' + query
        }
        if (fullUrl) checkAuth(fullUrl, false)
    })
}

// ==================== 导航辅助方法 ====================
export const router = {
    /**
     * 智能 Push
     */
    push: (url: string) => {
        // 1. 防御空路径和非法的 'undefined' 字符串
        if (!url || url === 'undefined') {
            return uni.showToast({
                title: '未开发',
                icon: 'none',
                mask: true
            })
        }

        // 2. 智能分发逻辑
        if (isTabBar(url)) {
            // 如果是 TabBar，自动剥离参数防报错
            const cleanUrl = url.split('?')[0]
            return uni.switchTab({ url: cleanUrl })
        }

        // 普通页面正常跳转
        return uni.navigateTo({ url })
    },

    /**
     * 智能 Replace
     */
    replace: (url: string) => {
        if (!url || url === 'undefined') {
            return uni.showToast({ title: '未开发', icon: 'none', mask: true })
        }

        if (isTabBar(url)) {
            const cleanUrl = url.split('?')[0]
            return uni.switchTab({ url: cleanUrl })
        }
        return uni.redirectTo({ url })
    },

    pushTab: (url: string) => {
        if (!url || url === 'undefined') return
        const cleanUrl = url.split('?')[0]
        return uni.switchTab({ url: cleanUrl })
    },
    back: (delta = 1) => uni.navigateBack({ delta }),
    relaunch: (url: string) => uni.reLaunch({ url }),
}

export const tools = { router, getMeta }
export type ToolsType = typeof tools
export default router

// <script setup lang="ts">
// import { getCurrentInstance } from 'vue'
// // 方式 A：通过全局实例调用（不推荐，稍微有点繁琐）
// const { proxy } = getCurrentInstance()!
// proxy?.$tools.router.push('/pages/index/index')

// // 方式 B：直接导入使用（强烈推荐！TS 体验完美）
// import { router } from '@/utils/router'
// router.push('/pages/index/index')
// </script>

// // 方式 C：直接在Html使用
// <button @click="$tools.router.push('/pages/index/index')">去首页</button>