/**
 * 商品分类管理模块 API
 *
 * 对应后端 CategoryController
 * 路径前缀：/api/public/admin/product/category
 */

import request from '@/utils/request'

// ===== 类型定义 =====

/** 分类级别 */
export type CategoryLevel = 0 | 1 | 2

/** 商品分类列表行 */
export interface CategoryViewVO {
  id: string
  name: string
  alias: string | null
  parentId: string
  parentName: string | null
  level: number
  levelName: string
  parentIdPath: string | null
  parentNamePath: string | null
  sort: number
  status: string
  memo: string | null
  createdAt: string
  updatedAt: string
}

/** 编辑分类别名入参 */
export interface CategoryEditVO {
  alias: string | null
}

/** 分页查询参数 */
export interface CategoryQuery {
  pageNum: number
  pageSize: number
  level?: number
  code?: string
  name?: string
}

/** 分页响应 */
interface PageResult<T> {
  records: T[]
  total: number
  current: number
  pages: number
}

/** 统一响应包装 */
interface ApiResponse<T> {
  code: number
  data: T
  msg: string | null
}

// ===== API 方法 =====

/** 分页查询商品分类列表 */
export function getCategoryPage(
  params: CategoryQuery,
): Promise<ApiResponse<PageResult<CategoryViewVO>>> {
  return request.get('/api/public/admin/product/category/list', { params })
}

/** 编辑商品分类自定义名称 */
export function updateCategoryAlias(
  id: string,
  data: CategoryEditVO,
): Promise<ApiResponse<null>> {
  return request.put('/api/public/admin/product/category', data, { params: { id } })
}
