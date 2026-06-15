## Why

Content creators spend 30-60 minutes crafting a single Xiaohongshu (RED) article — brainstorming titles, structuring paragraphs, adding emojis, and matching the platform's "live person" tone. An AI-powered generator that turns images + keywords into complete, platform-optimized articles reduces that to seconds, enabling creators to scale output and monetize through content, subscriptions, and B2B services.

## What Changes

- New mobile-first web app that accepts image uploads + keyword input to generate full Xiaohongshu articles via LLM
- Structured output pipeline: viral title (18-20 chars) → intro (50 chars) → 3 body sections (subtitle + 100 chars each, with emoji toggle) → summary (50 chars) → store name/address (50 chars)
- Configurable generation options: style templates (retro, minimalist, humorous, etc.), content赛道 (food, sports, photography, pets, home, beauty, tech, baby, F&B), optional emoji, optional viral section subtitles
- "Analyze viral article" input: paste a URL or text of a trending article, AI extracts its language style, then generates new content matching that style
- Freemium monetization: free tier (5-10 generations, basic styles, mandatory emoji), VIP subscription (custom templates, batch generation, advanced rewrite features), token/points recharge system
- B2B merchant mode: bulk upload 50 images → batch generate 50 unique shop-review articles

## Capabilities

### New Capabilities

- `article-generation`: Core LLM-powered article generation from image + keyword input, producing structured Xiaohongshu-formatted output with title, intro, body sections, summary, and location info
- `style-system`: Configurable style templates and赛道 (content category) selection that influence the tone and format of generated articles
- `viral-analysis`: Paste a trending article URL or text to analyze its language style, then apply that style to new content generation
- `user-auth`: User registration, login, and account management with freemium tier differentiation
- `subscription-billing`: Token/points recharge system, VIP subscription tiers, and payment integration
- `batch-generation`: B2B merchant bulk upload and batch article generation workflow

### Modified Capabilities

<!-- No existing specs to modify — this is a greenfield project -->

## Impact

- New project: full-stack web application (React/Vue frontend + Node.js/Python API backend + database)
- LLM API integration (OpenAI/Anthropic/etc.) for core text generation and image analysis
- Payment gateway integration (WeChat Pay, Alipay) for subscription and token recharge
- Cloud storage for user-uploaded images
- No existing code affected — greenfield build
