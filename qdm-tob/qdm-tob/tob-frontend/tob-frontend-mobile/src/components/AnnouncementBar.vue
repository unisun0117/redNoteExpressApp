<template>
  <!-- ============================================================
       AnnouncementBar — 首页公告横幅（轮播 + 红点 + 点击跳转）
       同事只需在首页放 <AnnouncementBar /> 即可，无需任何 props
       ============================================================ -->
  <view
    v-if="store.list.length > 0"
    style="margin:10px 12px 0;border-radius:8px;padding:12px;display:flex;align-items:center;gap:8px;background:#FFFFFF;box-shadow:0 2px 12px rgba(220,38,38,0.08);"
    @click="goList"
  >
    <!-- 🔴 红点提示 -->
    <view style="position:relative;flex-shrink:0;">
      <text style="font-size:18px;">📢</text>
      <view
        v-if="store.hasUnread"
        style="position:absolute;top:-4px;right:-6px;width:8px;height:8px;border-radius:50%;background:#DC2626;"
      />
    </view>

    <!-- 轮播文字区 -->
    <view style="flex:1;min-width:0;overflow:hidden;">
      <text
        style="font-size:15px;font-weight:600;color:#450A0A;display:block;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;"
      >
        {{ currentTitle }}
      </text>
    </view>

    <!-- 未读数量 + 箭头 -->
    <view style="display:flex;align-items:center;gap:4px;flex-shrink:0;">
      <view
        v-if="store.unreadCount > 0"
        style="background:#DC2626;color:#FFFFFF;font-size:11px;padding:1px 6px;border-radius:10px;font-weight:600;line-height:16px;"
      >
        {{ store.unreadCount }}条
      </view>
      <text style="font-size:14px;color:#D1D5DB;">›</text>
    </view>
  </view>
</template>

<script setup lang="ts">
import { ref, onMounted, onUnmounted } from 'vue'
import { useAnnouncementStore } from '@/store/modules/announcement'

const store = useAnnouncementStore()

// --- 轮播（仅展示最新 5 条）---
const MAX_CAROUSEL = 5
const currentIndex = ref(0)
let timer: ReturnType<typeof setInterval> | null = null

const currentTitle = ref('')

function updateTitle(): void {
  const items = store.list.slice(0, MAX_CAROUSEL)
  if (items.length > 0) {
    const idx = currentIndex.value % items.length
    const item = items[idx]
    // 从 HTML content 提取纯文本标题（取第一个 <h2> 或前 40 字符）
    const h2 = item.content.match(/<h2[^>]*>([^<]+)<\/h2>/i)
    currentTitle.value = h2 ? h2[1] : item.content.replace(/<[^>]+>/g, '').slice(0, 40)
  }
}

function startCarousel(): void {
  stopCarousel()
  const items = store.list.slice(0, MAX_CAROUSEL)
  if (items.length <= 1) return
  timer = setInterval(() => {
    currentIndex.value = (currentIndex.value + 1) % items.length
    updateTitle()
  }, 3000)
}

function stopCarousel(): void {
  if (timer) {
    clearInterval(timer)
    timer = null
  }
}

// --- 初始化 ---
onMounted(async () => {
  await store.fetch()
  if (store.list.length > 0) {
    currentIndex.value = 0
    updateTitle()
    startCarousel()
  }
})

onUnmounted(() => {
  stopCarousel()
})

// --- 点击跳转公告列表 ---
function goList(): void {
  uni.navigateTo({ url: '/sub-pages/announcement/pages/list/index' })
}
</script>

<style lang="scss" scoped>
/* 全部使用内联样式 */
</style>
