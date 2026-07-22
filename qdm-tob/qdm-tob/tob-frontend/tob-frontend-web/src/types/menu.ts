/** 菜单树节点 */
export interface MenuTreeNode {
  id: number
  code: string
  name: string
  type: 'MENU' | 'PAGE' | 'BUTTON'
  path: string
  icon: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
  parentId: number

  permissionCodes?: string[]
  group: 'WEB' | 'WECOM'
  children?: MenuTreeNode[]
}

/** 权限选项（对应 PermissionDefinitionVO） */
export interface PermissionOption {
  authority: string
  authorityName: string
}

/** 菜单创建请求 */
export interface MenuSaveDTO {
  parentId: number
  code: string
  name: string
  type: 'MENU' | 'PAGE' | 'BUTTON'
  group: 'WEB' | 'WECOM'
  path?: string
  component?: string
  icon?: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
  permissionCodes?: string[]
}

/** 菜单更新请求 */
export interface MenuEditDTO {
  id: number
  name: string
  path?: string
  component?: string
  icon?: string
  sort: number
  status: 'ACTIVE' | 'INACTIVE'
  permissionCodes?: string[]
}

