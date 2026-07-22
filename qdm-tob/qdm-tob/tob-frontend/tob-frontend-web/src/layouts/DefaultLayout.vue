<template>
  <div class="tw-flex tw-h-full">
    <!-- 遮罩层（移动端侧边栏展开时） -->
    <div v-if="mobileSidebarOpen" class="sidebar-overlay" @click="mobileSidebarOpen = false" />

    <!-- ===== 侧边栏 ===== -->
    <aside
      class="tw-h-full tw-flex tw-flex-col tw-shrink-0 tw-transition-all tw-duration-300 resp-sidebar"
      :class="{ 'sidebar-open': mobileSidebarOpen }"
      :style="{ width: sidebarWidth, backgroundColor: '#304156' }"
    >
      <!-- Logo + 系统名称 -->
      <div
        class="tw-h-[--header-height] tw-flex tw-items-center tw-justify-center tw-border-b tw-border-white/10 tw-px-3 tw-gap-2 tw-overflow-hidden"
      >
        <img
          src="@/assets/img/logo.png"
          alt="logo"
          class="tw-w-8 tw-h-8 tw-object-contain tw-shrink-0"
        />
        <h1
          class="tw-text-lg tw-font-bold tw-text-white tw-whitespace-nowrap tw-transition-opacity tw-duration-300"
          :class="isCollapsed ? 'tw-opacity-0 tw-w-0' : 'tw-opacity-100'"
        >
          TOB运营管理
        </h1>
      </div>

      <!-- 菜单列表 -->
      <!-- 注：不可加 overflow-x-hidden，否则收起态悬浮框（left-full 定位）会被裁剪 -->
      <nav class="tw-flex-1 tw-pb-4 tw-pt-0 tw-overflow-y-auto">
        <template v-for="item in menuItems" :key="item.title">
          <!-- ===== 有子菜单 ===== -->
          <div v-if="item.children && item.children.length > 0" class="tw-relative">
            <!-- 菜单项（展开态可点击展开子菜单，收起态仅图标） -->
            <div
              class="tw-flex tw-items-center tw-py-3 tw-text-sm tw-cursor-pointer tw-transition-colors tw-duration-200"
              :class="[
                isParentActive(item)
                  ? 'tw-bg-[#304156]/10 tw-text-white tw-mx-2'
                  : 'tw-text-white/60 hover:tw-bg-[#304156]/10 hover:tw-text-white',
                isCollapsed ? 'tw-justify-center tw-px-0' : 'tw-justify-start tw-px-3',
              ]"
              @click="toggleSubmenu(item.title)"
              @mouseenter="onItemEnter(item.title)"
              @mouseleave="onItemLeave"
            >
              <el-icon class="tw-text-base tw-shrink-0"><component :is="item.icon" /></el-icon>
              <span
                class="tw-ml-2 tw-whitespace-nowrap tw-transition-opacity tw-duration-300"
                :class="isCollapsed ? 'tw-opacity-0 tw-w-0 tw-overflow-hidden' : 'tw-opacity-100'"
              >
                {{ item.title }}
              </span>
              <el-icon
                v-if="!isCollapsed"
                class="tw-ml-auto tw-text-xs tw-transition-transform"
                :class="isSubmenuOpen(item.title) ? 'tw-rotate-90' : ''"
              >
                <ArrowRight />
              </el-icon>
            </div>

            <!-- 内联子菜单（仅展开态显示） -->
            <div v-show="isSubmenuOpen(item.title) && !isCollapsed" class="tw-bg-black/15">
              <router-link
                v-for="child in item.children"
                :key="child.path"
                :to="child.path"
                class="tw-flex tw-items-center tw-pl-12 tw-pr-3 tw-py-2.5 tw-text-sm tw-text-white/55 hover:tw-bg-[#304156]/10 hover:tw-text-white tw-transition-colors tw-duration-200"
                active-class="!tw-text-white !tw-bg-[#409eff] !tw-text-white"
              >
                {{ child.title }}
              </router-link>
            </div>

            <!-- 悬浮弹出菜单（仅收起态 + 当前项被 hover 时显示） -->
            <div
              v-if="isCollapsed && hoveredItem === item.title"
              class="tw-absolute tw-left-full tw-top-0 tw-z-50 tw-bg-[#304156] tw-rounded-r-lg tw-py-2 tw-min-w-[150px]"
              style="box-shadow: 4px 4px 24px rgba(0, 0, 0, 0.25)"
              @mouseenter="onItemEnter(item.title)"
              @mouseleave="onItemLeave"
            >
              <div
                class="tw-flex tw-items-center tw-px-4 tw-py-2.5 tw-text-sm tw-text-white/80 tw-cursor-default tw-border-b tw-border-white/10"
              >
                <el-icon class="tw-text-base tw-shrink-0 tw-mr-2"
                  ><component :is="item.icon"
                /></el-icon>
                <span>{{ item.title }}</span>
              </div>
              <router-link
                v-for="child in item.children"
                :key="child.path"
                :to="child.path"
                class="tw-flex tw-items-center tw-pl-10 tw-pr-4 tw-py-2 tw-text-sm tw-text-white/55 hover:tw-bg-[#304156]/10 hover:tw-text-white tw-transition-colors tw-duration-200 tw-no-underline"
                active-class="!tw-text-white !tw-bg-[#409eff] !tw-text-white"
              >
                {{ child.title }}
              </router-link>
            </div>
          </div>

          <!-- ===== 无子菜单（直接跳转） ===== -->
          <!-- 外层 div 托管 mouseenter/mouseleave（router-link 组件不 emit 这些原生事件） -->
          <div
            v-else
            class="tw-relative"
            @mouseenter="onItemEnter(item.title)"
            @mouseleave="onItemLeave"
          >
            <router-link
              :to="item.path!"
              class="tw-flex tw-items-center tw-py-3 tw-text-sm tw-transition-colors tw-duration-200 tw-no-underline"
              :class="[
                isMenuActive(item.path!)
                  ? 'tw-bg-[#304156]/10 tw-text-white tw-mx-2'
                  : 'tw-text-white/60 hover:tw-bg-[#304156]/10 hover:tw-text-white',
                isCollapsed ? 'tw-justify-center tw-px-0' : 'tw-justify-start tw-px-3',
              ]"
            >
              <el-icon class="tw-text-base tw-shrink-0"><component :is="item.icon" /></el-icon>
              <span
                class="tw-ml-2 tw-whitespace-nowrap tw-transition-opacity tw-duration-300"
                :class="isCollapsed ? 'tw-opacity-0 tw-w-0 tw-overflow-hidden' : 'tw-opacity-100'"
              >
                {{ item.title }}
              </span>
            </router-link>

            <!-- 悬浮标签（仅收起态 + 当前项被 hover 时显示） -->
            <div
              v-if="isCollapsed && hoveredItem === item.title"
              class="tw-absolute tw-left-full tw-top-0 tw-z-50 tw-bg-[#304156] tw-rounded-r-lg tw-py-2 tw-min-w-[120px]"
              style="box-shadow: 4px 4px 24px rgba(0, 0, 0, 0.25)"
              @mouseenter="onItemEnter(item.title)"
              @mouseleave="onItemLeave"
            >
              <router-link
                :to="item.path!"
                class="tw-flex tw-items-center tw-px-4 tw-py-2.5 tw-text-sm tw-text-white/65 hover:tw-bg-[#304156]/10 hover:tw-text-white tw-transition-colors tw-duration-200 tw-no-underline"
                active-class="!tw-text-white !tw-bg-[#409eff] !tw-text-white"
              >
                <el-icon class="tw-text-base tw-shrink-0 tw-mr-2"
                  ><component :is="item.icon"
                /></el-icon>
                <span>{{ item.title }}</span>
              </router-link>
            </div>
          </div>
        </template>
      </nav>

      <!-- 折叠/展开开关（收起居中，展开右对齐） -->
      <div
        class="tw-p-3 tw-border-t tw-border-white/10 tw-flex"
        :class="isCollapsed ? 'tw-justify-center' : 'tw-justify-end'"
      >
        <el-icon
          class="tw-text-white/65 hover:tw-text-white tw-cursor-pointer tw-text-base tw-transition-colors"
          @click="toggleCollapse"
        >
          <component :is="isCollapsed ? 'Expand' : 'Fold'" />
        </el-icon>
      </div>
    </aside>

    <!-- ===== 右侧内容区 ===== -->
    <div class="tw-flex-1 tw-flex tw-flex-col tw-overflow-hidden resp-content">
      <!-- 顶栏 -->
      <header
        class="tw-h-[--header-height] tw-bg-white tw-border-b tw-border-gray-200 tw-flex tw-items-center tw-justify-between tw-px-6 tw-shrink-0 resp-header"
      >
        <div class="tw-flex tw-items-center tw-gap-3">
          <!-- 移动端汉堡菜单按钮 -->
          <el-button class="hamburger-btn" icon="Expand" text @click="mobileSidebarOpen = !mobileSidebarOpen" />
          <!-- <span class="tw-text-sm header-welcome" style="color: #303133">欢迎使用TOB</span> -->
        </div>

        <!-- 用户区域：头像 + 名称 + 下拉菜单 -->
        <el-dropdown trigger="click" @command="handleUserCommand">
          <div
            class="tw-flex tw-items-center tw-gap-2 tw-cursor-pointer hover:tw-opacity-80 tw-transition-opacity tw-select-none"
          >
            <img
              :src="avatarUrl"
              alt="avatar"
              class="tw-w-7 tw-h-7 tw-rounded-full tw-object-cover tw-border tw-border-white/10"
            />
            <span class="tw-text-sm tw-text-gray-600">{{ userName || '管理员' }}</span>
            <el-icon class="tw-text-gray-400 tw-text-xs"><ArrowDown /></el-icon>
          </div>
          <template #dropdown>
            <el-dropdown-menu>
              <el-dropdown-item command="settings">
                <el-icon class="tw-mr-2"><Setting /></el-icon>
                个人设置
              </el-dropdown-item>
              <el-dropdown-item command="logout" divided>
                <el-icon class="tw-mr-2"><SwitchButton /></el-icon>
                退出登录
              </el-dropdown-item>
            </el-dropdown-menu>
          </template>
        </el-dropdown>
      </header>

      <!-- 面包屑 -->
      <div
        v-if="showBreadcrumb"
        class="tw-px-6 tw-py-2 tw-bg-white tw-border-b tw-border-gray-100 tw-shrink-0"
      >
        <el-breadcrumb separator="/">
          <el-breadcrumb-item :to="{ path: '/home' }">首页</el-breadcrumb-item>
          <el-breadcrumb-item v-if="firstLevelMenu" :to="firstLevelLink">
            {{ firstLevelMenu }}
          </el-breadcrumb-item>
          <el-breadcrumb-item v-if="currentPageTitle && currentPageTitle !== '首页'">
            {{ currentPageTitle }}
          </el-breadcrumb-item>
        </el-breadcrumb>
      </div>

      <!-- 内容区 -->
      <main class="tw-flex-1 tw-overflow-auto tw-p-3 tw-bg-[--bg-color]">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup lang="ts">
import { computed, ref, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getUserTree } from '@/api/modules/menu'
import type { MenuTreeNode } from '@/types/menu'
import portraitDefault from '@/assets/img/portrait.png'
import request from '@/utils/request'
import { clearAuth } from '@/utils/auth'
import { getCasLogoutUrl } from '@/config/env'
import { getCurrentUser } from '@/api/auth'

// ===== 类型定义 =====

interface SubMenuItem {
  path: string
  title: string
  icon?: string
}

interface MenuItem {
  title: string
  icon: string
  path?: string
  children?: SubMenuItem[]
}

// ===== 路由实例 =====

const route = useRoute()
const router = useRouter()

// ===== 菜单定义 =====

const menuItems = ref<MenuItem[]>([])

// 加载用户菜单
onMounted(async () => {
  try {
    const tree = await getUserTree('WEB')
    if (tree && tree.length > 0) {
      menuItems.value = buildMenuItems(tree)
      return
    }
  } catch {
    console.error('加载菜单失败，使用默认菜单')
  }
  // API 不可用时回退到硬编码菜单
  menuItems.value = [
    { path: '/home', title: '首页', icon: 'HomeFilled' },
    {
      title: '客户管理',
      icon: 'User',
      children: [{ path: '/customer/customer-account', title: '登录账号' }],
    },
    {
      title: '运营管理',
      icon: 'Setting',
      children: [
        { path: '/operation/sales-region', title: '销售大区' },
        { path: '/operation/salesman', title: '销售员管理' },
        { path: '/operation/warehouse', title: '仓库管理' },
      ],
    },
    {
      title: '系统管理',
      icon: 'Operation',
      children: [
        { path: '/system/user', title: '用户管理' },
        { path: '/system/menu', title: '菜单管理' },
      ],
    },
  ]
})

function buildMenuItems(nodes: MenuTreeNode[]): MenuItem[] {
  return nodes
    .filter((n) => n.type !== 'BUTTON') // 侧边栏不显示按钮
    .sort((a, b) => a.sort - b.sort)
    .map((node): MenuItem => {
      if (node.children && node.children.length > 0) {
        return {
          title: node.name,
          icon: node.icon || 'Menu',
          children: node.children
            .filter((c) => c.type !== 'BUTTON')
            .sort((a, b) => a.sort - b.sort)
            .map((c) => ({
              path: c.path,
              title: c.name,
            })),
        }
      }
      return { title: node.name, icon: node.icon || 'Menu', path: node.path }
    })
}

// ===== 折叠状态 =====

const isCollapsed = ref(false)

  const mobileSidebarOpen = ref(false)

/** 展开宽度 198px（220px 缩小 10%），收起宽度 64px */
const sidebarWidth = computed(() => (isCollapsed.value ? '64px' : '198px'))

/** 当前被 hover 的菜单项 title（仅收起态生效，逐项触发悬浮框） */
const hoveredItem = ref<string | null>(null)

/** mouseleave 延迟计时器（避免鼠标从图标移到悬浮框时闪烁消失） */
let leaveTimer: ReturnType<typeof setTimeout> | null = null

function toggleCollapse() {
  isCollapsed.value = !isCollapsed.value
  hoveredItem.value = null
}

// ===== 响应式自动折叠（视口宽度 < 1200px 强制收起，跨越阈值时切换） =====

/** 自动折叠断点：视口小于该宽度自动收起侧边栏 */
const COLLAPSE_BREAKPOINT = 1200

/** 上一次是否处于窄屏，用于仅在跨越阈值时切换（宽屏下不覆盖用户手动折叠） */
let wasNarrow = false

function handleResize() {
  const isNarrow = window.innerWidth < COLLAPSE_BREAKPOINT
  if (isNarrow !== wasNarrow) {
    isCollapsed.value = isNarrow
    hoveredItem.value = null
    wasNarrow = isNarrow
  }
}

onMounted(() => {
  wasNarrow = window.innerWidth < COLLAPSE_BREAKPOINT
  isCollapsed.value = wasNarrow
  window.addEventListener('resize', handleResize)
})

onUnmounted(() => {
  window.removeEventListener('resize', handleResize)
})

function onItemEnter(itemTitle: string) {
  if (leaveTimer) {
    clearTimeout(leaveTimer)
    leaveTimer = null
  }
  hoveredItem.value = itemTitle
}

function onItemLeave() {
  leaveTimer = setTimeout(() => {
    hoveredItem.value = null
    leaveTimer = null
  }, 150)
}

// ===== 子菜单展开/折叠（展开态用） =====

const openSubmenus = ref<Set<string>>(new Set())

function toggleSubmenu(title: string) {
  // 手风琴模式：展开当前菜单时收起其他一级菜单
  const wasOpen = openSubmenus.value.has(title)
  openSubmenus.value.clear()
  if (!wasOpen) {
    openSubmenus.value.add(title)
  }
}

function isSubmenuOpen(title: string): boolean {
  return openSubmenus.value.has(title)
}

// ===== 菜单激活状态 =====

function isMenuActive(menuPath: string): boolean {
  if (menuPath === '/home') {
    return route.path === '/home'
  }
  return route.path.startsWith(menuPath)
}

function isParentActive(item: MenuItem): boolean {
  // 仅直接路径匹配时高亮父级（有子菜单时不因子级激活而高亮）
  if (item.path) {
    return isMenuActive(item.path)
  }
  return false
}

// ===== 子菜单自动展开（子级路由激活时父级自动打开） =====

watch(
  () => route.path,
  () => {
    menuItems.value.forEach((item) => {
      if (item.children) {
        const isChildActive = item.children.some((child) => route.path.startsWith(child.path))
        if (isChildActive) {
          openSubmenus.value.add(item.title)
        }
      }
    })
  },
  { immediate: true },
)

// ===== 用户信息 =====

const userName = ref('')

onMounted(async () => {
  try {
    const info = await getCurrentUser()
    userName.value = info.name
  } catch { /* 忽略 */ }
})

// TODO: 接入用户信息接口后，替换为接口返回的头像 URL；若返回空则保持默认
const avatarUrl = portraitDefault

// ===== 用户下拉菜单 =====

function handleUserCommand(command: string) {
  if (command === 'logout') {
    // 先调后端登出接口使 token 失效
    request.post('/api/admin/auth/logout').finally(() => {
      clearAuth()
      window.location.replace(getCasLogoutUrl())
    })
  } else if (command === 'settings') {
    // TODO: 跳转个人设置页
    router.push('/home')
  }
}

// ===== 面包屑逻辑 =====

/** 过滤掉 layout 路由自身（path === '/'），保留有 meta.title 的有效层级 */
const meaningfulMatched = computed(() => {
  return route.matched.filter((r) => r.path !== '/' && r.meta?.title)
})

/** 当前页面标题（面包屑最后一级） */
const currentPageTitle = computed(() => {
  const items = meaningfulMatched.value
  if (items.length === 0) return ''
  return (items[items.length - 1]?.meta?.title as string) || ''
})

/** 一级菜单名（面包屑第二级，当路由层级 > 1 时出现） */
const firstLevelMenu = computed(() => {
  const items = meaningfulMatched.value
  if (items.length <= 1) return null
  return (items[items.length - 2]?.meta?.title as string) || null
})

/** 一级菜单点击跳转路径（跳转到该菜单的第一个子页面） */
const firstLevelLink = computed(() => {
  const items = meaningfulMatched.value
  if (items.length <= 1) return '/home'
  const parentRoute = items[items.length - 2]
  if (!parentRoute) return '/home'

  const allRoutes = router.getRoutes()
  const matched = allRoutes.find((r) => r.path === parentRoute.path)
  if (matched && matched.children && matched.children.length > 0) {
    const first = matched.children[0]
    return first.path.startsWith('/') ? first.path : `${matched.path}/${first.path}`
  }
  return parentRoute.path
})

/** 是否显示面包屑 */
const showBreadcrumb = computed(() => {
  return meaningfulMatched.value.length > 0 && route.path !== '/' && route.path !== ''
})
</script>
