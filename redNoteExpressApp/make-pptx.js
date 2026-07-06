const pptxgen = require("pptxgenjs");

const pres = new pptxgen();
pres.layout = "LAYOUT_16x9";
pres.author = "阿珊";
pres.title = "红薯快写 RedNote Express — AI小红书文章生成器";

// ---- Color Palette (Warm Coral) ----
const C = {
  coral:    "C44F3C",
  coralDeep:"8B2C1E",
  cream:    "FEF9F5",
  white:    "FFFFFF",
  charcoal: "2D3436",
  gray:     "7B7370",
  lightGray:"EDE8E4",
  pink:     "F0D5D2",
  green:    "5C9A6B",
  greenBg:  "E8F0E4",
  amber:    "C4883C",
  amberBg:  "FDF3E5",
  blue:     "4A8BC2",
  blueBg:   "EDF4FA",
};

// ---- Helpers ----
const makeShadow = () => ({ type: "outer", blur: 6, offset: 3, angle: 135, color: "000000", opacity: 0.08 });
const makeCard = (slide, x, y, w, h, color) => {
  slide.addShape(pres.shapes.RECTANGLE, { x, y, w, h, fill: { color: C.white }, shadow: makeShadow() });
  slide.addShape(pres.shapes.RECTANGLE, { x, y, w, h: 0.06, fill: { color } });
};
const addIconBox = (slide, x, y, icon, color, bgColor) => {
  slide.addShape(pres.shapes.OVAL, { x, y, w: 0.5, h: 0.5, fill: { color: bgColor } });
  slide.addText(icon, { x, y, w: 0.5, h: 0.5, fontSize: 14, align: "center", valign: "middle", color });
};

// ===== SLIDE 1: COVER =====
let s1 = pres.addSlide();
s1.background = { color: C.coralDeep };
s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 5.625, fill: { color: C.coralDeep } });
// Gradient effect via overlay
s1.addShape(pres.shapes.RECTANGLE, { x: 0, y: 0, w: 10, h: 3, fill: { color: C.coral, transparency: 60 } });
s1.addText("🍠", { x: 0.8, y: 0.8, w: 1.5, h: 1.2, fontSize: 52, align: "center" });
s1.addText("红薯快写", { x: 0.8, y: 1.9, w: 8.4, h: 1.0, fontSize: 48, fontFace: "Arial Black", color: C.white, bold: true, margin: 0 });
s1.addText("RedNote Express App", { x: 0.8, y: 2.7, w: 8.4, h: 0.5, fontSize: 18, fontFace: "Arial", color: C.pink, margin: 0 });
s1.addShape(pres.shapes.RECTANGLE, { x: 0.8, y: 3.3, w: 1.8, h: 0.04, fill: { color: C.coral } });
s1.addText("AI 驱动的小红书风格文章一键生成器", { x: 0.8, y: 3.5, w: 8, h: 0.5, fontSize: 16, color: C.lightGray, margin: 0 });
s1.addText("PRESENTATION · 2026.07", { x: 0.8, y: 4.8, w: 5, h: 0.4, fontSize: 10, color: C.gray, margin: 0 });

// ===== SLIDE 2: PRODUCT OVERVIEW =====
let s2 = pres.addSlide();
s2.background = { color: C.cream };
s2.addText("产品概述", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

// Three cards
const cards2 = [
  { icon:"🎯", title:"一句话定位", body:"上传图片 + 输入关键词\n+ 选择风格 → AI 生成\n完整小红书风格文章", color:C.coral, bg:C.pink },
  { icon:"👥", title:"目标用户", body:"电商店主 · 自媒体新手\n实体店主 · 营销人员\n想写小红书但不会写的人", color:C.amber, bg:C.amberBg },
  { icon:"⚡", title:"核心价值", body:"30秒出文，微调即发\n20+赛道风格模板\n7段式结构化输出", color:C.green, bg:C.greenBg },
];
cards2.forEach((c, i) => {
  const x = 0.7 + i * 3.0;
  makeCard(s2, x, 1.2, 2.7, 3.2, c.color);
  addIconBox(s2, x + 0.2, 1.4, c.icon, c.color, c.bg);
  s2.addText(c.title, { x: x + 0.85, y: 1.4, w: 1.6, h: 0.5, fontSize: 16, fontFace: "Arial", color: C.charcoal, bold: true, margin: 0 });
  s2.addText(c.body, { x: x + 0.2, y: 2.1, w: 2.3, h: 2.1, fontSize: 12, color: C.gray, lineSpacingMultiple: 1.5, margin: 0 });
});

s2.addText("当前状态：MVP 已上线运行，注册登录 + 文章生成 + 点数系统已可用", { x: 0.7, y: 4.7, w: 9, h: 0.4, fontSize: 12, color: C.green, italic: true, margin: 0 });

// ===== SLIDE 3: CORE FEATURES =====
let s3 = pres.addSlide();
s3.background = { color: C.white };
s3.addText("核心功能", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

const feats = [
  { icon:"📤", title:"图片上传", desc:"支持单图/多图上传\n拖拽 + 点击上传\n实时预览", status:"✅ 已上线" },
  { icon:"🏷️", title:"风格配置", desc:"20+内容赛道\n风格模板切换\nEmoji/小标题开关", status:"✅ 已上线" },
  { icon:"🤖", title:"AI 生成", desc:"DeepSeek Chat 驱动\n7段式结构化文章\nJSON 结构化输出", status:"✅ 已上线" },
  { icon:"📋", title:"一键复制", desc:"结构化渲染展示\n一键复制全文\n即复制即发布", status:"✅ 已上线" },
  { icon:"📦", title:"批量生成", desc:"多图批量上传\n批量产出文章\nVIP 专属功能", status:"🚧 开发中" },
  { icon:"🔍", title:"爆款分析", desc:"粘贴爆款链接\nAI风格拆解\nVIP 专属功能", status:"🚧 开发中" },
];
feats.forEach((f, i) => {
  const col = i % 3;
  const row = Math.floor(i / 3);
  const x = 0.7 + col * 3.05;
  const y = 1.2 + row * 2.05;
  const color = f.status.includes("✅") ? C.green : C.amber;
  const bg = f.status.includes("✅") ? C.greenBg : C.amberBg;
  makeCard(s3, x, y, 2.8, 1.8, color);
  addIconBox(s3, x + 0.15, y + 0.15, f.icon, color, bg);
  s3.addText(f.title, { x: x + 0.8, y: y + 0.15, w: 1.8, h: 0.45, fontSize: 15, fontFace: "Arial", color: C.charcoal, bold: true, margin: 0 });
  s3.addText(f.desc, { x: x + 0.15, y: y + 0.75, w: 2.5, h: 0.85, fontSize: 11, color: C.gray, lineSpacingMultiple: 1.4, margin: 0 });
  s3.addText(f.status, { x: x + 0.15, y: y + 1.52, w: 2.5, h: 0.25, fontSize: 9, color, margin: 0 });
});

// ===== SLIDE 4: HOW IT WORKS =====
let s4 = pres.addSlide();
s4.background = { color: C.cream };
s4.addText("使用流程", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

const steps = [
  { num:"01", icon:"📸", title:"拍产品图", desc:"用手机拍好产品照片\n或从相册选择已有图片" },
  { num:"02", icon:"📤", title:"上传图片", desc:"拖拽到红薯快写页面\n支持 JPG/PNG/WEBP" },
  { num:"03", icon:"🏷️", title:"选赛道风格", desc:"选择内容赛道和风格模板\n输入产品关键词和卖点" },
  { num:"04", icon:"🤖", title:"AI 生成", desc:"DeepSeek 分析图片+参数\n30秒内产出完整文章" },
  { num:"05", icon:"✏️", title:"微调文章", desc:"预览生成结果\n根据需要微调文字" },
  { num:"06", icon:"📋", title:"复制发布", desc:"一键复制全文\n粘贴到小红书发布" },
];
steps.forEach((st, i) => {
  const x = 0.5 + i * 1.55;
  const y = 1.3;
  s4.addShape(pres.shapes.RECTANGLE, { x, y, w: 1.35, h: 3.5, fill: { color: C.white }, shadow: makeShadow() });
  s4.addShape(pres.shapes.RECTANGLE, { x, y, w: 1.35, h: 0.06, fill: { color: C.coral } });
  s4.addText(st.num, { x, y: y + 0.15, w: 1.35, h: 0.35, fontSize: 24, fontFace: "Arial Black", color: C.coral, align: "center", margin: 0 });
  s4.addText(st.icon, { x, y: y + 0.55, w: 1.35, h: 0.5, fontSize: 22, align: "center", margin: 0 });
  s4.addText(st.title, { x: x + 0.1, y: y + 1.1, w: 1.15, h: 0.4, fontSize: 12, fontFace: "Arial", color: C.charcoal, bold: true, align: "center", margin: 0 });
  s4.addText(st.desc, { x: x + 0.1, y: y + 1.55, w: 1.15, h: 1.5, fontSize: 9, color: C.gray, align: "center", lineSpacingMultiple: 1.4, margin: 0 });
  // Arrow between steps
  if (i < 5) {
    s4.addText("→", { x: x + 1.35, y: y + 1.5, w: 0.2, h: 0.4, fontSize: 18, color: C.coral, align: "center", margin: 0 });
  }
});

s4.addText("从拍照到发布，全程不超过 2 分钟", { x: 0.7, y: 5.0, w: 9, h: 0.4, fontSize: 13, color: C.gray, align: "center", italic: true, margin: 0 });

// ===== SLIDE 5: TECH ARCHITECTURE =====
let s5 = pres.addSlide();
s5.background = { color: C.white };
s5.addText("技术架构", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

// Architecture layers
const layers = [
  { label:"📱 前端", tech:"React 19 + TypeScript 6\nVite 8 + antd-mobile 5\n部署：Vercel", color:C.coral, bg:C.pink, y:1.2, h:1.3 },
  { label:"🔐 鉴权", tech:"JWT access + refresh\n双 Token 机制\n15分钟过期 + 自动刷新", color:C.amber, bg:C.amberBg, y:2.7, h:1.2 },
  { label:"⚙️ 后端", tech:"FastAPI (Python 3.10)\nSQLAlchemy + SQLite\n部署：PythonAnywhere", color:C.blue, bg:C.blueBg, y:1.2, h:1.3, x:5.2 },
  { label:"🤖 AI 引擎", tech:"DeepSeek Chat API\nOpenAI 兼容协议\nJSON 结构化输出", color:C.green, bg:C.greenBg, y:2.7, h:1.2, x:5.2 },
];
layers.forEach(l => {
  const x = l.x || 0.7;
  makeCard(s5, x, l.y, 4.3, l.h, l.color);
  addIconBox(s5, x + 0.15, l.y + 0.15, "", l.color, l.bg);
  s5.addText(l.label, { x: x + 0.7, y: l.y + 0.15, w: 3.3, h: 0.35, fontSize: 15, fontFace: "Arial", color: C.charcoal, bold: true, margin: 0 });
  s5.addText(l.tech, { x: x + 0.15, y: l.y + 0.55, w: 3.8, h: l.h - 0.7, fontSize: 11, color: C.gray, lineSpacingMultiple: 1.4, margin: 0 });
});

// Arrows
s5.addText("← → ↑ ↓", { x: 4.85, y: 1.5, w: 0.5, h: 2, fontSize: 24, color: C.gray, align: "center", valign: "middle", margin: 0 });

// Bottom note
s5.addText("成本极低：DeepSeek ≈ ¥0.001/次  ·  PythonAnywhere $5/月  ·  Vercel 免费", { x: 0.7, y: 4.6, w: 9, h: 0.4, fontSize: 11, color: C.gray, margin: 0 });

// ===== SLIDE 6: ARTICLE STRUCTURE =====
let s6 = pres.addSlide();
s6.background = { color: C.cream };
s6.addText("文章结构 · 7 段式 AI 输出", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

const sections = [
  { title:"📌 标题", words:"18-20字", desc:"吸引点击的爆款标题\n包含关键词和卖点", color:C.coralDeep },
  { title:"📝 前言", words:"≈50字", desc:"场景引入 + 痛点共鸣\n建立读者兴趣", color:C.coral },
  { title:"📖 段落一", words:"小标题+≈100字", desc:"第一个核心卖点\n详细展开说明", color:C.amber },
  { title:"📖 段落二", words:"小标题+≈100字", desc:"第二个核心卖点\n使用体验/效果", color:C.amber },
  { title:"📖 段落三", words:"小标题+≈100字", desc:"第三个核心卖点\n对比/性价比/推荐", color:C.amber },
  { title:"✨ 总结", words:"≈50字", desc:"全文要点回顾\n强化购买理由", color:C.green },
  { title:"🏪 店铺信息", words:"≈50字", desc:"店铺名称/地址/优惠\n引导行动（CTA）", color:C.blue },
];
sections.forEach((sec, i) => {
  const y = 1.15 + i * 0.58;
  s6.addShape(pres.shapes.RECTANGLE, { x: 0.7, y, w: 8.6, h: 0.5, fill: { color: i % 2 === 0 ? C.white : C.lightGray } });
  s6.addShape(pres.shapes.RECTANGLE, { x: 0.7, y, w: 0.06, h: 0.5, fill: { color: sec.color } });
  s6.addText(sec.title, { x: 1.0, y, w: 1.5, h: 0.5, fontSize: 12, fontFace: "Arial", color: C.charcoal, bold: true, valign: "middle", margin: 0 });
  s6.addText(sec.words, { x: 2.5, y, w: 1.2, h: 0.5, fontSize: 10, color: sec.color, valign: "middle", margin: 0 });
  s6.addText(sec.desc, { x: 3.8, y, w: 5.3, h: 0.5, fontSize: 10, color: C.gray, valign: "middle", margin: 0 });
});

// ===== SLIDE 7: BUSINESS MODEL =====
let s7 = pres.addSlide();
s7.background = { color: C.white };
s7.addText("商业模式", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

const tiers = [
  { name:"🆓 免费层", price:"¥0", items:["注册送 3 次免费生成","每日签到 +1 次","基础风格模板"], color:C.gray, bg:C.lightGray },
  { name:"💳 点数包", price:"¥9.9 起", items:["10篇 ¥9.9","50篇 ¥29.9","100篇 ¥49.9","永久有效"], color:C.amber, bg:C.amberBg, accent:true },
  { name:"👑 VIP 订阅", price:"¥19.9/月", items:["月费 100篇/月","年度 ¥99 无限生成","批量生成 + 爆款分析","优先接入新模型"], color:C.coral, bg:C.pink },
];
tiers.forEach((t, i) => {
  const x = 0.7 + i * 3.05;
  const isAccent = t.accent;
  s7.addShape(pres.shapes.RECTANGLE, { x, y: 1.2, w: 2.8, h: 3.5, fill: { color: C.white }, shadow: makeShadow(),
    line: isAccent ? { color: C.amber, width: 2 } : undefined });
  s7.addShape(pres.shapes.RECTANGLE, { x, y: 1.2, w: 2.8, h: 0.06, fill: { color: t.color } });
  s7.addText(t.name, { x: x + 0.2, y: 1.4, w: 2.4, h: 0.4, fontSize: 14, fontFace: "Arial", color: C.charcoal, bold: true, margin: 0 });
  s7.addText(t.price, { x: x + 0.2, y: 1.8, w: 2.4, h: 0.5, fontSize: 28, fontFace: "Arial Black", color: t.color, margin: 0 });
  s7.addText(t.items.map(it => "• " + it).join("\n"), { x: x + 0.2, y: 2.5, w: 2.4, h: 2.0, fontSize: 11, color: C.gray, lineSpacingMultiple: 1.6, margin: 0 });
  if (isAccent) {
    s7.addShape(pres.shapes.RECTANGLE, { x: x + 0.3, y: 1.15, w: 1.2, h: 0.3, fill: { color: C.amber } });
    s7.addText("推荐", { x: x + 0.3, y: 1.15, w: 1.2, h: 0.3, fontSize: 10, color: C.white, align: "center", margin: 0 });
  }
});

s7.addText("月运营成本 ≈ ¥50-200（DeepSeek API）+ $5（PythonAnywhere）+ Vercel（免费）", { x: 0.7, y: 4.9, w: 9, h: 0.4, fontSize: 11, color: C.gray, margin: 0 });

// ===== SLIDE 8: ROADMAP =====
let s8 = pres.addSlide();
s8.background = { color: C.cream };
s8.addText("产品路线图", { x: 0.7, y: 0.3, w: 8, h: 0.7, fontSize: 36, fontFace: "Arial Black", color: C.charcoal, margin: 0 });

const phases = [
  { phase:"Phase 1", title:"MVP 上线", time:"2026.Q1 ✅", items:"注册登录 · 单篇生成 · 点数系统 · 风格配置", color:C.green, bg:C.greenBg },
  { phase:"Phase 2", title:"变现闭环", time:"2026.Q3 📋", items:"微信/支付宝支付 · 点数包 · VIP订阅", color:C.amber, bg:C.amberBg },
  { phase:"Phase 3", title:"批量提效", time:"2026.Q4 📋", items:"批量生成 · 历史记录 · 文章收藏 · 模板市场", color:C.blue, bg:C.blueBg },
  { phase:"Phase 4", title:"智能升级", time:"2027 💡", items:"爆款分析 · 图片AI识别 · 多图关联生成", color:C.coral, bg:C.pink },
  { phase:"Phase 5", title:"平台化", time:"2027+ 💡", items:"多平台分发 · 数据看板 · 团队协作", color:C.charcoal, bg:C.lightGray },
];
phases.forEach((p, i) => {
  const y = 1.2 + i * 0.82;
  // Timeline line
  s8.addShape(pres.shapes.RECTANGLE, { x: 1.55, y, w: 0.04, h: 0.72, fill: { color: p.color } });
  // Circle node
  s8.addShape(pres.shapes.OVAL, { x: 1.38, y: y + 0.18, w: 0.38, h: 0.38, fill: { color: p.color } });
  s8.addText(p.phase, { x: 1.38, y: y + 0.18, w: 0.38, h: 0.38, fontSize: 7, color: C.white, align: "center", valign: "middle", bold: true, margin: 0 });
  // Content card
  s8.addShape(pres.shapes.RECTANGLE, { x: 2.0, y, w: 7.3, h: 0.7, fill: { color: C.white }, shadow: makeShadow() });
  s8.addShape(pres.shapes.RECTANGLE, { x: 2.0, y, w: 0.06, h: 0.7, fill: { color: p.color } });
  s8.addText(p.title, { x: 2.3, y: y + 0.05, w: 1.8, h: 0.3, fontSize: 14, fontFace: "Arial", color: C.charcoal, bold: true, margin: 0 });
  s8.addText(p.time, { x: 2.3, y: y + 0.35, w: 1.8, h: 0.25, fontSize: 10, color: p.color, margin: 0 });
  s8.addText(p.items, { x: 4.0, y: y + 0.1, w: 5.1, h: 0.5, fontSize: 11, color: C.gray, valign: "middle", margin: 0 });
});

// ===== SLIDE 9: CLOSING =====
let s9 = pres.addSlide();
s9.background = { color: C.coralDeep };
s9.addText("🍠", { x: 0, y: 0.8, w: 10, h: 1.2, fontSize: 64, align: "center", margin: 0 });
s9.addText("Thank You", { x: 0, y: 1.9, w: 10, h: 0.8, fontSize: 40, fontFace: "Arial Black", color: C.white, align: "center", margin: 0 });
s9.addShape(pres.shapes.RECTANGLE, { x: 3.5, y: 2.7, w: 3, h: 0.04, fill: { color: C.coral } });
s9.addText("红薯快写 · RedNote Express", { x: 0, y: 2.9, w: 10, h: 0.5, fontSize: 18, color: C.pink, align: "center", margin: 0 });
s9.addText("huanglishan123.pythonanywhere.com\nred-note-express-app.vercel.app", { x: 0, y: 3.6, w: 10, h: 0.8, fontSize: 13, color: C.gray, align: "center", lineSpacingMultiple: 1.5, margin: 0 });

// ---- OUTPUT ----
(async () => {
  try {
    await pres.writeFile({ fileName: "红薯快写_RedNote_Express.pptx" });
    console.log("PPTX generated successfully!");
  } catch (err) {
    console.error("ERROR:", err);
  }
})();
