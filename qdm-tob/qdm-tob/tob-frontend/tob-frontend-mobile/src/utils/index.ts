/**
 * 工具函数统一导出
 *
 * 使用方式：
 *   import { env, debounce, formatDate } from '@/utils'
 */

// ---------------------------------------------------------------------------
// 环境配置（从 env.ts 重导出）
// ---------------------------------------------------------------------------

export { env } from './env'
export type { EnvMode, EnvConfig } from './env'

// ---------------------------------------------------------------------------
// 类型判断
// ---------------------------------------------------------------------------

/** 判断值是否为 null 或 undefined */
export function isNil(value: unknown): value is null | undefined {
  return value === null || value === undefined
}

/** 判断值是否为字符串 */
export function isString(value: unknown): value is string {
  return typeof value === 'string'
}

/** 判断值是否为数字 */
export function isNumber(value: unknown): value is number {
  return typeof value === 'number' && !Number.isNaN(value)
}

/** 判断值是否为普通对象 */
export function isObject(value: unknown): value is Record<string, unknown> {
  return Object.prototype.toString.call(value) === '[object Object]'
}

// ---------------------------------------------------------------------------
// 防抖 & 节流
// ---------------------------------------------------------------------------

/** 防抖：等待 delay 毫秒后执行，期间重复调用则重新计时 */
export function debounce<T extends (...args: never[]) => void>(fn: T, delay = 300): T {
  let timer: ReturnType<typeof setTimeout> | null = null
  return ((...args: never[]) => {
    if (timer) clearTimeout(timer)
    timer = setTimeout(() => {
      fn(...args)
      timer = null
    }, delay)
  }) as T
}

/** 节流：每 interval 毫秒最多执行一次 */
export function throttle<T extends (...args: never[]) => void>(fn: T, interval = 300): T {
  let lastTime = 0
  return ((...args: never[]) => {
    const now = Date.now()
    if (now - lastTime >= interval) {
      lastTime = now
      fn(...args)
    }
  }) as T
}

// ---------------------------------------------------------------------------
// 日期格式化
// ---------------------------------------------------------------------------

/** 格式化日期对象为字符串 */
export function formatDate(date: Date, format = 'YYYY-MM-DD HH:mm:ss'): string {
  const year = date.getFullYear()
  const month = String(date.getMonth() + 1).padStart(2, '0')
  const day = String(date.getDate()).padStart(2, '0')
  const hours = String(date.getHours()).padStart(2, '0')
  const minutes = String(date.getMinutes()).padStart(2, '0')
  const seconds = String(date.getSeconds()).padStart(2, '0')

  return format
    .replace('YYYY', String(year))
    .replace('MM', month)
    .replace('DD', day)
    .replace('HH', hours)
    .replace('mm', minutes)
    .replace('ss', seconds)
}

// ---------------------------------------------------------------------------
// 小程序专用
// ---------------------------------------------------------------------------

/** 获取当前页面路径 */
export function getCurrentPagePath(): string {
  const pages = getCurrentPages()
  if (pages.length === 0) return ''
  const currentPage = pages[pages.length - 1]
  return '/' + (currentPage.route ?? '')
}
