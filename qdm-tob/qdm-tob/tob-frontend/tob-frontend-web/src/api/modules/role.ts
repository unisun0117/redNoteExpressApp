import request from '@/utils/request'
import type { RoleViewVO, RoleCreationVO, RoleEditVO, RoleQueryVO, PageResult } from '@/types/role'

interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

/** 分页搜索角色 */
export async function getRoleList(params: RoleQueryVO): Promise<PageResult<RoleViewVO>> {
  const res = await request.get<ApiResponse<PageResult<RoleViewVO>>>('/api/admin/roles/search', {
    params,
  })
  return (res as unknown as ApiResponse<PageResult<RoleViewVO>>).data
}

/** 查看角色详情 */
export async function getRoleView(id: number): Promise<RoleViewVO> {
  const res = await request.get<ApiResponse<RoleViewVO>>('/api/admin/roles/view', {
    params: { id },
  })
  return (res as unknown as ApiResponse<RoleViewVO>).data
}

/** 获取角色已绑定的菜单ID */
export async function getRoleMenus(roleId: number): Promise<number[]> {
  const res = await request.get<ApiResponse<number[]>>('/api/admin/roles/menus', {
    params: { roleId },
  })
  return (res as unknown as ApiResponse<number[]>).data
}

/** 新增角色 */
export async function createRole(data: RoleCreationVO): Promise<void> {
  await request.post('/api/admin/roles/create', data)
}

/** 编辑角色 */
export async function updateRole(data: RoleEditVO): Promise<void> {
  await request.post('/api/admin/roles/update', data)
}

/** 删除角色 */
export async function deleteRole(id: number): Promise<void> {
  await request.delete('/api/admin/roles/delete', { params: { id } })
}
