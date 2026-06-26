# Lab 07：前端生成页面 — 图片上传 + 参数配置

> **预计用时：** 40-45 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 06（AI 生成接口必须能调通）

---

## 📌 前言

后端接口调通了，但用户不能每次都去 Swagger 填参数吧？这一 Lab 我们做一个真正可用的前端生成页面：一个输入框写关键词、两个下拉选风格和赛道、两个开关控制 emoji 和小标题、一个上传按钮传图片、一个生成按钮触发生成。

做这个页面的时候，我犯过一个低级错误——生成按钮忘了加 loading 状态。用户点了生成没反应，以为卡住了，连点三下，点数全扣光了。后来加了 loading + disabled，才把这个坑填上。

---

## 📚 基础知识储备

- **React 受控组件** — input/select 的值由 state 控制，不是 DOM 自己管
- **FormData** — 浏览器原生 API，用来组装 multipart/form-data 请求（文件上传必须用这个）
- **File API** — 浏览器原生 API，处理用户上传的文件
- **useState / useEffect** — React 最基础的两个 Hook
- **条件渲染** — 根据状态显示不同内容（loading 时显示加载中、有结果时显示文章）

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 实现完整的生成页面，包含所有输入控件
2. 理解 FormData 的组装逻辑（同时传文本 + 文件 + 布尔值）
3. 实现 loading 状态和错误提示
4. 在页面上展示生成结果

---

## 🛠 动手实战

### 步骤 1：读懂生成页面 `pages/GeneratorPage.tsx`

**打开文件：** `frontend/src/pages/GeneratorPage.tsx`

**核心 state：**

```typescript
const [images, setImages] = useState<File[]>([]);     // 上传的图片
const [keywords, setKeywords] = useState("");          // 关键词
const [storeName, setStoreName] = useState("");        // 店铺名
const [storeAddress, setStoreAddress] = useState("");  // 店铺地址
const [styleConfig, setStyleConfig] = useState<StyleConfig>(
  saved ? { ...DEFAULT_CONFIG, ...JSON.parse(saved) } : DEFAULT_CONFIG
);
const [generating, setGenerating] = useState(false);   // 是否正在生成
const [result, setResult] = useState<any>(null);       // 生成结果
const [error, setError] = useState<string | null>(null); // 错误信息
```

**默认风格配置：**
```typescript
const DEFAULT_CONFIG: StyleConfig = {
  template: "简约风",
  track: "美食",
  emojiEnabled: true,
  subtitlesEnabled: true,
};
```

---

### 步骤 2：理解生成按钮的核心逻辑

```typescript
const handleGenerate = async () => {
  // 1. 基础校验
  if (!keywords.trim() && images.length === 0) {
    setError("请至少上传一张图片或输入关键词");
    return;
  }
  setError(null);
  setGenerating(true);  // ← 开始 loading

  try {
    // 2. 组装 FormData（关键：字段名必须和后端一致！）
    const fd = new FormData();
    fd.append("keywords", keywords);
    fd.append("style_template", styleConfig.template);
    fd.append("track", styleConfig.track);
    fd.append("emoji_enabled", String(styleConfig.emojiEnabled));
    fd.append("subtitles_enabled", String(styleConfig.subtitlesEnabled));
    fd.append("store_name", storeName);
    fd.append("store_address", storeAddress);
    if (images.length > 0) fd.append("image", images[0]);

    // 3. 调 API（自动带 token！）
    const data = await api.generate(fd);

    if (data.title) {
      setResult(data);  // 展示结果
    } else {
      setError(data.detail || "生成失败，请重试");
    }
  } catch {
    setError("网络错误，请检查后端服务是否启动");
  } finally {
    setGenerating(false);  // ← 不管成功失败，都要关 loading
  }
};
```

**关键细节：**
- FormData 的字段名（如 `style_template`）必须和后端 `generate.py` 里的参数名完全一致
- 布尔值要转成字符串：`String(styleConfig.emojiEnabled)` → `"true"` 或 `"false"`
- `try...finally` 确保不管成功失败都关掉 loading

---

### 步骤 3：理解风格配置面板 `components/StylePanel.tsx`

**核心：下拉联动 + localStorage 持久化**

```typescript
const update = (partial: Partial<StyleConfig>) => {
  const newConfig = { ...config, ...partial };
  onChange(newConfig);
  localStorage.setItem("style_config", JSON.stringify(newConfig));
  // ↑ 存到 localStorage，下次打开页面自动恢复用户偏好
};
```

用户选了"幽默风 + 数码赛道 + 关闭 emoji"——下次打开页面，这些设置还在。这就是产品体验。

---

### 步骤 4：理解图片上传 `components/ImageUpload.tsx`

支持预览、替换、移除。核心用的是浏览器的 `FileReader` API 做本地预览，不需要上传到服务器。

---

### 步骤 5：跑通完整生成流程

**操作：**
1. 启动前后端
2. 浏览器打开 http://localhost:5173
3. 登录后进入生成页
4. 输入关键词：`深圳 咖啡店 brunch 周末`
5. 风格选"幽默风"，赛道选"美食"
6. Emoji 和小标题保留默认开启
7. 可选：上传一张美食图片
8. 点击"✨ 一键生成小红书文案"
9. 等待几秒 → 文章出来了！

**验证：** 完整流程跑通，页面上展示了生成的文章 ✅

---

### 步骤 6（可选）：给生成按钮加防重复点击

看看当前代码里已经有这个逻辑了：
```tsx
<button disabled={generating}>
  {generating ? "生成中..." : "✨ 一键生成小红书文案"}
</button>
```

`disabled={generating}` → 生成中按钮灰色，不能再点。这就是防重复提交。

**验证：** 生成中按钮是灰色的，不能点击 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| FormData 字段名不一致 | 后端收不到参数 | 前端 `fd.append("style_template", ...)` 和后端 `style_template: str = Form(...)` 不匹配 | 对齐字段名 |
| 多图生成失败 | 500 错误 | 当前版本只支持单图 | 先只传一张图 |
| 生成卡住 | 一直转圈不返回 | DeepSeek API 超时或 token 过期 | 检查后端日志，检查 token 是否有效 |
| 生成成功但页面没显示 | result 是 null | `data.title` 判断没通过 | 检查返回的 JSON 结构 |

---

## 📝 总结

**本章核心要点：**
- FormData 组装：字段名和后端严格一致，布尔值转字符串
- loading 状态：`try...finally` 模式确保一定关掉
- 防重复提交：`disabled={generating}` + 按钮文案切换
- `localStorage` 持久化用户偏好

**你现在应该能做到：**
- 独立实现一个带文件上传 + 多参数的表单页面
- 说出 FormData 的工作原理
- 处理 loading、error、success 三种状态

**下一步：** Lab 08 我们优化结果展示——支持一键复制全文。

---

> —— 阿珊，前端开发者 & AI 提效实践者
