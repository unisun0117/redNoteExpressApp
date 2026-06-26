# Lab 08：结果展示 + 一键复制 — 让生成结果好看又好用

> **预计用时：** 25-30 分钟
> **难度：** ⭐
> **前置 Lab：** Lab 07（生成页面必须能跑通）

---

## 📌 前言

做好了生成功能，第一个用户跟我说："能不能一键复制？我要粘贴到小红书 App。"我加了个复制按钮。第二个用户说："能不能分段复制？我只想复制正文，不要标题。"第三个用户说："复制出来的格式在小红书里不对。"

产品就是这样——用起来才知道缺什么。这一 Lab 我们把结果展示打磨到可以直接发小红书的程度。

---

## 📚 基础知识储备

- **Clipboard API** — 浏览器提供的剪贴板 API：`navigator.clipboard.writeText(text)`
- **条件渲染** — `{article && <ResultView article={article} />}` → 有结果才渲染
- **模板字符串** — `` `\n` `` 换行拼接多段文本

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 实现文章结果的分段展示（标题、前言、正文、总结）
2. 实现一键复制全文（格式直接适配小红书）
3. 理解组件间 props 传递的最佳实践

---

## 🛠 动手实战

### 步骤 1：读懂结果展示组件 `components/ResultView.tsx`

```typescript
interface Props {
  article: {
    title: string;
    intro: string;
    sections: { subtitle: string; content: string }[];
    summary: string;
    store_info: string;
  };
}

export function ResultView({ article }: Props) {
  if (!article) return null;  // 没数据不渲染

  // 拼接全文（格式适配小红书）
  const copyText = () => {
    const text = [
      article.title,                             // 标题
      "",
      article.intro,                             // 前言
      "",
      ...article.sections.map((s) =>             // 3 个段落
        s.subtitle ? `${s.subtitle}\n${s.content}` : s.content
      ),
      "",
      article.summary,                           // 总结
      "",
      article.store_info,                        // 店铺信息
    ].join("\n");
    navigator.clipboard.writeText(text);         // 写入剪贴板
  };

  return (
    <div className="article-preview">
      {/* 复制按钮 */}
      <button className="copy-btn" onClick={copyText}>📋 复制全文</button>

      {/* 标题 */}
      <div className="article-title">{article.title}</div>

      {/* 前言 */}
      <div className="article-intro">{article.intro}</div>

      {/* 三个正文段落 */}
      {article.sections?.map((section, i) => (
        <div key={i}>
          {section.subtitle && <div className="article-section-subtitle">{section.subtitle}</div>}
          <div className="article-section-content">{section.content}</div>
        </div>
      ))}

      {/* 总结 */}
      <div className="article-summary">{article.summary}</div>

      {/* 店铺信息（可选） */}
      {article.store_info && <div className="article-store">{article.store_info}</div>}
    </div>
  );
}
```

---

### 步骤 2：理解复制逻辑

```typescript
const text = ["标题", "", "前言", "", "段落1", "段落2", "段落3", "", "总结", "", "店铺信息"].join("\n");
```

用 `\n` 换行拼接，复制出来直接就是小红书的发文格式。不需要用户手动分段。

**验证：** 点击"复制全文" → 打开记事本粘贴 → 格式正确 ✅

---

### 步骤 3：看看实际效果

生成一篇文章后，你会看到：
- 📋 复制全文（按钮，右上角）
- 标题（大字加粗）
- 前言（正文风格）
- 段落 1（小标题 + 正文）
- 段落 2
- 段落 3
- 总结
- 店铺信息

整个布局适合移动端阅读（因为小红书主要用户在手机上）。

---

### 步骤 4（可选）：增加"复制后提示"

用户体验细节——复制完给个反馈：

```tsx
const [copied, setCopied] = useState(false);

const handleCopy = () => {
  copyText();
  setCopied(true);
  setTimeout(() => setCopied(false), 2000);  // 2 秒后消失
};

// 按钮改成：
<button onClick={handleCopy}>
  {copied ? "✅ 已复制" : "📋 复制全文"}
</button>
```

这是你自己加的优化，项目源码里没有——正好练手！

**验证：** 点击按钮后显示"✅ 已复制"，2 秒后恢复 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| Clipboard API 不可用 | 复制没反应 | 非 HTTPS 环境或浏览器不支持 | 本地开发 localhost 是安全的，可以用 |
| `article.sections` 报 `undefined` | 页面白屏 | 后端返回的 JSON 没有 sections 字段 | 加可选链：`article.sections?.map(...)` |
| 复制的文本格式不对 | 小红书里换行不对 | 用了 `\r\n` 或不同的换行符 | 统一用 `\n` |

---

## 📝 总结

**本章核心要点：**
- `navigator.clipboard.writeText()` 实现一键复制
- 用 `\n` 换行拼接，复制出来的格式直接适配小红书
- 可选链 `?.` 防止字段缺失导致白屏
- 复制后给用户反馈（"已复制"提示）提升体验

**你现在应该能做到：**
- 实现分段展示文章 + 一键复制功能
- 处理字段可能缺失的防御性编程

**下一步：** Lab 09 我们把风格系统做得更完整——理解 4 种风格 + 9 个赛道的 Prompt 设计。

---

> —— 阿珊，前端开发者 & AI 提效实践者
