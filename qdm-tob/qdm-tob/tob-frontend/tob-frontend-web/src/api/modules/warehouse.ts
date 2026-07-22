/**
 * 仓库管理模块 API
 * 对应后端 WarehouseController，路径前缀：/api/admin/warehouse
 */

import request from '@/utils/request'

export interface WarehouseViewVO {
  id: string
  code: string
  name: string
  region: string
  type: string
  province: string
  city: string
  district: string
  address: string
  lng: number
  lat: number
  updatedBy: string
  updatedAt: string
}

export interface WarehouseCreationVO {
  code: string
  name: string
  region: string
  type: string
  province?: string
  city?: string
  district?: string
  address?: string
  lng?: number
  lat?: number
  createdBy?: string
}

export interface WarehouseEditVO {
  name: string
  region: string
  type: string
  province?: string
  city?: string
  district?: string
  address?: string
  lng?: number
  lat?: number
  updatedBy?: string
}

export interface PageResult<T> { records: T[]; total: number; current: number; pages: number }
interface ApiResponse<T> { code: number; data: T; msg: string }

export function getWarehouseList(params: {
  pageNum: number; pageSize: number; keyword?: string; region?: string
}): Promise<ApiResponse<PageResult<WarehouseViewVO>>> {
  return request.get('/api/admin/warehouse/list', { params })
}

export function createWarehouse(data: WarehouseCreationVO): Promise<ApiResponse<null>> {
  return request.post('/api/admin/warehouse', data)
}

export function updateWarehouse(code: string, data: WarehouseEditVO): Promise<ApiResponse<null>> {
  return request.put('/api/admin/warehouse', data, { params: { code } })
}
