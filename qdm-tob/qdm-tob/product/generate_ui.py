"""使用 Gemini 生成小程序登录页和注册页的 HTML5 文件"""
import os
import re
from google import genai

client = genai.Client(
    api_key='sk-ant-api03-7t0bgAhPleNlq0MzOSO5P8-eIEP4IjQVm_siRsgroGGkLXeQmugvAHIY3nmsSlC1JHG_PiUA8QqAO7Z5GnxWig',
    http_options={
        'base_url': 'https://api.aicodemirror.com/api/gemini',
    },
)

OUTPUT_DIR = os.path.join(os.path.dirname(__file__), 'prd')


def extract_html(text):
    matches = [
        re.search(r'<!DOCTYPE html>[\s\S]*?</html>', text, re.IGNORECASE),
        re.search(r'<html[\s\S]*?</html>', text, re.IGNORECASE),
        re.search(r'```html\n([\s\S]*?)```', text),
    ]
    for m in matches:
        if m:
            content = m.group(1) if '```' in m.re.pattern else m.group(0)
            if not content.lower().startswith('<!doctype'):
                content = '<!DOCTYPE html>\n' + content
            return content
    return None


def generate_html(prompt, filename):
    print(f"正在生成: {filename} ...")
    response = client.models.generate_content(
        model='gemini-3.5-flash',
        contents=prompt,
    )

    full_text = response.text
    html = extract_html(full_text)
    if html:
        fpath = os.path.join(OUTPUT_DIR, filename)
        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(html)
        print(f"  已保存: {fpath}")
    else:
        fpath = os.path.join(OUTPUT_DIR, filename)
        with open(fpath, 'w', encoding='utf-8') as f:
            f.write(full_text)
        print(f"  未提取到 HTML，已保存原始文本: {fpath}")


# ===== 登录页 (v1.3 spec) =====
login_prompt = """请生成一个微信小程序风格的登录页面，输出完整可运行的 HTML5 文件（单文件包含 CSS 和 JS）。

技术要求：
- 纯 HTML5 + CSS3 + 原生 JavaScript，无外部依赖
- 移动端适配，375px 宽居中，模拟手机屏幕
- 所有交互逻辑用 JS 实现

============================================================
UI 规格（严格按照 v1.3 SPEC）：
============================================================

整体风格：简洁商业风格，白色背景，主色调 #1677FF，微信绿 #07C160。

## 默认界面（页面初始状态）

1. **品牌区**：圆形 logo（蓝色渐变背景，白色 SVG 图标）+ "钱鲜达" 文字，居中
2. **描述文案**："欢迎使用钱鲜达" 之类的引导语，居中，灰色
3. **微信一键登录按钮**：绿色(#07C160)圆角全宽按钮，左侧白色微信 SVG 图标 + 文字"微信一键登录"
4. **手机号登录入口**：灰色(#f0f0f0)背景按钮，文字"手机号登录"，hover 时背景变橙色(#ff6b35)且有过渡动画
5. **合规协议勾选区**：自定义勾选框 + "我已阅读并同意钱鲜达的《用户服务协议》和《隐私政策》"（协议名蓝色可点击）
6. **底部链接**："没有账号？去注册"（"去注册"蓝色可点击）

## 底部 Sheet（点击「手机号登录」后弹出）

- 半透明黑色遮罩（opacity 0.5）覆盖全屏
- 底部白色 sheet 从下方滑入，动画 cubic-bezier(0.32, 0.72, 0, 1)，时长 300ms
- sheet 顶部：灰色拖拽手柄横条 + 标题"手机号登录" + 右侧 ✕ 关闭按钮
- sheet 内容：手机号输入框 + 验证码输入框 + "获取验证码"并排按钮 + 蓝色"登录"按钮
- 点击遮罩或关闭按钮 → sheet 滑出收起
- 检测 prefers-reduced-motion 时取消动画，直接显示/隐藏

## 协议弹窗

- 点击"微信一键登录"或"登录"按钮时，若未勾选协议：
  - 弹出居中模态弹窗，标题"温馨提示"，内容"请阅读并且同意协议"，确定按钮
  - 点击确定 → 自动勾选协议 + 执行登录流程
  - 弹窗外点击不关闭

## 交互要求

- 手机号限制 11 位数字，自动过滤非数字
- 验证码按钮：校验手机号格式 → 60s 倒计时（按钮文字"XXs后重试"），倒计时中置灰
- Sheet 内登录按钮：手机号+验证码都填写 → 可点击
- 微信登录点击（已勾协议）→ alert 模拟微信授权
- 协议链接点击 → alert 展示协议名称
- "去注册"点击 → alert 提示跳转

只输出完整 HTML 代码，不要任何解释。"""


# ===== 注册页 (v1.3 spec) =====
register_prompt = """请生成一个微信小程序风格的注册页面，输出完整可运行的 HTML5 文件（单文件包含 CSS 和 JS）。

技术要求：
- 纯 HTML5 + CSS3 + 原生 JavaScript，无外部依赖
- 移动端适配，375px 宽居中，模拟手机屏幕
- 所有交互逻辑用 JS 实现

UI 规格（严格按照 SPEC）：

整体风格：简洁商业风格，白色背景，主色调 #1677FF。

页面结构：
1. 顶部导航栏：返回箭头（左侧）+ 标题"注册账号"居中
2. 输入区：
   - 姓名输入框 + 人物图标，placeholder "请输入姓名"
   - 手机号输入框 + 手机图标，placeholder "请输入手机号"
   - 验证码输入框 + "获取验证码"按钮并排，按钮下方小字"60s后重新获取"（初始隐藏，倒计时时显示）
3. 协议勾选区：勾选框 + "我已阅读并同意钱鲜达的《用户服务协议》和《隐私政策》"（协议名蓝色可点击）
4. 注册按钮：蓝色圆角满宽，所有字段填完且勾选协议后才可点击
5. 底部："已有账号？去登录" 链接

交互要求：
- 手机号限制 11 位数字
- 验证码按钮点击校验手机号 → 开始 60s 倒计时，显示倒计时文字
- 注册按钮状态联动：姓名+手机号+验证码+勾选协议 → 才可点击（未满足时置灰半透明）
- 点击注册做基本校验并 alert 提示"注册成功"
- 返回箭头和"去登录"点击 alert 提示

只输出完整 HTML 代码，不要任何解释。"""


if __name__ == '__main__':
    os.makedirs(OUTPUT_DIR, exist_ok=True)
    generate_html(login_prompt, 'login.html')
    generate_html(register_prompt, 'register.html')
    print("\n全部完成！")
