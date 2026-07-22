/**
 * 公告模块 API（对接后端 AnnouncementMallController）
 *
 * 后端路径：/api/mall/announcements
 * 成功码 code === 0
 */

import { http } from '@/api/request'
import type { ApiResponse } from '@/api/request'

/** 公告列表项（与后端 AnnouncementViewVO 对齐） */
export interface AnnouncementItem {
  id: number
  regionCode: string
  regionName: string
  content: string
  enabled: boolean
  updatedBy: string
  updatedAt: string
}

/**
 * 获取当前用户所属大区的已启用公告列表
 * GET /api/mall/announcements?regionCode=
 */
export function getAnnouncements(regionCode?: string): Promise<ApiResponse<AnnouncementItem[]>> {
  return http.get('/api/mall/announcements', {
    params: regionCode ? { regionCode } : {},
  }) as Promise<ApiResponse<AnnouncementItem[]>>
}
