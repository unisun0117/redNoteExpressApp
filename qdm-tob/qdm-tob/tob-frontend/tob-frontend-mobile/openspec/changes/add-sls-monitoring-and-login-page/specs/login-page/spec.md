## ADDED Requirements

### Requirement: 登录页展示品牌标识

登录页 SHALL 展示品牌 Logo（`/static/login/logo.png`）和品牌名称"钱鲜达"，标语为"欢迎使用钱鲜达"。

#### Scenario: 品牌信息展示

- **WHEN** 用户进入登录页
- **THEN** 页面 SHALL 显示 Logo 图片和"钱鲜达"品牌名称

### Requirement: 微信一键登录

登录页 SHALL 提供微信一键登录按钮作为主要操作入口。

#### Scenario: 点击微信登录

- **WHEN** 用户点击"微信一键登录"按钮
- **AND** 用户未勾选协议
- **THEN** 系统 SHALL 提示"请先同意用户协议"

#### Scenario: 协议已勾选时点击微信登录

- **WHEN** 用户已勾选协议并点击"微信一键登录"
- **THEN** 系统 SHALL 调用微信登录 API（当前阶段显示待对接提示）

### Requirement: 手机号验证码登录

登录页 SHALL 提供手机号 + 验证码登录方式作为辅助登录入口。

#### Scenario: 发送验证码

- **WHEN** 用户输入 11 位手机号并点击"获取验证码"
- **THEN** 按钮 SHALL 进入 60 秒倒计时，期间不可重复点击

#### Scenario: 验证码倒计时

- **WHEN** 倒计时进行中
- **THEN** 按钮文字 SHALL 显示"Xs 后重发"并置灰

#### Scenario: 手机号格式校验

- **WHEN** 用户输入不完整的手机号并点击"获取验证码"
- **THEN** 系统 SHALL 提示"请输入正确的手机号"

#### Scenario: 完整登录流程

- **WHEN** 用户输入正确的手机号和验证码
- **AND** 勾选用户协议
- **AND** 点击"登录"按钮
- **THEN** 按钮 SHALL 显示 loading 状态
- **AND** 登录成功后 SHALL 跳转至首页

### Requirement: 用户协议勾选

登录页 SHALL 要求用户在登录前勾选同意《隐私政策》和《用户协议》。

#### Scenario: 未勾选协议时拦截

- **WHEN** 用户未勾选协议并尝试任何登录方式
- **THEN** 系统 SHALL 提示"请先同意用户协议"并阻止登录

### Requirement: Restaurant & Food 设计风格

登录页 SHALL 遵循 Restaurant & Food 设计风格：食欲红主色 `#DC2626`、暖金 CTA 色 `#CA8A04`、暖色渐变背景、圆角输入框、深红棕文字。

#### Scenario: 视觉一致性

- **WHEN** 页面渲染
- **THEN** 所有交互元素 SHALL 使用设计体系规定的颜色和圆角
- **AND** 触控目标 SHALL 不小于 44px
