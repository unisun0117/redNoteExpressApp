/**
 * 登录账号管理模块 API
 *
 * 对应后端 SysUserAdminController
 * 路径前缀：/api/admin/system/users
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 注册来源（后端 UserSource 枚举） */
export type UserSource = 'WECHAT' | 'ADMIN'

/** 账号状态（后端 UserStatus 枚举） */
export type UserStatus = 'ACTIVE' | 'INACTIVE' | 'FROZEN'

/** 登录账号列表行 */
export interface SysUserSummaryVO {
  id: number
  realName: string
  mobile: string
  source: UserSource
  wechatOpenid: string | null
  wechatId: string | null
  wechatNickname: string | null
  status: UserStatus
  registeredAt: string | null
  lastLoginAt: string | null
  /** 已绑定客户档案数（依赖客户档案模块，本期固定 0） */
  boundCount: number
}

/** 登录账号详情 */
export interface SysUserViewVO {
  id: number
  realName: string
  mobile: string
  source: UserSource
  wechatOpenid: string | null
  wechatId: string | null
  wechatNickname: string | null
  wechatAvatar: string | null
  status: UserStatus
  registeredAt: string | null
  lastLoginAt: string | null
  boundCount: number
}

/** 新增登录账号入参 */
export interface SysUserCreationVO {
  realName: string
  mobile: string
}

/** 编辑登录账号入参（仅姓名可改） */
export interface SysUserEditVO {
  id: number
  realName: string
}

/** 分页查询参数 */
export interface SysUserQuery {
  pageNum: number
  pageSize: number
  keyword?: string
  source?: UserSource
  status?: UserStatus
}

/** 分页响应 */
export interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

/** 统一响应包装 */
interface ApiResponse<T> {
  code: number
  data: T
  msg: string
}

// ===== API 方法 =====

/** 分页查询登录账号 */
export function getSysUserPage(
  params: SysUserQuery,
): Promise<ApiResponse<PageResult<SysUserSummaryVO>>> {
  return request.get('/api/admin/system/users/page', { params })
}

/** 查询登录账号详情 */
export function getSysUserDetail(id: number): Promise<ApiResponse<SysUserViewVO>> {
  return request.get('/api/admin/system/users/detail', { params: { id } })
}

/** 后台新增登录账号（免短信验证码） */
export function createSysUser(data: SysUserCreationVO): Promise<ApiResponse<null>> {
  return request.post('/api/admin/system/users/create', data)
}

/** 编辑登录账号（仅姓名可改） */
export function editSysUser(data: SysUserEditVO): Promise<ApiResponse<null>> {
  return request.put('/api/admin/system/users/edit', data)
}

/** 切换账号状态（ACTIVE↔INACTIVE） */
export function toggleSysUserStatus(id: number): Promise<ApiResponse<null>> {
  return request.put('/api/admin/system/users/toggle-status', undefined, {
    params: { id },
  })
}

/** 硬删除登录账号（释放手机号） */
export function deleteSysUser(id: number): Promise<ApiResponse<null>> {
  return request.delete('/api/admin/system/users/delete', { params: { id } })
}
