import request from '@/utils/request'
import type {
  AnnouncementVO,
  AnnouncementSaveDTO,
  AnnouncementQuery,
  PageResult,
} from '@/types/announcement'

// ================================================================
// 通用响应包装（对应后端 ResponseResult）
// ================================================================

export interface ApiResponse<T> {
  code: number
  data: T
  msg: string
  traceId?: string
}

// ================================================================
// 公告管理 API
// ================================================================

/** 公告分页列表 */
export function getAnnouncementPage(params: AnnouncementQuery) {
  return request.get<unknown, ApiResponse<PageResult<AnnouncementVO>>>(
    '/api/admin/announcements/list',
    { params },
  )
}

/** 新增公告（返回新建 id） */
export function createAnnouncement(data: AnnouncementSaveDTO) {
  return request.post<unknown, ApiResponse<number>>('/api/admin/announcements', data)
}

/** 编辑公告 */
export function updateAnnouncement(id: number, data: Partial<AnnouncementSaveDTO>) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/announcements', data, {
    params: { id },
  })
}

/** 切换公告启用状态（服务端翻转，无需入参） */
export function toggleAnnouncement(id: number) {
  return request.put<unknown, ApiResponse<null>>('/api/admin/announcements/toggle', null, {
    params: { id },
  })
}
