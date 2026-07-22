/**
 * 公告状态管理 Store
 *
 * 管理公告列表、已读状态、未读计数，供首页横幅和公告列表页共享
 */

import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { getAnnouncements, type AnnouncementItem } from '@/api/modules/announcement'

/** 本地存储 key：已读公告 ID 集合 */
const READ_IDS_KEY = 'announcement_read_ids'

export const useAnnouncementStore = defineStore('announcement', () => {
  // --- 状态 ---
  const list = ref<AnnouncementItem[]>([])
  const loading = ref(false)
  const readIds = ref<Set<number>>(loadReadIds())

  // --- 计算属性 ---
  const unreadCount = computed(() => list.value.filter((a) => !readIds.value.has(a.id)).length)
  const hasUnread = computed(() => unreadCount.value > 0)

  // --- 操作 ---

  /** 从后端拉取已启用公告 */
  async function fetch(regionCode?: string): Promise<void> {
    loading.value = true
    try {
      const res = await getAnnouncements(regionCode)
      if (res.code === 0 || res.code === 200) {
        list.value = res.data ?? []
      }
    } catch {
      // 拦截器已 toast
    } finally {
      loading.value = false
    }
  }

  /** 标记某条公告为已读 */
  function markRead(id: number): void {
    readIds.value.add(id)
    persistReadIds()
  }

  // --- 私有 ---

  function loadReadIds(): Set<number> {
    try {
      const raw = uni.getStorageSync(READ_IDS_KEY)
      if (raw && Array.isArray(raw)) return new Set(raw as number[])
    } catch {
      // ignore
    }
    return new Set()
  }

  function persistReadIds(): void {
    uni.setStorageSync(READ_IDS_KEY, [...readIds.value])
  }

  return { list, loading, readIds, unreadCount, hasUnread, fetch, markRead }
})
