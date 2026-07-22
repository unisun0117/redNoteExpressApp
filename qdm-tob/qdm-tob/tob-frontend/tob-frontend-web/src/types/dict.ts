/** 字典视图（列表用） */
export interface DictVO {
  code: string
  name: string
  description: string
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  updatedAt: string
}

/** 字典项视图（列表用） */
export interface DictItemVO {
  dictCode: string
  value: string
  label: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
}

/** 字典保存请求 */
export interface DictSaveDTO {
  code: string
  name: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE'
  items: DictItemBatchSaveDTO[]
}

/** 字典项保存请求（含 dictCode，用于单条更新） */
export interface DictItemSaveDTO {
  dictCode: string
  value: string
  label: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
}

/** 字典项批量新增请求（不含 dictCode） */
export interface DictItemBatchSaveDTO {
  value: string
  label: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
}

/** 字典项列表查询参数 */
export interface DictItemQuery {
  code: string
  keyword?: string
  status?: string
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
  pages: number
}
