import request from '@/utils/request'
import type { MenuTreeNode, PermissionOption, MenuSaveDTO, MenuEditDTO } from '@/types/menu'

interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

// ===== 管理端 =====

export async function getAdminTree(
  group: 'WEB' | 'WECOM',
  includeButtons?: boolean,
): Promise<MenuTreeNode[]> {
  const res = await request.get<ApiResponse<MenuTreeNode[]>>('/api/admin/menu/tree', {
    params: { group, includeButtons },
  })
  return (res as unknown as ApiResponse<MenuTreeNode[]>).data
}

export async function getButtons(pageId: number): Promise<MenuTreeNode[]> {
  const res = await request.get<ApiResponse<MenuTreeNode[]>>('/api/admin/menu/buttons', {
    params: { pageId },
  })
  return (res as unknown as ApiResponse<MenuTreeNode[]>).data
}

export async function getPermissionOptions(): Promise<PermissionOption[]> {
  const res = await request.get<ApiResponse<PermissionOption[]>>(
    '/api/admin/auth/permission/list',
  )
  return (res as unknown as ApiResponse<PermissionOption[]>).data
}

export async function createMenu(data: MenuSaveDTO): Promise<void> {
  await request.post('/api/admin/menu/create', data)
}

export async function updateMenu(data: MenuEditDTO): Promise<void> {
  await request.post('/api/admin/menu/update', data)
}

export async function deleteMenu(id: number): Promise<void> {
  await request.delete('/api/admin/menu/delete', { params: { id } })
}

export async function getUserTree(group: 'WEB' | 'WECOM'): Promise<MenuTreeNode[]> {
  const res = await request.get<ApiResponse<MenuTreeNode[]>>('/api/admin/menu/userTree', {
    params: { group },
  })
  return (res as unknown as ApiResponse<MenuTreeNode[]>).data
}
