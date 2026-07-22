/**
 * 企业微信 JS-SDK (jweixin-1.6.0) 全局类型声明
 *
 * 补充 @types/jweixin 尚未覆盖的 WWOpenData 及新版 API。
 * 基础接口（wx.config / wx.agentConfig / wx.ready / wx.error 等）
 * 由 @types/jweixin 提供，此处仅扩展。
 */

declare const wx: WeixinJSConfig

interface WeixinJSConfig {
  /** 注入企业的身份与权限 */
  config(cfg: WxConfigParams): void

  /** 注入应用的身份与权限（agentConfig） */
  agentConfig(cfg: WxAgentConfigParams): void

  /** config 验证成功回调 */
  ready(fn: () => void): void

  /** config 验证失败回调 */
  error(fn: (err: WxError) => void): void

  /** 判断当前客户端版本是否兼容指定 API */
  checkJsApi(cfg: {
    jsApiList: string[]
    success?: (res: { checkResult: Record<string, boolean>; errMsg: string }) => void
    fail?: (err: WxError) => void
  }): void

  /** 隐藏右上角菜单项 */
  hideOptionMenu(): void

  /** 显示右上角菜单项 */
  showOptionMenu(): void

  /** 关闭当前网页窗口 */
  closeWindow(): void

  /** 隐藏所有菜单项 */
  hideMenuItems(cfg: { menuList: string[] }): void

  /** 显示所有菜单项 */
  showMenuItems(cfg: { menuList: string[] }): void

  /** 获取网络状态 */
  getNetworkType(cfg: {
    success?: (res: { networkType: string }) => void
    fail?: (err: WxError) => void
  }): void

  /** 获取地理位置 */
  getLocation(cfg: {
    type?: string
    success?: (res: {
      latitude: number
      longitude: number
      speed: number
      accuracy: number
    }) => void
    fail?: (err: WxError) => void
    cancel?: () => void
  }): void

  /** 预览图片 */
  previewImage(cfg: {
    current: string
    urls: string[]
  }): void

  /** 扫码 */
  scanQRCode(cfg: {
    needResult?: 0 | 1
    scanType?: ('qrCode' | 'barCode')[]
    success?: (res: { resultStr: string }) => void
    fail?: (err: WxError) => void
  }): void

  /** 通过企业微信打开默认浏览器 */
  openDefaultBrowser(cfg: {
    url: string
    success?: (res: unknown) => void
    fail?: (err: WxError) => void
  }): void

  /** 调用自定义菜单 */
  invoke(
    name: string,
    params: Record<string, unknown>,
    callback: (res: Record<string, unknown>) => void,
  ): void

  /** 监听事件 */
  on(event: string, callback: (res: Record<string, unknown>) => void): void

  // ===== 预留扩展槽位 =====
  [key: string]: unknown
}

// ===== 配置参数类型 =====

interface WxConfigParams {
  /** 开启调试模式 */
  debug?: boolean
  /** 企业微信的 corpID */
  appId: string
  /** 时间戳（签名生成用） */
  timestamp: number
  /** 随机串（签名生成用） */
  nonceStr: string
  /** 签名 */
  signature: string
  /** 需要使用的 JS 接口列表 */
  jsApiList: string[]
}

interface WxAgentConfigParams {
  /** 企业微信的 corpID */
  corpid: string
  /** 应用 agentId */
  agentid: string
  /** 时间戳（签名生成用） */
  timestamp: number
  /** 随机串（签名生成用） */
  nonceStr: string
  /** 签名 */
  signature: string
  /** 需要使用的 JS 接口列表 */
  jsApiList: string[]
  /** 成功回调 */
  success?: (res: unknown) => void
  /** 失败回调 */
  fail?: (err: WxError) => void
}

interface WxError {
  errMsg: string
  [key: string]: unknown
}

// ===== WWOpenData 类型声明 =====

/** 企业微信开放数据（敏感数据 + 企业通讯录） */
interface WWOpenData {
  /** 获取企业微信开放数据 */
  get(
    opts: WWOpenDataGetOptions,
  ): void
}

interface WWOpenDataGetOptions {
  /** 开放数据类型 */
  type: 'userInfo' | 'department' | 'externalContact' | string
  /** 成功回调 */
  success?: (res: WWOpenDataResponse) => void
  /** 失败回调 */
  fail?: (err: WxError) => void
  /** 完成回调 */
  complete?: () => void
}

interface WWOpenDataResponse {
  /** 加密数据 */
  encryptedData: string
  /** 加密向量 */
  iv: string
  /** 云 ID */
  cloudID?: string
}
