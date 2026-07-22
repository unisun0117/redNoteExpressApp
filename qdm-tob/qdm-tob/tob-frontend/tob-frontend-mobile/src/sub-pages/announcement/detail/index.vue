<template>
  <view style="min-height:100vh;background:#FFFFFF;display:flex;flex-direction:column;">

    <!-- 顶栏 -->
    <view style="display:flex;align-items:center;padding:12px;border-bottom:1px solid #F3F4F6;gap:8px;">
      <view style="width:32px;height:32px;display:flex;align-items:center;justify-content:center;flex-shrink:0;" @click="goBack">
        <text style="font-size:18px;color:#450A0A;">←</text>
      </view>
      <text style="font-size:16px;font-weight:600;flex:1;text-align:center;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#450A0A;">公告详情</text>
      <view style="width:32px;height:32px;flex-shrink:0;" />
    </view>

    <!-- 内容 -->
    <scroll-view v-if="item" scroll-y style="flex:1;padding:16px;">
      <text style="font-size:18px;font-weight:700;color:#450A0A;display:block;margin-bottom:12px;">{{ title }}</text>
      <text style="font-size:13px;color:#9CA3AF;display:block;margin-bottom:16px;">{{ item.updatedAt }}</text>
      <view style="border-top:1px solid #F3F4F6;padding-top:16px;">
        <rich-text :nodes="item.content" style="font-size:15px;color:#450A0A;line-height:1.8;" />
      </view>
    </scroll-view>

    <!-- 加载 -->
    <view v-else style="display:flex;justify-content:center;padding-top:128px;">
      <text style="color:#9CA3AF;">加载中...</text>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useAnnouncementStore } from '@/store/modules/announcement'
import type { AnnouncementItem } from '@/api/modules/announcement'

const store = useAnnouncementStore()
const item = ref<AnnouncementItem | null>(null)
const title = ref('')

function extractTitle(content: string): string {
  const h2 = content.match(/<h2[^>]*>([^<]+)<\/h2>/i)
  if (h2) return h2[1]
  return content.replace(/<[^>]+>/g, '').slice(0, 40)
}

onLoad(async (options) => {
  const rawId = options?.id as string | undefined
  const id = rawId ? Number(rawId) : null

  if (store.list.length === 0) {
    await store.fetch()
  }

  // 优先按 id 查找，找不到取第一个
  const found = id ? store.list.find((a) => a.id === id) : null
  const target = found ?? store.list[0] ?? null

  if (target) {
    store.markRead(target.id)
    item.value = target
    title.value = extractTitle(target.content)
  }
})

function goBack(): void {
  uni.navigateBack()
}
</script>

<style lang="scss" scoped>
/* 全部使用内联样式 */
</style>
