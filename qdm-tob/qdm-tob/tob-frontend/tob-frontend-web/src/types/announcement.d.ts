// ================================================================
// 公告管理类型定义
// 对应后端 AnnouncementController（/api/admin/announcements）
// ================================================================

/** 公告出参 VO */
export interface AnnouncementVO {
  id: number
  /** 销售大区编码 */
  regionCode: string
  /** 销售大区名称（后端影子字段） */
  regionName: string
  /** 公告内容 */
  content: string
  /** 是否启用 */
  enabled: boolean
  /** 更新人 */
  updatedBy: string
  /** 更新时间 */
  updatedAt: string
}

/** 新增 / 编辑公告入参 DTO */
export interface AnnouncementSaveDTO {
  /** 销售大区编码 */
  regionCode: string
  /** 公告内容 */
  content: string
  /** 是否启用 */
  enabled: boolean
}

/** 公告分页查询参数 */
export interface AnnouncementQuery {
  regionCode?: string
  enabled?: boolean
  pageNum?: number
  pageSize?: number
}

/** 分页结果（对应后端 MyBatis-Plus IPage） */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}
