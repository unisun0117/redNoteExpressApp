/**
 * API 模块统一导出入口
 *
 * 使用方式：
 *   import { userApi } from '@/api'
 *   const res = await userApi.login({ username, password })
 *
 * 新增 API 模块步骤：
 *   1. 在 `modules/` 下创建模块文件（如 `order.ts`）
 *   2. 在此文件中添加 export
 */

export * as userApi from './modules/user'
export * as payApi from './modules/pay'
export * as companyAddressApi from './modules/companyAddress'
export * as privacyApi from './modules/privacy'
export * as announcementApi from './modules/announcement'
// 后续业务模块按需取消注释：
// export * as orderApi from './modules/order'
// export * as goodsApi from './modules/goods'
