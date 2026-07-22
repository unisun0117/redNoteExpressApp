# HTML 工具箱

拖拽转换 / 命令行截图 / 批量导出。

---

## 🌟 可视化截图服务（推荐）

**双击 `screenshot_server.py` 或在终端运行：**

```bash
pip install playwright    # 只需安装一次
python screenshot_server.py
```

→ 浏览器自动打开 → **拖入 HTML 文件 → 选模式 → 点「截图」→ 自动下载 PNG**

| 模式 | 效果 |
|------|------|
| 整页长图 | 完整页面高度，一张 PNG |
| 分段截图 | 每个 Section 单独一张 PNG |

**Playwright 真浏览器渲染**，Google 字体、CSS 渐变、动画全部完美保留。和你在 Chrome 里看到的一模一样。

---

## 📸 命令行截图

```bash
# 全页截图（默认 2x 高清）
python screenshot.py guide.html

# 每个 <section> 截一张图
python screenshot.py guide.html --sections

# 批量截图整个目录
python screenshot.py ../Agent/ --sections -o screenshots/
```

### 参数说明

| 参数 | 说明 |
|------|------|
| `--sections, -s` | 每个 `<section>` 单独截一张 |
| `--full-page, -f` | 截取完整页面高度 |
| `--width` | 视口宽度（默认 1280） |
| `--scale` | 像素比（默认 2 = 2x Retina 高清） |
| `--delay` | 加载等待秒数（默认 1s，给字体加载时间） |
| `--format` | png / jpeg |
| `-o` | 输出目录 |

---

## 📝 命令行转换

需要先安装 Python 依赖：

```bash
pip install beautifulsoup4 python-pptx lxml Pillow
```

### MD / PPTX 互转

```bash
# 单文件
python convert.py guide.html --to md
python convert.py guide.html --to pptx
python convert.py guide.html --to both

# 批量目录
python convert.py ../Agent/ --to both -o output/
```

---

## 📁 文件说明

```
html/tools/
├── converter.html      ← 拖拽版（双击打开即用）
├── screenshot.py       ← 高清截图工具
├── convert.py          ← MD/PPTX 命令行
├── html_to_md.py       ← MD 解析引擎
├── html_to_pptx.py     ← PPTX 生成引擎
└── README.md
```
