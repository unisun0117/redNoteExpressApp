## ADDED Requirements

### Requirement: HTTP 请求失败时自动上报 SLS 日志

系统 SHALL 在所有 HTTP 请求失败时自动调用 `sendSlsLog()` 上报错误日志到阿里云 SLS，无需业务代码手动干预。

#### Scenario: 业务错误自动上报

- **WHEN** 接口返回 `code` 不为 `0` 或 `200`
- **THEN** 系统 SHALL 自动上报包含 `url`、`statusCode`、`errorResponseBody`、`requestTime` 的日志
- **AND** 日志的 `system` 字段 SHALL 为 `'tob-小程序'`

#### Scenario: 网络异常自动上报

- **WHEN** 接口请求发生网络错误、超时或 HTTP 4xx/5xx 错误
- **THEN** 系统 SHALL 自动上报包含 `url`、`statusCode`、`errorResponseBody`（或 `errMsg`）的日志

#### Scenario: SLS 上报失败不影响业务流程

- **WHEN** SLS 日志上报本身发生错误
- **THEN** 系统 SHALL 静默失败，不影响用户的正常操作流程

### Requirement: SLS Tracker 单例管理

系统 SHALL 使用单例模式管理 `SlsTracker` 实例，通过 STS 插件自动刷新临时凭证。

#### Scenario: 首次调用时创建 Tracker

- **WHEN** 首次调用 `sendSlsLog()`
- **THEN** 系统 SHALL 创建 `SlsTracker` 实例并挂载 STS 插件
- **AND** 后续调用 SHALL 复用同一实例
