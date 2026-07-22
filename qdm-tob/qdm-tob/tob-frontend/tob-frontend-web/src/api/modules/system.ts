import request from '@/utils/request'

/** 后端统一响应包装 */
interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

/** 分页结果 */
export interface PageResult<T> {
  records: T[]
  total: number
  size: number
  current: number
}

/** 运营人员视图 */
export interface OperatorVO {
  id: number
  employeeCode: string
  loginId: string
  realName: string
  mobile: string
  type: 'ADMIN' | 'SALESMAN'
  status: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
  createdAt: string
  createdBy: string
  lastLoginAt: string | null
}

/** 查询参数 */
export interface OperatorQuery {
  employeeCode?: string
  realName?: string
  mobile?: string
  status?: string
  type?: string
  page?: number
  size?: number
}

/** 保存请求（创建/更新复用） */
export interface OperatorSaveDTO {
  employeeCode?: string
  realName: string
  mobile: string
  type: 'ADMIN' | 'SALESMAN'
  status?: 'ACTIVE' | 'INACTIVE' | 'LOCKED'
}

/** 分页列表 */
export async function getOperatorPage(params: OperatorQuery): Promise<PageResult<OperatorVO>> {
  const res = await request.get<ApiResponse<PageResult<OperatorVO>>>('/api/admin/operator/list', {
    params,
  })
  return (res as ApiResponse<PageResult<OperatorVO>>).data
}

/** 查看详情 */
export async function getOperator(id: number): Promise<OperatorVO> {
  const res = await request.get<ApiResponse<OperatorVO>>('/api/admin/operator/view', {
    params: { id },
  })
  return (res as ApiResponse<OperatorVO>).data
}

/** 新增 */
export async function createOperator(data: OperatorSaveDTO): Promise<void> {
  await request.post('/api/admin/operator/create', data)
}

/** 编辑 */
export async function updateOperator(id: number, data: OperatorSaveDTO): Promise<void> {
  await request.post('/api/admin/operator/update', data, { params: { id } })
}

/** 角色简易视图（全量列表用） */
export interface RoleSimpleVO {
  id: number
  name: string
}

/** 销售大区简易视图（全量列表用，前端通过 code 绑定） */
export interface RegionSimpleVO {
  id: number
  code: string
  name: string
}

/** 查询用户已绑定的角色 ID */
export async function getOperatorRoles(operatorId: number): Promise<number[]> {
  const res = await request.get<ApiResponse<number[]>>('/api/admin/operator/roles', {
    params: { operatorId },
  })
  return (res as ApiResponse<number[]>).data
}

/** 覆盖式设置用户角色 */
export async function setOperatorRoles(operatorId: number, roleIds: number[]): Promise<void> {
  await request.put('/api/admin/operator/roles', roleIds, { params: { operatorId } })
}

/** 查询用户已绑定的销售大区 code */
export async function getOperatorRegions(operatorId: number): Promise<string[]> {
  const res = await request.get<ApiResponse<string[]>>('/api/admin/operator/regions', {
    params: { operatorId },
  })
  return (res as ApiResponse<string[]>).data
}

/** 覆盖式设置用户销售大区（通过 region_code） */
export async function setOperatorRegions(operatorId: number, regionCodes: string[]): Promise<void> {
  await request.put('/api/admin/operator/regions', regionCodes, { params: { operatorId } })
}

/** 获取所有角色（id+name） */
export async function getAllRoles(): Promise<RoleSimpleVO[]> {
  const res = await request.get<ApiResponse<RoleSimpleVO[]>>('/api/admin/roles/all')
  return (res as ApiResponse<RoleSimpleVO[]>).data
}

/** 获取所有销售大区（id+name） */
export async function getAllRegions(): Promise<RegionSimpleVO[]> {
  const res = await request.get<ApiResponse<RegionSimpleVO[]>>('/api/admin/operation/sales-region/all')
  return (res as ApiResponse<RegionSimpleVO[]>).data
}

