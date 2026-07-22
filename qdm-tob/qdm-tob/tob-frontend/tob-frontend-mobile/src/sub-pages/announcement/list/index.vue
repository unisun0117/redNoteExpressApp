<template>
  <view style="min-height:100vh;background:linear-gradient(180deg,#FEF2F2 0%,#FFFFFF 40%);display:flex;flex-direction:column;">

    <!-- 加载中 -->
    <view v-if="loading" style="display:flex;flex-direction:column;padding:12px;gap:12px;">
      <view v-for="i in 3" :key="'sk-'+i" style="border-radius:12px;padding:16px;background:#F3F4F6;display:flex;flex-direction:column;gap:8px;">
        <view style="height:16px;width:60%;background:#E5E7EB;border-radius:4px;" />
        <view style="height:12px;width:95%;background:#E5E7EB;border-radius:4px;" />
        <view style="height:12px;width:80%;background:#E5E7EB;border-radius:4px;" />
      </view>
    </view>

    <!-- 空状态 -->
    <view v-else-if="store.list.length === 0" style="display:flex;flex-direction:column;align-items:center;justify-content:center;min-height:100vh;padding:0 32px;">
      <view style="width:96px;height:96px;border-radius:50%;display:flex;align-items:center;justify-content:center;margin-bottom:20px;background:#FEF2F2;">
        <text style="font-size:40px;">📢</text>
      </view>
      <text style="font-size:18px;font-weight:600;margin-bottom:8px;color:#450A0A;">暂无公告</text>
      <text style="font-size:14px;text-align:center;color:#9A3412;">暂时没有新的公告信息</text>
    </view>

    <!-- 公告卡片列表 -->
    <view v-else style="flex:1;padding:12px 12px 24px;">
      <view
        v-for="item in store.list"
        :key="item.id"
        style="border-radius:12px;padding:16px;margin-bottom:12px;background:#FFFFFF;box-shadow:0 2px 16px rgba(220,38,38,0.08);"
        @tap="openDetail(item)"
      >
          <!-- 卡片头：标题 + 红点 + 时间 -->
          <view style="display:flex;align-items:center;justify-content:space-between;">
            <view style="display:flex;align-items:center;gap:6px;flex:1;min-width:0;">
              <text style="font-size:16px;font-weight:600;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;color:#450A0A;">
                {{ extractTitle(item.content) }}
              </text>
              <view
                v-if="!store.readIds.has(item.id)"
                style="width:8px;height:8px;border-radius:50%;flex-shrink:0;background:#DC2626;"
              />
            </view>
            <text style="font-size:12px;flex-shrink:0;margin-left:8px;color:#9CA3AF;">{{ formatDate(item.updatedAt) }}</text>
          </view>

          <!-- 卡片体：3 行纯文本预览 -->
          <text style="font-size:14px;color:#7F1D1D;line-height:1.6;max-height:67px;overflow:hidden;">
            {{ extractText(item.content) }}
          </text>

          <!-- 卡片底：右箭头 -->
          <view style="display:flex;justify-content:flex-end;">
            <text style="font-size:14px;color:#D1D5DB;">查看详情 ›</text>
          </view>
        </view>
    </view>

  </view>
</template>

<script setup lang="ts">
import { ref, onMounted } from 'vue'
import { useAnnouncementStore } from '@/store/modules/announcement'
import type { AnnouncementItem } from '@/api/modules/announcement'

const store = useAnnouncementStore()
const loading = ref(true)

onMounted(async () => {
  await store.fetch()
  loading.value = false
})

// --- 从 HTML 富文本提取纯文本标题 ---
function extractTitle(content: string): string {
  const h2 = content.match(/<h2[^>]*>([^<]+)<\/h2>/i)
  if (h2) return h2[1]
  return content.replace(/<[^>]+>/g, '').slice(0, 40)
}

// --- 从 HTML 富文本提取纯文本（用于卡片预览）---
function extractText(content: string): string {
  return content.replace(/<[^>]+>/g, '').replace(/\s+/g, ' ').trim()
}

// --- 格式化日期 ---
function formatDate(dateStr: string): string {
  if (!dateStr) return ''
  return dateStr.slice(0, 10)
}

// --- 点击卡片 → 跳转独立详情页 ---
function openDetail(item: AnnouncementItem): void {
  store.markRead(item.id)
  uni.navigateTo({ url: '/sub-pages/announcement/pages/detail/index?id=' + item.id })
}
</script>

<style lang="scss" scoped>
/* 全部使用内联样式 */
</style>
