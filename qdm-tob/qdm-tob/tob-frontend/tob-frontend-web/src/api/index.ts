export { loginByTicket, loginByPassword } from './auth'
export type { LoginResponse } from './auth'

// 业务模块 API 通过 import.meta.glob 动态聚合
// 在 api/modules/ 下新增 .ts 文件即可自动注册，无需手动 re-export
const _apiModules = import.meta.glob<Record<string, unknown>>('./modules/*.ts', { eager: true })

export const apiModules = Object.fromEntries(
  Object.entries(_apiModules).map(([path, mod]) => [
    path.replace('./modules/', '').replace('.ts', ''),
    mod,
  ]),
)
