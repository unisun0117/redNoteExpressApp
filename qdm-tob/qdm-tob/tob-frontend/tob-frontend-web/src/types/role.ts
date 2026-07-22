/** 角色视图 */
export interface RoleViewVO {
  id: number
  code: string
  name: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE'
  createdAt: string
  createdBy: string
  updatedAt: string
  updatedBy: string
}

/** 新增角色请求 */
export interface RoleCreationVO {
  code: string
  name: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE'
  menuIds: number[]
}

/** 编辑角色请求 */
export interface RoleEditVO {
  id: number
  name: string
  description?: string
  status: 'ACTIVE' | 'INACTIVE'
  menuIds: number[]
}

/** 分页查询参数 */
export interface RoleQueryVO {
  code?: string
  name?: string
  page: number
  size: number
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}
