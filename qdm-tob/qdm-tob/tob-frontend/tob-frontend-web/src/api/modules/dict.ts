import request from '@/utils/request'
import type {
  DictVO,
  DictItemVO,
  DictSaveDTO,
  DictItemSaveDTO,
  DictItemBatchSaveDTO,
  DictItemQuery,
  PageResult,
} from '@/types/dict'

/** 后端统一响应包装 */
interface ApiResponse<T> {
  code: number
  msg: string
  data: T
}

/** 字典分页列表 */
export async function getDictList(params: {
  page: number
  size: number
  keyword?: string
}): Promise<PageResult<DictVO>> {
  const res = await request.get<ApiResponse<PageResult<DictVO>>>('/api/admin/dict/list', { params })
  return (res as ApiResponse<PageResult<DictVO>>).data
}

/** 获取字典项列表（支持关键词搜索和状态筛选） */
export async function getDictItems(query: DictItemQuery): Promise<DictItemVO[]> {
  const res = await request.get<ApiResponse<DictItemVO[]>>('/api/admin/dict/item/list', {
    params: query,
  })
  return (res as ApiResponse<DictItemVO[]>).data
}

/** 获取启用状态的字典项（下拉框用） */
export async function getActiveItems(code: string): Promise<DictItemVO[]> {
  const res = await request.get<ApiResponse<DictItemVO[]>>('/api/admin/dict/item/active', {
    params: { code },
  })
  return (res as ApiResponse<DictItemVO[]>).data
}

/** 新增字典（含初始字典项） */
export async function createDict(data: DictSaveDTO): Promise<void> {
  await request.post('/api/admin/dict/create', data)
}

/** 更新字典（名称/描述/状态） */
export async function updateDict(data: Omit<DictSaveDTO, 'items'>): Promise<void> {
  await request.post('/api/admin/dict/update', data)
}

/** 删除字典 */
export async function deleteDict(code: string): Promise<void> {
  await request.delete('/api/admin/dict/delete', { params: { code } })
}

/** 更新单个字典项 */
export async function updateDictItem(data: DictItemSaveDTO): Promise<void> {
  await request.post('/api/admin/dict/item/update', data)
}

/** 批量新增字典项 */
export async function batchCreateItems(
  dictCode: string,
  items: DictItemBatchSaveDTO[],
): Promise<void> {
  await request.post('/api/admin/dict/item/batch/create', items, { params: { dictCode } })
}

/** 删除字典项 */
export async function deleteDictItem(dictCode: string, value: string): Promise<void> {
  await request.delete('/api/admin/dict/item/delete', { params: { dictCode, value } })
}
