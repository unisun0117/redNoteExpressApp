## Context

The RedNote Express App (红薯快写App) is a greenfield project. No existing codebase. The target users are content creators in the Chinese Xiaohongshu ecosystem who need to produce high volumes of platform-optimized articles quickly. The app uses LLM APIs to generate structured articles from image + keyword inputs.

Technical environment: mobile-first web application, Chinese-language content, integration with Chinese payment gateways (WeChat Pay, Alipay). The app must support both individual creators (B2C) and merchant bulk operations (B2B).

## Goals / Non-Goals

**Goals:**
- Single-page web app with mobile-first responsive design (PWA-capable for future app-store deployment)
- RESTful API backend handling user auth, article generation, payment processing
- LLM integration for image-to-text analysis + structured article generation
- Configurable generation pipeline: style templates, content categories, emoji toggle, viral article style extraction
- Freemium user model: free tier with usage caps, VIP subscription, token recharge
- B2B batch mode: upload 50 images → generate 50 unique articles

**Non-Goals:**
- Native iOS/Android apps (v1 is web-only; PWA provides app-like experience)
- Social features (comments, likes, sharing) — users publish directly to Xiaohongshu platform
- Real-time collaboration
- Content scheduling or publishing automation to Xiaohongshu
- Custom LLM fine-tuning (v1 uses prompt engineering only)

## Decisions

### 1. Tech Stack: React (Vite) + Python FastAPI + PostgreSQL

**Decision:** React 18+ with Vite for frontend, Python 3.11+ with FastAPI for backend, PostgreSQL 15 for primary database.

**Rationale:**
- React: Large ecosystem, mobile-first component libraries (Ant Design Mobile / Radix), PWA support
- FastAPI: Async-native, excellent for I/O-bound LLM API calls, auto-generated OpenAPI docs, strong typing with Pydantic
- PostgreSQL: Mature, supports JSONB for flexible article templates, row-level security for multi-tenant B2B data

**Alternatives considered:**
- Next.js: Heavier than needed for an SPA; SSR adds complexity without benefit for this use case
- Django: More opinionated than needed; FastAPI's async performance better suits LLM API calling patterns
- MongoDB: Schema flexibility tempting but ACID transactions needed for billing/payments

### 2. LLM Integration: OpenAI GPT-4o + Vision API (primary), Claude API (fallback)

**Decision:** Primary use of GPT-4o with Vision for image analysis; support model switching via configuration. Claude API as alternative for text-only generation.

**Rationale:**
- GPT-4o Vision: Best-in-class image understanding for extracting content context from user uploads
- Both providers support Chinese-language generation well
- Model abstraction layer allows switching based on cost/performance

### 3. Article Generation Pipeline

**Decision:** Multi-stage pipeline with structured prompts at each stage:
```
Image → Vision API (extract subjects, mood, context)
       ↓
Keywords + Style Config → Prompt Builder → LLM (title + intro)
       ↓
Style Config → Prompt per section → LLM (body × 3)
       ↓
Template Assembly → Final article
```

**Rationale:** Single-prompt generation produces inconsistent structure. Staged generation gives precise control over word counts, emoji placement, and section formatting. The 20+50+300+50+50 word budget is enforced per-stage.

### 4. Viral Article Style Analysis

**Decision:** When user pastes a URL/text, extract the text → feed to LLM with a "style analysis" prompt that outputs a structured style profile (tone, sentence length, emoji density, hook patterns) → merge style profile into generation prompts.

### 5. Authentication: JWT + OAuth (WeChat)

**Decision:** JWT-based auth with access/refresh tokens. WeChat OAuth for Chinese users, email/password as fallback.

**Rationale:** WeChat login is the dominant auth method for Chinese internet users. JWT enables stateless API scaling.

### 6. Payment Integration: Stripe (international) + 支付宝/微信支付 (domestic)

**Decision:** Abstract payment provider interface. v1 integrates 微信支付 (JSAPI) for mini-program/web payment flow. Stripe for international credit card users.

### 7. Database Schema (core entities)

- `users`: id, auth_method, openid/email, password_hash, tier (free/vip), credits_remaining, created_at
- `generations`: id, user_id, input_images[], keywords, style_config, generated_article (JSONB with sections), viral_source_url, tokens_used, created_at
- `subscriptions`: id, user_id, plan_type, start_date, end_date, auto_renew, payment_provider
- `transactions`: id, user_id, type (recharge/subscribe), amount, provider, status, created_at
- `templates`: id, name, style_config (JSONB), is_preset, is_vip_only

### 8. Frontend Component Architecture

- `AppRouter`: React Router v6 with auth guards
- `GeneratorPage`: Main generation interface (image upload + keyword input + options)
- `StylePanel`: Dropdown selectors for template, category, emoji toggle, subtitle toggle
- `ViralAnalyzer`: Input box for pasting viral article URL/text
- `ResultView`: Rendered article preview with copy-to-clipboard
- `UserDashboard`: Usage stats, credit balance, subscription management
- `AdminMerchant`: B2B batch upload + batch result view

## Risks / Trade-offs

- **LLM API cost at scale** → Implement token budgeting, caching for repeated similar prompts, credit system to pass costs to heavy users
- **Chinese content compliance** → Add content moderation layer before returning generated text (sensitive word filtering)
- **Viral article URL scraping may fail** → Fall back to user-pasted text; URL fetch is best-effort only
- **WeChat Pay integration complexity** → v1 can launch with manual recharge via admin; payment gateway is abstracted so it can be deferred
- **Image upload storage costs** → Aggressively expire uploaded images after generation (keep max 7 days); use CDN for delivery

## Open Questions

- Mini-program (微信小程序) vs PWA: Mini-program offers better WeChat ecosystem integration but requires separate codebase. PWA is faster to ship. Decision deferred to post-v1.
- Which LLM provider offers the best Chinese Xiaohongshu-style generation quality? Requires A/B testing during development.
