# Lab 10：点数计费系统 — 让 App 能赚钱

> **预计用时：** 30-35 分钟
> **难度：** ⭐⭐
> **前置 Lab：** Lab 09（生成流程必须能跑通）

---

## 📌 前言

做收费功能之前，我想了很久：怎么定价？免费给多少？

我调研了市面上 5 款 AI 写作工具，发现主流模式就两种：订阅制（按月付费）和点数制（按次扣费）。最后我选了混合模式——新用户注册送 10 次免费体验，后续 9.9 元买 100 次，或者 29.9 元/月升级 VIP 解锁全部功能。

做产品，定价就是定生死。定高了没人买，定低了不赚钱。这一 Lab 我们把点数系统做出来——从数据库设计到扣点逻辑到前端展示。

---

## 📚 基础知识储备

- **Freemium 模式** — 免费 + 增值。免费版有限制，付费版解锁所有功能
- **数据库原子操作** — 扣点要确保"查余额"和"扣点"之间不会被其他请求插队
- **HTTP 402** — "Payment Required"，表示需要付费（点数不足时返回）

---

## 🎯 实战目标

学完这个 Lab，你将能够：

1. 理解点数扣减的完整流程
2. 理解计费套餐的设计逻辑
3. 在前端展示剩余点数
4. 理解 VIP 权限控制

---

## 🛠 动手实战

### 步骤 1：理解点数扣减逻辑

**打开文件：** `backend/app/routers/generate.py`

```python
@router.post("/generate")
async def generate_article(..., user: User = Depends(get_current_user), ...):
    # 1. 检查点数
    if user.credits_remaining <= 0:
        raise HTTPException(status_code=402, detail="Insufficient credits. Please recharge.")

    # 2. 生成文章...
    result = await generator.generate(...)

    # 3. 扣点
    user.credits_remaining -= 1
    db.commit()

    return GenerateResponse(**result)
```

**流程：** 检查 → 生成 → 扣点。如果检查发现点数不足，直接返回 402，不会调用 AI（避免浪费钱）。

---

### 步骤 2：理解计费套餐 `routers/billing.py`

```python
CREDIT_PACKAGES = [
    {"id": "credits-100", "name": "100 credits", "price": 9.9, "credits": 100},
]

VIP_PLANS = [
    {"id": "vip-monthly", "name": "VIP Monthly", "price": 29.9, "duration_days": 30},
    {"id": "vip-quarterly", "name": "VIP Quarterly", "price": 79.9, "duration_days": 90},
]
```

**定价策略解读：**
- 100 次 = 9.9 元 → 约 0.1 元/次，用户觉得便宜
- VIP 月卡 = 29.9 元 → 高频用户划算（100 次/小时 vs 免费 10 次/小时）
- 季卡 = 79.9 元 → 相当于 26.6 元/月，比月卡便宜 11%

---

### 步骤 3：理解 VIP 权限控制

**打开文件：** `backend/app/middleware/auth.py`

```python
def require_vip(user: User = Depends(get_current_user)) -> User:
    if user.tier != UserTier.VIP:
        raise HTTPException(
            status_code=403,
            detail="This feature requires VIP subscription",
        )
    return user
```

**使用示例（在 `routers/batch.py`）：**
```python
@router.post("/batch/generate")
async def batch_generate(..., user: User = Depends(require_vip)):
    # 只有 VIP 能批量生成
```

`Depends(require_vip)` → 普通用户访问直接 403。

---

### 步骤 4：前端展示剩余点数

**在 `GeneratorPage.tsx` 的顶部：**
```tsx
{user && (
  <span style={{ fontSize: 13, color: "#999" }}>
    剩余：{user.credits_remaining} 次
  </span>
)}
```

每次生成完成后，前端应该重新获取用户信息以更新点数显示。
（当前实现里点数在页面刷新时才更新，你可以自己优化——生成成功后调一次 `api.getMe()`）

---

### 步骤 5：测试完整计费流程

**操作：**
1. 连续生成 10 次（注意看右上角点数减少）
2. 第 11 次应该报错："Insufficient credits"
3. 去计费页面（底栏"我的" → 充值）
4. 选择"100 credits - ¥9.9"
5. 充值后点数恢复到 100

> ⚠️ 当前版本支付是桩实现（没有接微信支付），充值直接在后台加了点数。真实上线需要对接支付接口。

**验证：** 点数耗尽后无法生成 ✅

---

## ⚠️ 常见坑点

| 坑 | 现象 | 原因 | 解决 |
|----|------|------|------|
| 并发扣点 | 同时发两个请求，只扣了一次 | 数据库操作不是原子性的 | 生产环境用数据库锁或 `UPDATE ... SET credits = credits - 1 WHERE credits > 0` |
| 点数扣了但生成失败 | 扣了点但没拿到文章 | 扣点在生成之后但在 commit 之前 | 把扣点放在生成成功确认之后 |
| VIP 权限不生效 | 普通用户也能访问 VIP 接口 | 接口没加 `require_vip` | 检查路由定义 |

---

## 📝 总结

**本章核心要点：**
- 点数扣减三步骤：检查 → 生成 → 扣点
- 点数不足返回 402（而不是 403 或 500）
- VIP 权限用 `Depends(require_vip)` 控制
- 当前是桩实现，支付对接是后续优化项

**你现在应该能做到：**
- 设计一个简单的点数计费系统
- 实现 VIP 权限控制
- 说出 Freemium 模式的定价逻辑

**下一步：** Lab 11 把前端部署到 Vercel——让全世界都能访问你的 App！

---

> —— 阿珊，前端开发者 & AI 提效实践者
